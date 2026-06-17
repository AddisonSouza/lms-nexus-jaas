## Context

RF-17 introduz o módulo `reporting`, que ainda não existe no código (apenas citado em `pscode/config.yaml`). É o primeiro módulo puramente de leitura agregada do projeto — não possui agregado próprio nem tabela própria; consolida dados que já existem em `classroom` (`ClassroomJpaEntity`, status `ACTIVE`/`ARCHIVED`), `organization` (`OrganizationMemberJpaEntity`, `role`), e `assessment` (`TaskJpaEntity`, `TaskSubmissionJpaEntity`, status `SUBMITTED`/`EVALUATED`). O vínculo tarefa → turmas elegíveis passa por `subject_id` → `findClassroomIdsBySubject` (curriculum), mesmo Port já criado em RF-16.

Não há, hoje, nenhuma agregação `COUNT/GROUP BY` cross-module no projeto (os Ports existentes em `communication`/`assessment` resolvem listas de ids, não métricas); este RF estabelece esse padrão. Também é a primeira geração de PDF do projeto — não há dependência de PDF no `pom.xml`.

A rota `/organizations/:id` já existe (`OrganizationDashboardPage`) e hoje é um placeholder genérico acessível a qualquer papel autenticado, com a nota "Dashboard completo disponível em RF-17".

## Goals / Non-Goals

**Goals:**
- Métricas agregadas por `organization_id` e por período (`from`/`to` obrigatórios): turmas ativas/arquivadas, membros por papel, tarefas criadas/avaliadas no período, taxa média de entrega.
- Feed de últimas atividades no período (turmas criadas/arquivadas, tarefas criadas/avaliadas, membros ingressados).
- Exportação em PDF do mesmo relatório.
- Acesso restrito a `ADMIN_ORG` da própria organização.

**Non-Goals:**
- Dashboards de GESTOR/PROFESSOR/ALUNO (RF-18/19/20) — reaproveitarão o módulo `reporting`, mas com queries e RBAC próprios; não implementados aqui.
- Cache do resultado agregado em Redis — sem cache nesta entrega; reavaliar se a agregação ficar lenta em produção.
- Paginação do feed de atividades — consistente com o padrão atual do projeto (nenhuma listagem pagina ainda).
- Agendamento/envio automático de relatórios por e-mail.

## Decisions

**1. Módulo `reporting` é somente leitura — sem migration Flyway, sem agregado de domínio próprio.**
Todas as métricas são derivadas via Query Ports cross-module (JPQL com FQN, padrão já aprovado em RF-15/16). `domain/model/` do `reporting` contém apenas VOs de resposta (`DashboardPeriod`, `ActivityType`), não entidades persistidas. Alternativa considerada: materializar uma tabela de métricas pré-calculada — rejeitada por complexidade desproporcional ao volume de dados do MVP (turmas de sala de aula, não milhões de linhas).

**2. Período (`from`/`to`) é obrigatório em ambos os endpoints, sem default no backend.**
Evita ambiguidade sobre "período implícito" e mantém o contrato simples; o frontend decide a UX (atalhos de 7/30/90 dias, populando os params). Validação: `from <= to`, senão 400.

**3. Três novos Query Ports `out/` no `reporting`, cada um com um método de agregação `COUNT`/`GROUP BY` (padrão novo no projeto, seguindo a convenção FQN-JPQL de `SubjectQueryAdapter`):**
- `ClassroomMetricsQueryPort`: `countByStatus(organizationId)` → `Map<String,Long>` (`ACTIVE`/`ARCHIVED`); `listActivity(organizationId, from, to)` → turmas criadas/arquivadas no período.
- `MemberMetricsQueryPort`: `countByRole(organizationId)` → `Map<String,Long>`; `listActivity(organizationId, from, to)` → membros com `joined_at` no período.
- `TaskMetricsQueryPort`: `countCreated(organizationId, from, to)`, `countEvaluated(organizationId, from, to)` (tarefas com ao menos uma submissão `EVALUATED` criada no período), `averageDeliveryRate(organizationId, from, to)`, `listActivity(organizationId, from, to)` → tarefas criadas/avaliadas no período.

Alternativa considerada: um único `DashboardQueryPort` monolítico — rejeitada porque cada métrica pertence a um bounded context diferente (`classroom`, `organization`, `assessment`); manter 3 Ports separados, cada um implementado em `infrastructure/persistence/` do `reporting`, preserva a regra "módulos comunicam via interfaces Java" sem misturar responsabilidades em uma única classe.

**4. Taxa média de entrega = média das taxas por tarefa, não taxa global agregada.**
Para cada tarefa criada no período: taxa = `submissions (SUBMITTED ou EVALUATED) / alunos elegíveis` (alunos das turmas vinculadas ao `subject_id` da tarefa, via `findClassroomIdsBySubject` + `listMemberUserIds(classroomId, "ALUNO")`, já existentes desde RF-16). A métrica final é a média aritmética dessas taxas entre as tarefas do período; tarefas sem nenhum aluno elegível são ignoradas no cálculo (evita divisão por zero e não distorce a média). Alternativa considerada: `total de submissions / total de (tarefa × aluno elegível)` no período — rejeitada porque pondera mais as tarefas com mais alunos, escondendo tarefas pequenas com baixa entrega, que é justamente o sinal que o `ADMIN_ORG` quer ver.

