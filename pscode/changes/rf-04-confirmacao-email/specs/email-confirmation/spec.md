## ADDED Requirements

### REQ-EMAILCONF-01 — Persistência do token de confirmação no cadastro
No momento do cadastro (`RegisterUserService`), o token UUID gerado SHALL ser salvo no Redis com chave `ect:{token}` mapeando para o `userId`, com TTL de 24 horas, antes de enviar o e-mail de confirmação.

#### Scenario: Cadastro bem-sucedido persiste o token
- **WHEN** `RegisterUserService.execute()` completa com sucesso
- **THEN** `EmailConfirmationTokenRepository.save(token, userId, Duration.ofHours(24))` é chamado antes de `emailPort.sendConfirmationEmail()`

#### Scenario: Token expirado após 24h
- **WHEN** 24 horas se passam após o cadastro
- **THEN** a chave Redis `ect:{token}` não existe mais (TTL expirado automaticamente)

---

### REQ-EMAILCONF-02 — Confirmação de e-mail via token
O sistema SHALL expor `GET /auth/confirm-email?token={token}`. Ao receber um token válido, o sistema deve: (1) buscar o `userId` no Redis, (2) atualizar o status do usuário para `ACTIVE`, (3) invalidar o token (del Redis), (4) retornar HTTP 204.

#### Scenario: Token válido e conta pendente
- **WHEN** `GET /auth/confirm-email?token={token}` recebe token existente no Redis e usuário com status `PENDING_CONFIRMATION`
- **THEN** usuário.status atualizado para `ACTIVE`, token removido do Redis, resposta HTTP 204

#### Scenario: Token inexistente ou expirado
- **WHEN** `GET /auth/confirm-email?token={token}` recebe token não encontrado no Redis
- **THEN** resposta HTTP 400 com código de erro `INVALID_CONFIRMATION_TOKEN`

#### Scenario: Conta já confirmada (idempotência)
- **WHEN** `GET /auth/confirm-email?token={token}` recebe token de usuário já `ACTIVE`
- **THEN** resposta HTTP 409 com código de erro `EMAIL_ALREADY_CONFIRMED`

---

### REQ-EMAILCONF-03 — Reenvio de e-mail de confirmação com rate limiting
O sistema SHALL expor `POST /auth/resend-confirmation` com body `{ "email": "..." }`. O reenvio SHALL ser limitado a 3 tentativas por hora por endereço de e-mail usando Redis (chave `ect-rl:{email}`, TTL 1 hora, operação `INCR` atômica).

#### Scenario: Reenvio permitido dentro do limite
- **WHEN** `POST /auth/resend-confirmation` recebido para e-mail com menos de 3 tentativas na janela
- **THEN** contador Redis incrementado atomicamente, novo token gerado e salvo, e-mail enviado, resposta HTTP 204

#### Scenario: Rate limit excedido
- **WHEN** `POST /auth/resend-confirmation` recebido e contador Redis >= 3 na janela de 1 hora
- **THEN** resposta HTTP 429 com header `Retry-After` e código de erro `RESEND_RATE_LIMIT_EXCEEDED`

#### Scenario: E-mail não cadastrado ou conta já ativa
- **WHEN** `POST /auth/resend-confirmation` recebido para e-mail desconhecido ou usuário já `ACTIVE`
- **THEN** resposta HTTP 204 sem enviar e-mail (não revelar existência da conta)

---

### REQ-EMAILCONF-04 — Frontend: página de callback de confirmação
A rota `/confirm-email` SHALL detectar a presença do query param `?token`. Com token: exibir loading, chamar `GET /auth/confirm-email?token=...`, exibir resultado (sucesso → redirecionar para `/login?confirmed=true`; erro → mensagem com botão de reenvio). Sem token: exibir tela estática "verifique seu e-mail".

#### Scenario: Token presente e confirmação bem-sucedida
- **WHEN** usuário acessa `/confirm-email?token={token}` e API retorna 204
- **THEN** exibir mensagem de sucesso e redirecionar para `/login?confirmed=true` após 2 segundos

#### Scenario: Token presente mas inválido/expirado
- **WHEN** usuário acessa `/confirm-email?token={token}` e API retorna 400
- **THEN** exibir mensagem de erro "Link inválido ou expirado" com botão "Reenviar e-mail de confirmação"

#### Scenario: Sem token na URL
- **WHEN** usuário acessa `/confirm-email` sem query param `token`
- **THEN** exibir tela estática "Confirmação de e-mail enviada — verifique sua caixa de entrada"

#### Scenario: Login com confirmação recém-feita
- **WHEN** usuário chega em `/login?confirmed=true`
- **THEN** exibir banner informativo "E-mail confirmado com sucesso! Faça login para continuar."
