### REQ-PWRESET-01 — Solicitar reset de senha

O sistema deve permitir que um usuário solicite a redefinição de senha informando apenas o e-mail. A resposta NUNCA revela se o e-mail existe.

#### Scenario: E-mail de conta ativa existente
- **WHEN** `POST /auth/forgot-password` com e-mail de usuário ACTIVE
- **THEN** token UUID gerado, armazenado no Redis (`prt:{token}`) com TTL 1h, e-mail com link enviado, retorna `204`

#### Scenario: E-mail inexistente ou conta não ativa
- **WHEN** `POST /auth/forgot-password` com e-mail que não existe ou usuário PENDING_CONFIRMATION
- **THEN** retorna `204` sem enviar e-mail (proteção contra enumeração de contas)

#### Scenario: Múltiplas solicitações
- **WHEN** usuário solicita reset mais de uma vez enquanto token anterior ainda é válido
- **THEN** novo token gerado e anterior sobrescrito no Redis; apenas o último token é válido

---

### REQ-PWRESET-02 — Confirmar reset de senha

#### Scenario: Token válido, senhas conferem
- **WHEN** `POST /auth/reset-password` com token válido (não expirado, não usado) e nova senha ≥ 8 chars
- **THEN** senha atualizada com BCrypt (fator 12), token invalidado, todos os Refresh Tokens do usuário deletados, retorna `204`

#### Scenario: Token expirado
- **WHEN** `POST /auth/reset-password` com token cujo TTL expirou no Redis
- **THEN** retorna `400 Bad Request`

#### Scenario: Token já utilizado
- **WHEN** `POST /auth/reset-password` com token que já foi consumido anteriormente
- **THEN** retorna `400 Bad Request`

#### Scenario: Token inexistente
- **WHEN** `POST /auth/reset-password` com token que nunca existiu
- **THEN** retorna `400 Bad Request`
