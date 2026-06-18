## Context

O módulo `reporting` já existe desde o RF-17 (Admin Dashboard): é um módulo somente leitura, sem agregado próprio, que consulta outros módulos via Query Ports implementados com JPQL cross-module por FQN (padrão já aprovado em RF-15/16/17). O RF-18 estende esse módulo para o papel `GESTOR`.

No modelo de dados atual, `GESTOR` é um papel **org-wide** (`organization_members.role`), sem vínculo turma-a-turma — `ClassroomMemberRole` só tem `PROFESSOR`/`ALUNO` (`apps/api/.../classroom/domain/model/ClassroomMemberRole.java`). `ListClassroomsService` já trata `GESTOR` igual a `ADMIN_ORG`: ambos veem todas as turmas da organização via `ClassroomRepository.findAllByOrganization`. O dashboard do gestor, portanto, cobre **todas as turmas da organização**, não um subconjunto vinculado.

`TaskSubmissionJpaEntity` já tem um campo `grade` (`BigDecimal`, precisão 5,2 — adicionado na V019) preenchido por `EvaluateSubmissionService` junto com o feedback. Não existe status `LATE`: atraso é derivado em tempo de consulta (`task.deadline` no passado sem submissão, ou submissão criada após o deadline).

## Goals / Non-Goals

**Goals:**
- `GET /organizations/{id}/gestor-dashboard` retorna, para cada turma ativa da organização: status, % de entrega (mesma fórmula do RF-17: média das taxas por tarefa, não taxa global), média de notas das submissões avaliadas, e os 5 alunos com mais tarefas pendentes/atrasadas.
- Indicadores refletem o estado atual de todas as tarefas já criadas na turma — sem filtro `from`/`to` (diferente do RF-17, que mede atividade num período).
- Exportação em PDF do mesmo relatório, restrita ao mesmo controle de acesso.
- Acesso restrito a `GESTOR` da própria organização.

**Non-Goals:**
- Dashboards de PROFESSOR/ALUNO (RF-19/RF-20).
- Vínculo explícito gestor↔turma (fora de escopo; modelo atual mantém `GESTOR` org-wide, consistente com `ListClassroomsService`).
- Cache do resultado agregado em Redis.
- Paginação da lista de turmas ou da lista de alunos em risco (mesmo padrão do RF-17 — nenhuma listagem do projeto pagina ainda).
- Notificação proativa ao gestor sobre turmas/alunos em risco (apenas exibição no dashboard).

## Decisions

**1. Um único novo Query Port `GestorDashboardQueryPort` em `reporting/domain/port/out/`, em vez de três ports separados como no RF-17.**
No RF-17 cada métrica (turmas, membros, tarefas) é independente e agregada no nível da organização — por isso fazia sentido um Port por bounded context. Aqui a "saúde da turma" é inerentemente um dado **por turma** que já cruza `classroom` (status), `curriculum` (vínculo tarefa→turma via `subject_classroom`) e `assessment` (tarefas/submissões/notas) numa única visão coesa; dividir em três ports forçaria o `GetGestorDashboardService` a re-agrupar por `classroomId` na camada de aplicação sem ganho real de separação. Alternativa considerada (três ports, mesmo padrão do RF-17) — rejeitada por esse motivo.

Métodos do Port:
```java
public interface GestorDashboardQueryPort {
    List<ClassroomHealth> getClassroomsHealth(String organizationId);
    List<AtRiskStudent> listAtRiskStudents(String classroomId, int limit);
}
```
`ClassroomHealth` (VO): `classroomId`, `classroomName`, `status`, `deliveryRate` (BigDecimal), `averageGrade` (BigDecimal, `null` se não há submissões avaliadas).
`AtRiskStudent` (VO): `studentId`, `studentName`, `pendingCount`.

**2. `GetGestorDashboardService` orquestra: lista as turmas com saúde via `getClassroomsHealth`, depois busca os top 5 alunos em risco de cada turma via `listAtRiskStudents(classroomId, 5)`.**
Mantém a mesma separação domain/application já usada em `GetAdminDashboardService` (RF-17): o Use Case não conhece JPQL, apenas orquestra o Port e monta o DTO de resposta (`GestorDashboardResponse` com lista de `ClassroomHealthResponse`, cada um já contendo seus `atRiskStudents`).

**3. Implementação em `GestorDashboardQueryPortImpl` (`reporting/infrastructure/persistence/`), JPQL cross-module por FQN — mesmo padrão de `TaskMetricsQueryPortImpl`.**
- `getClassroomsHealth`: para cada turma ativa da organização (`ClassroomJpaEntity`), calcula `deliveryRate` com a mesma lógica do RF-17 (`averageDeliveryRate`: média das taxas `submetidos/elegíveis` por tarefa vinculada à turma via `SubjectClassroomJpaEntity`, ignorando tarefas sem alunos elegíveis), e `averageGrade` como a média do campo `grade` das submissões com status `EVALUATED` dessas tarefas.
- `listAtRiskStudents`: para a turma, lista os alunos (`ClassroomMemberJpaEntity` com `role = 'ALUNO'`) e conta, por aluno, as tarefas vinculadas à turma com `deadline < NOW()` para as quais não existe submissão do aluno, ou a submissão existente foi criada após o `deadline`; ordena por contagem decrescente e retorna os top N.

