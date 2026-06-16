## ADDED Requirements

### Requirement: Professor publica tarefa
O sistema SHALL permitir que o professor autor de uma tarefa mude seu status de `DRAFT` para `PUBLISHED` via `PATCH /tasks/{id}/publish`, tornando-a visível para os alunos da turma.

#### Scenario: Publicação bem-sucedida
- **WHEN** professor autor envia `PATCH /tasks/{id}/publish` para tarefa em status `DRAFT`
- **THEN** sistema muda status para `PUBLISHED`, retorna `200 OK` com o recurso atualizado

#### Scenario: Evento disparado ao publicar
- **WHEN** tarefa é publicada com sucesso
- **THEN** sistema dispara `TaskCreatedEvent` via CDI contendo `taskId`, `subjectId` e `organizationId`

#### Scenario: Tarefa já publicada
- **WHEN** professor tenta publicar tarefa que já está em `PUBLISHED`
- **THEN** sistema rejeita com `409 Conflict` e mensagem indicando estado inválido

#### Scenario: Professor não autor da tarefa
- **WHEN** professor diferente do autor tenta publicar a tarefa
- **THEN** sistema rejeita com `403 Forbidden`

#### Scenario: Tarefa visível para alunos após publicação
- **WHEN** tarefa muda para status `PUBLISHED`
- **THEN** alunos vinculados ao Subject passam a enxergar a tarefa em consultas de leitura
