## Why

RF-14 — módulo `assessment`. Alunos já conseguem enviar respostas (RF-12) e professores já avaliam (RF-13), mas o aluno não tem como visualizar a nota recebida nem o feedback do professor. Essa lacuna encerra o ciclo avaliativo: sem visibilidade do resultado, o aprendizado fica incompleto.

## What Changes

- Novo endpoint `GET /tasks/my-grades` (ALUNO): retorna todas as tarefas publicadas da organização com a submissão do próprio aluno embutida (status, nota, feedback, datas, indicação de atraso).
- Novo endpoint `GET /submissions/{id}/feedback` (ALUNO): retorna o feedback detalhado de uma submissão específica, apenas se avaliada.
- Nova resposta DTO `TaskWithGradeResponse` combinando dados de tarefa + submissão do aluno.
- `SubmissionRepository` ganha método `findByStudentAndOrganization(studentId, orgId)`.
- `StudentTaskListPage` (FE) aprimorada: exibe status da submissão, nota e badge de prazo; botão muda para "Ver Nota" quando avaliado, abrindo drawer com feedback completo.

## Capabilities

### New Capabilities

- `student-grades`: Visualização de notas, feedback e status de submissão pelo aluno — inclui endpoint de listagem combinada e endpoint de detalhe de feedback.

### Modified Capabilities

- `task-submission`: Repositório ganha novo método de consulta por aluno+organização (sem mudança de requisito funcional visível ao usuário).

## Impact

- **Backend**: módulo `assessment` — novo usecase `ListStudentGradesService`, novo DTO, nova rota no `TaskResource`, novo método no `SubmissionRepository` e sua implementação JPA.
- **Frontend**: `features/assessment` — novo hook `useStudentGrades`, novo tipo `TaskWithGrade`, componente `GradeFeedbackDrawer`, atualização de `StudentTaskListPage`.
- **Sem impacto em banco**: dados já existem em `task_submissions` (grade, feedback, status).
- **Non-goals**: edição de nota pelo professor (já coberta em RF-13); dashboard agregado de desempenho (RF-20); notificações ao receber nota (RF-16).
