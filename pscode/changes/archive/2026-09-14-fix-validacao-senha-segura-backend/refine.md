# [fix] validação de senha segura no backend

## Summary
Hoje o site exige senha forte, mas quem chama a API direto consegue cadastrar ou
redefinir senha com quaisquer 8 caracteres. O back-end passará a exigir a mesma
senha forte e recusar as fracas.

## Technical detail
- Regra (espelha `apps/web/src/features/auth/schemas/passwordSchema.ts`): mín. 8
  caracteres + maiúscula `[A-Z]` + minúscula `[a-z]` + número `\d` + símbolo `[^A-Za-z0-9]`.
- Nova constraint `@StrongPassword` + `StrongPasswordValidator` em
  `module/identity/interfaces/rest/validation/`; mensagem dinâmica igual ao front
  ("Senha deve ter no mínimo 8 caracteres" / "A senha precisa de: uma maiúscula, ...").
- Aplicar em `RegisterRequest.password` e `ResetPasswordRequest.newPassword`,
  substituindo `@Size(min = 8)` (mantém `@NotBlank`).
- Falha cai no `ValidationExceptionMapper` existente → `422 { "errors": [...] }`.
- Aceite de convite usa o cadastro normal — coberto por `/auth/register`.
- Fixtures de teste com senha fraca (ex.: `newpassword123` no
  `PasswordResetResourceIT`) precisam virar senhas fortes.

## Scope
### In
- Constraint + validator com teste unitário dos critérios e mensagens.
- Aplicação nos DTOs de cadastro e reset de senha.
- ITs (Testcontainers) cobrindo senha fraca → 422 nos dois endpoints.
- `API_CONTRACT.md` atualizado com a regra.
### Out
- Front-end, medidor de força, histórico/expiração de senhas.
- Senhas já cadastradas; `POST /auth/login` (sem validação de força).

## Subtasks
- [x] Criar `@StrongPassword` + `StrongPasswordValidator` com teste unitário
- [x] Aplicar `@StrongPassword` em `RegisterRequest` e `ResetPasswordRequest` e ajustar fixtures fracas
- [x] ITs: senha fraca retorna 422 em `/auth/register` e `/auth/reset-password`
- [x] Atualizar `API_CONTRACT.md` com a regra de senha
