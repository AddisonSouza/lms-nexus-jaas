## Context

O RF-01 registra o usuário com status `PENDING_CONFIRMATION` e envia o e-mail com um token UUID, mas o token nunca é persistido (bug silencioso). A tabela `email_confirmation_tokens` foi criada na V002 mas nunca é escrita. O RF-04 fecha este ciclo.

Estado atual do código relevante:
- `RegisterUserService.execute()`: gera token → chama `emailPort.sendConfirmationEmail(email, token)` → token descartado
- `AuthenticateService`: já bloqueia usuários não-`ACTIVE`
- `QuarkusMailAdapter`: envia e-mail para `http://localhost:5173/confirm-email?token=...`
- Frontend `/confirm-email`: rota existe mas exibe tela estática (não lê `?token`)

## Goals / Non-Goals

**Goals:**
- Persistir token de confirmação no Redis no momento do cadastro
- Implementar `GET /auth/confirm-email?token` que ativa a conta
- Implementar `POST /auth/resend-confirmation` com rate limit
- Dropar tabela MySQL `email_confirmation_tokens` (V003)
- Frontend: página `/confirm-email` que detecta `?token` e chama a API

**Non-Goals:**
- Confirmação por meios alternativos (SMS, magic link)
- Expiração progressiva (TTL fixo de 24h)
- Reenvio automático agendado

## Decisions

### 1. Redis para tokens (não MySQL)

Token armazenado como `ect:{token}` → `{userId}` com TTL 24h via `SetArgs.ex(Duration)`. Segue o padrão do RF-03 (password reset) e elimina a tabela V002, que nunca foi usada. A tabela será removida pela V003.

Alternativa descartada: usar a tabela `email_confirmation_tokens` (V002). Exigiria JPA entity, repository impl e clean-up manual do `used_at` — mais código para o mesmo resultado.

### 2. Nova Port `EmailConfirmationTokenRepository`

Padrão idêntico a `PasswordResetTokenRepository`:
```
save(token, userId, ttl)
findUserId(token) → Optional<String>
invalidate(token)
```
Implementação: `EmailConfirmationRedisRepository` (prefixo `ect:`).

### 3. Rate limiting no resend via Redis

Chave `ect-rl:{email}` com TTL de 1 hora, valor = contador de tentativas. Máximo 3 reenvios/hora. Se exceder → `429 Too Many Requests`. Mesmo padrão que o rate limit de `/auth/**` já descrito em SEC-08.

Não foi necessário um Port separado: o rate limit é responsabilidade do use case de resend (verificação inline no `ResendConfirmationService`), direto via `RedisDataSource`.

### 4. Frontend: mesma rota `/confirm-email`, comportamento condicional

A rota `/confirm-email` permanece no router. O componente `ConfirmEmailCallbackPage` é novo e substitui `EmailConfirmationPage` nessa rota. Detecta a presença de `?token`:
- **Com token**: chama `GET /auth/confirm-email?token=...`, exibe loading → sucesso (redireciona para `/login?confirmed=true`) → ou erro (token inválido/expirado + botão resend)
- **Sem token**: renderiza o conteúdo estático atual (enviamos um e-mail, verifique sua caixa)

`EmailConfirmationPage.tsx` é reutilizado como sub-componente "sem token" dentro de `ConfirmEmailCallbackPage`, ou simplesmente inlinado.

### 5. Estrutura de pacotes (backend)

```
identity/
  domain/port/in/
    ConfirmEmailUseCase.java          (novo)
    ResendConfirmationUseCase.java    (novo)
  domain/port/out/
    EmailConfirmationTokenRepository.java  (novo)
  domain/exception/
    EmailAlreadyConfirmedException.java   (novo)
    InvalidConfirmationTokenException.java (novo)
    ResendRateLimitExceededException.java  (novo)
  application/dto/
    ResendConfirmationCommand.java    (novo)
  application/usecase/
    ConfirmEmailService.java          (novo)
    ResendConfirmationService.java    (novo)
  infrastructure/security/
    EmailConfirmationRedisRepository.java  (novo)
  interfaces/rest/
    AuthResource.java                 (adiciona 2 endpoints)
```

### 6. Endpoints

| Método | Path | Auth | Response |
|--------|------|------|----------|
| `GET`  | `/auth/confirm-email` | público | `204` (sucesso) / `400` (token inválido) / `409` (já confirmado) |
| `POST` | `/auth/resend-confirmation` | público | `204` / `429` (rate limit) |

O `GET` retorna `204` sem body — o frontend redireciona por conta própria.

### 7. Flyway V003

```sql
-- V003__drop_email_confirmation_tokens_table.sql
DROP TABLE IF EXISTS email_confirmation_tokens;
```

## Risks / Trade-offs

- **Token único-uso**: o Redis não impõe uso único nativamente. O `ConfirmEmailService` deve invalidar o token (`del`) imediatamente após a confirmação — se falhar no meio, o token pode ser reusado numa janela curta. Mitigação: a confirmação é idempotente (já `ACTIVE` → retorna `409` sem erro grave).
- **Race condition no resend**: dois requests simultâneos podem ambos passar pelo check de rate limit antes de incrementar o contador. Mitigação: usar `INCR` com `EXPIRE` (operação atômica no Redis).
- **Tabela V002 dropada**: irreversível. Mitigação: nenhuma linha foi inserida na tabela até hoje (token nunca foi persistido no RF-01).

## Migration Plan

1. Deployar V003 (DROP TABLE — seguro, tabela vazia)
2. Deployar API com `EmailConfirmationRedisRepository` e novos endpoints
3. Deployar frontend com `ConfirmEmailCallbackPage`
4. Rollback: reverter deploy; tabela já não existe mas Redis keys expiram em 24h — sem impacto
