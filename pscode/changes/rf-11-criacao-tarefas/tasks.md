## 1. [INFRA] Flyway Migrations

- [ ] 1.1 [INFRA] Criar `V015__create_tasks_table.sql` com colunas: `id`, `subject_id`, `organization_id`, `created_by`, `title`, `description` (LONGTEXT), `deadline`, `max_score` (DECIMAL nullable), `status` (ENUM: DRAFT, PUBLISHED, CLOSED, GRADED), `created_at`, `updated_at`, `deleted_at`
- [ ] 1.2 [INFRA] Criar `V016__create_task_attachments_table.sql` com colunas: `id`, `task_id` (FK), `file_key`, `original_name`, `mime_type`, `size_bytes`, `created_at`

## 2. [BE] Domain Layer — Assessment Module

- [ ] 2.1 [BE] Criar Value Object `TaskId` (UUID wrapper, imutável com Lombok `@Value`)
- [ ] 2.2 [BE] Criar enum `TaskStatus` com valores `DRAFT`, `PUBLISHED`, `CLOSED`, `GRADED`
- [ ] 2.3 [BE] Criar aggregate `Task` com Lombok `@Builder` e campos: `id`, `subjectId`, `organizationId`, `createdBy`, `title`, `description`, `deadline`, `maxScore`, `status`, `createdAt`, `updatedAt`, `deletedAt`
- [ ] 2.4 [BE] Criar record `TaskAttachment` (file_key, originalName, mimeType, sizeBytes)
- [ ] 2.5 [BE] Criar `TaskCreatedEvent` (record com `taskId`, `subjectId`, `organizationId`)
- [ ] 2.6 [BE] Criar exceções de domínio: `TaskNotFoundException`, `InvalidTaskStateException`, `UnauthorizedTaskOperationException`
- [ ] 2.7 [BE] Criar port `CreateTaskUseCase` (interface em `domain/port/in/`)
- [ ] 2.8 [BE] Criar port `PublishTaskUseCase` (interface em `domain/port/in/`)
- [ ] 2.9 [BE] Criar port `TaskRepository` (interface em `domain/port/out/`): `save`, `findById`, `findByIdAndOrganization`
- [ ] 2.10 [BE] Criar port `SubjectQueryPort` (interface em `domain/port/out/`): `existsByIdAndTeacher`, `existsById`

## 3. [BE] Application Layer

- [ ] 3.1 [BE] Criar `CreateTaskCommand` DTO (título, descrição, deadline, maxScore, subjectId, lista de anexos)
- [ ] 3.2 [BE] Criar `TaskResponse` e `TaskAttachmentResponse` DTOs com MapStruct mapper
- [ ] 3.3 [BE] Implementar `CreateTaskService`: valida professor no Subject via `SubjectQueryPort`, valida prazo futuro, persiste anexos via `StoragePort` (tipo `task_attachment`), salva task com status `DRAFT`
- [ ] 3.4 [BE] Implementar `PublishTaskService`: carrega task, valida que o chamador é o `createdBy`, valida transição `DRAFT → PUBLISHED`, salva, dispara `TaskCreatedEvent` via CDI `Event<TaskCreatedEvent>.fire()`

## 4. [BE] Infrastructure Layer

- [ ] 4.1 [BE] Criar `TaskEntity` com Hibernate/Panache (`@Entity`, Lombok `@Data`), campos conforme migration V015, soft delete
- [ ] 4.2 [BE] Criar `TaskAttachmentEntity` (`@Entity`, FK para `TaskEntity`)
- [ ] 4.3 [BE] Criar `TaskRepositoryAdapter` implementando `TaskRepository` com MapStruct para conversão Entity ↔ Domain
- [ ] 4.4 [BE] Criar `SubjectQueryAdapter` implementando `SubjectQueryPort` consultando `SubjectEntity` (módulo curriculum) via repositório interno
- [ ] 4.5 [BE] Criar `TaskMapper` (MapStruct) para `Task ↔ TaskEntity`, `TaskAttachment ↔ TaskAttachmentEntity`, `Task → TaskResponse`

## 5. [BE] Interface Layer (REST)

- [ ] 5.1 [BE] Criar `TaskResource` com `@Path("/tasks")`, `@RolesAllowed("PROFESSOR")` na classe
- [ ] 5.2 [BE] Implementar `POST /tasks` com `@MultipartForm` para receber dados JSON + arquivos; extrair `organizationId` do JWT claim `org`
- [ ] 5.3 [BE] Implementar `PATCH /tasks/{id}/publish` retornando `TaskResponse`

## 6. [BE] Testes Backend

- [ ] 6.1 [BE] Testes unitários `CreateTaskServiceTest` com Mockito: prazo passado, tipo de arquivo inválido, professor não vinculado ao Subject, criação bem-sucedida
- [ ] 6.2 [BE] Testes unitários `PublishTaskServiceTest` com Mockito: publicação bem-sucedida, tarefa já publicada, professor não autor
- [ ] 6.3 [BE] Teste de integração `TaskResourceIT` com `@QuarkusTest` + Testcontainers: POST /tasks e PATCH /tasks/{id}/publish; verificar persistência e evento

## 7. [FE] Feature Assessment

- [ ] 7.1 [FE] Criar `apps/web/src/features/assessment/types.ts` com tipos `Task`, `TaskStatus`, `TaskAttachment`
- [ ] 7.2 [FE] Criar `apps/web/src/features/assessment/schemas/task.schema.ts` com Zod schema para o formulário (título, enunciado, deadline, maxScore, arquivos)
- [ ] 7.3 [FE] Criar `apps/web/src/features/assessment/api/tasks.ts` com funções `createTask` e `publishTask` usando `fetch`/axios
- [ ] 7.4 [FE] Criar hook `useCreateTask` (TanStack Query mutation, invalida query key `['tasks']`)
- [ ] 7.5 [FE] Criar hook `usePublishTask` (TanStack Query mutation, invalida task específica)
- [ ] 7.6 [FE] Criar componente `TaskFormDialog.tsx`: React Hook Form + Zod, campos título, textarea markdown para enunciado, date picker para deadline, number input para pontuação máxima, file input multi-arquivo com validação de tipo/extensão
- [ ] 7.7 [FE] Criar `TaskListPage.tsx` com listagem de tarefas do professor, botão "Nova Tarefa" abrindo `TaskFormDialog`, ação "Publicar" por linha chamando `usePublishTask`
- [ ] 7.8 [FE] Registrar rota `/assessment/tasks` em `apps/web/src/router.tsx` protegida via `ProtectedRoute` com role `PROFESSOR`

## 8. [FE] Testes Frontend

- [ ] 8.1 [FE] Testes unitários `TaskFormDialog.test.tsx` com Vitest + Testing Library + MSW: validação de prazo passado, tipo de arquivo inválido, submissão bem-sucedida
- [ ] 8.2 [FE] Teste unitário `useCreateTask.test.ts` e `usePublishTask.test.ts` com MSW mockando os endpoints
