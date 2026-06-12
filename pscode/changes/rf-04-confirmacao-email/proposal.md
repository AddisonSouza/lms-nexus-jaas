## Why

RF-04 fecha o ciclo iniciado pelo RF-01: o cadastro cria a conta com status `PENDING_CONFIRMATION` e envia o e-mail, mas o sistema ainda não possui o endpoint que valida o token e ativa a conta. Sem ele, nenhum usuário pode passar de cadastrado para ativo. Módulo: `identity`.

## What Changes

- Novo endpoint `GET /auth/confirm-email?token={token}` que valida o token Redis e muda o status do usuário para `ACTIVE`
- Novo endpoint `POST /auth/resend-confirmation` com rate limiting (3/hora por e-mail) para reenviar o link
- Correção no `RegisterUserService`: o token gerado hoje não é persistido — passa a ser salvo no Redis (chave `ect:{token}` → userId, TTL 24h)
- Migração V003 para dropar a tabela `email_confirmation_tokens` criada na V002 (substituída pelo Redis)
- Frontend: a rota `/confirm-email` passa a ser uma página dinâmica — com `?token` chama a API e exibe sucesso/erro; sem `?token` exibe a tela estática pós-cadastro atual

## Capabilities

### New Capabilities

- `email-confirmation`: Validação do token de confirmação de e-mail e ativação de conta; reenvio com rate limiting

### Modified Capabilities

- `authentication`: O `RegisterUserService` passa a persistir o token de confirmação no Redis

## Non-goals

- Confirmação por SMS ou método alternativo
- Painel administrativo para confirmar manualmente contas
- Reenvio automático após X dias sem confirmação

## Impact

- **Backend:** módulo `identity` — `domain/port/in`, `domain/port/out`, `application/usecase`, `infrastructure/security`, `interfaces/rest/AuthResource`
- **Frontend:** `features/auth` — novo componente `ConfirmEmailCallbackPage`, hook `useConfirmEmail`, rota `/confirm-email`
- **Infra:** Redis (nova chave `ect:*` e rate limit `ect-rl:*`), Flyway V003
- Sem impacto em outros módulos
