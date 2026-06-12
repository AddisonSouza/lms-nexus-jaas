## MODIFIED Requirements

### Requirement: REQ-AUTH-REFRESH-01 — Refresh com contexto de organização
O endpoint `POST /auth/refresh` passa a aceitar o campo opcional `organizationId` no body. Quando presente, o backend valida a membership do usuário e emite um access token com claim `org` e claim `roles` com o papel do usuário naquela organização.

#### Scenario: Refresh com organizationId
- **WHEN** `POST /auth/refresh` body `{refreshToken, organizationId}`
- **THEN** access token inclui `{"org": "<organizationId>", "roles": ["<role>"]}`

#### Scenario: Refresh sem organizationId
- **WHEN** `POST /auth/refresh` body `{refreshToken}` (campo ausente ou null)
- **THEN** access token sem claim `org` e `roles=[]` (comportamento atual)
