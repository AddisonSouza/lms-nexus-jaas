## Context

O módulo `assessment` não existe no backend nem no frontend. O projeto segue Hexagonal Architecture + DDD — o mesmo padrão do módulo `curriculum` (referência). O `storage` já expõe `StoragePort` para upload de arquivos; o módulo `curriculum` já o usa em `SubjectContent`. Dois casos de uso são entregues por este RF: criação (`DRAFT`) e publicação (`PUBLISHED`), conforme escopo acordado.

## Goals / Non-Goals

**Goals:**
- Criar o módulo `assessment` com aggregate `Task` e ciclo de vida DRAFT → PUBLISHED
- Persistir anexos via `StoragePort` (contexto `task_attachment`)
- Disparar `TaskCreatedEvent` via CDI ao publicar
- Entregar formulário React com markdown textarea, upload de anexos e ação de publicação

**Non-Goals:**
- Transição automática PUBLISHED → CLOSED (scheduler — RF futuro)
- Status GRADED (depende de submissões avaliadas)
- Editor WYSIWYG (enunciado em Markdown + textarea)
- Listagem de tarefas para alunos

## Decisions

**1. Vinculação Task → Subject**

Task é vinculada ao `Subject` (disciplina), não diretamente ao `Classroom`. Um Subject pode estar vinculado a múltiplas Classrooms. Consultas que precisem filtrar por Classroom devem resolver via Subject.

*Alternativa rejeitada:* Task → Classroom diretamente. Geraria duplicação quando o mesmo Subject corre em múltiplas Classrooms simultaneamente.

**2. Enunciado em Markdown**

Campo `description` armazenado como TEXT/LONGTEXT no banco. Frontend renderiza textarea com suporte a Markdown sem dependência nova. O RF menciona "texto rico" como intenção de usuário, não como requisito técnico de editor.

*Alternativa rejeitada:* TipTap WYSIWYG — adiciona ~50KB e uma nova dependência ao projeto sem ganho funcional suficiente para MVP.

**3. Anexos em tabela separada (`task_attachments`)**

Relacionamento 1:N entre `tasks` e `task_attachments`. Cada registro armazena `file_key`, `original_name`, `mime_type` e `size_bytes`. Upload via endpoint multipart separado ou junto ao POST de criação da tarefa.

*Decisão:* Upload inline no POST /tasks como multipart/form-data (mesmo padrão do RF-10 em `/subjects/{id}/contents`).

**4. TaskCreatedEvent via CDI**

Evento disparado pelo `PublishTaskUseCase` usando `jakarta.enterprise.event.Event<TaskCreatedEvent>`. Desacoplamento do módulo `communication` (RF-16). O evento é fire-and-forget no mesmo contexto transacional.

*Alternativa rejeitada:* Mensageria (Kafka/ActiveMQ) — overhead desnecessário para MVP com módulo monolítico.

**5. Estrutura de pacotes**

Segue exatamente o padrão do módulo `curriculum`:
```
apps/api/src/main/java/br/edu/lms/module/assessment/
  domain/model/          Task.java, TaskId.java, TaskStatus.java, TaskAttachment.java
  domain/event/          TaskCreatedEvent.java
  domain/exception/      TaskNotFoundException.java, InvalidTaskStateException.java
  domain/port/in/        CreateTaskUseCase.java, PublishTaskUseCase.java
  domain/port/out/       TaskRepository.java, SubjectQueryPort.java
  application/usecase/   CreateTaskService.java, PublishTaskService.java
  application/dto/       CreateTaskCommand.java, TaskResponse.java, TaskAttachmentResponse.java
  infrastructure/persistence/  TaskEntity.java, TaskAttachmentEntity.java, TaskRepositoryAdapter.java
  interfaces/rest/       TaskResource.java
```

## Risks / Trade-offs

- [Autorização por professor-autor] A validação de que o professor é o autor da tarefa depende do `userId` extraído do JWT. Mitigação: `Task` armazena `createdBy` (userId do JWT); `PublishTaskService` compara antes de prosseguir.
- [Upload multipart + JSON] O endpoint `POST /tasks` precisa combinar dados JSON com arquivos. Mitigação: usar `@MultipartForm` do Quarkus REST (RESTEasy Reactive), igual ao padrão do RF-10.
- [Visibilidade DRAFT] Consultas de alunos devem filtrar por `status = 'PUBLISHED'`. Mitigação: `SubjectQueryPort` retorna apenas tarefas publicadas quando o chamador é ALUNO (filtro no repository adapter via role claim).
