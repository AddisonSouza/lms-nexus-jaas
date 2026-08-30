# Listagens de turmas e disciplinas relatam falha — Delta

## Changed

- **As listagens de Turmas e Disciplinas distinguem falha de lista vazia.**
  Antes consumiam apenas `isLoading` e `data`, nunca `isError`. Numa requisição
  com erro `data` fica `undefined`, o teste `?.length === 0` dá falso e a tela
  caía no ramo da tabela sem renderizar linha alguma: tabela vazia, sem
  mensagem, indistinguível de uma organização sem turmas. Era isso que os
  relatos #74 e #75 descreviam como "a listagem não exibe os registros
  existentes". Agora o ramo de erro vem antes do teste de vazio, e o estado
  vazio só aparece quando a requisição teve **sucesso** e voltou vazia.
- **`classroomSchema` passa a ser defensivo**, espelhando o `subjectSchema`: só
  `id` e `name` seguem obrigatórios. Um campo ausente na resposta degrada aquela
  célula em vez de derrubar o `z.array().parse()` da lista inteira — que a tela
  mostraria como "nenhuma turma".

## Added

- `components/shared/ListErrorState.tsx`: estado de falha das listagens —
  `role="alert"`, mensagem nomeando o que não pôde ser carregado e ação
  "Tentar de novo", desabilitada enquanto o retry está em voo.
- Specs `classroom-management` e `subject-management` ganham o Requirement
  *"list reports load failures"*, com os cenários de falha, retry, vazio real e
  (em turmas) resposta com campos opcionais ausentes.
- 11 testes: `ListErrorState`, `ClassroomListPage` e `SubjectListPage`, cada
  tela nos três estados. Removendo o ramo `isError`, 2 dos 4 testes de cada tela
  falham.

## Unchanged

- **Back-end intacto.** As queries filtram por `organization_id` e
  `deleted_at IS NULL` corretamente; o `organization_id` continua vindo do JWT.
  A causa original do relato — token sem o claim `org`, que a API responde com
  **403** — já havia sido resolvida pelo trabalho de token org-scoped
  (#98/#112/#120), e o caminho feliz foi verificado na stack local.
- Com dados já em cache, uma navegação que não refaz o fetch mantém a lista na
  tela em vez de virar erro — comportamento desejado do TanStack Query.
- Um reload duro com a API totalmente fora continua levando a `/login`: o
  refresh silencioso do boot falha e o app desloga. É outro caminho, que não
  passa por esta tela.
- Ficaram de fora, com o mesmo padrão a corrigir depois: `StudentTaskListPage`,
  `ClassroomDetailPage` e `SubjectDetailPage`.
