## 1. Flyway Migrations

- [ ] 1.1 [INFRA] Criar `V017__create_task_submissions_table.sql` com colunas: id, task_id (FK tasks), student_id (FK users), organization_id (FK organizations), text_response (LONGTEXT NULL), status (ENUM SUBMITTED/EVALUATED), created_at, updated_at, deleted_at
- [ ] 1.2 [INFRA] Criar `V018__create_submission_attachments_table.sql` com colunas: id, submission_id (FK task_submissions), file_key, original_name, mime_type, size_bytes, created_at

## 2. Domain — assessment

- [ ] 2.1 [BE] Criar `SubmissionId` value object (`@Value`, UUID)
- [ ] 2.2 [BE] Criar `SubmissionStatus` enum (`SUBMITTED`, `EVALUATED`)
- [ ] 2.3 [BE] Criar `SubmissionAttachment` domain model (`@Getter @Builder`)
- [ ] 2.4 [BE] Criar `TaskSubmission` aggregate root (`@Getter @Builder`, campos: id, taskId, studentId, organizationId, textResponse, status, attachments, timestamps)
- [ ] 2.5 [BE] Criar `TaskSubmittedEvent` em `domain/event/`
- [ ] 2.6 [BE] Criar `SubmissionRepository` port em `domain/port/out/` com métodos: `save`, `findById`, `findByTaskAndStudent`
- [ ] 2.7 [BE] Criar `SubmitTaskUseCase` port em `domain/port/in/` (método `execute(SubmitTaskCommand)`)
- [ ] 2.8 [BE] Criar `EditSubmissionUseCase` port em `domain/port/in/` (método `execute(EditSubmissionCommand)`)

## 3. Application — assessment

- [ ] 3.1 [BE] Criar `SubmitTaskCommand` DTO (`@Builder`: taskId, studentId, organizationId, textResponse, attachments)
- [ ] 3.2 [BE] Criar `EditSubmissionCommand` DTO (`@Builder`: submissionId, taskId, studentId, textResponse, attachments)
- [ ] 3.3 [BE] Criar `SubmissionResponse` DTO (`@Builder`)
- [ ] 3.4 [BE] Criar `SubmitTaskService`: valida tarefa PUBLISHED, verifica deadline, verifica submissão duplicada, chama `StoragePort` para arquivos, persiste `TaskSubmission`, publica `TaskSubmittedEvent`
- [ ] 3.5 [BE] Criar `EditSubmissionService`: carrega submissão, valida ownership, valida deadline, valida status SUBMITTED, atualiza campos, publica `TaskSubmittedEvent`

## 4. Infrastructure — assessment

- [ ] 4.1 [BE] Criar `TaskSubmissionJpaEntity` com `@Data @Entity @Table(name="task_submissions")`
- [ ] 4.2 [BE] Criar `SubmissionAttachmentJpaEntity` com `@Data @Entity @Table(name="submission_attachments")`
- [ ] 4.3 [BE] Criar `SubmissionMapper` com MapStruct (domain ↔ JPA entity ↔ DTO)
- [ ] 4.4 [BE] Criar `SubmissionRepositoryImpl` implementando `SubmissionRepository`
- [ ] 4.5 [BE] Adicionar `StorageContext.SUBMISSION_ATTACHMENT` ao enum existente

## 5. REST Interface — assessment

- [ ] 5.1 [BE] Adicionar `GET /tasks/published` em `TaskResource` com `@RolesAllowed("ALUNO")` (mover role de classe para métodos existentes)
- [ ] 5.2 [BE] Adicionar `POST /tasks/{id}/submissions` com `@RolesAllowed("ALUNO")`, `@Consumes(MULTIPART_FORM_DATA)`
- [ ] 5.3 [BE] Adicionar `PUT /tasks/{id}/submissions/{submissionId}` com `@RolesAllowed("ALUNO")`
- [ ] 5.4 [BE] Adicionar `findPublishedByOrganization(orgId)` ao `TaskRepository` port e implementar em `TaskRepositoryImpl`

## 6. Testes — Backend

- [ ] 6.1 [BE] Testes unitários para `SubmitTaskService` (Mockito): prazo expirado → 422, duplicata → 409, sem texto nem arquivo → 422, sucesso → 201 + evento publicado
- [ ] 6.2 [BE] Testes unitários para `EditSubmissionService`: ownership violado → 403, prazo expirado → 422, status EVALUATED → 422, sucesso → 200

## 7. Frontend — assessment

- [ ] 7.1 [FE] Adicionar tipos `TaskSubmission` e `SubmissionStatus` em `features/assessment/types.ts`
- [ ] 7.2 [FE] Criar `features/assessment/api/submissions.ts` com funções `createSubmission` e `updateSubmission` (Axios, multipart)
- [ ] 7.3 [FE] Adicionar query key `submissions` em `features/assessment/api/query-keys.ts`
- [ ] 7.4 [FE] Criar hook `useStudentTasks` em `features/assessment/hooks/` (TanStack Query, `GET /tasks/published`)
- [ ] 7.5 [FE] Criar hook `useSubmitTask` em `features/assessment/hooks/` (useMutation, multipart FormData)
- [ ] 7.6 [FE] Criar schema Zod `submissionSchema` em `features/assessment/schemas/`
- [ ] 7.7 [FE] Criar componente `SubmissionFormDialog.tsx` (React Hook Form + Zod, campo texto + upload múltiplo, desabilitado após prazo)
- [ ] 7.8 [FE] Criar página `StudentTaskListPage.tsx` com listagem de tarefas publicadas e botão "Enviar Resposta"
- [ ] 7.9 [FE] Adicionar rota `/assessment/student-tasks` em `routes.tsx` protegida com `@ProtectedRoute` para role `ALUNO`
