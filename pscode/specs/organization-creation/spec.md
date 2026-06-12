### REQ-ORG-01 — Criar organização
Usuário autenticado com e-mail confirmado (`status=ACTIVE`) pode criar uma organização informando `name` (obrigatório, único por usuário) e `description` (opcional). O sistema persiste em `organizations` e cria vínculo em `organization_members` com `role=ADMIN_ORG`. O `owner_id` da organização é o userId do criador e é usado para impedir sua remoção como membro (REQ-INV-03).

#### Scenario: Criação bem-sucedida
- **WHEN** `POST /organizations` com JWT válido (`sub=userId`), body `{name, description?}`
- **THEN** 201 com `{id, name, description, createdAt}`; `organizations` contém novo registro com `owner_id=userId`; `organization_members` contém `(userId, orgId, ADMIN_ORG)`; `OrganizationCreatedEvent` publicado via CDI

#### Scenario: Nome duplicado para o mesmo usuário
- **WHEN** usuário cria org com nome idêntico a uma org ativa que ele já possui
- **THEN** 409 `{"error": "ORGANIZATION_NAME_ALREADY_EXISTS"}`

#### Scenario: Usuário com conta não confirmada
- **WHEN** JWT válido mas usuário com `status=PENDING_CONFIRMATION`
- **THEN** 403 `{"error": "EMAIL_NOT_CONFIRMED"}`

#### Scenario: Um usuário cria múltiplas organizações
- **WHEN** usuário cria uma segunda org com nome diferente
- **THEN** 201; ambas as orgs existem; `organization_members` tem duas entradas com `role=ADMIN_ORG`

---

### REQ-ORG-02 — Obter JWT contextualizado com org
Após criar (ou selecionar) uma organização, o frontend pode solicitar um access token com o `org` claim preenchido chamando `POST /auth/refresh` com `organizationId`.

#### Scenario: Refresh com organizationId válido
- **WHEN** `POST /auth/refresh` com `{refreshToken, organizationId}` e usuário é membro da org
- **THEN** novo access token com claim `org=organizationId` e claims `roles=[ADMIN_ORG]` para aquela org

#### Scenario: Refresh sem organizationId (compatibilidade)
- **WHEN** `POST /auth/refresh` com `{refreshToken}` sem `organizationId`
- **THEN** novo access token sem claim `org` (comportamento atual preservado)

#### Scenario: Refresh com organizationId de org da qual não é membro
- **WHEN** `POST /auth/refresh` com `organizationId` de org que não pertence ao usuário
- **THEN** 403 `{"error": "NOT_AN_ORGANIZATION_MEMBER"}`
