## Why

RF-12 completa o ciclo de avaliação iniciado no RF-11: tarefas publicadas permanecem inacessíveis para o aluno até que exista um mecanismo de envio. Sem submissões, professores não têm o que avaliar e alunos não têm como participar da disciplina.

## What Changes

- **Novo:** endpoint `GET /tasks/published` para alunos listarem tarefas publicadas de sua organização
- **Novo:** endpoint `POST /tasks/{id}/submissions` para alunos enviarem resposta (texto e/ou arquivos)
- **Novo:** endpoint `PUT /tasks/{id}/submissions/{submissionId}` para alunos editarem resposta antes do prazo
- **Novo:** `TaskSubmission` domain model com status `SUBMITTED` e `EVALUATED`
- **Novo:** `TaskSubmittedEvent` publicado após cada envio/edição
- **Novo:** migrations V017 (task_submissions) e V018 (submission_attachments)
- **Novo:** página do aluno com lista de tarefas e formulário de envio (FE)
- **Regra:** prazo expirado bloqueia submissão e edição com 422

## Capabilities

### New Capabilities

- `task-submission`: Aluno visualiza tarefas publicadas e envia/edita resposta com texto e/ou arquivos antes do prazo

### Modified Capabilities

- `file-storage`: Novo contexto `submission_attachment` usado pelo `StoragePort` (sem mudança de requisito, apenas extensão de uso)

## Impact

- **Backend:** módulo `assessment` — novos models, use cases, ports, infra e endpoint REST
- **Frontend:** feature `assessment` — nova rota `/assessment/student-tasks` com `@RolesAllowed("ALUNO")`
- **Banco:** 2 novas tabelas (task_submissions, submission_attachments)
- **Non-goals:** avaliação/nota (RF-13), notificação push ao professor (consumidor do `TaskSubmittedEvent`), histórico de versões da submissão
