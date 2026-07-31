# Botão para ver senha nos campos de senha

## Summary

Cada campo de senha das telas de login, cadastro e redefinição ganha um botão de
olho dentro do input que alterna entre senha oculta e visível, para o usuário
conferir o que digitou antes de enviar. Começa oculto, e alternar um campo não
mexe nos outros.

## Technical detail

- Novo primitivo `apps/web/src/components/ui/password-input.tsx`: `PasswordInput`
  com `forwardRef<HTMLInputElement>`, aceitando `React.ComponentProps<'input'>`
  menos `type` (fixado internamente). Compatível com `{...register(...)}` do
  react-hook-form.
- Estado local `visible` (`useState`) por instância — a independência entre
  campos sai de graça. Nada de client state global.
- Wrapper `relative`; `Input` recebe `pr-10`; botão `absolute right-3 top-1/2
  -translate-y-1/2`, com `type="button"` (não submete o form).
- Ícones `Eye` / `EyeOff` do **Lucide React** (único pacote de ícones permitido).
- Acessibilidade: botão focável por Tab, `aria-label` alternando entre
  `Mostrar senha` / `Ocultar senha` e `aria-pressed={visible}`. Os textos
  distintos por estado evitam colisão com os dois campos do reset.
- Trocas nos consumidores (4 campos, 3 arquivos): `LoginForm.tsx` (`password`),
  `RegisterForm.tsx` (`password`), `ResetPasswordPage.tsx` (`newPassword` e
  `confirmPassword`). `autoComplete` e `id` de cada campo permanecem como estão.
- Os testes atuais usam `getByLabelText(/senha/i)`; o botão é consultado por
  `getByRole('button')`, então não há conflito de query.

## Scope

### In

- `PasswordInput` em `components/ui/` + teste unitário.
- Adoção nos 4 campos de senha de `LoginForm`, `RegisterForm` e
  `ResetPasswordPage`.
- Verificação de que os testes existentes desses forms seguem passando.

### Out

- Medidor de força de senha e regras de validação.
- Qualquer mudança no fluxo de autenticação, schemas Zod ou API.
- Campos de senha fora das telas de auth.
- Alterar o primitivo `Input` existente.

## Subtasks

- [ ] Criar o primitivo `PasswordInput` em `apps/web/src/components/ui/password-input.tsx` com toggle Eye/EyeOff, `forwardRef` e acessibilidade (aria-label alternado, aria-pressed, focável por Tab)
- [ ] Escrever o teste unitário do `PasswordInput` cobrindo alternância de `type`, troca de `aria-label` e independência entre duas instâncias
- [ ] Adotar `PasswordInput` no campo de senha de `LoginForm.tsx` e `RegisterForm.tsx`
- [ ] Adotar `PasswordInput` nos campos `newPassword` e `confirmPassword` de `ResetPasswordPage.tsx`
- [ ] Rodar lint, typecheck e a suíte de testes do `apps/web` e confirmar que os testes existentes dos forms seguem verdes
