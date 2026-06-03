## 1. Backend — Domain & Ports

- [x] 1.1 [BE] Criar interfaces de Port: `AuthenticateUseCase`, `LogoutUseCase`, `RefreshTokenUseCase` em `identity/domain/port/in/`
- [x] 1.2 [BE] Criar interface `RefreshTokenRepository` em `identity/domain/port/out/`
- [x] 1.3 [BE] Criar value objects / DTOs: `AuthenticateCommand`, `AuthResult`, `RefreshCommand` em `identity/application/dto/`

## 2. Backend — Application (Use Cases)

- [x] 2.1 [BE] Implementar `AuthenticateUseCaseImpl`: valida credenciais via `UserRepository`, verifica `status == ACTIVE`, gera JWT + Refresh Token
- [x] 2.2 [BE] Implementar `LogoutUseCaseImpl`: invalida Refresh Token no `RefreshTokenRepository`
- [x] 2.3 [BE] Implementar `RefreshTokenUseCaseImpl`: valida token existente, deleta e emite novo par (rotation)
- [x] 2.4 [BE] Testes unitários de `AuthenticateUseCaseImpl` com Mockito (credencial inválida, status PENDING, ACTIVE)
- [x] 2.5 [BE] Testes unitários de `LogoutUseCaseImpl` e `RefreshTokenUseCaseImpl`

## 3. Backend — Infrastructure

- [x] 3.1 [BE] Implementar `JwtTokenService` (gera Access Token RS256 com claims sub/org/roles) em `identity/infrastructure/security/`
- [x] 3.2 [BE] Implementar `RefreshTokenRedisRepository` (save/exists/delete com TTL) em `identity/infrastructure/security/`
- [x] 3.3 [BE] Teste de integração `@QuarkusTest` para `AuthResource`: login OK, login inválido, logout, refresh

## 4. Backend — Interface REST

- [x] 4.1 [BE] Criar `AuthResource` com endpoints `POST /auth/login`, `POST /auth/logout`, `POST /auth/refresh` em `identity/interfaces/rest/`
- [x] 4.2 [BE] Configurar cookie HttpOnly para Refresh Token no `AuthResource`
- [x] 4.3 [BE] Configurar CORS em `application.properties` para aceitar `credentials: include` do frontend

## 5. Frontend — Core Auth

- [x] 5.1 [FE] Criar `authStore` (Zustand) com `accessToken`, `isAuthenticated`, `setToken`, `clearToken` em `features/auth/store/`
- [x] 5.2 [FE] Criar schema Zod `loginSchema` e hook `useLogin` (TanStack Query mutation) em `features/auth/hooks/`
- [x] 5.3 [FE] Criar componente `LoginForm` com React Hook Form + Zod em `features/auth/components/`
- [x] 5.4 [FE] Criar `LoginPage` e rota pública `/login` com `PublicRoute` (redireciona se autenticado)

## 6. Frontend — Session Persistence

- [x] 6.1 [FE] Implementar refresh silencioso na inicialização do app (chama `/auth/refresh` antes de redirecionar para login)
- [x] 6.2 [FE] Adicionar interceptor Axios/fetch para renovar token em respostas 401
- [x] 6.3 [FE] Testes Vitest + Testing Library: `LoginForm` (submit, erro de validação, erro da API)

## 7. Integração & Finalização

- [x] 7.1 [INFRA] Verificar configuração do Redis no docker-compose (já presente — apenas validar)
- [x] 7.2 [FE] Conectar `LoginPage` ao fluxo real: login → salva token → redireciona para `/`
- [ ] 7.3 [BE] Teste e2e manual: login → refresh → logout → refresh inválido retorna 401
