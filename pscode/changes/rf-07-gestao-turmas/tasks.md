## 1. [INFRA] Flyway Migrations

- [ ] 1.1 [INFRA] Criar `V007__create_classrooms_table.sql` com colunas id, organization_id, name, description, academic_period, status (ENUM), invite_code (UNIQUE), timestamps e deleted_at
- [ ] 1.2 [INFRA] Criar `V008__create_classroom_members_table.sql` com colunas id, classroom_id, user_id, organization_id, role (ENUM PROFESSOR/ALUNO), joined_at, deleted_at e unique constraint (classroom_id, user_id)

## 2. [BE] Domain — Modelo e Ports

- [ ] 2.1 [BE] Criar value objects `ClassroomId`, `InviteCode` (geração aleatória 6-char alfanum) e enum `ClassroomStatus` (ACTIVE, ARCHIVED)
- [ ] 2.2 [BE] Criar aggregate `Classroom` com campos name, description, academicPeriod, status, inviteCode, organizationId, createdAt, deletedAt (sem anotações JPA/Quarkus)
- [ ] 2.3 [BE] Criar enum `ClassroomMemberRole` (PROFESSOR, ALUNO) e entity `ClassroomMember` com campos classroomId, userId, organizationId, role, joinedAt, deletedAt
- [ ] 2.4 [BE] Criar exceptions: `ClassroomNotFoundException`, `ClassroomMemberNotFoundException`, `MemberNotInOrganizationException`, `ClassroomArchivedException`
- [ ] 2.5 [BE] Criar ports de entrada (interfaces): `CreateClassroomUseCase`, `UpdateClassroomUseCase`, `DeleteClassroomUseCase`, `GetClassroomUseCase`, `ListClassroomsUseCase`
- [ ] 2.6 [BE] Criar ports de entrada (interfaces): `AddClassroomMemberUseCase`, `RemoveClassroomMemberUseCase`, `ListClassroomMembersUseCase`
- [ ] 2.7 [BE] Criar port de saída `ClassroomRepository` com métodos: save, findById, findAllByOrganization, findAllByMember, softDelete, findMember, saveMember, softDeleteMember, findMembersByClassroom, isUserInOrganization

## 3. [BE] Application — DTOs e Use Cases

- [ ] 3.1 [BE] Criar DTOs: `CreateClassroomCommand`, `UpdateClassroomCommand`, `AddClassroomMemberCommand`, `ClassroomResponse`, `ClassroomMemberResponse`
- [ ] 3.2 [BE] Implementar `CreateClassroomService`: valida campos, gera `InviteCode`, extrai organizationId do JWT, salva via repository
- [ ] 3.3 [BE] Implementar `UpdateClassroomService`: busca turma, valida ownership por organizationId, aplica mudanças, retorna response
- [ ] 3.4 [BE] Implementar `DeleteClassroomService`: soft delete na turma e cascade em classroom_members
- [ ] 3.5 [BE] Implementar `GetClassroomService` e `ListClassroomsService`: filtra por role (ADMIN_ORG/GESTOR veem tudo; PROFESSOR/ALUNO veem só as suas)
- [ ] 3.6 [BE] Implementar `AddClassroomMemberService`: valida que o usuário pertence à org, verifica se turma está ACTIVE, idempotente (not found → cria; found → retorna existente)
- [ ] 3.7 [BE] Implementar `RemoveClassroomMemberService` e `ListClassroomMembersService`

## 4. [BE] Infrastructure — Persistência e Mapper

- [ ] 4.1 [BE] Criar `ClassroomJpaEntity` com `@Data @Entity @Table(name="classrooms")` e todos os campos mapeados com `@Column(name=...)`
- [ ] 4.2 [BE] Criar `ClassroomMemberJpaEntity` com `@Data @Entity @Table(name="classroom_members")`
- [ ] 4.3 [BE] Implementar `ClassroomRepositoryImpl` com todos os métodos de `ClassroomRepository` usando Panache (Repository Pattern — sem Active Record)
- [ ] 4.4 [BE] Criar `ClassroomMapper` (MapStruct) com métodos: toDomain(JpaEntity), toJpaEntity(Domain), toResponse(Domain), toMemberResponse(Member)

