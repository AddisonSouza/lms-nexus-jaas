# Aluno não vê tarefas em Minhas Tarefas (schema my-grades) — Delta

## Changed

- **O schema de `my-grades` não exige mais `updatedAt`.** `taskWithGradeSchema`
  era derivado de `taskSchema.extend(...)` e herdava `updatedAt:
  z.string().nullable()` — obrigatório, já que `nullable` não é `optional`.
  `TaskWithGradeResponse` nunca envia o campo, então todo parse falhava, o
  `useQuery` entrava em erro e a tela caía no fallback `data = []`: o aluno via
  "Nenhuma tarefa disponível" com 5 tarefas publicadas. Agora o schema usa
  `taskSchema.omit({ updatedAt: true })`.
- **A resposta de submissão aceita `createdAt` nulo.** O `PUT
  /tasks/{id}/submissions/{submissionId}` responde 200 mas sem `createdAt`
  (o registro mantém `created_at` no banco — só a resposta o descarta), e
  `submissionSchema` exigia string. O parse lançava `ZodError`, a mutation
  entrava em erro e o diálogo de edição ficava aberto sem feedback. `createdAt`
  passa a ser `string | null` no schema e no tipo `TaskSubmission`; nenhum
  componente lê o campo.
- **Falha de carga deixa de parecer lista vazia.** `StudentTaskListPage`
  ignorava `isError`; agora renderiza `ListErrorState` com retry, como
  `SubjectListPage`.
- **`useEditSubmission` invalida também `myGrades()`.** Invalidava só
  `byTask()`, então a lista do aluno seguia mostrando a submissão antiga depois
  de uma edição.

## Added

- **Botão "Editar resposta"** na lista do aluno, visível apenas quando
  `submission.status === 'SUBMITTED'` e o prazo não expirou. Reaproveita
  `SubmissionFormDialog` num modo `edit`: título "Editar Resposta", botão
  "Salvar Resposta" e aviso de que a nova resposta substitui a anterior,
  inclusive os anexos. O campo abre vazio porque o summary de `my-grades` não
  traz `textResponse`.
- Testes: parse de `my-grades` com payload real (e o caso que falha sem o
  `omit`), parse da resposta do `PUT` sem `createdAt`, página em erro com
  retry, e o botão de edição visível só com `SUBMITTED` + prazo aberto.
- Specs vivas `student-grades` e `task-submission`: cenários da tela do aluno.

## Unchanged

- Back-end, contrato e spec de `GET /tasks/my-grades` — a correção é toda no
  front, conforme decidido no refino.
- A edição não pré-preenche o texto anterior: exigiria `textResponse` no
  summary de `my-grades`.
- Status `CLOSED` após o prazo segue pendente no card #271.
