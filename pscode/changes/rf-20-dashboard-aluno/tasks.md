## 1. Backend — Domínio e Ports

- [x] 1.1 [BE] Criar `UpcomingTask` (VO: `taskId`, `title`, `subjectName`, `deadline`) em `reporting/domain/model/`
- [x] 1.2 [BE] Criar `RecentGrade` (VO: `taskId`, `title`, `subjectName`, `grade`, `feedback`) em `reporting/domain/model/`
- [x] 1.3 [BE] Criar `SubjectAverageGrade` (VO: `subjectId`, `subjectName`, `averageGrade`) em `reporting/domain/model/`
- [x] 1.4 [BE] Criar porta de entrada `GetStudentDashboardUseCase` em `reporting/domain/port/in/`
- [x] 1.5 [BE] Criar porta de saída `StudentDashboardQueryPort` (`getUpcomingPendingTasks`, `countSubmittedTasks`, `countPendingTasks`, `getRecentGrades`, `getAverageGradePerSubject`) em `reporting/domain/port/out/`

## 2. Backend — Infraestrutura (Query Port)

- [x] 2.1 [BE] Implementar `StudentDashboardQueryPortImpl.getUpcomingPendingTasks` (tarefas `PUBLISHED` de disciplinas vinculadas, via `subject_classrooms`, a turmas em que o aluno está matriculado em `classroom_members` (`role = 'ALUNO'`), sem submissão do aluno em `task_submissions`, ordenadas por `deadline ASC`)
- [x] 2.2 [BE] Implementar `StudentDashboardQueryPortImpl.countPendingTasks` e `countSubmittedTasks` (mesma base elegível de 2.1; pendente = sem submissão, entregue = com submissão de qualquer status)
- [x] 2.3 [BE] Implementar `StudentDashboardQueryPortImpl.getRecentGrades` (submissões `EVALUATED` do aluno, ordenadas por `updatedAt DESC`, limitadas a 5)
- [x] 2.4 [BE] Implementar `StudentDashboardQueryPortImpl.getAverageGradePerSubject` (média de `grade` agrupada por `subject_id`/`subject.name`, considerando apenas submissões `EVALUATED` do aluno)
- [x] 2.5 [BE] Garantir filtro por `organization_id` (extraído do JWT) em todas as queries acima, usando a coluna `organization_id` já presente em `tasks`, `task_submissions` e `classroom_members`

## 3. Backend — Use Case e DTOs

- [x] 3.1 [BE] Criar `StudentDashboardResponse`, `UpcomingTaskResponse`, `RecentGradeResponse`, `SubjectAverageGradeResponse` em `reporting/application/dto/`
- [x] 3.2 [BE] Implementar `GetStudentDashboardService`: chama os métodos do `StudentDashboardQueryPort` com `studentId`/`organizationId` e monta `StudentDashboardResponse`

## 4. Backend — Endpoint REST

- [x] 4.1 [BE] Criar `StudentDashboardResource` com `GET /students/me/dashboard` (`@RolesAllowed("ALUNO")`, extraindo `studentId = jwt.getSubject()` e `organizationId = jwt.getClaim("org")`, sem `@PathParam`)

## 5. Backend — Testes

- [x] 5.1 [BE] Testes unitários de `GetStudentDashboardService` (monta o dashboard completo a partir dos dados do Port; aluno sem nenhuma tarefa retorna indicadores vazios/zero)
- [x] 5.2 [BE] Testes de integração `@QuarkusTest` de `StudentDashboardQueryPortImpl` cobrindo: próximas tarefas pendentes ordenadas por deadline (com e sem tarefas elegíveis), contagem de entregues vs pendentes, últimas notas (com e sem submissões avaliadas, respeitando o limite de 5), média de notas por disciplina (com mais de uma disciplina e com disciplina sem avaliação)
- [x] 5.3 [BE] Testes de integração `@QuarkusTest` para `StudentDashboardResource` cobrindo os cenários da spec (sucesso, papel não autorizado → 403, isolamento por `organization_id`)

## 6. Frontend — Tipos e API

- [x] 6.1 [FE] Adicionar `UpcomingTask`, `RecentGrade`, `SubjectAverageGrade`, `StudentDashboardData` a `features/dashboard/types.ts`
- [x] 6.2 [FE] Criar `features/dashboard/api/student-dashboard.ts` com `getStudentDashboard()` (sem parâmetro, self-scoped, validação Zod do response)
- [x] 6.3 [FE] Adicionar `dashboardKeys.student()` a `features/dashboard/api/query-keys.ts`

## 7. Frontend — Hooks e componentes

- [x] 7.1 [FE] Criar hook `useStudentDashboard()` (TanStack Query, `refetchInterval: 30_000`, mesmo padrão de `useProfessorDashboard`)
- [x] 7.2 [FE] Criar `UpcomingTasksList.tsx` (próximas tarefas pendentes, deadline formatado; trata lista vazia)
- [x] 7.3 [FE] Criar `SubmissionStatusSummary.tsx` (contagem de entregues vs pendentes)
- [x] 7.4 [FE] Criar `RecentGradesList.tsx` (últimas notas/feedbacks recebidos; trata lista vazia)
- [x] 7.5 [FE] Criar `SubjectAverageGradesList.tsx` (média de notas por disciplina; trata lista vazia)
- [x] 7.6 [FE] Criar `StudentDashboard.tsx` compondo os componentes acima, sem props
- [x] 7.7 [FE] Atualizar `OrganizationDashboardPage.tsx`: renderizar `<StudentDashboard />` quando `useAuthStore((s) => s.role) === 'ALUNO'`, ao lado dos branches existentes de `ADMIN_ORG`/`GESTOR`

## 8. Frontend — Testes

- [x] 8.1 [FE] Testes Vitest + Testing Library para `UpcomingTasksList` (renderização de dados ordenados, lista vazia)
- [x] 8.2 [FE] Testes para `SubmissionStatusSummary` e `RecentGradesList` (lista com notas, lista vazia)
- [x] 8.3 [FE] Testes para `SubjectAverageGradesList`
- [x] 8.4 [FE] Atualizar teste de `OrganizationDashboardPage` cobrindo a renderização condicional para `ALUNO`
