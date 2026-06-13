## 1. [INFRA] Flyway Migrations

- [x] 1.1 [INFRA] Criar `V007__create_classrooms_table.sql` com colunas id, organization_id, name, description, academic_period, status (ENUM), invite_code (UNIQUE), timestamps e deleted_at
- [x] 1.2 [INFRA] Criar `V008__create_classroom_members_table.sql` com colunas id, classroom_id, user_id, organization_id, role (ENUM PROFESSOR/ALUNO), joined_at, deleted_at e unique constraint (classroom_id, user_id)

## 2. [BE] Domain — Modelo e Ports

- [x] 2.1 [BE] Criar value objects `ClassroomId`, `InviteCode` (geração aleatória 6-char alfanum) e enum `ClassroomStatus` (ACTIVE, ARCHIVED)
- [x] 2.2 [BE] Criar aggregate `Classroom` com campos name, description, academicPeriod, status, inviteCode, organizationId, createdAt, deletedAt (sem anotações JPA/Quarkus)
- [x] 2.3 [BE] Criar enum `ClassroomMemberRole` (PROFESSOR, ALUNO) e entity `ClassroomMember` com campos classroomId, userId, organizationId, role, joinedAt, deletedAt
- [x] 2.4 [BE] Criar exceptions: `ClassroomNotFoundException`, `ClassroomMemberNotFoundException`, `MemberNotInOrganizationException`, `ClassroomArchivedException`
- [x] 2.5 [BE] Criar ports de entrada (interfaces): `CreateClassroomUseCase`, `UpdateClassroomUseCase`, `DeleteClassroomUseCase`, `GetClassroomUseCase`, `ListClassroomsUseCase`
- [x] 2.6 [BE] Criar ports de entrada (interfaces): `AddClassroomMemberUseCase`, `RemoveClassroomMemberUseCase`, `ListClassroomMembersUseCase`
- [x] 2.7 [BE] Criar port de saída `ClassroomRepository` com métodos: save, findById, findAllByOrganization, findAllByMember, softDelete, findMember, saveMember, softDeleteMember, findMembersByClassroom, isUserInOrganization

## 3. [BE] Application — DTOs e Use Cases

- [x] 3.1 [BE] Criar DTOs: `CreateClassroomCommand`, `UpdateClassroomCommand`, `AddClassroomMemberCommand`, `ClassroomResponse`, `ClassroomMemberResponse`
- [x] 3.2 [BE] Implementar `CreateClassroomService`: valida campos, gera `InviteCode`, extrai organizationId do JWT, salva via repository
- [x] 3.3 [BE] Implementar `UpdateClassroomService`: busca turma, valida ownership por organizationId, aplica mudanças, retorna response
- [x] 3.4 [BE] Implementar `DeleteClassroomService`: soft delete na turma e cascade em classroom_members
- [x] 3.5 [BE] Implementar `GetClassroomService` e `ListClassroomsService`: filtra por role (ADMIN_ORG/GESTOR veem tudo; PROFESSOR/ALUNO veem só as suas)
- [x] 3.6 [BE] Implementar `AddClassroomMemberService`: valida que o usuário pertence à org, verifica se turma está ACTIVE, idempotente (not found → cria; found → retorna existente)
- [x] 3.7 [BE] Implementar `RemoveClassroomMemberService` e `ListClassroomMembersService`

## 4. [BE] Infrastructure — Persistência e Mapper

- [x] 4.1 [BE] Criar `ClassroomJpaEntity` com `@Data @Entity @Table(name="classrooms")` e todos os campos mapeados com `@Column(name=...)`
- [x] 4.2 [BE] Criar `ClassroomMemberJpaEntity` com `@Data @Entity @Table(name="classroom_members")`
- [x] 4.3 [BE] Implementar `ClassroomRepositoryImpl` com todos os métodos de `ClassroomRepository` usando Panache (Repository Pattern — sem Active Record)
- [x] 4.4 [BE] Criar `ClassroomMapper` (MapStruct) com métodos: toDomain(JpaEntity), toJpaEntity(Domain), toResponse(Domain), toMemberResponse(Member)

## 5. [BE] REST — Resource e RBAC

