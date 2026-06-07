## Context

O módulo `identity` já possui login/logout/refresh (RF-02) com tokens Redis e envio de e-mail via `QuarkusMailAdapter`. Os ports `RefreshTokenRepository` e `EmailPort` existem mas não cobrem o fluxo de reset. `UserRepository` expõe `findByEmail` e `save`, mas não `findById`.

## Goals / Non-Goals

**Goals:**
- Implementar `POST /auth/forgot-password` e `POST /auth/reset-password` seguindo os critérios de aceite do RF-03
- Invalidar todos os Refresh Tokens do usuário ao concluir o reset
- Frontend com dois formulários (`ForgotPasswordPage`, `ResetPasswordPage`)

**Non-Goals:**
- Rate limiting nos endpoints de reset (futuro)
- Reenvio automático de e-mail de reset
- Alteração de senha por usuário autenticado (escopo de perfil)

## Decisions

### Token de reset em Redis (não DB)
`prt:{token} → userId` com TTL 1h. Consistente com o padrão dos Refresh Tokens. Sem migration Flyway. `PasswordResetRedisRepository` implementa o novo port `PasswordResetTokenRepository`.

### Invalidar todos os Refresh Tokens via secondary SET
Alternativa "generation counter" exigiria mudar a estrutura do valor armazenado nos tokens existentes. O secondary SET `rt:user:{userId}` não altera o contrato atual:
- `save()` passa a também adicionar o token ao SET
- `delete()` remove do SET
- Novo `deleteAllByUserId()` lê o SET, deleta cada `rt:{token}` e o próprio SET

`RefreshTokenRepository` port recebe `deleteAllByUserId(String userId)`.

### EmailPort — método adicional
`sendPasswordResetEmail(Email to, String token)` adicionado ao `EmailPort` existente. Mesma abordagem do `sendConfirmationEmail` — HTML inline, URL configurada. Não justifica port separado pois é o mesmo adaptador e mesmo módulo.

### UserRepository — findById
`findById(UserId id)` adicionado ao port. Necessário em `ResetPasswordService` para carregar o usuário pelo `userId` extraído do token Redis antes de atualizar a senha.

### Estrutura de pacotes afetados

```
identity/
  domain/
    port/in/
      RequestPasswordResetUseCase.java    [NEW]
      ResetPasswordUseCase.java            [NEW]
    port/out/
      PasswordResetTokenRepository.java    [NEW]
      RefreshTokenRepository.java          [MODIFY +deleteAllByUserId]
      UserRepository.java                  [MODIFY +findById]
      EmailPort.java                       [MODIFY +sendPasswordResetEmail]
  application/
    dto/
      RequestPasswordResetCommand.java     [NEW]
      ResetPasswordCommand.java            [NEW]
    usecase/
      RequestPasswordResetService.java     [NEW]
      ResetPasswordService.java            [NEW]
  infrastructure/
    security/
      PasswordResetRedisRepository.java    [NEW]
      RefreshTokenRedisRepository.java     [MODIFY]
    persistence/
      UserRepositoryImpl.java              [MODIFY +findById]
    mail/
      QuarkusMailAdapter.java              [MODIFY]
  interfaces/rest/
    AuthResource.java                      [MODIFY +2 endpoints]
    dto/
      ForgotPasswordRequest.java           [NEW]
      ResetPasswordRequest.java            [NEW]
```

### Flyway
Nenhuma migration necessária — tokens armazenados exclusivamente no Redis.

### Endpoints REST

| Método | Path | Request Body | Response |
|--------|------|-------------|---------|
| `POST` | `/auth/forgot-password` | `{ "email": "..." }` | `204 No Content` (sempre, sem revelar existência) |
| `POST` | `/auth/reset-password` | `{ "token": "...", "newPassword": "..." }` | `204 No Content` |

Ambos públicos — exceção `SEC-06`. Reset retorna `400` se token inválido/expirado/já usado.

### Frontend

```
features/auth/
  schemas/
    forgotPasswordSchema.ts              [NEW]  — { email: z.string().email() }
    resetPasswordSchema.ts               [NEW]  — { newPassword, confirmPassword } + refine
  hooks/
    useForgotPassword.ts                 [NEW]  — useMutation POST /auth/forgot-password
    useResetPassword.ts                  [NEW]  — useMutation POST /auth/reset-password
  components/
    ForgotPasswordPage.tsx               [NEW]  — página + formulário
    ResetPasswordPage.tsx                [NEW]  — lê ?token= da URL, formulário de nova senha
  api/
    auth-api.ts                          [MODIFY] — forgotPassword(), resetPassword()
```

Roteamento: `/forgot-password` → `ForgotPasswordPage`; `/reset-password` → `ResetPasswordPage` (token via query param `?token=`).

## Risks / Trade-offs

- **Secondary SET e consistência**: se `save()` gravar o token mas o `SADD` falhar, o SET ficará incompleto. Mitigação: Redis é single-threaded, operações são atômicas individualmente; aceitável para MVP
- **Token não expirado no SET**: o SET pode acumular tokens já expirados que o Redis limpou automaticamente (TTL), mas cuja entrada no SET persiste. `deleteAllByUserId` deleta chaves inexistentes sem erro — impacto zero
- **E-mail HTML inline**: `QuarkusMailAdapter` usa texto inline como `sendConfirmationEmail`. Sem sistema de templates por ora — aceitável para MVP
