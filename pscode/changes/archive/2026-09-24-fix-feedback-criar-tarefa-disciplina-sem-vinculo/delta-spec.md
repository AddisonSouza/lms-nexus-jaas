# [fix] feedback ao criar tarefa em disciplina sem vínculo — Delta

## Added
- `TaskFormDialog` aceita a prop `error` e a exibe acima dos botões
  (`role="alert"`), no padrão do `AnnouncementForm`.
- Cenário "Recusa exibida no formulário" na spec `task-creation`.

## Changed
- Criação recusada: diálogo sem feedback → mensagem da API via
  `apiErrorMessage`, com o formulário mantido preenchido.
- `TASK_FORBIDDEN` na criação: texto genérico → "Você não leciona esta
  disciplina, então não pode criar tarefas nela." (override só nesta tela).
- Reabrir "Nova Tarefa" limpa a recusa anterior (`createTask.reset()`).
- Cenário "Professor não vinculado ao Subject" passa a cobrir gestor/admin e o
  feedback na tela.
