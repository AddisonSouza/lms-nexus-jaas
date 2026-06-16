## Context

O módulo `classroom` foi estabelecido no RF-07. Já existem: `Classroom` (com `InviteCode`), `ClassroomMember`, `ClassroomRepository` (sem `findByInviteCode`), `ClassroomMemberRole` (PROFESSOR, ALUNO). As tabelas `classrooms` e `classroom_members` já existem via Flyway V007/V008.

O RF-08 precisa apenas de novos use cases e da extensão do repositório — sem migration de schema.

## Goals / Non-Goals

**Goals:**
- ALUNO ingressa em turma com código de 6 chars via `POST /classrooms/join` (idempotente).
- PROFESSOR/GESTOR regenera o código via `POST /classrooms/{id}/invite-code/regenerate`.
- Turma arquivada bloqueia novo ingresso com erro claro.
- Frontend: formulário de ingresso por código e atualização do código exibido.

**Non-Goals:**
- Convite por e-mail (RF-06).
- Link compartilhável (o FE usa o código existente para montar a URL).
- Ingresso sem autenticação prévia.
- Migração de schema (já existe).

## Decisions

### 1. `findByInviteCode` no `ClassroomRepository` (Port Out)
Adicionar `Optional<Classroom> findByInviteCode(String code)` ao Port. A implementação faz query JPA por `invite_code` na tabela `classrooms` filtrando `deleted_at IS NULL`. Alternativa de query por `organization_id` foi descartada: o código é global e único por construção (`SecureRandom` + 36 chars → colisão improvável; unicidade garantida pela geração em `CreateClassroomService`).

### 2. Idempotência no join
Se `findMember(classroomId, userId)` retornar presente → retornar 200 com o `ClassroomResponse` da turma sem lançar exceção. Alternativa de retornar 409 foi descartada por dificultar o UX (botão "Entrar" clicado duas vezes).

### 3. `organization_id` no join
O endpoint `POST /classrooms/join` recebe apenas `inviteCode` no body. O `organization_id` NÃO é extraído do JWT neste caso (diferente do padrão do projeto), pois o aluno pode não pertencer ainda à organização da turma — o próprio join o vincula implicitamente via `classroom_members.organization_id`. O `organization_id` vem da `Classroom` encontrada pelo código.

### 4. Regeneração invalida o código anterior
`RegenerateInviteCodeService` faz `InviteCode.generate()`, atualiza a `Classroom` via `classroomRepository.save()`. O código antigo deixa de funcionar imediatamente — sem lista de códigos históricos.

### 5. Pacotes afetados
```
classroom/
  domain/
    exception/   + InvalidInviteCodeException
    port/in/     + JoinClassroomUseCase, RegenerateInviteCodeUseCase
    port/out/    ~ ClassroomRepository (+findByInviteCode)
  application/
    dto/         + JoinClassroomCommand
    usecase/     + JoinClassroomService, RegenerateInviteCodeService
  interfaces/rest/
    dto/         + JoinClassroomRequest
    ClassroomResource ~ (+2 endpoints)

web/
  features/classroom/
    components/  + JoinClassroomForm.tsx
    hooks/       + useJoinClassroom.ts
    schemas/     + joinClassroomSchema.ts (Zod)
```

### 6. Endpoints REST
| Método | Path | Roles | Resposta |
|--------|------|-------|---------|
| `POST` | `/classrooms/join` | ALUNO | 200 `ClassroomResponse` (já membro) / 201 `ClassroomResponse` (novo) |
| `POST` | `/classrooms/{id}/invite-code/regenerate` | PROFESSOR, GESTOR, ADMIN_ORG | 200 `ClassroomResponse` |

## Risks / Trade-offs

- **Colisão de código**: improvável (36^6 ≈ 2 bilhões), mas sem índice UNIQUE na coluna `invite_code`. Risco → Mitigação: adicionar `UNIQUE INDEX` via migration (V009) para garantia em nível de banco.
- **ALUNO de outra org ingressa**: o `classroom_members` registra o `organization_id` da turma — o ALUNO fica vinculado à org pela turma, sem ser membro da `organization_members`. Comportamento esperado pelo RF-08.

## Migration Plan

- V009__add_unique_index_classrooms_invite_code.sql: `ALTER TABLE classrooms ADD UNIQUE INDEX uq_classrooms_invite_code (invite_code);`
- Deploy: aditivo, sem downtime.
- Rollback: `DROP INDEX uq_classrooms_invite_code ON classrooms;`
