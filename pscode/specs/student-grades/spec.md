### Requirement: List tasks with grades
O sistema SHALL fornecer ao ALUNO um endpoint que retorna todas as tarefas publicadas da organização, cada uma acompanhada da própria submissão (se existir), incluindo nota, feedback, status, datas e indicação de atraso.

#### Scenario: Aluno lista tarefas com submissão avaliada
- **WHEN** ALUNO faz `GET /tasks/my-grades`
- **THEN** retorna 200 com lista de `TaskWithGradeResponse`; para tarefas com submissão avaliada, os campos `grade`, `feedback` e `lateSubmission` estão preenchidos

#### Scenario: Aluno lista tarefas sem submissão
- **WHEN** ALUNO faz `GET /tasks/my-grades` e não enviou resposta para uma tarefa
- **THEN** o item retornado traz `submission: null`

#### Scenario: Aluno lista tarefas com submissão pendente de avaliação
- **WHEN** ALUNO faz `GET /tasks/my-grades` e a submissão ainda não foi avaliada
- **THEN** `submission.grade` e `submission.feedback` retornam `null`; `submission.status` retorna `SUBMITTED`

#### Scenario: PROFESSOR não acessa o endpoint de notas do aluno
- **WHEN** PROFESSOR faz `GET /tasks/my-grades`
- **THEN** retorna 403

### Requirement: View submission feedback detail
O sistema SHALL fornecer ao ALUNO um endpoint `GET /submissions/{id}/feedback` que retorna o feedback detalhado de uma submissão específica, somente após avaliação do professor.

#### Scenario: Aluno visualiza feedback de submissão avaliada
- **WHEN** ALUNO faz `GET /submissions/{id}/feedback` para uma submissão com status `EVALUATED`
- **THEN** retorna 200 com `grade`, `feedback` e metadados da tarefa

#### Scenario: Feedback não disponível antes da avaliação
- **WHEN** ALUNO faz `GET /submissions/{id}/feedback` para uma submissão com status `SUBMITTED`
- **THEN** retorna 409 com mensagem indicando que a submissão ainda não foi avaliada

#### Scenario: Aluno não acessa feedback de outro aluno
- **WHEN** ALUNO faz `GET /submissions/{id}/feedback` para uma submissão de outro estudante
- **THEN** retorna 403

### Requirement: Late submission indicator
O sistema SHALL calcular e expor no DTO se a submissão foi entregue após o prazo da tarefa.

#### Scenario: Submissão dentro do prazo
- **WHEN** `submission.createdAt <= task.deadline`
- **THEN** `lateSubmission: false`

#### Scenario: Submissão fora do prazo
- **WHEN** `submission.createdAt > task.deadline`
- **THEN** `lateSubmission: true`
