# feat: destacar tarefas com respostas na lista do professor — Delta

## Added
- **Contadores de entrega em `GET /tasks`**: `submissionCount` (respostas
  recebidas) e `pendingEvaluationCount` (ainda em `SUBMITTED`). Tarefa sem
  nenhuma resposta vem com os dois em `0`, não omitida.
- **`SubmissionRepository.countByTasks(taskIds, orgId)`**: uma consulta agregada
  (`GROUP BY s.taskId`) para a lista inteira, respeitando o soft delete e a
  organização. Lista vazia não toca o banco — `IN ()` é erro de sintaxe em SQL.
  Tarefa sem submissão fica ausente do mapa; o default zero é de quem chama.
- **`TaskSummaryResponse`** e `TaskSummaryMapper` (MapStruct, duas fontes: a
  tarefa e suas contagens). DTO exclusivo do professor — `TaskResponse` segue
  servindo `GET /tasks/published`, que o aluno consome, e **não** expõe os
  contadores. Há teste de integração com token de aluno provando isso.
- **Badge "N a avaliar"** (`accent`) na lista do professor quando há pendência,
  com o total recebido ao lado como texto discreto. Sem pendência, sem badge;
  sem resposta nenhuma, sem texto. Singular com uma resposta só.
- **`GET /tasks` no `API_CONTRACT.md`**: a rota não estava documentada — só o
  `POST`. Entrou inteira: papéis, formato da resposta e a nota de que os
  contadores são exclusivos dela.

## Changed
- **`ListTasksUseCase`** passa a devolver `List<TaskSummaryResponse>`;
  `TaskResource.list()` acompanha. `ListTasksService` ganhou a dependência de
  `SubmissionRepository` e faz uma chamada de contagem por listagem, não uma
  por tarefa.
- **`listTasks` no front** valida com `taskSummarySchema`; `createTask` e
  `publishTask` seguem no `taskSchema`, espelhando o back-end.

## Removed
- Nada.
