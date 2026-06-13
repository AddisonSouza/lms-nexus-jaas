## Context

RF-07 introduz o módulo `classroom` — um novo bounded context no monolito. Turmas pertencem a organizações e agrupam professores e alunos. Este design cobre backend (Quarkus) e frontend (React) para CRUD completo + gestão de membros.

Último Flyway: `V006__create_invitations_table.sql` → próximas: V007, V008.

## Goals

- CRUD de turmas com RBAC (ADMIN_ORG + GESTOR para escrita; todos para leitura filtrada)
- Geração de código de convite de 6 chars alfanuméricos na criação
- Soft delete em turmas e membros
- Frontend fullstack: listagem, formulário, painel de membros

## Non-Goals

- Ingresso via código (RF-08)
- Vínculo turma-disciplina (RF-09)
- Regeneração de código de convite

---

## Backend — Módulo `classroom`

### Estrutura de pacotes

```
apps/api/src/main/java/br/edu/lms/module/classroom/
  domain/
    model/        Classroom.java, ClassroomId.java, ClassroomStatus.java, InviteCode.java
                  ClassroomMember.java, ClassroomMemberId.java, ClassroomMemberRole.java
    exception/    ClassroomNotFoundException.java, ClassroomMemberNotFoundException.java
                  MemberNotInOrganizationException.java, ClassroomArchivedExceptionomain.java
    port/
      in/         CreateClassroomUseCase.java, UpdateClassroomUseCase.java
                  DeleteClassroomUseCase.java, AddClassroomMemberUseCase.java
                  RemoveClassroomMemberUseCase.java, GetClassroomUseCase.java
                  ListClassroomsUseCase.java, ListClassroomMembersUseCase.java
      out/        ClassroomRepository.java
  application/
    usecase/      CreateClassroomService.java, UpdateClassroomService.java
                  DeleteClassroomService.java, AddClassroomMemberService.java
                  RemoveClassroomMemberService.java, GetClassroomService.java
                  ListClassroomsService.java, ListClassroomMembersService.java
    dto/          CreateClassroomCommand.java, UpdateClassroomCommand.java
                  AddClassroomMemberCommand.java, ClassroomResponse.java
                  ClassroomMemberResponse.java
  infrastructure/
    persistence/  ClassroomJpaEntity.java, ClassroomMemberJpaEntity.java
                  ClassroomRepositoryImpl.java
    mapper/       ClassroomMapper.java  (MapStruct)
  interfaces/
    rest/         ClassroomResource.java
```

### Domain Model

```java
// Classroom — Aggregate Root
@Getter @Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Classroom {
    @EqualsAndHashCode.Include private final ClassroomId id;
    private String name;
    private String description;
    private String academicPeriod;
    private ClassroomStatus status;
    private InviteCode inviteCode;
    private String organizationId;  // from JWT
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}

// InviteCode — Value Object (6 alphanumeric chars)
@Value
public class InviteCode {
    String value;
    public static InviteCode generate() { /* random 6-char alphanum */ }
}
```

### Ports (interfaces)

**In:**
- `CreateClassroomUseCase.execute(CreateClassroomCommand)` → `ClassroomResponse`
- `UpdateClassroomUseCase.execute(ClassroomId, UpdateClassroomCommand)` → `ClassroomResponse`
- `DeleteClassroomUseCase.execute(ClassroomId, String organizationId)`
- `AddClassroomMemberUseCase.execute(AddClassroomMemberCommand)` → `ClassroomMemberResponse`
- `RemoveClassroomMemberUseCase.execute(ClassroomId, String memberId, String organizationId)`
- `GetClassroomUseCase.execute(ClassroomId, String requesterId, String organizationId)` → `ClassroomResponse`
- `ListClassroomsUseCase.execute(String organizationId, String requesterId, MemberRole requesterRole)` → `List<ClassroomResponse>`
- `ListClassroomMembersUseCase.execute(ClassroomId, String organizationId)` → `List<ClassroomMemberResponse>`

**Out:**
```java
public interface ClassroomRepository {
    Classroom save(Classroom classroom);
    Optional<Classroom> findById(ClassroomId id, String organizationId);
    List<Classroom> findAllByOrganization(String organizationId);
    List<Classroom> findAllByMember(String userId, String organizationId);
    void softDelete(ClassroomId id, String organizationId);
    Optional<ClassroomMember> findMember(ClassroomId classroomId, String userId);
    ClassroomMember saveMember(ClassroomMember member);
    void softDeleteMember(ClassroomId classroomId, String userId);
    List<ClassroomMember> findMembersByClassroom(ClassroomId classroomId, String organizationId);
    boolean isUserInOrganization(String userId, String organizationId);
}
```

