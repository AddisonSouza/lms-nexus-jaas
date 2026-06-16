## 1. [INFRA] Flyway Migration

- [x] 1.1 Criar `V019__add_evaluation_fields_to_task_submissions.sql` adicionando colunas `grade DECIMAL(5,2) NULL` e `feedback LONGTEXT NULL` em `task_submissions`

## 2. [BE] Domain — Modelo e Evento

- [x] 2.1 Adicionar campos `grade` (BigDecimal) e `feedback` (String) ao domain model `TaskSubmission.java` (com `@Builder`)
- [x] 2.2 Criar record `SubmissionEvaluatedEvent(submissionId, taskId, studentId, organizationId)` em `domain/event/`
- [x] 2.3 Criar interface `EvaluateSubmissionUseCase` em `domain/port/in/`
- [x] 2.4 Adicionar método `findByTask(String taskId, String organizationId)` em `SubmissionRepository` port out

## 3. [BE] Application — Use Case e DTOs

- [x] 3.1 Criar `EvaluateSubmissionCommand` (submissionId, professorId, organizationId, grade, feedback) em `application/dto/`
- [x] 3.2 Atualizar `SubmissionResponse` adicionando campos `grade` e `feedback`
- [x] 3.3 Implementar `EvaluateSubmissionService` em `application/usecase/`:
  - Carrega submissão por ID; lança `SubmissionNotFoundException` se não encontrada
  - Carrega tarefa associada; verifica `task.organizationId == orgId` e `task.createdBy == professorId` (403 se falhar)
  - Valida `status == SUBMITTED`; lança `SubmissionAlreadyEvaluatedException` se `EVALUATED`
  - Valida `grade`: se `task.maxScore == null` e `grade != null` → 422; se `grade > maxScore` → 422
  - Persiste grade, feedback e status `EVALUATED`; publica `SubmissionEvaluatedEvent`

## 4. [BE] Infrastructure — Persistence

- [x] 4.1 Adicionar campos `grade` e `feedback` em `TaskSubmissionJpaEntity.java`
- [x] 4.2 Atualizar `TaskMapper.java` (MapStruct) para mapear `grade` e `feedback` nos dois sentidos
- [x] 4.3 Implementar `findByTask(taskId, orgId)` em `SubmissionRepositoryImpl.java`

## 5. [BE] Interface — Endpoints REST

- [x] 5.1 Adicionar endpoint `GET /tasks/{taskId}/submissions` em `TaskResource.java` (`@RolesAllowed("PROFESSOR")`), delegando ao `SubmissionRepository.findByTask` após verificar que a tarefa pertence ao professor
- [x] 5.2 Adicionar endpoint `PATCH /submissions/{id}/evaluation` em `TaskResource.java` (`@RolesAllowed("PROFESSOR")`), chamando `EvaluateSubmissionUseCase`

## 6. [BE] Testes

- [x] 6.1 Testes unitários de `EvaluateSubmissionService` com Mockito: cenários de sucesso, submissão já avaliada, nota inválida, tarefa sem pontuação, 403
- [x] 6.2 Teste de integração `@QuarkusTest` para `PATCH /submissions/{id}/evaluation` e `GET /tasks/{taskId}/submissions` com Testcontainers

## 7. [FE] Types e API

- [x] 7.1 Atualizar `types.ts`: adicionar `grade: number | null` e `feedback: string | null` a `TaskSubmission`
- [x] 7.2 Adicionar em `api/submissions.ts`: `listSubmissions(taskId)` → `GET /tasks/{taskId}/submissions` e `evaluateSubmission(id, payload)` → `PATCH /submissions/{id}/evaluation`
- [x] 7.3 Adicionar query keys `submissions(taskId)` em `api/query-keys.ts`

## 8. [FE] Hooks, Schema e Componentes

- [x] 8.1 Criar hook `useSubmissions(taskId)` com TanStack Query em `hooks/useSubmissions.ts`
- [x] 8.2 Criar hook `useEvaluateSubmission()` (mutation) em `hooks/useEvaluateSubmission.ts`
- [x] 8.3 Criar schema Zod `evaluationSchema` em `schemas/evaluation.schema.ts`: `feedback` obrigatório (min 1), `grade` opcional numérico
- [x] 8.4 Criar componente `EvaluationDialog.tsx`: dialog com React Hook Form + Zod para submeter nota e feedback
- [x] 8.5 Criar componente `SubmissionListDrawer.tsx`: drawer com lista de submissões, status badge, e botão "Avaliar" que abre `EvaluationDialog`
- [x] 8.6 Atualizar `TaskListPage.tsx`: adicionar botão "Ver Submissões" por tarefa publicada, abrindo `SubmissionListDrawer`
