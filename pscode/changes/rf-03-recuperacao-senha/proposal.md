## Why

RF-03 (módulo `identity`): usuários com conta ativa não conseguem recuperar o acesso quando perdem a senha. O fluxo de reset é pré-requisito para o MVP e segurança básica da plataforma.

## What Changes

- Dois novos endpoints públicos em `/auth`: `POST /auth/forgot-password` e `POST /auth/reset-password`
- Token de reset UUID armazenado no Redis com TTL de 1 hora (prefix `prt:`)
- E-mail com link de reset enviado via `EmailPort` (extensão do port existente)
- Ao confirmar o reset: senha atualizada com BCrypt e **todos os Refresh Tokens do usuário invalidados** (secondary Redis SET `rt:user:{userId}`)
- A resposta da API não revela se o e-mail existe (proteção contra enumeração)
- Frontend: `ForgotPasswordPage` (formulário de e-mail) e `ResetPasswordPage` (formulário de nova senha com token da URL)

## Capabilities

### New Capabilities

- `password-reset`: Fluxo completo de recuperação de senha via e-mail — solicitação, validação de token (uso único, 1h TTL) e reset com invalidação de sessões ativas

### Modified Capabilities

- `authentication`: `RefreshTokenRepository` estendido com `deleteAllByUserId` + secondary SET; `UserRepository` estendido com `findById`

## Impact

- **Backend:** `identity` — novos ports `PasswordResetTokenRepository` e `RequestPasswordResetUseCase`/`ResetPasswordUseCase`; modificação em `RefreshTokenRepository`, `UserRepository`, `EmailPort` e `RefreshTokenRedisRepository`
- **Frontend:** `features/auth` — dois novos formulários, schemas Zod e hooks TanStack Query
- **Sem migration Flyway** — tokens de reset armazenados exclusivamente em Redis
- **Não impacta:** outros módulos, JWT, roles, organização
