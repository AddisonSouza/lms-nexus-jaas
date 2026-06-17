## 1. Backend — Setup e domínio

- [x] 1.1 [INFRA] Adicionar dependência `com.openhtmltopdf:openhtmltopdf-pdfbox` (+ `quarkus-qute`) ao `apps/api/pom.xml`
- [x] 1.2 [BE] Criar `DashboardPeriod` (VO, valida `from <= to`) e `ActivityType` (enum: `CLASSROOM_CREATED`, `CLASSROOM_ARCHIVED`, `TASK_CREATED`, `TASK_EVALUATED`, `MEMBER_JOINED`) em `reporting/domain/model/`

## 2. Backend — Ports

- [x] 2.1 [BE] Criar porta de entrada `GetAdminDashboardUseCase` em `reporting/domain/port/in/`
- [x] 2.2 [BE] Criar porta de entrada `ExportAdminDashboardPdfUseCase` em `reporting/domain/port/in/`
- [x] 2.3 [BE] Criar porta de saída `ClassroomMetricsQueryPort` (`countByStatus`, `listActivity`) em `reporting/domain/port/out/`
- [x] 2.4 [BE] Criar porta de saída `MemberMetricsQueryPort` (`countByRole`, `listActivity`) em `reporting/domain/port/out/`
- [x] 2.5 [BE] Criar porta de saída `TaskMetricsQueryPort` (`countCreated`, `countEvaluated`, `averageDeliveryRate`, `listActivity`) em `reporting/domain/port/out/`

## 3. Backend — Infraestrutura (Query Ports)

- [x] 3.1 [BE] Implementar `ClassroomMetricsQueryPortImpl` (JPQL contra `ClassroomJpaEntity`: `COUNT ... GROUP BY status` filtrado por `organizationId`; `listActivity` por `created_at`/status no período)
- [x] 3.2 [BE] Implementar `MemberMetricsQueryPortImpl` (JPQL contra `OrganizationMemberJpaEntity`: `COUNT ... GROUP BY role` filtrado por `organizationId` e `deletedAt IS NULL`; `listActivity` por `joined_at` no período)
- [x] 3.3 [BE] Implementar `TaskMetricsQueryPortImpl.countCreated`/`countEvaluated` (JPQL contra `TaskJpaEntity`/`TaskSubmissionJpaEntity` filtrado por `organizationId` e período)
- [x] 3.4 [BE] Implementar `TaskMetricsQueryPortImpl.averageDeliveryRate` (por tarefa do período: alunos elegíveis via JPQL cross-module direto contra `SubjectClassroomJpaEntity`/`ClassroomMemberJpaEntity`, submissions `SUBMITTED`/`EVALUATED`; média das taxas por tarefa, ignorando tarefas sem alunos elegíveis — conforme Decisão 4 do design.md)
- [x] 3.5 [BE] Implementar `TaskMetricsQueryPortImpl.listActivity` (tarefas criadas e submissões avaliadas no período)

## 4. Backend — Use Cases

- [x] 4.1 [BE] Implementar `GetAdminDashboardService`: agrega os 3 Query Ports, monta `AdminDashboardResponse` (métricas + feed de atividades ordenado por data decrescente)
- [x] 4.2 [BE] Implementar `DashboardPdfRenderer` (`reporting/infrastructure/pdf/`): renderiza template Qute `templates/reporting/dashboard.html` e converte para PDF via OpenHTMLtoPDF
- [x] 4.3 [BE] Implementar `ExportAdminDashboardPdfService`: reutiliza `GetAdminDashboardService` e delega ao `DashboardPdfRenderer`
- [x] 4.4 [BE] Criar template Qute `templates/reporting/dashboard.html` (métricas + feed de atividades em tabelas, sem gráficos)

## 5. Backend — Endpoints REST

- [x] 5.1 [BE] Criar `AdminDashboardResource` com `GET /organizations/{id}/dashboard?from=&to=` (`@RolesAllowed("ADMIN_ORG")`, valida `id == jwt.org`, 403 caso contrário; 400 se `from > to`)
- [x] 5.2 [BE] Adicionar `GET /organizations/{id}/reports/pdf?from=&to=` (mesmas regras de acesso/validação, `Content-Type: application/pdf`)

## 6. Backend — Testes

- [x] 6.1 [BE] Testes unitários de `GetAdminDashboardService` (agregação correta, ordenação do feed, período vazio) e de `DashboardPeriod` (validação `from <= to`)
- [x] 6.2 [BE] Testes de integração dos 3 `*MetricsQueryPortImpl` (`ReportingMetricsQueryPortsIT`, contagens e listagem de atividades por período)
- [x] 6.3 [BE] Testes de integração `@QuarkusTest` para `AdminDashboardResource` cobrindo os cenários da spec (sucesso, isolamento por organização, papel não autorizado, período inválido)
- [x] 6.4 [BE] Teste de integração do endpoint de PDF (`Content-Type` correto, 403 para papel/organização incorretos)

## 7. Frontend — Tipos e API

- [x] 7.1 [FE] Adicionar `recharts` às dependências de `apps/web`
- [x] 7.2 [FE] Criar `features/dashboard/types.ts` (`AdminDashboardData`, `ActivityItem`)
- [x] 7.3 [FE] Criar `features/dashboard/api/dashboard.ts` com `getAdminDashboard(organizationId, period)` e `exportAdminDashboardPdf(organizationId, period)`
- [x] 7.4 [FE] Criar `features/dashboard/api/query-keys.ts` (`["dashboard", "admin", organizationId, period]`)

## 8. Frontend — Hooks e componentes

- [x] 8.1 [FE] Criar hook `useAdminDashboard(organizationId, period)` (TanStack Query)
- [x] 8.2 [FE] Criar `PeriodSelector.tsx` (atalhos 7/30/90 dias + range customizado, validação Zod `from <= to`)
- [x] 8.3 [FE] Criar `MetricsCards.tsx` (turmas ativas/arquivadas, membros por papel, tarefas criadas/avaliadas, taxa de entrega com tooltip explicando o cálculo)
- [x] 8.4 [FE] Criar `DashboardCharts.tsx` (Recharts: membros por papel, tarefas criadas vs avaliadas)
- [x] 8.5 [FE] Criar `ActivityFeed.tsx` (lista de atividades ordenada por data decrescente, ícone por `ActivityType`)
- [x] 8.6 [FE] Criar `AdminDashboard.tsx` compondo os componentes acima + botão "Exportar PDF" (busca o blob autenticado via axios e abre em nova aba)
- [x] 8.7 [FE] Atualizar `OrganizationDashboardPage.tsx`: renderizar `<AdminDashboard organizationId={id} />` quando `useAuthStore((s) => s.role) === 'ADMIN_ORG'`, mantendo o conteúdo atual para os demais papéis

## 9. Frontend — Testes

- [x] 9.1 [FE] Testes Vitest + Testing Library para `PeriodSelector` (validação `from <= to`, atalhos)
- [x] 9.2 [FE] Testes para `MetricsCards`/`ActivityFeed` (renderização de dados, período vazio)
- [x] 9.3 [FE] Teste para `OrganizationDashboardPage` cobrindo a renderização condicional por papel (`ADMIN_ORG` vs demais)
