## Context

Módulo `organization` não existe. JWT atual só tem `sub` (userId), sem claim `org`. RF-06+ precisam do `organization_id` extraído do token (DB-MT-03). O refresh endpoint será o ponto de "context switch" para org.

## Goals / Non-Goals

**Goals:**
- Criar módulo `organization` completo (domain → infra → REST)
- `POST /organizations` — criação com vínculo automático ADMIN_ORG
- Estender `POST /auth/refresh` para aceitar `organizationId` e emitir JWT com claim `org`
- Flyway V004 + V005 para o schema

**Non-Goals:**
- Upload de logotipo (StoragePort futuro)
- CRUD completo de org (listagem, edição, deleção — RF-06+)
- Dashboard de org (RF-17)

## Decisions

### Estrutura de pacotes — módulo organization

```
module/organization/
  domain/
    model/          Organization, OrganizationId, OrganizationMember, MemberRole (enum)
    event/          OrganizationCreatedEvent
    exception/      OrganizationNameAlreadyExistsException, NotAnOrganizationMemberException
    port/
      in/           CreateOrganizationUseCase
      out/          OrganizationRepository, OrganizationMemberRepository
  application/
    usecase/        CreateOrganizationService
    dto/            CreateOrganizationCommand, OrganizationResponse
  infrastructure/
    persistence/    OrganizationJpaEntity, OrganizationMemberJpaEntity,
                    OrganizationRepositoryImpl, OrganizationMemberRepositoryImpl
  interfaces/
    rest/           OrganizationResource
    rest/dto/       CreateOrganizationRequest, OrganizationResponseDto
```

### Migrations Flyway

**V004__create_organizations_table.sql**
```sql
CREATE TABLE organizations (
  id            CHAR(36)     NOT NULL PRIMARY KEY,
  name          VARCHAR(255) NOT NULL,
  description   TEXT,
  owner_id      CHAR(36)     NOT NULL,
  created_at    DATETIME(6)  NOT NULL,
  updated_at    DATETIME(6),
  deleted_at    DATETIME(6),
  CONSTRAINT fk_org_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);
```

**V005__create_organization_members_table.sql**
```sql
CREATE TABLE organization_members (
  id              CHAR(36)     NOT NULL PRIMARY KEY,
  organization_id CHAR(36)     NOT NULL,
  user_id         CHAR(36)     NOT NULL,
  role            VARCHAR(50)  NOT NULL,
  joined_at       DATETIME(6)  NOT NULL,
  deleted_at      DATETIME(6),
  CONSTRAINT fk_member_org  FOREIGN KEY (organization_id) REFERENCES organizations(id),
  CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT uq_member      UNIQUE (organization_id, user_id)
);
```

### Endpoint REST

| Método | Path            | Auth    | Status | Descrição                      |
|--------|-----------------|---------|--------|--------------------------------|
| POST   | /organizations  | JWT     | 201    | Cria org, vincula ADMIN_ORG    |

### Validação de nome duplicado

Escopo: por `owner_id` + `name` (case-insensitive) entre orgs não deletadas. Não é nome globalmente único — dois usuários diferentes podem ter orgs com o mesmo nome.

### JwtTokenService — extensão

```java
// Assinatura nova (identity module)
String generateAccessToken(String userId, String orgId, String role);
String generateAccessToken(String userId); // sobrecarga sem org (compatibilidade)
```

Claim `org` adicionado apenas quando `orgId` não é null. Claim `roles` inclui o papel do usuário naquela org.

### RefreshCommand — extensão (identity module)

`RefreshCommand` recebe `organizationId` opcional. `RefreshService` injeta `OrganizationMemberRepository` via port (dependência `identity → organization` via interface — MOD-01 respeitado).

Port `OrganizationMemberRepository` expõe um método mínimo em `identity`:
```java
// Em identity/domain/port/out/ — interface, sem deps de organization
interface OrganizationMemberLookupPort {
    Optional<String> findRoleByUserAndOrg(String userId, String orgId);
}
```
`OrganizationMemberRepositoryImpl` (no módulo `organization`) implementa essa interface — CDI resolve o binding.

### Frontend

```
features/organization/
  api/          organization-api.ts  (POST /organizations)
  hooks/        useCreateOrganization.ts
  components/   CreateOrganizationForm.tsx, CreateOrganizationPage.tsx
  schemas/      createOrganizationSchema.ts
```

`authStore` (Zustand): adicionar campo `organizationId: string | null`.

Fluxo pós-criação:
1. `POST /organizations` → recebe `{id}`
2. `POST /auth/refresh` com `{refreshToken, organizationId: id}` → novo access token com `org`
3. Atualiza `authStore` com `organizationId` e novo `accessToken`
4. Redireciona para `/organizations/:id` (placeholder)

### Rota frontend

`/organizations/new` → `CreateOrganizationPage` (protegida por `ProtectedRoute`)
`/organizations/:id` → placeholder (sem dashboard ainda)

## Risks / Trade-offs

- **Dependência cruzada identity → organization**: resolvida via `OrganizationMemberLookupPort` em `identity/domain/port/out/`. A implementação fica em `organization/infrastructure/persistence/`. CDI injeta automaticamente. MOD-01 respeitado.
- **Nome único por owner vs global**: optamos por escopo de owner para permitir orgs homônimas de usuários distintos.
