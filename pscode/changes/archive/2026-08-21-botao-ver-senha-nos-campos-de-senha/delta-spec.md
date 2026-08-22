# Botão para ver senha nos campos de senha — Delta

## Added

- **Primitivo `PasswordInput`** (`components/ui/password-input.tsx`): input de
  senha com botão de alternância embutido (ícones `Eye` / `EyeOff` do Lucide
  React), `forwardRef` e as mesmas props do `Input`. Estado padrão: oculto
  (`type="password"`).
- **Acessibilidade do toggle**: o botão é focável por Tab, expõe `aria-pressed`
  refletindo a visibilidade e alterna o `aria-label` entre "Mostrar senha" e
  "Ocultar senha".
- **Independência entre instâncias**: cada `PasswordInput` mantém seu próprio
  estado — alternar um campo não afeta os demais na mesma tela.

## Changed

- **`authentication` / `password-reset`**: os quatro campos de senha das telas
  públicas passam a usar `PasswordInput` no lugar de `Input type="password"` —
  `LoginForm` (`password`), `RegisterForm` (`password`) e `ResetPasswordPage`
  (`newPassword` e `confirmPassword`). `id`, `autoComplete`, labels, schemas Zod
  e o fluxo de autenticação permanecem inalterados.

## Out of scope (inalterado)

- Medidor de força de senha e regras de validação de senha.
- Campos de senha fora das telas de autenticação.
- O primitivo `Input` existente.
