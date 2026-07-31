# Questions

- [x] Como o toggle deve ser implementado nos 4 campos de senha?
  → **Primitivo `PasswordInput`** em `components/ui/password-input.tsx`,
  encapsulando `Input` + botão. Os 3 forms trocam `<Input type="password">`
  por `<PasswordInput>`.
- [x] O botão deve entrar na navegação por Tab?
  → **Sim, focável** — botão normal no fluxo de Tab, com `aria-label` e
  `aria-pressed`.
- [x] Quais testes automatizados fazem parte desta change?
  → **Unit no primitivo** (alterna `type`, `aria-label`, independência entre
  campos) **+ smoke nos forms** (os testes existentes continuam passando).
