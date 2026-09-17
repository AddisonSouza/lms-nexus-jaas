# fix: tarefa não fecha após o prazo — Delta

## Added
- **`Task.effectiveStatus()`** no domínio: uma tarefa `PUBLISHED` cujo prazo já
  passou é lida como `CLOSED`. Derivação na leitura — sem job agendado, sem
  `quarkus-scheduler`, sem migration. O banco continua gravando `PUBLISHED`, e
  um teste de integração trava esse invariante.
- **Badge "Encerrada"** na lista do aluno (`StudentTaskListPage`), ao lado do
  badge de entrega, quando a API reporta `CLOSED`.
- **`TASK_STATUS_LABEL`** na lista do professor: Rascunho / Publicada /
  Encerrada / Avaliada.

## Changed
- **`GET /tasks`, `GET /tasks/published` e `GET /tasks/my-grades`** passam a
  reportar o status efetivo em vez do armazenado. Como o mapeamento é
  compartilhado, as respostas de criar e publicar também o reportam — publicar
  uma tarefa já vencida devolve `CLOSED`.
- **`findPublishedByOrganization`** aceita `PUBLISHED` e `CLOSED`. Sem isso a
  tarefa vencida sumiria das listas do aluno e levaria junto a nota já recebida
  (RF-14).
- **Ordem das validações em `SubmitTaskService`**: o prazo é checado antes do
  estado, preservando o `422 DEADLINE_EXPIRED` do `API_CONTRACT.md`. A checagem
  de estado segue lendo o status armazenado, onde só interessam `DRAFT` e
  `GRADED`.
- **"Ver Submissões"** deixa de exigir `PUBLISHED` e aceita `CLOSED` também —
  é depois do prazo que o professor precisa alcançar as respostas para avaliar.
- A lista do professor deixa de exibir o enum cru (`PUBLISHED`).

## Removed
- Nada.
