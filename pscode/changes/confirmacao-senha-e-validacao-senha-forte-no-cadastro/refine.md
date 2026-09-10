# [feat] confirmação de senha e validação de senha forte no cadastro

## Summary
O cadastro passa a pedir a senha duas vezes e só aceita senhas fortes. Se os dois
campos não forem iguais, ou se a senha não atender aos critérios mínimos, o
formulário aponta exatamente o que falta e não envia nada ao servidor.

## Technical detail
- Novo `apps/web/src/features/auth/schemas/passwordSchema.ts`: regra única de
  força — mínimo 8 caracteres com maiúscula, minúscula, número e símbolo.
- A mensagem é montada com os critérios **não atendidos** via `superRefine`
  ("A senha precisa de: uma maiúscula, um número"); o comprimento continua com a
  mensagem própria ("Senha deve ter no mínimo 8 caracteres").
- `registerSchema` consome o `passwordSchema` em `password`, ganha
  `confirmPassword` e um `.refine` comparando os dois, com `path:
  ['confirmPassword']` e a mensagem "As senhas não conferem" — mesmo padrão já
  usado em `resetPasswordSchema.ts`.
- `RegisterForm.tsx` ganha o campo "Confirmar senha" com o primitivo
  `PasswordInput` e `autoComplete="new-password"`, seguindo o bloco do campo
  atual (`label htmlFor` + `<p className="text-xs text-destructive">`).
- Nada muda no envio: `registerUser` já monta o payload campo a campo
  (`fullName`, `email`, `password`), então `confirmPassword` não vaza para a API.
- Nenhuma mudança de back-end: `RegisterRequest` mantém `@Size(min = 8)`. O
  front fica mais estrito que o servidor, o que não quebra o contrato.

## Scope
### In
- `passwordSchema.ts` novo e `registerSchema.ts` consumindo ele.
- Campo "Confirmar senha" no `RegisterForm.tsx`.
- Testes unitários do schema e do formulário.
- Spec viva `user-registration` atualizada com os critérios de força.

### Out
- `resetPasswordSchema` / `ResetPasswordPage` (continuam com o mínimo de 8).
- Alteração de senha de usuário já cadastrado.
- Medidor visual de força ou checklist reativo abaixo do campo.
- Política de senha configurável por organização.
- Qualquer validação nova no back-end.

## Subtasks
- [x] Criar `apps/web/src/features/auth/schemas/passwordSchema.ts` com a regra de força e a mensagem que lista os critérios faltantes
- [x] Consumir o `passwordSchema` no `registerSchema` e adicionar `confirmPassword` com o `.refine` de igualdade
- [x] Adicionar o campo "Confirmar senha" ao `RegisterForm.tsx` com `PasswordInput` e exibição do erro
- [x] Escrever `passwordSchema.test.ts` cobrindo cada critério isolado, a mensagem composta e a senha válida
- [x] Escrever `RegisterForm.test.tsx` cobrindo senha fraca, senhas divergentes e o caminho feliz
- [x] Atualizar a spec `pscode/specs/user-registration/spec.md` com os critérios de força e a confirmação
- [x] Rodar lint, type-check e a suíte Vitest do `apps/web` e validar o cadastro no browser
