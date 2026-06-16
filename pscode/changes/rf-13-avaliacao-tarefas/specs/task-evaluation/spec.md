## ADDED Requirements

### Requirement: Professor lista submissões de uma tarefa
O sistema SHALL retornar todas as `TaskSubmission` de uma tarefa pertencente ao professor autenticado, incluindo status e dados de cada aluno.

#### Scenario: Listagem bem-sucedida
- **WHEN** professor autenticado chama `GET /tasks/{taskId}/submissions` e a tarefa pertence à sua organização e foi criada por ele
- **THEN** sistema retorna 200 com lista de submissões contendo `id`, `studentId`, `textResponse`, `status`, `grade`, `feedback`, `attachments`, `createdAt`

#### Scenario: Tarefa sem submissões
- **WHEN** professor chama `GET /tasks/{taskId}/submissions` e nenhum aluno submeteu resposta
- **THEN** sistema retorna 200 com lista vazia

#### Scenario: Tarefa pertence a outro professor
- **WHEN** professor chama `GET /tasks/{taskId}/submissions` e a tarefa foi criada por outro professor
- **THEN** sistema retorna 403

#### Scenario: Tarefa não encontrada
- **WHEN** professor chama `GET /tasks/{taskId}/submissions` com ID inexistente na organização
- **THEN** sistema retorna 404

---

### Requirement: Professor avalia uma submissão
O sistema SHALL permitir que o professor atribua nota (se a tarefa tiver `maxScore`) e/ou feedback textual a uma `TaskSubmission` com status `SUBMITTED`, alterando o status para `EVALUATED` e publicando `SubmissionEvaluatedEvent`.

#### Scenario: Avaliação com nota e feedback
- **WHEN** professor chama `PATCH /submissions/{id}/evaluation` com `grade` e `feedback`, a tarefa tem `maxScore` não-nulo e a submissão está em `SUBMITTED`
- **THEN** sistema persiste `grade` e `feedback`, altera `status` para `EVALUATED`, publica `SubmissionEvaluatedEvent` e retorna 200 com a submissão atualizada

#### Scenario: Avaliação apenas com feedback (tarefa sem pontuação)
- **WHEN** professor chama `PATCH /submissions/{id}/evaluation` com `feedback` e sem `grade`, e a tarefa tem `maxScore` nulo
- **THEN** sistema persiste `feedback`, altera `status` para `EVALUATED`, publica `SubmissionEvaluatedEvent` e retorna 200

#### Scenario: Nota fornecida mas tarefa sem pontuação máxima
- **WHEN** professor envia `grade` em `PATCH /submissions/{id}/evaluation` e a tarefa tem `maxScore` nulo
- **THEN** sistema retorna 422 com mensagem indicando que a tarefa não aceita nota

#### Scenario: Nota excede pontuação máxima
- **WHEN** professor envia `grade > task.maxScore`
- **THEN** sistema retorna 422

#### Scenario: Submissão já avaliada
- **WHEN** professor chama `PATCH /submissions/{id}/evaluation` e `submission.status == EVALUATED`
- **THEN** sistema retorna 422 com `SubmissionAlreadyEvaluatedException`

#### Scenario: Submissão não pertence à organização do professor
- **WHEN** professor chama `PATCH /submissions/{id}/evaluation` e a submissão é de outra organização
- **THEN** sistema retorna 403

#### Scenario: Submissão não encontrada
- **WHEN** professor chama `PATCH /submissions/{id}/evaluation` com ID inexistente
- **THEN** sistema retorna 404
