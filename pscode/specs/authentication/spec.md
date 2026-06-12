### REQ-AUTH-01 — Login com e-mail e senha
O sistema deve aceitar e-mail e senha válidos e retornar um Access Token JWT RS256 (TTL 15min) no body da resposta e um Refresh Token opaque (TTL 7 dias) em cookie HttpOnly.

### REQ-AUTH-02 — Validação de credenciais
O sistema deve rejeitar login quando: (a) e-mail não cadastrado, (b) senha incorreta, (c) conta com status diferente de `ACTIVE`. Em todos os casos, retornar HTTP 401 sem discriminar o motivo. **O bloqueio de contas `PENDING_CONFIRMATION` já está implementado em `AuthenticateService` — nenhuma mudança de código, apenas documentação do comportamento confirmado pelo RF-04.**

#### Scenario: Conta pendente de confirmação
- **WHEN** `POST /auth/login` recebido para usuário com status `PENDING_CONFIRMATION`
- **THEN** resposta HTTP 401 `{"error": "INVALID_CREDENTIALS"}` — sem discriminar o motivo

#### Scenario: Conta ativa
- **WHEN** `POST /auth/login` recebido para usuário com status `ACTIVE` e credenciais corretas
- **THEN** resposta HTTP 200 com Access Token e Refresh Token cookie

### REQ-AUTH-03 — Logout
O sistema deve invalidar o Refresh Token no Redis ao receber `POST /auth/logout` com Bearer Token válido. Após logout, requisições de refresh com o token invalidado devem retornar HTTP 401.

### REQ-AUTH-04 — Refresh de tokens
O sistema deve emitir novo par de tokens (Access + Refresh) ao receber `POST /auth/refresh` com Refresh Token válido via cookie. O Refresh Token usado deve ser deletado (rotation obrigatória).

### REQ-AUTH-REFRESH-01 — Refresh com contexto de organização
O endpoint `POST /auth/refresh` passa a aceitar o campo opcional `organizationId` no body. Quando presente, o backend valida a membership do usuário e emite um access token com claim `org` e claim `roles` com o papel do usuário naquela organização.

#### Scenario: Refresh com organizationId
- **WHEN** `POST /auth/refresh` body `{refreshToken, organizationId}`
- **THEN** access token inclui `{"org": "<organizationId>", "roles": ["<role>"]}`

#### Scenario: Refresh sem organizationId
- **WHEN** `POST /auth/refresh` body `{refreshToken}` (campo ausente ou null)
- **THEN** access token sem claim `org` e `roles=[]` (comportamento atual)

### REQ-AUTH-05 — Claims do JWT
O Access Token deve conter os claims: `sub` (userId UUID), `org` (organizationId UUID, null se sem org), `roles` (array de strings).

### REQ-AUTH-06 — Frontend: tela de login
O sistema deve exibir formulário de login acessível em `/login`. Usuário autenticado redirecionado para `/` automaticamente.

### REQ-AUTH-07 — Frontend: persistência de sessão
O Access Token deve ser persistido no `authStore` (Zustand). Ao recarregar a página, o sistema deve tentar renovar a sessão via `/auth/refresh` antes de redirecionar para login.

### REQ-AUTH-08 — Invalidação de Refresh Tokens ao resetar senha
Extensão do comportamento de `RefreshTokenRepository`: ao concluir o reset de senha, todos os Refresh Tokens ativos do usuário são invalidados (SEC-03).

#### Scenario: Reset concluído com sessões ativas
- **WHEN** `ResetPasswordService.execute()` conclui com sucesso
- **THEN** `RefreshTokenRepository.deleteAllByUserId(userId)` invocado — todas as entradas `rt:{token}` do usuário removidas do Redis

#### Scenario: Usuário sem sessões ativas
- **WHEN** reset concluído e usuário não possui Refresh Tokens ativos
- **THEN** `deleteAllByUserId` executa sem erro (no-op)
