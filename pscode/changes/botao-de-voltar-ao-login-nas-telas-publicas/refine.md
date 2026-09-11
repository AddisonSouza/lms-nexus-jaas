# [feat] botão de voltar ao login nas telas públicas

## Summary
Quem cai numa tela pública de autenticação hoje fica sem saída: só o botão do
navegador leva de volta ao login. As quatro telas passam a ter um `← Voltar ao
login` no topo, igual em todas — inclusive nos estados sem formulário, onde a
falta de saída é mais sentida.

## Technical detail
- Novo `apps/web/src/components/shared/BackToLogin.tsx`: um `<Link to="/login">`
  com `ArrowLeft` do Lucide e o texto "Voltar ao login", no tom discreto
  (`text-muted-foreground hover:text-foreground`) dos retornos já existentes em
  `ClassroomDetailPage` e `SubjectDetailPage`. Sem props — o destino é fixo.
- Fica **fora** do `AuthLayout`: o `LoginPage` usa o mesmo layout e não pode
  herdar o controle, e o `/confirm-email` nem usa `AuthLayout` — monta o próprio
  `StatusCard`. Um componente inserido página a página resolve os dois.
- `RegisterPage`: acima do `<h2>Criar conta</h2>`. O rodapé "Já tem conta?
  Entrar" **permanece** — é convite contextual, não navegação de retorno.
- `ForgotPasswordPage`: nos **dois** retornos — o formulário e o estado de
  sucesso "E-mail enviado", que hoje é um beco sem saída.
- `ResetPasswordPage`: no formulário e no retorno de token ausente ("Link de
  redefinição inválido."), que hoje é só uma frase solta.
- `ConfirmEmailCallbackPage`: no `StaticPendingPage` e no ramo `isError` (tanto
  "Link inválido ou expirado" quanto "E-mail já confirmado"). **Não** entra no
  loading nem no sucesso, que redirecionam para `/login` sozinhos.
- O `StatusCard` centraliza o conteúdo (`text-center`, `items-center`), então lá
  o controle precisa ficar alinhado à esquerda do card (`self-start`) para não
  virar um link solto no meio.

## Scope
### In
- `BackToLogin.tsx` novo, com teste unitário.
- Inserção nas quatro telas, cobrindo os estados sem formulário.
- Testes de tela para os estados que hoje não têm saída.
- Spec viva `app-layout` com o requirement do retorno nas rotas públicas.

### Out
- `LoginPage` — é o destino, não ganha controle.
- `/welcome`, `/organizations/new` e o aceite de convite: já têm saída pelo
  `MinimalHeader` / botão "Voltar" próprio.
- Redesenho do `AuthLayout`, do `StatusCard` ou dos textos das telas.
- Qualquer mudança de rota, guard ou regra de autenticação.

## Subtasks
- [ ] Criar `apps/web/src/components/shared/BackToLogin.tsx` com `ArrowLeft` + link para `/login`, e seu teste unitário
- [ ] Inserir o `BackToLogin` no `RegisterPage`, mantendo o rodapé "Já tem conta? Entrar"
- [ ] Inserir no `ForgotPasswordPage`, no formulário e no estado "E-mail enviado"
- [ ] Inserir no `ResetPasswordPage`, no formulário e no retorno de token ausente
- [ ] Inserir no `ConfirmEmailCallbackPage`, no pendente e nos dois estados de erro, alinhado à esquerda no `StatusCard`
- [ ] Escrever os testes de tela dos estados sem formulário (e-mail enviado, token ausente, link inválido, já confirmado)
- [ ] Atualizar a spec `pscode/specs/app-layout/spec.md` e rodar lint, type-check e a suíte Vitest do `apps/web`