### Flyway Migrations

**`V007__create_classrooms_table.sql`**
```sql
CREATE TABLE classrooms (
  id            VARCHAR(36)  NOT NULL PRIMARY KEY,
  organization_id VARCHAR(36) NOT NULL,
  name          VARCHAR(255) NOT NULL,
  description   TEXT,
  academic_period VARCHAR(100) NOT NULL,
  status        ENUM('ACTIVE','ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
  invite_code   VARCHAR(6)   NOT NULL UNIQUE,
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at    TIMESTAMP    NULL,
  CONSTRAINT fk_classrooms_org FOREIGN KEY (organization_id) REFERENCES organizations(id)
);
```

**`V008__create_classroom_members_table.sql`**
```sql
CREATE TABLE classroom_members (
  id            VARCHAR(36)  NOT NULL PRIMARY KEY,
  classroom_id  VARCHAR(36)  NOT NULL,
  user_id       VARCHAR(36)  NOT NULL,
  organization_id VARCHAR(36) NOT NULL,
  role          ENUM('PROFESSOR','ALUNO') NOT NULL,
  joined_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at    TIMESTAMP    NULL,
  CONSTRAINT fk_cm_classroom FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
  CONSTRAINT fk_cm_user      FOREIGN KEY (user_id)      REFERENCES users(id),
  CONSTRAINT fk_cm_org       FOREIGN KEY (organization_id) REFERENCES organizations(id),
  UNIQUE KEY uq_classroom_member (classroom_id, user_id)
);
```

### REST Endpoints

| Método | Path                              | Roles                        | Descrição                      |
|--------|-----------------------------------|------------------------------|--------------------------------|
| GET    | `/classrooms`                     | ALL_ORG_MEMBERS              | Listar turmas (filtrado)       |
| GET    | `/classrooms/{id}`                | ALL_ORG_MEMBERS              | Detalhar turma                 |
| POST   | `/classrooms`                     | ADMIN_ORG, GESTOR            | Criar turma                    |
| PUT    | `/classrooms/{id}`                | ADMIN_ORG, GESTOR            | Atualizar turma                |
| DELETE | `/classrooms/{id}`                | ADMIN_ORG, GESTOR            | Soft delete turma              |
| GET    | `/classrooms/{id}/members`        | ALL_ORG_MEMBERS              | Listar membros da turma        |
| POST   | `/classrooms/{id}/members`        | ADMIN_ORG, GESTOR            | Adicionar membro               |
| DELETE | `/classrooms/{id}/members/{uid}`  | ADMIN_ORG, GESTOR            | Remover membro                 |

---

## Frontend — Feature `classroom`

### Estrutura de pastas

```
apps/web/src/features/classroom/
  api/
    classroom-api.ts      # axios calls
    query-keys.ts         # TanStack Query keys
  components/
    ClassroomListPage.tsx      # /classrooms — tabela de turmas
    ClassroomDetailPage.tsx    # /classrooms/:id — detalhes + membros
    ClassroomFormDialog.tsx    # criar/editar turma (dialog)
    ClassroomMembersPanel.tsx  # painel de membros dentro do detail
    AddMemberDialog.tsx        # dialog para adicionar membro
  hooks/
    useClassrooms.ts           # listagem
    useClassroom.ts            # detalhe por id
    useCreateClassroom.ts      # mutation criar
    useUpdateClassroom.ts      # mutation editar
    useDeleteClassroom.ts      # mutation deletar
    useClassroomMembers.ts     # listar membros
    useAddClassroomMember.ts   # mutation adicionar membro
    useRemoveClassroomMember.ts
  schemas/
    classroomSchema.ts         # Zod — formulário criar/editar
    addMemberSchema.ts         # Zod — formulário adicionar membro
  types.ts                     # Classroom, ClassroomMember, ClassroomStatus, etc.
```

### Rotas novas (routes.tsx)

```tsx
/classrooms            → ClassroomListPage    (ADMIN_ORG, GESTOR, PROFESSOR, ALUNO)
/classrooms/:id        → ClassroomDetailPage  (mesmas roles, filtrado por membership)
```

Protegidas via `ProtectedRoute` verificando role do `authStore`.
