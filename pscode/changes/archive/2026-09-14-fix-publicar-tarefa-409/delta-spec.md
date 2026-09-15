# [fix] publicar tarefa retorna 409 mesmo tendo publicado — Delta

## Changed

- **`PATCH /tasks/{id}/publish` devolve a tarefa persistida por inteiro.** Antes
  a resposta vinha com `createdAt: null` (o `save()` devolvia a entidade mesclada,
  cujos campos de auditoria o mapper deixa a cargo do JPA); agora
  `TaskRepositoryImpl.save()` relê o estado gravado, então `createdAt` e
  `updatedAt` sempre vêm preenchidos. Vale para qualquer update de tarefa.
- **A lista de tarefas reflete a publicação.** Como o front rejeitava a resposta
  inválida, a mutation caía em erro e a lista nunca era invalidada — a tarefa
  seguia como `DRAFT` com o botão Publicar, e o clique seguinte trazia
  `409 Cannot transition task from PUBLISHED to PUBLISHED`. Agora a tarefa passa a
  `PUBLISHED` e o botão some.

## Added

- **Falha de publicação é visível**: mensagem de erro na lista de tarefas, com
  texto próprio para `409` (já publicada) e `403` (sem permissão). Antes a falha
  era silenciosa.
- **A lista resincroniza mesmo quando a publicação falha** (`onSettled` no
  `usePublishTask`), para nenhum erro deixar status velho na tela.
