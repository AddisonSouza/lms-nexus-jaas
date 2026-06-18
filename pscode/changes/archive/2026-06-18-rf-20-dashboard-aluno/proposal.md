## Why

O `ALUNO` não tem hoje uma visão consolidada do próprio progresso: precisa navegar entre turmas, disciplinas e tarefas para descobrir o que está pendente, o que já foi entregue e como estão suas notas. RF-20 (módulo `reporting`) fecha o conjunto de dashboards por papel (Admin/Gestor/Professor já implementados) entregando ao aluno um painel único com as próximas tarefas por urgência de prazo, entregues vs pendentes, últimas notas/feedbacks e a média geral por disciplina.

## What Changes

- Novo endpoint `GET /students/me/dashboard` (papel `ALUNO`, RBAC via `@RolesAllowed`), self-scoped pelo `sub` do JWT — sem path param de organização/turma.
- Novo `GetStudentDashboardUseCase` + `StudentDashboardQueryPort` no módulo `reporting`, seguindo o mesmo padrão Ports & Adapters do `GetProfessorDashboardUseCase` (RF-19): leitura cross-módulo via `EntityManager` (assessment, curriculum, classroom, identity).
- Lista de próximas tarefas pendentes (publicadas, sem submissão do aluno) ordenada por `deadline` ascendente (mais urgente primeiro).
- Contagem de tarefas entregues vs pendentes nas turmas do aluno.
- Últimas notas e feedbacks recebidos (submissões avaliadas, mais recentes primeiro).
- Média geral de notas por disciplina, calculada apenas sobre submissões avaliadas do aluno.
- Frontend: novo componente `StudentDashboard` em `features/dashboard`, montado em `OrganizationDashboardPage` quando `role === 'ALUNO'` (mesmo padrão usado para `AdminDashboard`/`GestorDashboard`).

## Capabilities

### New Capabilities
- `student-dashboard`: dashboard agregado do aluno com próximas tarefas por urgência, status de entregas, últimas notas/feedbacks e médias por disciplina, restrito aos próprios dados do aluno autenticado.

### Modified Capabilities

(nenhuma — não há mudança de comportamento em specs existentes)

## Impact

- **Backend**: novo pacote dentro de `reporting` (domain/model, domain/port/in e out, application/dto e usecase, infrastructure/persistence, interfaces/rest). Leitura cross-módulo de `assessment` (Task, TaskSubmission), `curriculum` (Subject, SubjectClassroom), `classroom` (ClassroomMember) e `identity` (User) — sem nova migration Flyway (somente leitura).
- **Frontend**: novo arquivo `api/student-dashboard.ts`, entrada em `api/query-keys.ts`, hook `useStudentDashboard`, componentes em `features/dashboard/components/`, e branch condicional em `OrganizationDashboardPage.tsx`.
- **Testes**: unitário do use case (mock do port), integração do query port (dados reais) e da REST resource (`@TestSecurity` papel `ALUNO`), além de testes de componente no frontend.
