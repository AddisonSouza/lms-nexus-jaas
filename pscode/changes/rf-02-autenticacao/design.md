## Context

RF-01 (cadastro) já persiste `User` com `status: PENDING_VERIFICATION`. O RF-02 autentica usuários com `status: ACTIVE` usando JWT RS256 gerenciado pelo Quarkus OIDC e Refresh Token armazenado no Redis.

## Goals / Non-Goals

**Goals:**
- Login com e-mail + senha → Access Token (JWT RS256, 15min) + Refresh Token (opaque, 7 dias)
- Logout invalida o Refresh Token no Redis
- Refresh rotaciona o par de tokens (sliding window)
- Frontend exibe tela de login e persiste o estado de autenticação via Zustand

**Non-Goals:**
- Login social / OAuth externo
- Revogação de tokens por admin
- RF-03 (recuperação de senha), RF-04 (confirmação de e-mail)

## Decisions

**JWT RS256 via Quarkus OIDC:** par de chaves gerado no startup; claims: `sub` (userId), `org` (organizationId), `roles`. Access Token tem TTL de 15 minutos.

**Refresh Token opaque no Redis:** chave `rt:{userId}:{tokenHash}`, TTL 7 dias. Logout deleta a chave. Refresh valida a chave, deleta e emite um novo par (rotation obrigatória).

**`AuthenticateUseCase` no domain:** recebe `AuthenticateCommand` (email, senha), retorna `AuthResult` (accessToken, refreshToken). Sem dependência de Jakarta/Quarkus — testa com Mockito puro.

**Refresh Token no Cookie HttpOnly:** enviado como cookie `__refresh_token` HttpOnly + Secure + SameSite=Strict. Access Token retornado no body JSON.

## Estrutura de pacotes (backend)

```
identity/
  domain/
    model/        — (reutiliza User)
    port/in/      — AuthenticateUseCase (interface)
    port/out/     — RefreshTokenRepository (interface)
  application/
    usecase/      — AuthenticateUseCaseImpl, LogoutUseCaseImpl, RefreshTokenUseCaseImpl
    dto/          — AuthenticateCommand, AuthResult, RefreshCommand
  infrastructure/
    security/     — JwtTokenService, RefreshTokenRedisRepository
    persistence/  — (reutiliza UserRepository)
  interfaces/
    rest/         — AuthResource
```

## Ports novos

**In:**
- `AuthenticateUseCase`: `AuthResult authenticate(AuthenticateCommand cmd)`
- `LogoutUseCase`: `void logout(String refreshToken)`
- `RefreshTokenUseCase`: `AuthResult refresh(String refreshToken)`

**Out:**
- `RefreshTokenRepository`: `void save(String userId, String tokenHash, Duration ttl)`, `boolean exists(String userId, String tokenHash)`, `void delete(String userId, String tokenHash)`

## Migration Flyway

Nenhuma migration necessária — `User` já existe (RF-01). O Refresh Token persiste no Redis, não em banco.

## Endpoints REST

| Método | Path             | Descrição                          | Auth |
|--------|------------------|------------------------------------|------|
| POST   | `/auth/login`    | Autentica, retorna tokens          | Pública |
| POST   | `/auth/logout`   | Invalida refresh token             | Bearer |
| POST   | `/auth/refresh`  | Rotaciona tokens via cookie        | Cookie HttpOnly |

## Frontend

**Componentes:** `LoginPage`, `LoginForm`
**Hooks:** `useLogin` (TanStack Query mutation)
**Schema Zod:** `loginSchema` (email + senha)
**Store Zustand:** `authStore` — campos: `accessToken`, `isAuthenticated`, actions: `setToken`, `clearToken`
**Query keys:** `['auth', 'session']`
**Rota pública:** `/login` via React Router, envolvida por `PublicRoute` (redireciona se já autenticado)

## Risks / Trade-offs

- **Refresh via Cookie vs Body:** Cookie HttpOnly é mais seguro contra XSS, mas exige CORS configurado com `credentials: 'include'`. Decisão: Cookie HttpOnly.
- **Token rotation:** se a requisição de refresh falhar após deletar o token (antes de salvar o novo), o usuário é deslogado. Risco aceito — operação atômica no Redis não é necessária para MVP.
