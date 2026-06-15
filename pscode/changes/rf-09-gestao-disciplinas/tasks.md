## 1. [INFRA] Flyway Migrations

- [x] 1.1 [INFRA] Criar `V010__create_subjects_table.sql` — tabela `subjects` com campos: `id CHAR(36) PK`, `name VARCHAR(255) NOT NULL`, `code VARCHAR(20)`, `description TEXT`, `workload_hours INT`, `organization_id CHAR(36) NOT NULL FK organizations`, `created_at`, `updated_at`, `deleted_at TIMESTAMP NULL`
- [x] 1.2 [INFRA] Criar `V011__create_subject_classrooms_table.sql` — tabela `subject_classrooms` com PK composta `(subject_id, classroom_id)`, FKs para `subjects` e `classrooms`, `created_at`
- [x] 1.3 [INFRA] Criar `V012__create_subject_teachers_table.sql` — tabela `subject_teachers` com PK composta `(subject_id, member_id)`, FK `subject_id → subjects`, `member_id → organization_members.id`, `created_at`

## 2. [BE] Domain — Modelo e Ports

- [x] 2.1 [BE] Criar Value Objects: `SubjectId` (`@Value`, UUID), `SubjectCode` (`@Value`, max 20 chars)
- [x] 2.2 [BE] Criar Domain Entity `Subject` (`@Getter @Builder`, campos: `SubjectId id`, `String name`, `SubjectCode code`, `String description`, `Integer workloadHours`, `String organizationId`, `boolean deleted`)
- [x] 2.3 [BE] Criar exceptions: `SubjectNotFoundException`, `SubjectAlreadyLinkedException`, `InvalidTeacherAssignmentException`
- [x] 2.4 [BE] Criar Ports de entrada (`domain/port/in/`): `CreateSubjectUseCase`, `UpdateSubjectUseCase`, `DeleteSubjectUseCase`, `GetSubjectUseCase`, `ListSubjectsUseCase`, `LinkSubjectToClassroomUseCase`, `UnlinkSubjectFromClassroomUseCase`, `AssignTeacherToSubjectUseCase`, `RemoveTeacherFromSubjectUseCase`
- [x] 2.5 [BE] Criar Port de saída (`domain/port/out/`): `SubjectRepository` (métodos: `save`, `findById`, `findAllByOrganizationId`, `delete`)
- [x] 2.6 [BE] Criar Port de saída `ClassroomQueryPort` em `curriculum/domain/port/out/` — método `existsByIdAndOrganizationId(classroomId, orgId)` e `isArchived(classroomId)`

## 3. [BE] Application — Commands, Responses e Services

- [x] 3.1 [BE] Criar DTOs de comando (`application/dto/`): `CreateSubjectCommand`, `UpdateSubjectCommand`, `LinkClassroomCommand`, `AssignTeacherCommand`
- [x] 3.2 [BE] Criar DTO de resposta `SubjectResponse` com campos: `id`, `name`, `code`, `description`, `workloadHours`, `organizationId`, `classroomIds`, `teacherMemberIds`
- [x] 3.3 [BE] Implementar `CreateSubjectService` e `UpdateSubjectService` — extrair `organizationId` do JWT via `SecurityContext`
- [x] 3.4 [BE] Implementar `DeleteSubjectService` — soft delete (`deleted_at`)
- [x] 3.5 [BE] Implementar `GetSubjectService` e `ListSubjectsService` — filtrar por `organization_id`, excluir soft-deleted
- [x] 3.6 [BE] Implementar `LinkSubjectToClassroomService` — validar existência e org da turma via `ClassroomQueryPort`; idempotente
- [x] 3.7 [BE] Implementar `UnlinkSubjectFromClassroomService`
- [x] 3.8 [BE] Implementar `AssignTeacherToSubjectService` — validar `member_id` pertence à org com papel PROFESSOR; idempotente
- [x] 3.9 [BE] Implementar `RemoveTeacherFromSubjectService`

## 4. [BE] Infrastructure — Persistência e Adapters

