## 1. [INFRA] Flyway Migrations

- [x] 1.1 [INFRA] Criar `V015__create_tasks_table.sql` com colunas: `id`, `subject_id`, `organization_id`, `created_by`, `title`, `description` (LONGTEXT), `deadline`, `max_score` (DECIMAL nullable), `status` (ENUM: DRAFT, PUBLISHED, CLOSED, GRADED), `created_at`, `updated_at`, `deleted_at`
- [x] 1.2 [INFRA] Criar `V016__create_task_attachments_table.sql` com colunas: `id`, `task_id` (FK), `file_key`, `original_name`, `mime_type`, `size_bytes`, `created_at`

## 2. [BE] Domain Layer — Assessment Module

- [x] 2.1 [BE] Criar Value Object `TaskId` (UUID wrapper, imutável com Lombok `@Value`)
- [x] 2.2 [BE] Criar enum `TaskStatus` com valores `DRAFT`, `PUBLISHED`, `CLOSED`, `GRADED`
- [x] 2.3 [BE] Criar aggregate `Task` com Lombok `@Builder` e campos: `id`, `subjectId`, `organizationId`, `createdBy`, `title`, `description`, `deadline`, `maxScore`, `status`, `createdAt`, `updatedAt`, `deletedAt`
- [x] 2.4 [BE] Criar record `TaskAttachment` (file_key, originalName, mimeType, sizeBytes)
- [x] 2.5 [BE] Criar `TaskCreatedEvent` (record com `taskId`, `subjectId`, `organizationId`)
- [x] 2.6 [BE] Criar exceções de domínio: `TaskNotFoundException`, `InvalidTaskStateException`, `UnauthorizedTaskOperationException`
- [x] 2.7 [BE] Criar port `CreateTaskUseCase` (interface em `domain/port/in/`)
- [x] 2.8 [BE] Criar port `PublishTaskUseCase` (interface em `domain/port/in/`)
- [x] 2.9 [BE] Criar port `TaskRepository` (interface em `domain/port/out/`): `save`, `findById`, `findByIdAndOrganization`
- [x] 2.10 [BE] Criar port `SubjectQueryPort` (interface em `domain/port/out/`): `existsByIdAndTeacher`, `existsById`

## 3. [BE] Application Layer

- [x] 3.1 [BE] Criar `CreateTaskCommand` DTO (título, descrição, deadline, maxScore, subjectId, lista de anexos)
- [x] 3.2 [BE] Criar `TaskResponse` e `TaskAttachmentResponse` DTOs com MapStruct mapper
- [x] 3.3 [BE] Implementar `CreateTaskService`: valida professor no Subject via `SubjectQueryPort`, valida prazo futuro, persiste anexos via `StoragePort` (tipo `task_attachment`), salva task com status `DRAFT`
- [x] 3.4 [BE] Implementar `PublishTaskService`: carrega task, valida que o chamador é o `createdBy`, valida transição `DRAFT → PUBLISHED`, salva, dispara `TaskCreatedEvent` via CDI `Event<TaskCreatedEvent>.fire()`

## 4. [BE] Infrastructure Layer

- [x] 4.1 [BE] Criar `TaskEntity` com Hibernate/Panache (`@Entity`, Lombok `@Data`), campos conforme migration V015, soft delete
- [x] 4.2 [BE] Criar `TaskAttachmentEntity` (`@Entity`, FK para `TaskEntity`)
- [x] 4.3 [BE] Criar `TaskRepositoryAdapter` implementando `TaskRepository` com MapStruct para conversão Entity ↔ Domain
- [x] 4.4 [BE] Criar `SubjectQueryAdapter` implementando `SubjectQueryPort` consultando `SubjectEntity` (módulo curriculum) via repositório interno
- [x] 4.5 [BE] Criar `TaskMapper` (MapStruct) para `Task ↔ TaskEntity`, `TaskAttachment ↔ TaskAttachmentEntity`, `Task → TaskResponse`

## 5. [BE] Interface Layer (REST)

- [x] 5.1 [BE] Criar `TaskResource` com `@Path("/tasks")`, `@RolesAllowed("PROFESSOR")` na classe
- [x] 5.2 [BE] Implementar `POST /tasks` com `@MultipartForm` para receber dados JSON + arquivos; extrair `organizationId` do JWT claim `org`
- [x] 5.3 [BE] Implementar `PATCH /tasks/{id}/publish` retornando `TaskResponse`

## 6. [BE] Testes Backend

- [x] 6.1 [BE] Testes unitários `CreateTaskServiceTest` com Mockito: prazo passado, tipo de arquivo inválido, professor não vinculado ao Subject, criação bem-sucedida
- [x] 6.2 [BE] Testes unitários `PublishTaskServiceTest` com Mockito: publicação bem-sucedida, tarefa já publicada, professor não autor
- [x] 6.3 [BE] Teste de integração `TaskResourceIT` com `@QuarkusTest` + Testcontainers: POST /tasks e PATCH /tasks/{id}/publish; verificar persistência e evento

## 7. [FE] Feature Assessment

- [x] 7.1 [FE] Criar `apps/web/src/features/assessment/types.ts` com tipos `Task`, `TaskStatus`, `TaskAttachment`
- [x] 7.2 [FE] Criar `apps/web/src/features/assessment/schemas/task.schema.ts` com Zod schema para o formulário (título, enunciado, deadline, maxScore, arquivos)
- [x] 7.3 [FE] Criar `apps/web/src/features/assessment/api/tasks.ts` com funções `createTask` e `publishTask` usando `fetch`/axios
- [x] 7.4 [FE] Criar hook `useCreateTask` (TanStack Query mutation, invalida query key `['tasks']`)
- [x] 7.5 [FE] Criar hook `usePublishTask` (TanStack Query mutation, invalida task específica)
- [x] 7.6 [FE] Criar componente `TaskFormDialog.tsx`: React Hook Form + Zod, campos título, textarea markdown para enunciado, date picker para deadline, number input para pontuação máxima, file input multi-arquivo com validação de tipo/extensão
- [x] 7.7 [FE] Criar `TaskListPage.tsx` com listagem de tarefas do professor, botão "Nova Tarefa" abrindo `TaskFormDialog`, ação "Publicar" por linha chamando `usePublishTask`
- [x] 7.8 [FE] Registrar rota `/assessment/tasks` em `apps/web/src/router.tsx` protegida via `ProtectedRoute` com role `PROFESSOR`

## 8. [FE] Testes Frontend

- [x] 8.1 [FE] Testes unitários `TaskFormDialog.test.tsx` com Vitest + Testing Library + MSW: validação de prazo passado, tipo de arquivo inválido, submissão bem-sucedida
- [x] 8.2 [FE] Teste unitário `useCreateTask.test.ts` e `usePublishTask.test.ts` com MSW mockando os endpoints
