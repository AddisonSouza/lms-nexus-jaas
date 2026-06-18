## Context

O módulo `reporting` é somente leitura desde o RF-17, com um Query Port por dashboard implementado via JPQL cross-module por FQN (sem dependência Java direta entre módulos). RF-20 fecha o conjunto de dashboards por papel com o do `ALUNO`, mas com uma diferença estrutural importante em relação aos demais: o escopo não é um path param (`organizations/{id}`, `subjects/{id}`), e sim **o próprio usuário autenticado** — `GET /students/me/dashboard`, sem nenhum `@PathParam`. O isolamento multi-tenant e o "ver apenas os próprios dados" (RN-07) colapsam na mesma checagem: tudo é filtrado por `userId = jwt.getSubject()` e `organizationId = jwt.getClaim("org")`.

Diferente do professor (vínculo via `subject_teachers` + `organization_members`), o vínculo aluno↔turma é direto em `classroom_members` (`user_id`, `role = 'ALUNO'`), e `tasks`/`task_submissions` já têm `organization_id` próprio — não é necessário navegar por `organization_members` para resolver o filtro de organização, como foi preciso no RF-19.

`Task.deadline` (LocalDateTime) e `TaskStatus` (`DRAFT`, `PUBLISHED`, `CLOSED`, `GRADED`) já existem. `TaskSubmission.status` é `SUBMITTED` ou `EVALUATED`, sem timestamp de avaliação dedicado — `updatedAt` é o melhor proxy disponível para "mais recente" (atualizado no `PreUpdate` quando a submissão é avaliada).

## Goals / Non-Goals

**Goals:**
- `GET /students/me/dashboard` retorna: próximas tarefas pendentes (publicadas, sem submissão do aluno) ordenadas por `deadline` ascendente; contagem de tarefas entregues vs pendentes nas turmas do aluno; últimas notas/feedbacks recebidos (submissões avaliadas, mais recente primeiro); média geral de notas por disciplina (apenas submissões avaliadas).
- Acesso restrito a `ALUNO`, escopado pelo próprio `userId` do JWT — sem path param, sem checagem de vínculo adicional (o vínculo é o próprio princípio do endpoint).
- Isolamento por `organization_id` do JWT, mesmo padrão dos demais dashboards.

**Non-Goals:**
- Gamificação (pontos, níveis, badges, ranking) — RF-21/22/23, módulo `gamification` (futuro).
- Exportação em PDF (não está nos critérios de aceite do RF-20).
- Cache em Redis do resultado agregado.
- Paginação das listas (mesmo padrão dos demais dashboards).
- Notificação proativa sobre prazos próximos (apenas exibição no dashboard).
- Status explícito de "atrasada" para tarefas pendentes — RF-20 só pede ordenação por urgência (deadline), não um cálculo de atraso; o frontend pode formatar o deadline relativo (ex: "vence em 2 dias") sem novo campo no backend.

## Decisions

**1. Novo Query Port `StudentDashboardQueryPort` em `reporting/domain/port/out/`, sem método de verificação de acesso (diferente do RF-19).**
Não há "vínculo a verificar" — o próprio `userId` do JWT define o escopo dos dados. Não há cenário de 403 por falta de vínculo, só por papel incorreto (resolvido via `@RolesAllowed("ALUNO")` na REST resource, mesmo padrão do RF-17/18). Por isso `GetStudentDashboardService` não precisa de exceção de domínio nova — reaproveita o fluxo simples (sem `UnauthorizedDashboardAccessException`, que é exclusiva do caso disciplina↔professor do RF-19).

Métodos do Port:
```java
public interface StudentDashboardQueryPort {
    List<UpcomingTask> getUpcomingPendingTasks(String studentId, String organizationId);
    long countSubmittedTasks(String studentId, String organizationId);
    long countPendingTasks(String studentId, String organizationId);
    List<RecentGrade> getRecentGrades(String studentId, String organizationId);
    List<SubjectAverageGrade> getAverageGradePerSubject(String studentId, String organizationId);
}
```
`UpcomingTask` (VO): `taskId`, `title`, `subjectName`, `deadline`. `RecentGrade` (VO): `taskId`, `title`, `subjectName`, `grade`, `feedback`. `SubjectAverageGrade` (VO): `subjectId`, `subjectName`, `averageGrade` (BigDecimal).

