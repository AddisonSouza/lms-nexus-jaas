# feat: destacar tarefas com respostas na lista do professor

## Summary

Hoje o professor só descobre que uma tarefa recebeu respostas abrindo o drawer
de submissões, uma por uma. A lista de `/assessment/tasks` passa a mostrar
quantas respostas cada tarefa recebeu e quantas ainda faltam avaliar, com uma
badge de destaque nas que têm avaliação pendente.

## Technical detail

- **DTO próprio do professor.** `GET /tasks` passa a devolver
  `TaskSummaryResponse` (campos de `TaskResponse` + `submissionCount` e
  `pendingEvaluationCount`). `TaskResponse` continua servindo
  `GET /tasks/published`, que o **aluno** consome — pôr os contadores lá
  exporia à turma quantas respostas já entraram. Mapeamento via MapStruct.
- **Uma query agregada.** `SubmissionRepository.countByTasks(taskIds, orgId)`
  com `SELECT s.taskId, COUNT(s), SUM(CASE WHEN s.status = 'SUBMITTED' ...)`
  e `GROUP BY s.taskId`, respeitando `deleted_at IS NULL`. `ListTasksService`
  faz uma chamada para a lista inteira e casa o resultado por `taskId`;
  tarefa sem submissão vem zerada, não ausente.
- **Pendente = `SUBMITTED`.** O enum do back tem só `SUBMITTED` e `EVALUATED`
  (o `LATE` do tipo do front nunca chega da API).
- **Front.** `taskSummarySchema` em `api/tasks.ts` estende o schema atual com
  os dois inteiros; `TaskListPage` renderiza a badge `accent` "N a avaliar"
  ao lado do status e "N respostas" como texto discreto. Sem pendência, sem
  badge; sem resposta nenhuma, sem texto.
- **Conflito previsto.** `TaskListPage.tsx` também é alterado por #270 (erros
  4xx) e #271 (status `CLOSED`) — quem entrar depois rebaseia.

## Scope

### In
- `countByTasks` na porta `SubmissionRepository` + implementação JPQL agregada,
  com teste.
- `TaskSummaryResponse`, mapper e `ListTasksService` preenchendo os contadores.
- `GET /tasks` respondendo os dois campos; `API_CONTRACT.md` e spec
  `task-creation` atualizados.
- `TaskListPage`: badge "N a avaliar" e o total de respostas na linha.

### Out
- Notificações e qualquer mudança no fluxo de avaliação.
- Total de alunos da turma como denominador (exige porta para `classroom`).
- Contadores nas telas do aluno (`/tasks/published`, `my-grades`).
- Reordenar a lista por pendência.
- Nome do aluno e anexos no drawer de submissões (cards próprios).

## Subtasks
- [ ] BE: `countByTasks(taskIds, orgId)` na porta `SubmissionRepository` e a query agregada no `SubmissionRepositoryImpl`, com teste de integração (tarefa sem submissão, só pendentes, mistas, submissão removida)
- [x] BE: `TaskSummaryResponse` + mapper MapStruct, com `ListTasksService` casando os contadores por `taskId`
- [x] BE: `TaskResource.list()` devolvendo o novo DTO, com teste de integração do `GET /tasks`
- [ ] Docs: registrar `submissionCount` e `pendingEvaluationCount` no `API_CONTRACT.md` e na spec `task-creation`
- [ ] FE: `taskSummarySchema` e o tipo `TaskSummary` em `api/tasks.ts` / `types.ts`
- [ ] FE: badge "N a avaliar" e total de respostas em `TaskListPage`, com teste dos três casos (sem respostas, com pendentes, todas avaliadas)
- [ ] Rodar lint, type-check e as duas suítes; validar a lista no navegador como professor