## 5. [BE] REST — Resource e RBAC

- [ ] 5.1 [BE] Criar `ClassroomResource` com endpoints GET /classrooms, GET /classrooms/{id}, POST /classrooms, PUT /classrooms/{id}, DELETE /classrooms/{id} com `@RolesAllowed` corretos e documentação OpenAPI (`@Operation`, `@APIResponse`)
- [ ] 5.2 [BE] Adicionar endpoints GET /classrooms/{id}/members, POST /classrooms/{id}/members, DELETE /classrooms/{id}/members/{userId} no `ClassroomResource`
- [ ] 5.3 [BE] Registrar exceptions de classroom no `GlobalExceptionMapper` (404 → ClassroomNotFoundException, 422 → MemberNotInOrganizationException / ClassroomArchivedException)

## 6. [BE] Testes

- [ ] 6.1 [BE] Testes unitários para `CreateClassroomService`, `ListClassroomsService` (mock do repository via Mockito)
- [ ] 6.2 [BE] Testes unitários para `AddClassroomMemberService` (cenários: sucesso, idempotente, membro fora da org, turma arquivada)
- [ ] 6.3 [BE] Teste de integração com `@QuarkusTest` + Testcontainers: criação de turma via POST /classrooms e verificação no banco
- [ ] 6.4 [BE] Teste de RBAC: PROFESSOR tentando criar turma deve retornar 403

## 7. [FE] Feature `classroom` — Setup e API

- [ ] 7.1 [FE] Criar `features/classroom/types.ts` com tipos `Classroom`, `ClassroomMember`, `ClassroomStatus`, `ClassroomMemberRole`, `CreateClassroomPayload`, `UpdateClassroomPayload`
- [ ] 7.2 [FE] Criar `features/classroom/api/query-keys.ts` com chaves tipadas para classrooms list, detail e members
- [ ] 7.3 [FE] Criar `features/classroom/api/classroom-api.ts` com funções axios: getClassrooms, getClassroom, createClassroom, updateClassroom, deleteClassroom, getClassroomMembers, addClassroomMember, removeClassroomMember
- [ ] 7.4 [FE] Criar schemas Zod: `classroomSchema.ts` (name required, description optional, academicPeriod required, status) e `addMemberSchema.ts` (userId, role)

## 8. [FE] Hooks

- [ ] 8.1 [FE] Criar hooks de leitura: `useClassrooms`, `useClassroom(id)`, `useClassroomMembers(id)` com TanStack Query
- [ ] 8.2 [FE] Criar hooks de mutação: `useCreateClassroom`, `useUpdateClassroom`, `useDeleteClassroom` com invalidação de query keys
- [ ] 8.3 [FE] Criar hooks de membros: `useAddClassroomMember(classroomId)`, `useRemoveClassroomMember(classroomId)` com invalidação de query keys

## 9. [FE] Componentes e Páginas

- [ ] 9.1 [FE] Criar `ClassroomListPage.tsx`: tabela com colunas nome, período, status, membros; botão "Nova Turma" visível apenas para ADMIN_ORG/GESTOR
- [ ] 9.2 [FE] Criar `ClassroomFormDialog.tsx`: dialog com React Hook Form + Zod para criar/editar; campos nome, descrição, período letivo, status
- [ ] 9.3 [FE] Criar `ClassroomDetailPage.tsx`: header com nome e status; painel de informações; área de membros
- [ ] 9.4 [FE] Criar `ClassroomMembersPanel.tsx`: tabela de membros (nome, role, data de entrada); botão "Adicionar membro" e ação "Remover" com confirmação
- [ ] 9.5 [FE] Criar `AddMemberDialog.tsx`: select de membros da org + select de role (PROFESSOR/ALUNO)

## 10. [FE] Rotas e Integração

- [ ] 10.1 [FE] Adicionar rotas `/classrooms` e `/classrooms/:id` em `routes.tsx` com `ProtectedRoute` (all org roles)
- [ ] 10.2 [FE] Adicionar item "Turmas" no sidebar com ícone Lucide `BookOpen`, visível para todos os membros autenticados
- [ ] 10.3 [FE] Testes: `useCreateClassroom` hook + `ClassroomFormDialog` (submit, validação de erro)
