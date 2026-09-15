# [fix] publicar tarefa retorna 409 mesmo tendo publicado

## Objetivo

Publicar uma tarefa deve terminar sem erro e a lista deve refletir o novo status.

## Comportamento observado

Criar a tarefa funciona. Ao clicar em **Publicar**, o `PATCH /tasks/{id}/publish`
responde `200` e a tarefa é publicada no banco — mas a lista continua mostrando
`DRAFT` e o botão Publicar. Um segundo clique traz
`409 {"error":"Cannot transition task from PUBLISHED to PUBLISHED"}`, sem
nenhuma mensagem na tela.

## Comportamento esperado

- Após publicar, a tarefa aparece como `PUBLISHED` e o botão Publicar some.
- Uma falha real de publicação mostra uma mensagem de erro ao professor.

## Fora de escopo

- Demais transições de status (`CLOSED`, `GRADED`) e outras telas.