**5. Exportação em PDF via OpenHTMLtoPDF, renderizando um template Qute HTML — primeira dependência de PDF do projeto.**
O use case de PDF (`ExportAdminDashboardPdfService`) reutiliza o mesmo `GetAdminDashboardService` que monta os dados do dashboard, apenas trocando o serializador de saída (JSON → HTML→PDF). Alternativa considerada: `OpenPDF` (API programática) — rejeitada por exigir montar o layout elemento a elemento em Java, mais verboso para manter visualmente alinhado ao dashboard web; `JasperReports` — rejeitado pelo peso da dependência e curva de aprendizado desproporcional ao escopo (um relatório, não um motor de relatórios).

**6. Frontend: dashboard completo só renderiza para `ADMIN_ORG`; demais papéis continuam vendo a página atual (placeholder com link de Turmas).**
A rota `/organizations/:id` é compartilhada por todos os papéis autenticados (não é exclusiva de `ADMIN_ORG`); em vez de restringir a rota via `ProtectedRoute roles`, `OrganizationDashboardPage` passa a checar `useAuthStore((s) => s.role)` e renderizar `<AdminDashboard organizationId={id} />` apenas quando `role === 'ADMIN_ORG'`, preservando o comportamento atual para os demais papéis. Evita duplicar rota/guarda só para este caso.

**7. Gráficos com Recharts — primeira dependência de gráficos do projeto, mas é a base dos componentes de chart oficiais do shadcn/ui (já adotado no projeto).**
Mantém consistência com a stack (`Shadcn/ui` é obrigatório pelo `pscode/config.yaml`); usar a mesma lib evita uma segunda biblioteca de visualização concorrente no bundle.

## Estrutura de pacotes (backend)

```
apps/api/src/main/java/br/edu/lms/module/reporting/
  domain/
    model/DashboardPeriod.java (VO: from/to, valida from<=to), ActivityType.java (enum: CLASSROOM_CREATED, CLASSROOM_ARCHIVED, TASK_CREATED, TASK_EVALUATED, MEMBER_JOINED)
    port/in/GetAdminDashboardUseCase.java, ExportAdminDashboardPdfUseCase.java
    port/out/ClassroomMetricsQueryPort.java, MemberMetricsQueryPort.java, TaskMetricsQueryPort.java
  application/
    usecase/GetAdminDashboardService.java, ExportAdminDashboardPdfService.java
    dto/AdminDashboardResponse.java (turmas, membros, tarefas, taxaEntrega, atividades), ActivityItemResponse.java
  infrastructure/
    persistence/ClassroomMetricsQueryPortImpl.java, MemberMetricsQueryPortImpl.java, TaskMetricsQueryPortImpl.java
    pdf/DashboardPdfRenderer.java (Qute template render → OpenHTMLtoPDF)
  interfaces/
    rest/AdminDashboardResource.java

apps/api/src/main/resources/templates/reporting/dashboard.html (template Qute do relatório PDF)
```

Nenhuma migration Flyway é necessária (módulo sem entidade própria).

## Dependência Maven

- `com.openhtmltopdf:openhtmltopdf-pdfbox` (+ `openhtmltopdf-core`) para renderizar o HTML do template Qute em PDF.

## Endpoints REST

- `GET /organizations/{id}/dashboard?from={data}&to={data}` — `@RolesAllowed("ADMIN_ORG")`. `id` deve ser igual ao claim `org` do JWT, senão 403. `from > to` → 400. Retorna 200 com `AdminDashboardResponse`.
- `GET /organizations/{id}/reports/pdf?from={data}&to={data}` — mesmas regras de acesso e validação. Retorna 200 com `Content-Type: application/pdf`.

## Frontend

- **Feature:** `apps/web/src/features/dashboard/` (nova)
  - `types.ts`: `AdminDashboardData`, `ActivityItem`
  - `api/dashboard.ts`: `getAdminDashboard(organizationId, period)`
  - `api/query-keys.ts`: `["dashboard", "admin", organizationId, period]`
  - `hooks/useAdminDashboard.ts` (TanStack Query)
  - `components/PeriodSelector.tsx` (atalhos 7/30/90 dias + range customizado, Zod valida `from <= to`)
  - `components/MetricsCards.tsx`, `components/DashboardCharts.tsx` (Recharts: membros por papel, tarefas criadas/avaliadas), `components/ActivityFeed.tsx`
  - `components/AdminDashboard.tsx` (compõe os anteriores + botão "Exportar PDF", que abre `GET .../reports/pdf` em nova aba)
- **Integração:** `OrganizationDashboardPage` (`apps/web/src/features/organization/components/OrganizationDashboardPage.tsx`) passa a renderizar `<AdminDashboard organizationId={id} />` quando `useAuthStore((s) => s.role) === 'ADMIN_ORG'`; mantém o conteúdo atual (link de Turmas) para os demais papéis.
- Nova dependência: `recharts`.

## Risks / Trade-offs

- [Risco] Agregações via JPQL cross-module (3 Ports, várias JOINs para taxa de entrega) podem ficar lentas conforme o volume de turmas/tarefas crescer. → Mitigação: aceitável no volume do MVP; sem cache nesta entrega (Non-Goal), reavaliar Redis ou view materializada se necessário.
- [Risco] Nova dependência de PDF (OpenHTMLtoPDF) aumenta o tamanho do artefato e introduz uma nova superfície de manutenção (template HTML). → Mitigação: template único e simples (mesmas métricas do dashboard, sem gráficos no PDF — apenas tabelas/números, evitando renderizar SVG do Recharts em PDF).
- [Trade-off] Taxa de entrega como média das taxas por tarefa (Decisão 4) pode divergir da intuição de "total entregue / total esperado" para quem só olha o número. → Aceito conscientemente; documentar o cálculo no tooltip do frontend.
- [Trade-off] Sem paginação no feed de atividades → aceito, consistente com o padrão atual do projeto.