- [x] 4.1 [BE] Criar `SubjectJpaEntity` (`@Data @Entity @Table(name="subjects")` com `@EqualsAndHashCode(onlyExplicitlyIncluded=true)`)
- [x] 4.2 [BE] Criar `SubjectClassroomJpaEntity` e `SubjectTeacherJpaEntity` (entidades de associação com PKs compostas via `@EmbeddedId`)
- [x] 4.3 [BE] Criar `SubjectRepositoryImpl` (`PanacheRepositoryBase`, Repository Pattern — Active Record proibido); implementar `findAllByOrganizationId` com filtro `deleted_at IS NULL`
- [x] 4.4 [BE] Implementar `ClassroomQueryPortImpl` no módulo `classroom` (ou adapter dedicado em `curriculum/infrastructure/`) usando `ClassroomRepository`
- [x] 4.5 [BE] Criar MapStruct mapper `SubjectMapper` (Subject ↔ SubjectJpaEntity ↔ SubjectResponse)

## 5. [BE] Interfaces REST

- [x] 5.1 [BE] Criar `SubjectResource` (`@Path("/subjects")`) com endpoints: `POST /subjects`, `GET /subjects`, `GET /subjects/{id}`, `PUT /subjects/{id}`, `DELETE /subjects/{id}`
- [x] 5.2 [BE] Criar `SubjectResource` sub-recursos: `POST /subjects/{id}/classrooms`, `DELETE /subjects/{id}/classrooms/{classroomId}`, `POST /subjects/{id}/teachers`, `DELETE /subjects/{id}/teachers/{memberId}`
- [x] 5.3 [BE] Criar request DTOs (`interfaces/rest/dto/`): `CreateSubjectRequest`, `UpdateSubjectRequest`, `LinkClassroomRequest`, `AssignTeacherRequest`
- [x] 5.4 [BE] Adicionar `@RolesAllowed` corretos; anotar endpoints com `@Operation` e `@APIResponse` (OpenAPI)

## 6. [BE] Testes

- [x] 6.1 [BE] Testes unitários para domain (`SubjectTest`): validação de campos obrigatórios, soft delete
- [x] 6.2 [BE] Testes unitários para use cases com Mockito: `CreateSubjectServiceTest`, `LinkSubjectToClassroomServiceTest`, `AssignTeacherToSubjectServiceTest`
- [x] 6.3 [BE] Teste de integração `@QuarkusTest` + Testcontainers: `SubjectResourceIT` cobrindo CRUD completo, vínculos e cenários de erro (404, 403, 422)

## 7. [FE] Feature curriculum — API e Hooks

- [x] 7.1 [FE] Criar `apps/web/src/features/curriculum/types.ts` — interfaces `Subject`, `CreateSubjectPayload`, `UpdateSubjectPayload`, `LinkClassroomPayload`, `AssignTeacherPayload`
- [x] 7.2 [FE] Criar `api/subject-api.ts` — funções: `createSubject`, `listSubjects`, `getSubject`, `updateSubject`, `deleteSubject`, `linkClassroom`, `unlinkClassroom`, `assignTeacher`, `removeTeacher`
- [x] 7.3 [FE] Criar `api/query-keys.ts` — query keys tipadas: `subjectKeys`
- [x] 7.4 [FE] Criar hooks TanStack Query: `useSubjects`, `useSubject`, `useCreateSubject`, `useUpdateSubject`, `useDeleteSubject`
- [x] 7.5 [FE] Criar hooks de vínculo: `useLinkClassroom`, `useUnlinkClassroom`, `useAssignTeacher`, `useRemoveTeacher`

## 8. [FE] Feature curriculum — Components

- [x] 8.1 [FE] Criar `schemas/subjectSchema.ts` (Zod) — schema de criação/edição com validação de `name` obrigatório, `workloadHours` positivo
- [x] 8.2 [FE] Criar `SubjectFormDialog.tsx` — dialog com React Hook Form + Zod para criar/editar disciplina
- [x] 8.3 [FE] Criar `SubjectListPage.tsx` — lista de disciplinas com ações (editar, excluir, ver detalhes)
- [x] 8.4 [FE] Criar `LinkClassroomDialog.tsx` — dialog para vincular turma à disciplina (select de turmas disponíveis)
- [x] 8.5 [FE] Criar `AssignTeacherDialog.tsx` — dialog para atribuir professor à disciplina (select de membros PROFESSOR)
- [x] 8.6 [FE] Registrar rotas em `app/routes.tsx`: `/curriculum` (lista) e `/curriculum/:id` (detalhe) — protegidas para ADMIN_ORG, GESTOR e PROFESSOR

## 9. [FE] Testes Frontend

- [x] 9.1 [FE] Testes unitários para `useCreateSubject` com `renderHook` + MSW mock
- [x] 9.2 [FE] Testes de componente para `SubjectFormDialog` — submissão válida, campos obrigatórios, estado de loading
