# Grill Me

- [x] Qual das duas saídas do card implementar? — **Abrir `GET /subjects` ao ALUNO**,
  filtrado pelas turmas de que ele é membro, + "Disciplinas" na sidebar. Sem
  endpoint novo; o painel na página da turma fica fora.
- [x] Corrigir junto o vazamento de `GET /subjects/{id}/topics` (sem checagem de
  matrícula, diferente de `/contents`)? — **Sim**, mesma checagem
  `isMemberOfAnyClassroom` no `ListTopicsService`.
- [x] Como o `ListErrorState` trata o 403? — **Mensagem de permissão e sem o botão
  "Tentar de novo"**; demais erros mantêm texto e botão atuais.
- [x] Mostrar o nome da disciplina na `SubjectDetailPage`? — **Sim**, via
  `useSubject(id)`, agora que `GET /subjects/{id}` abre para o ALUNO.