**2. "Tarefas pendentes" = tarefas com `status = 'PUBLISHED'` cujo `subject_id` está vinculado (via `subject_classrooms`) a uma turma em que o aluno está matriculado (`classroom_members`, `role = 'ALUNO'`), e que não têm submissão do aluno (`task_submissions.student_id`).**
Mesmo padrão de set-difference do RF-18/19 (`NOT IN` subquery de `task_submissions`), mas aqui a direção é turma→disciplina→tarefa (oposto do professor, que parte da disciplina). `countPendingTasks` reaproveita a mesma subquery; `countSubmittedTasks` é o complemento (tarefas publicadas elegíveis com submissão, qualquer status).

**3. Ordenação por urgência = `ORDER BY deadline ASC` nas tarefas pendentes (decisão confirmada com o usuário: lista só tarefas pendentes, não o histórico completo).**
Tarefas com submissão (entregues ou avaliadas) não entram na lista de "próximas tarefas" — elas já têm representação própria nos cards de contagem (entregues vs pendentes) e nas últimas notas. Evita redundância entre as quatro métricas do dashboard.

**4. `getRecentGrades` ordena por `updatedAt DESC` e limita a 5 registros (`setMaxResults(5)`), filtrando `status = 'EVALUATED'`.**
Não há timestamp de avaliação dedicado (`evaluatedAt`) no schema atual — `updatedAt` é atualizado no `@PreUpdate` de `TaskSubmissionJpaEntity` exatamente quando a nota é lançada (`TaskSubmission.evaluate()` é a única operação de domínio que muda o estado de uma submissão avaliada). Limite de 5 segue o mesmo espírito de "últimas notas" do RF-14 (`my-grades` não limita, mas lá é uma página dedicada de histórico completo; aqui é um resumo de dashboard).

**5. `getAverageGradePerSubject` agrupa por `subject_id`/`subject.name`, considerando apenas `status = 'EVALUATED'` — mesma lógica de `getAverageGradePerStudent` do RF-19, com `GROUP BY` trocado de aluno para disciplina.**
Disciplinas sem nenhuma submissão avaliada do aluno simplesmente não aparecem na lista (sem entrada com média zero/nula) — mesmo comportamento de "ausência implícita" já usado no RF-19 para `studentsWithoutSubmission`.

**6. Filtro de organização direto via `tasks.organization_id` / `task_submissions.organization_id` / `classroom_members.organization_id` (todas as entidades já têm a coluna), sem precisar navegar por `organization_members` como no RF-19.**
Mais simples que o professor porque o vínculo aluno↔turma (`classroom_members.user_id`) já usa o mesmo `userId` do `sub` do JWT — não há tradução userId→memberId envolvida.

**7. Novo `StudentDashboardResource` (`reporting/interfaces/rest/`) com `@Path("/students/me")`.**
Sem `@PathParam` — `studentId = jwt.getSubject()`, `organizationId = (String) jwt.getClaim("org")` extraídos diretamente na resource, mesmo padrão de `TaskResource.myGrades()`.
- `GET /students/me/dashboard` — `@RolesAllowed("ALUNO")`.

**8. Frontend: `StudentDashboard` montado em `OrganizationDashboardPage.tsx` quando `role === 'ALUNO'`, ao lado dos branches já existentes para `ADMIN_ORG`/`GESTOR` (mesmo arquivo, mesmo padrão — RF-17/18).**
Diferente do RF-19 (que embute em `SubjectDetailPage`, pois é por disciplina), o dashboard do aluno é uma visão pessoal sem escopo de entidade — pertence à mesma página "home" usada pelos outros papéis. Não recebe `organizationId` como prop (o endpoint é self-scoped), mantendo a assinatura mais simples possível.