- [x] 5.1 [BE] Criar `ClassroomResource` com endpoints GET /classrooms, GET /classrooms/{id}, POST /classrooms, PUT /classrooms/{id}, DELETE /classrooms/{id} com `@RolesAllowed` corretos e documentação OpenAPI (`@Operation`, `@APIResponse`)
- [x] 5.2 [BE] Adicionar endpoints GET /classrooms/{id}/members, POST /classrooms/{id}/members, DELETE /classrooms/{id}/members/{userId} no `ClassroomResource`
- [x] 5.3 [BE] Registrar exceptions de classroom no `GlobalExceptionMapper` (404 → ClassroomNotFoundException, 422 → MemberNotInOrganizationException / ClassroomArchivedException)

## 6. [BE] Testes

- [x] 6.1 [BE] Testes unitários para `CreateClassroomService`, `ListClassroomsService` (mock do repository via Mockito)
- [x] 6.2 [BE] Testes unitários para `AddClassroomMemberService` (cenários: sucesso, idempotente, membro fora da org, turma arquivada)
- [x] 6.3 [BE] Teste de integração com `@QuarkusTest` + Testcontainers: criação de turma via POST /classrooms e verificação no banco
- [x] 6.4 [BE] Teste de RBAC: PROFESSOR tentando criar turma deve retornar 403

## 7. [FE] Feature `classroom` — Setup e API

- [x] 7.1 [FE] Criar `features/classroom/types.ts` com tipos `Classroom`, `ClassroomMember`, `ClassroomStatus`, `ClassroomMemberRole`, `CreateClassroomPayload`, `UpdateClassroomPayload`
- [x] 7.2 [FE] Criar `features/classroom/api/query-keys.ts` com chaves tipadas para classrooms list, detail e members
- [x] 7.3 [FE] Criar `features/classroom/api/classroom-api.ts` com funções axios: getClassrooms, getClassroom, createClassroom, updateClassroom, deleteClassroom, getClassroomMembers, addClassroomMember, removeClassroomMember
- [x] 7.4 [FE] Criar schemas Zod: `classroomSchema.ts` (name required, description optional, academicPeriod required, status) e `addMemberSchema.ts` (userId, role)

## 8. [FE] Hooks

- [x] 8.1 [FE] Criar hooks de leitura: `useClassrooms`, `useClassroom(id)`, `useClassroomMembers(id)` com TanStack Query
- [x] 8.2 [FE] Criar hooks de mutação: `useCreateClassroom`, `useUpdateClassroom`, `useDeleteClassroom` com invalidação de query keys
- [x] 8.3 [FE] Criar hooks de membros: `useAddClassroomMember(classroomId)`, `useRemoveClassroomMember(classroomId)` com invalidação de query keys

## 9. [FE] Componentes e Páginas

- [x] 9.1 [FE] Criar `ClassroomListPage.tsx`: tabela com colunas nome, período, status, membros; botão "Nova Turma" visível apenas para ADMIN_ORG/GESTOR
- [x] 9.2 [FE] Criar `ClassroomFormDialog.tsx`: dialog com React Hook Form + Zod para criar/editar; campos nome, descrição, período letivo, status
- [x] 9.3 [FE] Criar `ClassroomDetailPage.tsx`: header com nome e status; painel de informações; área de membros
- [x] 9.4 [FE] Criar `ClassroomMembersPanel.tsx`: tabela de membros (nome, role, data de entrada); botão "Adicionar membro" e ação "Remover" com confirmação
- [x] 9.5 [FE] Criar `AddMemberDialog.tsx`: select de membros da org + select de role (PROFESSOR/ALUNO)

## 10. [FE] Rotas e Integração

- [x] 10.1 [FE] Adicionar rotas `/classrooms` e `/classrooms/:id` em `routes.tsx` com `ProtectedRoute` (all org roles)
- [x] 10.2 [FE] Adicionar item "Turmas" no sidebar com ícone Lucide `BookOpen`, visível para todos os membros autenticados
- [x] 10.3 [FE] Testes: `useCreateClassroom` hook + `ClassroomFormDialog` (submit, validação de erro)
