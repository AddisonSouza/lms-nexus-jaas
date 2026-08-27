## Summary

Quem pertence a mais de uma organização não consegue entrar: o login só coloca
organização no token quando há exatamente um vínculo, então o multi-organização
cai em `/welcome` — tela sem sidebar, e portanto sem o seletor. O login passa a
entrar na primeira organização por nome, e a sessão passa a lembrar em qual
organização está, para que um reload não desfaça a troca.

## Technical detail

**Escolha da organização no login**

- `AuthenticateService:50-52` usa `memberships.size() == 1` para decidir. Vira
  "tem algum vínculo": pega o primeiro da lista.
- Para "primeiro" significar algo estável, `findOrganizationsByUser` precisa de
  ordem. Hoje a JPQL em `OrganizationMemberRepositoryImpl` não ordena; ganha
  `JOIN OrganizationJpaEntity o ... ORDER BY o.name`, a mesma ordem que o
  seletor da sidebar desenha. A assinatura da porta não muda — continua
  devolvendo `OrgMembership(organizationId, role)`.
- A JPQL também passa a ignorar organização com soft delete, como já faz o
  `findUserOrganizations` do módulo `organization`.

**Organização da sessão no refresh**

- `RefreshTokenService:35-38` repete a mesma regra, então cada rotação de token
  — reload da página pelo `AuthBootstrap`, ou expiração do access token pelo
  interceptor do axios — devolve um token sem organização e derruba o usuário
  de volta para `/welcome`, desfazendo a troca.
- O Redis guarda hoje `rt:<token> → userId`. Passa a guardar também a
  organização da sessão: a porta `RefreshTokenRepository` ganha o
  `organizationId` no `save` e um `findSession` devolvendo usuário + organização
  (`findUserId` continua para quem só precisa do usuário).
- `SwitchOrganizationService` já conhece a organização de destino: grava o
  refresh token novo com ela. O login grava a organização que escolheu.
- No refresh, a organização da sessão é **revalidada** com
  `findRoleByUserAndOrg` antes de entrar no token novo — o vínculo pode ter sido
  removido no meio da sessão. Sem organização válida, cai na regra do login.

**Front-end**

- Nada muda. Com organização no token, o `RootRedirect` já leva ao destino do
  papel e o seletor já funciona.

## Scope

### In

- Login entra na primeira organização por nome quando o usuário tem qualquer
  vínculo.
- `findOrganizationsByUser` ordenada por nome e ignorando organização removida.
- Refresh preserva a organização da sessão, revalidando o vínculo.
- Testes de unidade dos três services e teste de integração do fluxo.

### Out

- Lembrar a última organização usada entre sessões (exigiria persistir a escolha
  no banco) — o login sempre entra pela primeira.
- Mudanças na `/welcome`, que continua servindo quem não tem organização alguma.
- Seletor da sidebar (#104, entregue), convites e criação de organização.

## Subtasks

- [x] [BE] Ordenar `findOrganizationsByUser` por nome da organização e ignorar organização removida
- [ ] [BE] Login entra na primeira organização quando o usuário tem qualquer vínculo
- [ ] [BE] Refresh token guarda a organização da sessão no Redis (porta, impl e o switch gravando o destino)
- [ ] [BE] Refresh reemite mantendo a organização da sessão, revalidando o vínculo
- [ ] [BE] Teste de integração do fluxo multi-organização (login → refresh → troca → refresh) e suíte da API
