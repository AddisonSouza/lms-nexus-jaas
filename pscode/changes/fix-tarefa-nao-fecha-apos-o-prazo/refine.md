# fix: tarefa não fecha após o prazo

## Summary

Hoje uma tarefa com prazo vencido continua aparecendo como "PUBLISHED" para
professor e aluno — só a tentativa de envio é bloqueada. A tarefa passa a ser
apresentada como **Encerrada** assim que o prazo expira, nas telas dos dois
papéis, sem deixar de mostrar a nota já recebida.

## Technical detail

- **Derivação na leitura, não persistência.** `Task.effectiveStatus()` no domínio
  retorna `CLOSED` quando `status == PUBLISHED` e `deadline` já passou; caso
  contrário devolve o `status` armazenado. Sem `quarkus-scheduler`, sem job, sem
  migration — o banco segue guardando `PUBLISHED`.
- Os três use cases de leitura (`ListTasksService`, `ListPublishedTasksService`,
  `ListStudentGradesService`) passam a expor `effectiveStatus()` no
  `TaskResponse` / `TaskWithGradeResponse`.
- `TaskRepositoryImpl.findPublishedByOrganization` hoje filtra
  `status = 'PUBLISHED'` no JPQL; passa a `status IN ('PUBLISHED','CLOSED')`,
  senão a tarefa vencida sumiria de `/tasks/published` e `/tasks/my-grades` e o
  aluno perderia acesso à própria nota (RF-14).
- `SubmitTaskService`: a checagem de prazo (`DeadlineExpiredException`, 422)
  passa a vir **antes** da checagem de status, preservando o código de erro
  documentado no `API_CONTRACT.md`. O `PublishTaskService` continua olhando o
  `status` armazenado — publicar não muda.
- Front: mapa único `TASK_STATUS_LABEL` (Rascunho / Publicada / Encerrada /
  Avaliada) em `TaskListPage`, hoje renderizando o enum cru; e badge extra
  "Encerrada" em `StudentTaskListPage`, ao lado do badge de submissão.

## Scope

### In
- `CLOSED` derivado do prazo em `GET /tasks`, `/tasks/published` e
  `/tasks/my-grades`.
- Badge "Encerrada" na lista do professor e na do aluno.
- Ordem das validações em `SubmitTaskService` (mantendo 422).

### Out
- Transição para `GRADED`.
- Reabertura/edição de prazo.
- Persistir `CLOSED` no banco (job agendado ou escrita em GET).
- Bloquear a edição de submissão (`PUT .../submissions/{id}`) — comportamento
  atual mantido.

## Subtasks
- [x] Adicionar `Task.effectiveStatus()` no domínio + teste unitário cobrindo prazo vencido, prazo futuro e DRAFT/GRADED
- [x] Expor `effectiveStatus()` nos três use cases de leitura (`ListTasksService`, `ListPublishedTasksService`, `ListStudentGradesService`)
- [x] Ampliar `findPublishedByOrganization` para `status IN ('PUBLISHED','CLOSED')` mantendo a tarefa vencida visível ao aluno
- [x] Inverter a ordem das checagens em `SubmitTaskService` para preservar o 422 `DeadlineExpired`
- [x] Teste de integração (Testcontainers) cobrindo tarefa vencida retornando `CLOSED` nos três endpoints
- [x] Front professor: mapa `TASK_STATUS_LABEL` no badge de `TaskListPage`
- [ ] Front aluno: badge "Encerrada" em `StudentTaskListPage` quando `status === 'CLOSED'`
