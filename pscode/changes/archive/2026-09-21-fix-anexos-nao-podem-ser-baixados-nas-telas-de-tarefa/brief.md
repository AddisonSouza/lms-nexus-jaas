# [fix] anexos não podem ser baixados nas telas de tarefa

## Objetivo
Tornar os anexos realmente baixáveis nas telas de tarefa. O endpoint
`GET /api/files/{fileKey}` existe, mas é `@RolesAllowed` e exige
`Authorization: Bearer` — o front ou não gera link, ou gera um `<a href>` que
não carrega o token.

## Comportamento esperado
- Aluno vê e baixa os anexos da tarefa em Minhas Tarefas (hoje
  `StudentTaskListPage` não renderiza `task.attachments`).
- Professor baixa o anexo da submissão em `EvaluationDialog` e
  `SubmissionListDrawer`, que hoje exibem apenas `originalName` como texto.
- O download leva o JWT e preserva o nome original do arquivo.

## Fora do escopo
- Upload e pré-visualização no navegador.
- Nome do aluno no drawer de submissões (item separado da bateria E2E).

## A confirmar no refino
- O mesmo defeito no link de conteúdo da disciplina (`TopicList`/`ContentCard`)
  entra neste card ou vira outro?
