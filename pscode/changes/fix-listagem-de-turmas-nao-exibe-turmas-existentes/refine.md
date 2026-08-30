# Listagens de turmas e disciplinas: falha visível em vez de lista vazia

## Summary

As listagens de Turmas e Disciplinas não sabem dizer que falharam: quando a
requisição dá erro, a tela desenha uma tabela vazia, sem mensagem nenhuma. Foi
isso que apareceu como "não exibe as turmas existentes". A causa original — o
token sem organização — já foi corrigida; falta corrigir a tela, que hoje
esconde qualquer falha e faz uma regressão parecer simplesmente "não há dados".

## Technical detail

Reproduzido com a stack local (API + web + MySQL), como ADMIN_ORG:

- **O caminho feliz funciona hoje.** `GET /classrooms` e `GET /subjects`
  devolvem 200 com os registros, e as duas telas renderizam normalmente. As
  queries em `ClassroomRepositoryImpl:39` e `SubjectRepositoryImpl:38` filtram
  por `organization_id` + `deleted_at IS NULL` corretamente. O bug relatado em
  31/07 foi resolvido pelo trabalho de token org-scoped (#98/#112/#120) —
  `useCreateOrganization.ts:17` já troca o token após criar a organização.
- **O modo de falha silenciosa é real.** Com um token válido sem o claim `org`,
  ambos os endpoints respondem **403** (verificado via curl). No front,
  `ClassroomListPage.tsx:18` e `SubjectListPage.tsx:22` consomem apenas
  `isLoading` e `data`, nunca `isError`. Em erro, `data` é `undefined`, o teste
  `?.length === 0` dá falso, cai no ramo da tabela e `?.map` não rende linha
  alguma: **tabela vazia, sem "Nenhuma turma encontrada" e sem erro**.
- **`classroomSchema` é frágil.** Em `classroom-api.ts:9`, `description`,
  `inviteCode`, `organizationId` e `createdAt` não têm `.default()`; um campo
  ausente derruba o `z.array().parse()` inteiro e cai no mesmo buraco silencioso.
  O `subjectSchema` equivalente já é defensivo — é a referência a seguir.

Fecha #74 e #75 juntas: mesma tela, mesmo defeito, mesma correção.

## Scope

### In
- Estado de erro visível (mensagem + ação de tentar de novo) em
  `ClassroomListPage` e `SubjectListPage`.
- Estado vazio exibido só quando a query teve sucesso e voltou vazia.
- `classroomSchema` defensivo, alinhado ao `subjectSchema`.
- Testes cobrindo os três estados nas duas telas.

### Out
- Redesenhar as listagens, filtros, busca, paginação ou novas colunas.
- Back-end: nenhuma mudança — as queries e o `organization_id` do JWT estão
  corretos.
- As demais telas com o mesmo padrão (`StudentTaskListPage`,
  `ClassroomDetailPage`, `SubjectDetailPage`) — registrar como follow-up.
- Corrigir o `infra/docker/api/Dockerfile.dev`, cuja imagem base não existe mais.

## Subtasks

- [ ] Criar `components/shared/ListErrorState.tsx` — mensagem de falha + botão
      "Tentar de novo", com teste unitário
- [ ] Consumir `isError`/`refetch` em `ClassroomListPage` e renderizar
      `ListErrorState`, mantendo o vazio só no sucesso
- [ ] Consumir `isError`/`refetch` em `SubjectListPage` do mesmo jeito
- [ ] Tornar `classroomSchema` defensivo em `classroom-api.ts`, espelhando o
      `subjectSchema`
- [ ] Testes das duas telas nos três estados: com dados, vazio e erro
- [ ] Rodar lint, type-check e a suíte do `apps/web`; validar as duas telas no
      browser
- [ ] Atualizar as specs `classroom-management` e `subject-management` e
      comentar na #75 que ela é fechada por esta change
