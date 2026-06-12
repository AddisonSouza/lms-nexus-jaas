## MODIFIED Requirements

### REQ-AUTH-02 — Validação de credenciais
O sistema deve rejeitar login quando: (a) e-mail não cadastrado, (b) senha incorreta, (c) conta com status diferente de `ACTIVE`. Em todos os casos, retornar HTTP 401 sem discriminar o motivo. **O bloqueio de contas `PENDING_CONFIRMATION` já está implementado em `AuthenticateService` — nenhuma mudança de código, apenas documentação do comportamento confirmado pelo RF-04.**

#### Scenario: Conta pendente de confirmação
- **WHEN** `POST /auth/login` recebido para usuário com status `PENDING_CONFIRMATION`
- **THEN** resposta HTTP 401 `{"error": "INVALID_CREDENTIALS"}` — sem discriminar o motivo

#### Scenario: Conta ativa
- **WHEN** `POST /auth/login` recebido para usuário com status `ACTIVE` e credenciais corretas
- **THEN** resposta HTTP 200 com Access Token e Refresh Token cookie