**4. Reaproveitar `DashboardPdfRenderer` (RF-17) adicionando um segundo método e um segundo template Qute, em vez de criar uma classe paralela.**
A classe já encapsula o setup do `PdfRendererBuilder`/`useFastMode`; duplicar essa classe só para trocar o template seria repetição sem ganho. `DashboardPdfRenderer` ganha `renderGestorDashboard(GestorDashboardResponse)` com um segundo campo `@Location("reporting/gestor-dashboard.html") Template`. Alternativa considerada (nova classe `GestorDashboardPdfRenderer`) — rejeitada por duplicar a configuração de PDF sem necessidade.

**5. Novo `GestorDashboardResource` (`reporting/interfaces/rest/`), em vez de estender `AdminDashboardResource`.**
Papéis de acesso (`@RolesAllowed`) e paths (`/gestor-dashboard` vs `/dashboard`) são diferentes; manter recursos separados evita um único controller com `if (role == X)` e mantém o padrão 1 Resource por capability já usado no projeto.
- `GET /organizations/{id}/gestor-dashboard` — `@RolesAllowed("GESTOR")`, valida `id == jwt.org` (403 caso contrário).
- `GET /organizations/{id}/gestor-dashboard/pdf` — mesmas regras, `Content-Type: application/pdf`.

**6. Frontend: `OrganizationDashboardPage` ganha uma terceira ramificação condicional por papel.**
`role === 'ADMIN_ORG'` → `AdminDashboard` (RF-17, já implementado); `role === 'GESTOR'` → novo `GestorDashboard`; demais papéis → placeholder atual (inalterado). Sem `PeriodSelector` no `GestorDashboard` (Decisão de escopo: sem filtro de período).

## Estrutura de pacotes (backend)

```
apps/api/src/main/java/br/edu/lms/module/reporting/
  domain/
    model/ClassroomHealth.java (novo — VO: classroomId, classroomName, status, deliveryRate, averageGrade)
    model/AtRiskStudent.java (novo — VO: studentId, studentName, pendingCount)
    port/in/GetGestorDashboardUseCase.java (novo)
    port/in/ExportGestorDashboardPdfUseCase.java (novo)
    port/out/GestorDashboardQueryPort.java (novo)
  application/
    usecase/GetGestorDashboardService.java (novo)
    usecase/ExportGestorDashboardPdfService.java (novo — mesmo padrão do RF-17: reusa GetGestorDashboardService + DashboardPdfRenderer)
    dto/GestorDashboardResponse.java, ClassroomHealthResponse.java, AtRiskStudentResponse.java (novos)
  infrastructure/
    persistence/GestorDashboardQueryPortImpl.java (novo)
    pdf/DashboardPdfRenderer.java (modificado — novo método renderGestorDashboard + novo Template injetado)
  interfaces/
    rest/GestorDashboardResource.java (novo)

apps/api/src/main/resources/templates/reporting/gestor-dashboard.html (novo template Qute)
```

Nenhuma migration Flyway é necessária (módulo somente leitura, sem entidade própria — mesmo padrão do RF-17).

## Endpoints REST

- `GET /organizations/{id}/gestor-dashboard` — `@RolesAllowed("GESTOR")`. `id` deve ser igual ao claim `org` do JWT, senão 403. Retorna 200 com `GestorDashboardResponse`.
- `GET /organizations/{id}/gestor-dashboard/pdf` — mesmas regras de acesso. Retorna 200 com `Content-Type: application/pdf`.

## Frontend

- **Feature:** `apps/web/src/features/dashboard/` (já existe desde o RF-17)
  - `types.ts`: adiciona `ClassroomHealth`, `AtRiskStudent`, `GestorDashboardData`
  - `api/gestor-dashboard.ts`: `getGestorDashboard(organizationId)`, `exportGestorDashboardPdf(organizationId)`
  - `api/query-keys.ts`: adiciona `dashboardKeys.gestor(organizationId)`
  - `hooks/useGestorDashboard.ts` (TanStack Query)
  - `components/ClassroomHealthCards.tsx` (cards comparativos por turma: status, % entrega, média de notas)
  - `components/AtRiskStudentsList.tsx` (lista dos 5 alunos com mais pendências, por turma)
  - `components/GestorDashboard.tsx` (compõe os dois acima + botão "Exportar PDF", mesmo padrão do `AdminDashboard`)
- **Integração:** `OrganizationDashboardPage.tsx` passa a renderizar `<GestorDashboard organizationId={id} />` quando `useAuthStore((s) => s.role) === 'GESTOR'`.
- Sem nova dependência de pacote (reaproveita `recharts` já instalado no RF-17, se necessário para os cards comparativos).

## Risks / Trade-offs

- [Risco] `listAtRiskStudents` roda uma contagem por aluno por turma (potencial N+1 entre turmas); aceitável no volume do MVP (turmas de sala de aula), mesmo trade-off já aceito em `TaskMetricsQueryPortImpl.averageDeliveryRate` no RF-17.
- [Trade-off] Sem filtro de período — indicadores sempre refletem o histórico completo da turma. Se a organização quiser "saúde do mês", seria uma change futura (adicionar período opcional).
- [Trade-off] `averageGrade` pode ser `null` quando a turma não tem nenhuma submissão avaliada ainda — frontend deve tratar esse caso explicitamente (ex: "Sem notas ainda") em vez de mostrar `0`.