## Estrutura de pacotes (backend)

```
apps/api/src/main/java/br/edu/lms/module/reporting/
  domain/
    model/UpcomingTask.java (novo — VO: taskId, title, subjectName, deadline)
    model/RecentGrade.java (novo — VO: taskId, title, subjectName, grade, feedback)
    model/SubjectAverageGrade.java (novo — VO: subjectId, subjectName, averageGrade)
    port/in/GetStudentDashboardUseCase.java (novo)
    port/out/StudentDashboardQueryPort.java (novo)
  application/
    usecase/GetStudentDashboardService.java (novo)
    dto/StudentDashboardResponse.java, UpcomingTaskResponse.java, RecentGradeResponse.java, SubjectAverageGradeResponse.java (novos)
  infrastructure/
    persistence/StudentDashboardQueryPortImpl.java (novo)
  interfaces/
    rest/StudentDashboardResource.java (novo)
```

Nenhuma migration Flyway é necessária (módulo somente leitura, sem entidade própria — mesmo padrão do RF-17/18/19).

## Endpoints REST

- `GET /students/me/dashboard` — `@RolesAllowed("ALUNO")`. Retorna 200 com `StudentDashboardResponse` (próximas tarefas pendentes, contagem entregues/pendentes, últimas notas, média por disciplina). Sem cenário de 403 específico do recurso (apenas o 403 genérico de papel incorreto, resolvido pelo `@RolesAllowed`).

## Frontend

- **Feature:** `apps/web/src/features/dashboard/` (já existe desde o RF-17)
  - `types.ts`: adiciona `UpcomingTask`, `RecentGrade`, `SubjectAverageGrade`, `StudentDashboardData`
  - `api/student-dashboard.ts`: `getStudentDashboard()` (sem parâmetro — self-scoped)
  - `api/query-keys.ts`: adiciona `dashboardKeys.student()`
  - `hooks/useStudentDashboard.ts` (TanStack Query, `refetchInterval: 30_000`, mesmo padrão do RF-19)
  - `components/UpcomingTasksList.tsx` (próximas tarefas, deadline formatado)
  - `components/SubmissionStatusSummary.tsx` (entregues vs pendentes)
  - `components/RecentGradesList.tsx` (últimas notas/feedbacks)
  - `components/SubjectAverageGradesList.tsx` (reaproveita o padrão visual de `StudentAverageGradesList.tsx` do RF-19)
  - `components/StudentDashboard.tsx` (compõe os componentes acima, sem props)
- **Integração:** `OrganizationDashboardPage.tsx` (`features/organization/components/`) ganha um branch `role === 'ALUNO'` que renderiza `<StudentDashboard />`, ao lado dos branches existentes de `ADMIN_ORG`/`GESTOR`.
- Sem nova dependência de pacote.

## Risks / Trade-offs

- [Trade-off] "Últimas notas" usa `updatedAt` como proxy de data de avaliação, não um timestamp dedicado → [Mitigação] aceitável porque a única transição que atualiza uma submissão já avaliada é a própria avaliação; se o projeto evoluir para permitir reavaliação, isso pode distorcer a ordenação (risco aceito, mesmo padrão de proxy já usado em outras partes do módulo `reporting`).
- [Trade-off] Tarefas com submissão não aparecem na lista de "próximas tarefas", mesmo que estejam perto do prazo → aceito pela decisão confirmada com o usuário (lista só de pendentes); informação de "entregue" já está representada na contagem separada.
- [Risco] `getUpcomingPendingTasks` e `getRecentGrades`/`getAverageGradePerSubject` percorrem caminhos de junção diferentes (turma→disciplina→tarefa vs submissão→tarefa→disciplina) para o mesmo conjunto de dados → [Mitigação] aceitável no volume do MVP, mesmo trade-off de queries não unificadas já aceito nos RF-17/18/19.
- [Trade-off] Sem filtro de período — indicadores sempre refletem o histórico completo das turmas do aluno, mesmo padrão dos demais dashboards.
