## Why

RF-11 — Professores precisam criar e publicar tarefas avaliativas para suas turmas no módulo `assessment`, que ainda não existe. Sem esse módulo, não é possível iniciar o ciclo de avaliação da plataforma (submissões, correções, notas).

## What Changes

- Criação do módulo `assessment` no backend (novo pacote `br.edu.lms.module.assessment`)
- Novo aggregate `Task` com ciclo de vida `DRAFT → PUBLISHED`
- `CreateTaskUseCase`: cria tarefa vinculada a um Subject, armazena anexos via `StoragePort`
- `PublishTaskUseCase`: muda status para `PUBLISHED` e dispara `TaskCreatedEvent` via CDI
- Migration Flyway `V015__create_tasks_table.sql` + `V016__create_task_attachments_table.sql`
- Nova feature `assessment` no frontend com formulário de criação e ação de publicação
- Enunciado em Markdown (textarea simples, sem nova dependência)

## Capabilities

### New Capabilities

- `task-creation`: Professor cria tarefa (`DRAFT`) vinculada a um Subject, com título, enunciado markdown, prazo, pontuação máxima (opcional) e upload de até N anexos (`task_attachment`)
- `task-publishing`: Professor publica a tarefa (`DRAFT → PUBLISHED`); sistema dispara `TaskCreatedEvent` para ser consumido por RF-16

### Modified Capabilities

_(nenhuma)_

## Impact

- **Backend:** novo módulo `assessment`; integração com `StoragePort` (storage); acoplamento por evento com `communication` (RF-16)
- **Frontend:** nova feature `apps/web/src/features/assessment`; nova rota protegida para `PROFESSOR`
- **Banco:** 2 novas migrations Flyway (V015, V016)
- **Fora de escopo:** CLOSED automático por scheduler, status GRADED, listagem de tarefas para alunos, processamento da notificação (RF-16)

### Non-goals

- Transição automática `PUBLISHED → CLOSED` ao expirar o prazo (scheduler separado)
- Status `GRADED` (depende de submissões avaliadas — RF futuro)
- Listagem/visualização de tarefas por alunos
- Edição de tarefa já publicada
