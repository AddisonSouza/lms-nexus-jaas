## Context

O módulo `assessment` já possui `Task`, `TaskRepository` e os endpoints de criação/publicação (RF-11). O módulo `storage` expõe `StoragePort` com implementação `S3StorageAdapter`. Este RF adiciona a capacidade de submissão de respostas pelos alunos, criando um novo aggregate `TaskSubmission` dentro do mesmo bounded context.

## Goals / Non-Goals

**Goals:**
- Aluno lista tarefas publicadas de sua organização
- Aluno cria e edita submissão (texto e/ou arquivos) dentro do prazo
- `TaskSubmittedEvent` publicado a cada envio/edição
- Deadline hard-block: 422 ao tentar submeter/editar fora do prazo

**Non-Goals:**
- Avaliação de submissões (RF-13)
- Consumidor do `TaskSubmittedEvent` (notificações ao professor)
- Paginação da lista de tarefas do aluno (pode ser adicionada depois)

## Decisions

### 1. `TaskSubmission` como aggregate próprio dentro do módulo `assessment`

`TaskSubmission` não é filho de `Task` no mesmo aggregate — é um aggregate independente com `taskId` como referência. Isso evita carregar todas as submissões ao abrir uma tarefa e mantém as operações isoladas.

**Alternativa considerada:** submissão como value object de `Task`. Rejeitada porque violaria SRP e dificultaria queries independentes (RF-13 lista submissões por tarefa).

### 2. Deadline check no Use Case, não no domínio puro

A verificação de prazo é feita no `SubmitTaskService` comparando `LocalDateTime.now()` com `task.getDeadline()`. O domínio `TaskSubmission` não conhece o tempo — apenas recebe o status final.

**Alternativa:** method `task.accept(submission)` no domain. Rejeitada porque requereria que `Task` conhecesse `TaskSubmission`, criando dependência circular dentro do módulo.

### 3. Status da submissão: apenas `SUBMITTED` e `EVALUATED` neste RF

`LATE` é omitido (decisão tomada no grill): prazo expirado → 422 imediato. `EVALUATED` é adicionado como enum value porque RF-13 vai usar, mas a transição é feita por outro use case.

### 4. Endpoint de listagem de tarefas para o aluno: `GET /tasks/published`

Reutiliza o `TaskRepository` existente com novo método `findPublishedByOrganization(orgId)`. Evita criar um resource separado, mantendo a coesão no `TaskResource`.

**Risco:** `TaskResource` tem `@RolesAllowed("PROFESSOR")` na classe. Solução: mover a anotação para cada método e liberar os endpoints do aluno com `@RolesAllowed("ALUNO")`.

### 5. Arquivos de submissão: `StoragePort` com contexto `submission_attachment`

Segue o mesmo padrão de `task_attachment` (RF-11). O `StorageContext` já existe como enum — adicionar `SUBMISSION_ATTACHMENT`.

## Migration Plan

- `V017__create_task_submissions_table.sql` — tabela principal com FK para `tasks`
- `V018__create_submission_attachments_table.sql` — arquivos da submissão com FK para `task_submissions`
- Rollback: `DROP TABLE submission_attachments; DROP TABLE task_submissions;`

## Risks / Trade-offs

- [Risco] Aluno tenta submeter após prazo com latência de relógio → Mitigação: comparação server-side com `LocalDateTime.now()` (sem ajuste de fuso, ambos no servidor)
- [Risco] `TaskResource` com roles mistas → Mitigação: anotar cada endpoint individualmente com `@RolesAllowed`
- [Trade-off] `GET /tasks/published` retorna todas as tarefas publicadas da org, não filtradas por turma — simplificação aceitável para MVP; filtro por turma fica para RF posterior
