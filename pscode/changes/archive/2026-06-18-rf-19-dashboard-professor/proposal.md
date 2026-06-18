## Why

RF-19 (módulo `reporting`) dá ao `PROFESSOR` uma visão consolidada por disciplina — hoje ele precisa abrir cada tarefa individualmente para saber quantas submissões aguardam avaliação ou quais alunos não entregaram. O módulo `reporting` já existe desde o RF-17/RF-18 (Admin/Gestor Dashboard); este RF estende o padrão para o papel `PROFESSOR`, agora com escopo por disciplina em vez de organização/turma.

## What Changes

- Novo endpoint `GET /subjects/{id}/dashboard` (`@RolesAllowed("PROFESSOR")`) retorna, para a disciplina informada: contagem de submissões pendentes de avaliação (status `SUBMITTED`, todas as tarefas da disciplina), distribuição de notas da última tarefa (mais recente por `createdAt`), lista de alunos sem entrega nessa última tarefa, e média de notas por aluno na disciplina.
- Acesso restrito ao professor vinculado à disciplina via `subject_teachers` (reaproveita `SubjectRepository.existsSubjectTeacherLink`); 403 caso não esteja vinculado.
- Sem filtro de período — mesmo padrão do RF-18 (estado atual, não atividade num intervalo).
- Sem exportação em PDF (fora do escopo do RF-19; critérios de aceite não pedem).
- Frontend: dashboard embutido na `SubjectDetailPage` existente (`/curriculum/:subjectId`), visível apenas quando `role === 'PROFESSOR'` e o professor leciona a disciplina; badge de pendências com polling de 30s (mesmo padrão de `useNotifications`).

## Capabilities

### New Capabilities
- `professor-dashboard`: agregação por disciplina (submissões pendentes, distribuição de notas da última tarefa, alunos sem entrega, média de notas por aluno) para o `PROFESSOR` vinculado à disciplina.

### Modified Capabilities

(nenhuma — RF-19 introduz uma nova capability sobre o módulo `reporting` já existente; nenhum contrato de `curriculum`, `assessment` ou `classroom` muda)

## Impact

- **Backend:** módulo `reporting` ganha um novo Query Port (`ProfessorDashboardQueryPort`, cross-module contra `curriculum` e `assessment`), um novo Use Case (`GetProfessorDashboardService`) e um novo `ProfessorDashboardResource`; sem migration Flyway (somente leitura, sem entidade própria, mesmo padrão do RF-17/RF-18).
- **Frontend:** feature `features/dashboard` ganha `ProfessorDashboard.tsx`, hook `useProfessorDashboard` e tipos correspondentes; `SubjectDetailPage` passa a renderizar o dashboard condicionalmente quando `role === 'PROFESSOR'`.
- Sem impacto em contratos REST existentes.
