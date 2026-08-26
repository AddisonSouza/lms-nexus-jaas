## Summary

Quem pertence a mais de uma organização hoje não tem como trocar de contexto pela
UI, e quem já tem uma não tem porta para fundar outra. O topo da sidebar passa a
mostrar a organização ativa e o papel do usuário nela, e a abrir a lista "Suas
organizações" — trocar de organização reemite o token e recarrega o app no
contexto novo; ao pé da lista, um atalho para criar outra.

## Technical detail

**Back-end — `GET /organizations`**

- Já existe `OrganizationMemberRepositoryImpl.findOrganizationsByUser(userId)`,
  mas ela serve à `OrganizationMemberLookupPort` do módulo `identity` (login e
  refresh escolhem a org padrão) e devolve só `organizationId` + `role`, sem o
  nome. Não é reaproveitada: entra um método novo na porta do próprio módulo,
  `OrganizationMemberRepository`, com JPQL unindo `OrganizationMemberJpaEntity` e
  `OrganizationJpaEntity` e filtrando `deletedAt IS NULL` nos dois lados.
- Use case `ListUserOrganizationsUseCase` (`domain/port/in/`) +
  `ListUserOrganizationsService` (`application/usecase/`), devolvendo
  `UserOrganizationResponse(id, name, role)` — só o que o seletor desenha.
  MapStruct faz o mapeamento da projeção para o DTO.
- Endpoint `@GET @Authenticated` em `OrganizationResource`, escopado pelo `sub`
  do JWT. Não é o caso da regra do `organization_id` vindo do token: a lista é do
  usuário, não de uma organização.
- `POST /auth/switch-organization` já existe e já responde `403` para não-membro;
  nada muda nele.

**Front-end**

- `organization-api.ts` ganha `listOrganizations()` com schema Zod, e recebe o
  `switchOrganization()` que hoje está inline em `useCreateOrganization.ts` como
  `api.post('/auth/switch-organization', ...)` — as duas chamadas passam a usar a
  mesma função.
- Hooks: `useOrganizations` (TanStack Query) e `useSwitchOrganization`. No sucesso
  da troca: `setToken` com o access token novo (é ele que atualiza `role` e
  `organizationId` no `authStore`), `queryClient.clear()` — todo o server state
  em cache pertence à organização anterior — e `navigate('/', { replace: true })`,
  deixando o `RootRedirect` decidir o destino pelo papel novo.
- `OrganizationSwitcher` vive em `features/organization/components/` e é
  importado pela `Sidebar`, seguindo o precedente do `Header` com o
  `NotificationBell`. Usa os primitivos `Popover` e `Badge` já existentes; o
  círculo de iniciais repete o padrão `bg-accent text-accent-foreground`.
- Papéis exibidos com rótulos neutros — `Administrador`, `Gestor`, `Professor`,
  `Aluno`. O protótipo usa formas com gênero ("Gestora", "Aluna") que não se
  sustentam sem saber o gênero do usuário.
- Sem organização o seletor continua visível, com estado vazio e apenas o
  "Criar organização" — é a única porta para criar uma segunda organização.
- `CreateOrganizationPage`: o "Voltar" hoje aponta sempre para `/welcome`, cujo
  texto afirma que o usuário não pertence a nenhuma organização. Com o atalho
  novo, quem já tem organização chega ali por dentro do app, então o destino passa
  a depender de `organizationId`.

## Scope

### In

- `GET /organizations` devolvendo `id`, `name` e `role` das organizações do
  usuário autenticado, com teste de integração.
- Registro do endpoint no `API_CONTRACT.md`.
- `OrganizationSwitcher` no topo da sidebar: organização ativa com papel, lista
  "Suas organizações" (ativa marcada, demais com a tag do papel), atalho
  "Criar organização".
- Troca de organização: token novo, cache limpo e redirecionamento para `/`.
- "Voltar" contextual em `/organizations/new`.

### Out

- Tela de Membros e lista de convites pendentes (precisam de
  `GET /organizations/{id}/members` e `GET /organizations/{id}/invitations`).
- Link aberto de convite de organização — não existe no design nem no back-end;
  é decisão de produto à parte.
- Qualquer mudança no fluxo de convite por e-mail.
- `GET /organizations/{id}` (detalhe da organização).
- Rodapé de usuário na sidebar do protótipo: o app mantém o usuário no `Header`.

## Subtasks

- [x] [BE] Método de repositório, use case e endpoint `GET /organizations` (id, name, role), com teste de integração (#106)
- [x] [BE] Registrar o `GET /organizations` no `API_CONTRACT.md` (#107)
- [x] [FE] `listOrganizations` e `switchOrganization` em `organization-api.ts`, com os hooks `useOrganizations` e `useSwitchOrganization` (#108)
- [x] [FE] `OrganizationSwitcher` (pill + popover "Suas organizações" + criar organização) encaixado no topo da `Sidebar` (#109)
- [ ] [FE] "Voltar" contextual em `CreateOrganizationPage` (#110)
- [ ] [FE] Testes do switcher, dos hooks e do "Voltar"; rodar lint, typecheck e a suíte Vitest (#111)
