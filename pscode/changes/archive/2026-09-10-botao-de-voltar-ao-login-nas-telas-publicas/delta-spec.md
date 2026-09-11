# Botão de voltar ao login nas telas públicas — Delta

## Added

- **`BackToLogin`** (`apps/web/src/components/shared/BackToLogin.tsx`): um
  `<Link to="/login">` com `ArrowLeft` e o texto "Voltar ao login", no tom
  discreto dos retornos já existentes em `ClassroomDetailPage` e
  `SubjectDetailPage`. Destino fixo; a única prop é um `className` opcional,
  usado para o `self-start` dentro do card centralizado do `/confirm-email`.
- **Saída nas quatro telas públicas.** `/register`, `/forgot-password`,
  `/reset-password` e `/confirm-email` exibem o controle acima do título —
  inclusive nos **estados terminais**, que eram o real motivo da change:
  "E-mail enviado" do forgot-password, "Link de redefinição inválido." do
  reset-password e os dois erros do confirm-email não tinham link nenhum, só o
  botão do navegador.
- **Spec viva `app-layout`**: requirement "Telas públicas de autenticação têm
  saída para o login" com três cenários — tela com formulário, estado terminal
  sem formulário, e os estados que já redirecionam sozinhos.
- Testes: `BackToLogin.test.tsx` novo, `ResetPasswordPage.test.tsx` novo (a tela
  não tinha teste algum) e asserções nos estados terminais de
  `ForgotPasswordPage.test.tsx` e `ConfirmEmailCallbackPage.test.tsx`.

## Changed

- **`ForgotPasswordPage` e `ResetPasswordPage` ganharam um wrapper nos retornos
  sem formulário.** O estado de sucesso e o de token ausente eram um bloco
  centralizado solto; agora o `text-center` vale só para o texto, com o controle
  alinhado à esquerda por cima.

## Unchanged

- **O `LoginPage` não exibe o controle** — é o destino. Por isso o componente
  vive fora do `AuthLayout`, que os dois compartilham.
- **O rodapé "Já tem conta? Entrar" do `/register` permanece**: é convite
  contextual para quem já tem conta, não navegação de retorno.
- **Loading e sucesso do `/confirm-email` ficam sem o controle** — os dois
  redirecionam para `/login` sozinhos; o botão só piscaria.
- `/welcome`, `/organizations/new` e o aceite de convite seguem com a saída
  própria do `MinimalHeader`. Nenhuma rota, guard ou regra de autenticação mudou.
