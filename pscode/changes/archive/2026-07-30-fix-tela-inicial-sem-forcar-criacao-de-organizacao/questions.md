# Questions

- [x] **Onde a tela inicial vive?** Rota própria `/welcome`, fora do `AppShell`
  (mesmo padrão de `/organizations/new`). Sem Sidebar quebrada.
- [x] **O que faz "entrar em organização existente"?** Campo para colar o link
  do convite; o app extrai o token e navega para `/invitations/:token/accept`.
  Não há endpoint para listar convites pendentes nem entrar por código, e o
  back-end está fora de escopo.
- [x] **Barrar rotas internas para quem não tem organização?** Não redirecionar.
  Renderizar um componente padrão de estado vazio dizendo que o usuário não tem
  organização vinculada e oferecendo os dois caminhos.
- [x] **Onde fica o componente?** `features/onboarding/`, feature nova — evita
  import cruzado entre `organization` e `invitation`.
- [x] **Peso dos dois caminhos na tela?** Os dois em destaque equivalente, cada
  um a uma ação de distância. Além do campo de convite, um aviso explica que o
  admin/gestor de uma organização existente pode convidar por e-mail — cobre
  quem ainda não recebeu convite e não tem link para colar.

## Em aberto

- [ ] A tela `/welcome` não tem Header, logo não tem "sair". Se virar problema,
  tratar em card separado.
