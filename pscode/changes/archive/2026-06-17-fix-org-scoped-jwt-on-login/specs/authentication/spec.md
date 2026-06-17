## MODIFIED Requirements

### REQ-AUTH-01 — Login com e-mail e senha
O sistema deve aceitar e-mail e senha válidos e retornar um Access Token JWT RS256 (TTL 15min) no body da resposta e um Refresh Token opaque (TTL 7 dias) em cookie HttpOnly. Se o usuário pertencer a exatamente uma organização (`organization_members` com `deleted_at IS NULL`), o Access Token deve incluir as claims `org` (organizationId) e `roles` (papel do usuário naquela organização), resolvidas automaticamente — sem exigir seleção manual. Se o usuário pertencer a zero ou a mais de uma organização, o Access Token é emitido sem essas claims (comportamento anterior).

#### Scenario: Login de usuário com exatamente uma organização
- **WHEN** `POST /auth/login` recebido com credenciais válidas para um usuário vinculado a exatamente uma organização (`organization_members` ativo)
- **THEN** resposta HTTP 200 com Access Token contendo `org` igual ao id dessa organização e `roles` igual ao papel do usuário nela

#### Scenario: Login de usuário sem organização ou com múltiplas
- **WHEN** `POST /auth/login` recebido com credenciais válidas para um usuário sem nenhum vínculo ativo, ou vinculado a mais de uma organização
- **THEN** resposta HTTP 200 com Access Token sem a claim `org` e `roles` vazio — comportamento idêntico ao anterior a esta change

### REQ-AUTH-REFRESH-01 — Refresh com contexto de organização
O endpoint `POST /auth/refresh` aceita o campo opcional `organizationId` no body. Quando presente, o backend valida a membership do usuário e emite um access token com claim `org` e claim `roles` com o papel do usuário naquela organização. Quando **ausente**, o backend agora tenta resolver automaticamente a organização do usuário: se ele pertencer a exatamente uma organização ativa, o token emitido inclui `org`/`roles` dessa organização; caso contrário, mantém o comportamento anterior (token sem `org`, `roles` vazio).

#### Scenario: Refresh com organizationId
- **WHEN** `POST /auth/refresh` body `{refreshToken, organizationId}`
- **THEN** access token inclui `{"org": "<organizationId>", "roles": ["<role>"]}`

#### Scenario: Refresh sem organizationId, usuário com exatamente uma organização
- **WHEN** `POST /auth/refresh` body `{refreshToken}` (campo ausente ou null) para um usuário vinculado a exatamente uma organização ativa
- **THEN** access token inclui `{"org": "<organizationId-da-unica-organizacao>", "roles": ["<role>"]}`, sem que o cliente precise informar `organizationId`

#### Scenario: Refresh sem organizationId, usuário sem organização ou com múltiplas
- **WHEN** `POST /auth/refresh` body `{refreshToken}` (campo ausente ou null) para um usuário sem vínculo ativo, ou vinculado a mais de uma organização
- **THEN** access token sem claim `org` e `roles=[]` — comportamento idêntico ao anterior a esta change
