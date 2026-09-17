# Questions

- [x] **Onde os contadores entram no contrato?** DTO próprio do professor
  (`TaskSummaryResponse`) no `GET /tasks`. `TaskResponse` é compartilhado com
  `GET /tasks/published`, que o aluno consome — os contadores vazariam.
- [x] **Como contar?** Uma query agregada `GROUP BY s.taskId` em
  `SubmissionRepository.countByTasks(taskIds, orgId)`. Sem N+1.
- [x] **Mostrar quantos alunos não entregaram?** Não. O total de alunos vive no
  módulo `classroom` e exigiria porta nova entre módulos.
- [x] **Destaque visual?** Badge `accent` "N a avaliar" ao lado do status, e o
  total recebido como texto discreto. Sem reordenar a lista.

## Notas do código
- Back-end tem só `SubmissionStatus.SUBMITTED` e `EVALUATED`; o `LATE` do tipo
  do front nunca chega. "Pendente" = `SUBMITTED`.
- `TaskListPage.tsx` também é tocado por #270 (erros 4xx) e #271 (status
  `CLOSED`) — quem entrar depois rebaseia.
