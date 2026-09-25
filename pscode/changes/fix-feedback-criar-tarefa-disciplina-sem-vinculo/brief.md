# [fix] feedback ao criar tarefa em disciplina sem vínculo

## Objetivo
Quando um professor, gestor ou admin que não é responsável pela disciplina tenta criar uma tarefa, o back responde 403 e a tela não mostra nada. O usuário não fica sabendo que a tarefa não foi cadastrada.

## Comportamento esperado
A tela de criação mostra uma mensagem clara de que a tarefa não pôde ser cadastrada por falta de vínculo com a disciplina, mantém o formulário preenchido e não indica sucesso.

## Fora de escopo
- Mudar a regra de quem pode criar tarefa em cada disciplina
- Alterar o restante do fluxo de tarefas
