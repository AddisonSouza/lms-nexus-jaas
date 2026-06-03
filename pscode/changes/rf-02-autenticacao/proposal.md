## Why

RF-02 (identity): O sistema já suporta cadastro de usuários (RF-01), mas não há mecanismo de autenticação. Sem login, nenhuma funcionalidade protegida da plataforma é acessível. Esta change implementa o fluxo completo de autenticação stateless usando JWT RS256 + Refresh Token, habilitando todas as features multi-tenant subsequentes.

Impacta backend e frontend.

## What Changes

- `POST /auth/login` — autentica com e-mail + senha, retorna Access Token (JWT RS256, 15min) e Refresh Token (opaque, 7 dias, armazenado no Redis)
- `POST /auth/logout` — invalida o Refresh Token no Redis
- `POST /auth/refresh` — rotaciona o Refresh Token e emite novo par de tokens
- `AuthenticateUseCase` — use case de domínio sem dependência de Quarkus
- `LoginForm` + `authStore` no frontend — fluxo de login com React Hook Form + Zod e estado de sessão via Zustand

## Non-goals

- Registro de usuários (RF-01 — já implementado)
- Recuperação de senha (RF-03)
- Confirmação de e-mail (RF-04)
- Login social / OAuth externo
- Revogação de tokens por admin

## Capabilities

### New Capabilities

- `authentication`: Login, logout e refresh de tokens JWT RS256 com Refresh Token no Redis

### Modified Capabilities

- `user-registration`: Nenhuma alteração de requisito — apenas a entidade `User` é reutilizada como porta de entrada

## Impact

- **Backend:** novo `AuthenticateUseCase`, `JwtTokenService`, `RefreshTokenRepository` (Redis), `AuthResource` em `identity/interfaces/rest/`
- **Frontend:** novo `LoginPage`, `LoginForm`, `authStore` (Zustand), `useLogin` hook, rota pública `/login` em `features/auth/`
- **Infra:** Redis já presente no docker-compose; chaves RS256 geradas no startup do Quarkus OIDC
