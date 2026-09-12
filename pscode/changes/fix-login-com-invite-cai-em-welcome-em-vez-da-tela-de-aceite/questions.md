# Questions — #198

- [x] Onde corrigir a corrida entre `PublicRoute` e `useLogin`? → **No `PublicRoute`**:
  com sessão e `?invite=` na URL, leva a `/invitations/<token>/accept` em vez de `/`.
  Os dois redirecionamentos apontam para o mesmo lugar e a corrida deixa de importar.
- [x] Quem já está logado e abre `/login?invite=<token>`? → **Vai para o aceite**,
  mesmo destino de quem acabou de logar.
- [x] `/register?invite=<token>` com sessão? → **Mesma regra**. O fluxo de cadastro
  em si (`RegisterPage`/`useRegister` ignorando o parâmetro) segue fora de escopo.
