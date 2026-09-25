# [fix] feedback ao criar tarefa em disciplina sem vínculo

## Summary
Hoje, quando a criação de uma tarefa é recusada (por exemplo, por quem não leciona a disciplina), o formulário simplesmente não fecha e nada é exibido. Com a correção, o motivo aparece dentro do próprio formulário, e o que foi digitado continua lá.

## Technical detail
- `POST /tasks` devolve 403 `TASK_FORBIDDEN` quando `existsByIdAndTeacher` falha (`CreateTaskService`). Outras recusas: `DEADLINE_NOT_IN_FUTURE`, tipo de arquivo, 422.
- `TaskListPage` só fecha o diálogo no `onSuccess` e só exibe o erro do *publish*; o `createTask.error` não é renderizado em lugar nenhum.
- `TaskFormDialog` ganha a prop `error?: string | null`, exibida acima do rodapé (`text-destructive`, `role="alert"`), no mesmo padrão do `AnnouncementForm`.
- `TaskListPage` passa `apiErrorMessage(createTask.error, { TASK_FORBIDDEN: 'Você não leciona esta disciplina, então não pode criar tarefas nela.' })` e chama `createTask.reset()` ao abrir o diálogo, para não herdar o erro anterior.
- O formulário já só é resetado ao abrir: numa falha, os campos continuam preenchidos.

## Scope
### In
- Erro de criação visível no diálogo, com override para `TASK_FORBIDDEN`
- Limpar o erro ao reabrir o diálogo
- Testes Vitest do `TaskFormDialog` e do `TaskListPage`
### Out
- Regra de quem cria tarefa e filtro do seletor de disciplinas
- Mudanças no back-end e no fluxo de publicar/submeter

## Subtasks
- [x] `TaskFormDialog`: prop `error` exibida no formulário + teste
- [ ] `TaskListPage`: repassar `apiErrorMessage` com override de `TASK_FORBIDDEN` e limpar o erro ao abrir + teste (403 mostra a mensagem e mantém o diálogo aberto)
