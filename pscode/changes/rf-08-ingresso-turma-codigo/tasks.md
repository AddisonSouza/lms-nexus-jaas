## 1. [INFRA] Migration Flyway

- [x] 1.1 [INFRA] Criar `V009__add_unique_index_classrooms_invite_code.sql` com `ALTER TABLE classrooms ADD UNIQUE INDEX uq_classrooms_invite_code (invite_code)`

## 2. [BE] Domain — Exceção e Ports

- [x] 2.1 [BE] Criar `InvalidInviteCodeException` em `domain/exception/`
- [x] 2.2 [BE] Adicionar `Optional<Classroom> findByInviteCode(String code)` ao `ClassroomRepository` (Port Out)
- [x] 2.3 [BE] Criar `JoinClassroomUseCase` em `domain/port/in/`
- [x] 2.4 [BE] Criar `RegenerateInviteCodeUseCase` em `domain/port/in/`

## 3. [BE] Application — DTOs e Use Cases

- [x] 3.1 [BE] Criar `JoinClassroomCommand` (campos: `inviteCode`, `userId`) em `application/dto/`
- [x] 3.2 [BE] Implementar `JoinClassroomService` em `application/usecase/` — busca por código, valida ACTIVE, idempotente, cria membro com `role=ALUNO`
- [x] 3.3 [BE] Implementar `RegenerateInviteCodeService` em `application/usecase/` — valida ACTIVE, gera novo `InviteCode`, persiste via `save()`

## 4. [BE] Infrastructure — Repositório

- [x] 4.1 [BE] Implementar `findByInviteCode` em `ClassroomRepositoryImpl` com query JPA por `invite_code` e `deleted_at IS NULL`

## 5. [BE] Interface REST — Endpoints

- [x] 5.1 [BE] Criar `JoinClassroomRequest` (DTO REST com `inviteCode: @NotBlank`) em `interfaces/rest/dto/`
- [x] 5.2 [BE] Adicionar `POST /classrooms/join` em `ClassroomResource` (role: ALUNO) — retorna 201 (novo membro) ou 200 (já membro)
- [x] 5.3 [BE] Adicionar `POST /classrooms/{id}/invite-code/regenerate` em `ClassroomResource` (roles: PROFESSOR, GESTOR, ADMIN_ORG)

## 6. [BE] Testes Unitários

- [x] 6.1 [BE] Criar `JoinClassroomServiceTest` — cenários: sucesso, idempotente, código inválido, turma arquivada
- [x] 6.2 [BE] Criar `RegenerateInviteCodeServiceTest` — cenários: sucesso, turma arquivada, turma não encontrada

## 7. [BE] Testes de Integração

- [x] 7.1 [BE] Adicionar cenários de join e regeneração em `ClassroomResourceIT` (Testcontainers MySQL)

## 8. [FE] Frontend — Formulário de Ingresso

- [x] 8.1 [FE] Criar `joinClassroomSchema.ts` com validação Zod (código 6 chars alfanumérico)
- [x] 8.2 [FE] Criar hook `useJoinClassroom` usando TanStack Query mutation
- [x] 8.3 [FE] Criar componente `JoinClassroomForm.tsx` (input de código + botão, feedback de erro/sucesso)
- [x] 8.4 [FE] Integrar `JoinClassroomForm` na página de turmas ou criar rota `/join` dedicada

## 9. [FE] Frontend — Regeneração de Código

- [x] 9.1 [FE] Criar hook `useRegenerateInviteCode` usando TanStack Query mutation
- [x] 9.2 [FE] Exibir botão de regeneração no detalhe da turma (visível apenas para PROFESSOR/GESTOR/ADMIN_ORG)
- [x] 9.3 [FE] Exibir o invite code atual com botão de cópia (Lucide `Copy`) no detalhe da turma
