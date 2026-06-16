## Why

RF-13 fecha o ciclo avaliativo do módulo `assessment`: após o aluno enviar sua resposta (RF-12), o professor precisa atribuir nota e feedback, alterando o status da submissão para `EVALUATED` e publicando `SubmissionEvaluatedEvent` via CDI.

## What Changes

- Novo endpoint `GET /tasks/{taskId}/submissions` — professor lista todas as submissões de uma tarefa com status de cada aluno.
- Novo endpoint `PATCH /submissions/{id}/evaluation` — professor avalia uma submissão (nota opcional + feedback).
- Campos `grade` (DECIMAL 5,2, nullable) e `feedback` (LONGTEXT, nullable) adicionados à tabela `task_submissions` via Flyway V019.
- `TaskSubmission` domain e `SubmissionResponse` DTO atualizados com os novos campos.
- `SubmissionEvaluatedEvent` publicado via CDI após avaliação (sem consumer por enquanto — RF-16 implementará notificações).
- Frontend: painel do professor na `TaskListPage` com drawer/dialog de submissões e formulário de avaliação.

## Capabilities

### New Capabilities

- `task-evaluation`: Avaliação de submissões pelo professor — listar submissões por tarefa, atribuir nota e feedback, transição de status para `EVALUATED`.

### Modified Capabilities

- `task-submission`: Adiciona campos `grade` e `feedback` ao modelo de submissão (schema change + DTO); nenhuma regra de negócio existente muda.

## Impact

- **Backend:** módulo `assessment` — novo use case, novo port in, migration V019, `TaskResource` com 2 novos endpoints, `SubmissionRepository` com novo método de busca por tarefa.
- **Frontend:** `features/assessment` — novo hook `useEvaluateSubmission`, novo componente `SubmissionListDrawer`, nova página/view de avaliação para PROFESSOR.
- **Sem breaking changes** para endpoints existentes.
