## 1. Infra — Flyway Migration

- [ ] 1.1 [INFRA] Criar `V003__drop_email_confirmation_tokens_table.sql` com `DROP TABLE IF EXISTS email_confirmation_tokens`

## 2. Backend — Domain & Ports

- [ ] 2.1 [BE] Criar `EmailConfirmationTokenRepository` em `domain/port/out/`: métodos `save(token, userId, ttl)`, `findUserId(token) → Optional<String>`, `invalidate(token)`
- [ ] 2.2 [BE] Criar `ConfirmEmailUseCase` em `domain/port/in/`: método `execute(String token)`
- [ ] 2.3 [BE] Criar `ResendConfirmationUseCase` em `domain/port/in/`: método `execute(ResendConfirmationCommand)`
- [ ] 2.4 [BE] Criar exceções de domínio: `InvalidConfirmationTokenException`, `EmailAlreadyConfirmedException`, `ResendRateLimitExceededException`

## 3. Backend — Infrastructure

- [ ] 3.1 [BE] Criar `EmailConfirmationRedisRepository` em `infrastructure/security/`: implementa `EmailConfirmationTokenRepository` com prefixo `ect:` e `ect-rl:` para rate limit

## 4. Backend — Application Use Cases

- [ ] 4.1 [BE] Corrigir `RegisterUserService`: injetar `EmailConfirmationTokenRepository` e persistir o token antes de enviar o e-mail (REQ-EMAILCONF-01)
- [ ] 4.2 [BE] Criar `ConfirmEmailService` em `application/usecase/`: busca userId no Redis, atualiza status para `ACTIVE`, invalida token (REQ-EMAILCONF-02)
- [ ] 4.3 [BE] Criar `ResendConfirmationCommand` em `application/dto/`
- [ ] 4.4 [BE] Criar `ResendConfirmationService` em `application/usecase/`: valida rate limit via Redis (`INCR` + `EXPIRE`), gera novo token, persiste e envia e-mail (REQ-EMAILCONF-03)

## 5. Backend — REST Interface

- [ ] 5.1 [BE] Adicionar `GET /auth/confirm-email` ao `AuthResource`: query param `token`, retorna 204/400/409 com mapeamento das exceções no `GlobalExceptionMapper`
- [ ] 5.2 [BE] Adicionar `POST /auth/resend-confirmation` ao `AuthResource`: body `{ email }`, retorna 204/429 com header `Retry-After`
- [ ] 5.3 [BE] Criar `ResendConfirmationRequest` em `interfaces/rest/dto/` com validação `@Email @NotBlank`
- [ ] 5.4 [BE] Registrar `InvalidConfirmationTokenException → 400`, `EmailAlreadyConfirmedException → 409`, `ResendRateLimitExceededException → 429` no `GlobalExceptionMapper`

## 6. Backend — Testes

- [ ] 6.1 [BE] Teste unitário `ConfirmEmailServiceTest`: cenários token válido, expirado e já confirmado
- [ ] 6.2 [BE] Teste unitário `ResendConfirmationServiceTest`: cenários dentro do limite, rate limit excedido e e-mail não encontrado
- [ ] 6.3 [BE] Teste unitário `RegisterUserServiceTest`: verificar que token é salvo no Redis antes do e-mail
- [ ] 6.4 [BE] Teste de integração/API `ConfirmEmailResourceIT` com `@QuarkusTest` + Testcontainers Redis: fluxo completo register → confirm → login

## 7. Frontend — Hook e API

- [ ] 7.1 [FE] Adicionar `confirmEmail(token: string)` e `resendConfirmation(email: string)` em `features/auth/api/auth-api.ts`
- [ ] 7.2 [FE] Adicionar query keys `AUTH_KEYS.confirmEmail` e `AUTH_KEYS.resendConfirmation` em `query-keys.ts`
- [ ] 7.3 [FE] Criar `useConfirmEmail.ts` em `features/auth/hooks/`: mutation que chama `confirmEmail`, gerencia estados loading/success/error
- [ ] 7.4 [FE] Criar `useResendConfirmation.ts` em `features/auth/hooks/`: mutation que chama `resendConfirmation`

## 8. Frontend — Componentes e Rota

- [ ] 8.1 [FE] Criar `ConfirmEmailCallbackPage.tsx` em `features/auth/components/`: detecta `?token`, sem token exibe tela estática (reutiliza conteúdo do `EmailConfirmationPage`), com token exibe loading → sucesso/erro (REQ-EMAILCONF-04)
- [ ] 8.2 [FE] Atualizar rota `/confirm-email` em `routes.tsx` para usar `ConfirmEmailCallbackPage` (remover `EmailConfirmationPage` direto)
- [ ] 8.3 [FE] Adicionar banner de sucesso em `LoginPage` ao detectar `?confirmed=true` na URL
- [ ] 8.4 [FE] Criar `ResendConfirmationForm.tsx` embutido no estado de erro da `ConfirmEmailCallbackPage`: campo e-mail + botão reenviar

## 9. Frontend — Testes

- [ ] 9.1 [FE] Teste `ConfirmEmailCallbackPage.test.tsx`: cenários sem token (tela estática), com token válido (redireciona), com token inválido (mostra erro + form resend)
- [ ] 9.2 [FE] Teste `useConfirmEmail.test.ts` com MSW: mock de 204, 400 e 409
