# feat: busca por nome ou e-mail ao adicionar membro à turma — Delta

## Added

- **Busca e paginação em `GET /organizations/{id}/members`.** Aceita `search`
  (nome **ou** e-mail, sem diferenciar maiúsculas, em qualquer posição), `page` e
  `size` (padrão 20, teto 100), e responde no envelope
  `{ content, totalElements, totalPages, number, size }`. É o **primeiro endpoint
  do projeto a implementar o padrão de paginação** que o `API_CONTRACT.md` já
  documentava. O filtro e a ordenação passaram para o SQL, com join cross-módulo
  até a entidade do `identity` por FQN — mesmo recurso que o `UserDirectoryAdapter`
  já usava.
- **`shared/domain/Page`** — o envelope como record, com `Page.of` calculando
  `totalPages`.
- **`components/ui/combobox.tsx`** sobre `@base-ui/react`. O design system não
  tinha combobox, autocomplete nem command. O filtro embutido nasce **desligado**
  (`filter={null}`): as listas chegam filtradas da API, e filtrar de novo no
  cliente esconderia linhas que a busca acabou de devolver.
- **`hooks/useDebouncedValue`** — fora de `features/`, porque `classroom` e
  `curriculum` consomem, e uma feature não importa da outra.
- **`lib/pagination`** com `pageSchema(item)`, validando o envelope uma vez para
  toda listagem.
- **`useOrgMemberSearch` (classroom) e `useTeacherSearch` (curriculum)**, cada um
  com o client da própria feature. O `useTeacherCandidates` fica intacto: a página
  da disciplina ainda quer a lista inteira, não uma busca.
- **Marcação "já na turma"** — quem já é membro aparece na busca, rotulado e
  desabilitado. Sumir da lista faria o usuário procurar alguém e não entender por
  que não acha.
- **Requirement da spec `member-invitations`** para o listing de membros, que
  nenhuma requirement cobria.
- Testes: 6 no repositório, 6 no use case, 5 no resource, 3 no `pageSchema`,
  4 no `useDebouncedValue`, 2 no combobox, e os dois diálogos reescritos.

## Changed

- **`AddMemberDialog`** troca o campo de UUID por busca. O Zod nem validava
  formato: bastava um texto qualquer. Agora o `userId` vem da escolha na lista.
- **`AssignTeacherDialog`** troca o `<select>` nativo pelo mesmo combobox.
- **Os dois diálogos limpam-se ao fechar.** Os painéis fecham alterando a prop
  `open` no sucesso, nunca pelo Cancelar, então a limpeza não rodava: reabrir
  trazia a pessoa recém-adicionada e abria a lista **já filtrada pelo nome dela**,
  escondendo quem ainda podia ser escolhido. Achado na validação no navegador.
- **`organization-api` e `org-member-api`** parseiam o envelope; os hooks seguem
  entregando array às telas via `select`, então `OrganizationMembersPage` e
  `SubjectDetailPage` não mudaram.
- **A ordenação por nome saiu da memória** (`Comparator` no use case) e passou ao
  banco. Ordenar depois de paginar quebraria a página.

## Removed

- **O caminho não paginado**, órfão assim que a resource migrou: `execute(String)`
  na porta de entrada e no serviço, `findActiveMembersByOrganization` na porta de
  saída e no impl, e os 6 testes unitários que cobriam a ordenação em memória.

## Conhecido, fora deste card

- **Os testes de integração rodam contra o MySQL de dev**, não Testcontainers,
  contrariando o `DECISIONS.md`. Pré-existente; card próprio.
- **O filtro `canTeach` age sobre uma página.** Com muitos membros, um professor
  além da primeira página não aparece no diálogo — a API não filtra por papel.
- **Cada seleção no combobox dispara uma requisição extra**: o base-ui escreve o
  rótulo no campo e isso conta como digitação. Cacheada, mas é desperdício.
- **`POST /classrooms/{id}/members` espera `userId` e `POST /subjects/{id}/teachers`
  espera `memberId`.** Os dois contratos seguem divergentes; unificar não entrou.
- Convidar de fora da organização, alterar papéis, adição em lote e `sort`
  configurável continuam fora.
