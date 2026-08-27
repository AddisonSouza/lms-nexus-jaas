# Seletor de organização na sidebar — Delta

## Added

- `GET /organizations` devolve as organizações do usuário autenticado — `id`,
  `name` e `role` em cada uma — escopado pelo `sub` do JWT, ordenado por nome.
  Vínculos e organizações com soft delete não aparecem. `401` sem autenticação.
- O topo da sidebar mostra a organização ativa com o papel do usuário nela e
  abre a lista "Suas organizações": a ativa marcada, as demais com a tag do
  papel. Rótulos neutros — `Administrador`, `Gestor`, `Professor`, `Aluno`.
- Escolher outra organização reemite o token, descarta todo o server state em
  cache (pertence ao contexto anterior) e recarrega o app em `/`, deixando o
  `RootRedirect` decidir o destino pelo papel novo.
- O pé da lista traz "Criar organização" — a porta para fundar uma segunda.
  Sem nenhuma organização, o seletor continua visível com estado vazio e só
  esse atalho.
- Nomes truncados na sidebar carregam o nome completo em tooltip.

## Changed

- Criar uma organização passa a descartar o cache junto com a troca de token:
  antes a lista em cache era a de antes da criação, e o seletor exibia
  "Sem organização" até que outra fosse escolhida.
- Enquanto a lista de organizações está em voo, o seletor mostra um rótulo de
  carregamento em vez de afirmar que não há organização.
- O "Voltar" de `/organizations/new` depende do `organizationId`: quem já tem
  organização volta ao painel dela; quem não tem continua indo para `/welcome`.
- `switchOrganization()` deixa de ser uma chamada inline no
  `useCreateOrganization` e passa a ser função compartilhada de
  `organization-api.ts`, usada também pela troca de organização.

## Known gap

- O login só coloca organização no token quando o usuário tem exatamente uma
  membership. Com duas ou mais ele cai em `/welcome`, tela sem sidebar — e
  portanto sem o seletor. Registrado em #113.
