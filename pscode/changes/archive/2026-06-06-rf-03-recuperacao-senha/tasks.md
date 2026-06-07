## 1. Domain — Ports e Exceções

- [x] 1.1 [BE] Criar `domain/port/in/RequestPasswordResetUseCase.java` — `void execute(RequestPasswordResetCommand command)`
- [x] 1.2 [BE] Criar `domain/port/in/ResetPasswordUseCase.java` — `void execute(ResetPasswordCommand command)`
- [x] 1.3 [BE] Criar `domain/port/out/PasswordResetTokenRepository.java` — `save(token, userId, ttl)`, `findUserId(token)`, `invalidate(token)`
- [x] 1.4 [BE] Modificar `domain/port/out/RefreshTokenRepository.java` — adicionar `deleteAllByUserId(String userId)`
- [x] 1.5 [BE] Modificar `domain/port/out/UserRepository.java` — adicionar `Optional<User> findById(UserId id)`
- [x] 1.6 [BE] Modificar `domain/port/out/EmailPort.java` — adicionar `sendPasswordResetEmail(Email to, String token)`

## 2. Application — Commands e Use Cases

- [x] 2.1 [BE] Criar `application/dto/RequestPasswordResetCommand.java` (`email: String`) — `@Value @Builder`
- [x] 2.2 [BE] Criar `application/dto/ResetPasswordCommand.java` (`token: String`, `newPassword: String`) — `@Value @Builder`
- [x] 2.3 [BE] Criar `application/usecase/RequestPasswordResetService.java` — lookup por email, gera UUID, salva `prt:{token}` no Redis (1h TTL), envia e-mail; retorna sem revelar existência do e-mail
- [x] 2.4 [BE] Criar `application/usecase/ResetPasswordService.java` — valida token, busca usuário por ID, atualiza senha (BCrypt), invalida token, invoca `deleteAllByUserId`

## 3. Infrastructure — Redis, Persistência e E-mail

- [x] 3.1 [BE] Criar `infrastructure/security/PasswordResetRedisRepository.java` — prefix `prt:`, implementa `PasswordResetTokenRepository`
- [x] 3.2 [BE] Modificar `infrastructure/security/RefreshTokenRedisRepository.java` — manter secondary SET `rt:user:{userId}`; `save()` faz `SADD`, `delete()` faz `SREM`, implementar `deleteAllByUserId()` iterando o SET
- [x] 3.3 [BE] Modificar `infrastructure/persistence/UserRepositoryImpl.java` — implementar `findById` via `entityManager.find` ou Panache
- [x] 3.4 [BE] Modificar `infrastructure/mail/QuarkusMailAdapter.java` — implementar `sendPasswordResetEmail` com HTML inline e URL `lms.auth.password-reset.url` configurável

## 4. Interfaces REST

- [x] 4.1 [BE] Criar `interfaces/rest/dto/ForgotPasswordRequest.java` — `@NotBlank @Email String email`
- [x] 4.2 [BE] Criar `interfaces/rest/dto/ResetPasswordRequest.java` — `@NotBlank String token`, `@Size(min=8) String newPassword`
- [x] 4.3 [BE] Modificar `interfaces/rest/AuthResource.java` — adicionar `POST /auth/forgot-password` (204) e `POST /auth/reset-password` (204 / 400)

## 5. Frontend — Schemas, API e Hooks

- [x] 5.1 [FE] Criar `features/auth/schemas/forgotPasswordSchema.ts` — `{ email: z.string().email() }`
- [x] 5.2 [FE] Criar `features/auth/schemas/resetPasswordSchema.ts` — `{ newPassword, confirmPassword }` com `.refine()` para confirmar igualdade
- [x] 5.3 [FE] Modificar `features/auth/api/auth-api.ts` — adicionar `forgotPassword(email)` e `resetPassword(token, newPassword)`
- [x] 5.4 [FE] Criar `features/auth/hooks/useForgotPassword.ts` — `useMutation` em `forgotPassword`
- [x] 5.5 [FE] Criar `features/auth/hooks/useResetPassword.ts` — `useMutation` em `resetPassword`

## 6. Frontend — Componentes e Roteamento

- [x] 6.1 [FE] Criar `features/auth/components/ForgotPasswordPage.tsx` — formulário de e-mail com `useForgotPassword`; após sucesso mostra mensagem genérica
- [x] 6.2 [FE] Criar `features/auth/components/ResetPasswordPage.tsx` — lê `?token=` via `useSearchParams`, formulário de nova senha com `useResetPassword`; redireciona para login no sucesso
- [x] 6.3 [FE] Registrar rotas `/forgot-password` e `/reset-password` no roteador da aplicação
- [x] 6.4 [FE] Adicionar link "Esqueci minha senha" no `LoginForm` apontando para `/forgot-password`

## 7. Testes

- [x] 7.1 [BE] Testes unitários de `RequestPasswordResetService` — e-mail não encontrado (sem erro), e-mail encontrado (token salvo + e-mail enviado)
- [x] 7.2 [BE] Testes unitários de `ResetPasswordService` — token inválido (400), token válido (senha atualizada + tokens invalidados)
- [x] 7.3 [BE] Teste de integração `@QuarkusTest` para `POST /auth/forgot-password` e `POST /auth/reset-password` com Testcontainers (Redis)
- [x] 7.4 [FE] Teste de componente `ForgotPasswordPage` — submit dispara mutation, exibe mensagem de sucesso
