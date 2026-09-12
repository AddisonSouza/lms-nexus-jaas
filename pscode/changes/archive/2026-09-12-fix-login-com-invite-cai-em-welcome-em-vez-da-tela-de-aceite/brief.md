# Login com `?invite=` cai em `/welcome` em vez da tela de aceite

## Objetivo

Quem abre um link de convite deslogado, é levado a `/login?invite=<token>` e faz
login deve chegar à tela de aceite desse convite — sempre, e não só quando ainda
existe um convite pendente para o e-mail dele.

## Comportamento esperado

- Login a partir de `/login?invite=<token>` → `/invitations/<token>/accept`.
- Vale mesmo quando o convite do link foi cancelado, reenviado ou expirou: a
  tela de aceite é quem explica o que aconteceu.
- Sem `?invite=`, o login segue para `/` como hoje.

## Comportamento atual (causa)

O `PublicRoute` em volta de `/login` troca a tela por `<Navigate to="/" />`
assim que `isAuthenticated` vira `true`, passando por cima do
`navigate('/invitations/<token>/accept')` do `useLogin`. Em `/`, o `RootRedirect`
só leva ao aceite se houver convite **pendente** — o que escondia o bug desde
#130/#161.

## Fora de escopo

- Regras de convite (estados, reenvio, cancelamento).
- O fluxo de cadastro por `?invite=` (`RegisterPage`/`useRegister` ignoram o
  parâmetro).

## Origem

Encontrado no golden path do #77 (PR #197).
