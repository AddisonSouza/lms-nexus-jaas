### Requirement: Aluno lista tarefas publicadas
O sistema SHALL retornar todas as tarefas com status `PUBLISHED` da organização do aluno autenticado.

#### Scenario: Listagem bem-sucedida
- **WHEN** aluno autenticado chama `GET /tasks/published`
- **THEN** sistema retorna 200 com lista de tarefas publicadas da organização extraída do JWT

#### Scenario: Organização sem tarefas publicadas
- **WHEN** aluno chama `GET /tasks/published` e não há tarefas publicadas
- **THEN** sistema retorna 200 com lista vazia

---

### Requirement: Aluno cria submissão dentro do prazo
O sistema SHALL aceitar uma submissão com texto e/ou arquivos enquanto `LocalDateTime.now()` for menor ou igual ao deadline da tarefa. Ao menos um dos campos (texto ou arquivo) MUST ser fornecido.

#### Scenario: Submissão com texto
- **WHEN** aluno chama `POST /tasks/{id}/submissions` com campo `text` preenchido e prazo não expirado
- **THEN** sistema persiste `TaskSubmission` com status `SUBMITTED`, publica `TaskSubmittedEvent` e retorna 201

#### Scenario: Submissão com arquivo
- **WHEN** aluno chama `POST /tasks/{id}/submissions` com arquivo multipart e prazo não expirado
- **THEN** sistema armazena arquivo via `StoragePort`, persiste `TaskSubmission` com `SubmissionAttachment`, publica `TaskSubmittedEvent` e retorna 201

#### Scenario: Submissão com texto e arquivo
- **WHEN** aluno chama `POST /tasks/{id}/submissions` com texto e arquivo e prazo não expirado
- **THEN** sistema persiste submissão com texto e attachment, publica `TaskSubmittedEvent` e retorna 201

#### Scenario: Submissão sem texto nem arquivo
- **WHEN** aluno chama `POST /tasks/{id}/submissions` sem texto e sem arquivo
- **THEN** sistema retorna 422 com mensagem de erro

#### Scenario: Submissão após prazo
- **WHEN** aluno chama `POST /tasks/{id}/submissions` e `LocalDateTime.now()` é maior que `task.deadline`
- **THEN** sistema retorna 422 com mensagem indicando prazo expirado

#### Scenario: Tarefa não publicada
- **WHEN** aluno chama `POST /tasks/{id}/submissions` para tarefa com status diferente de `PUBLISHED`
- **THEN** sistema retorna 422

#### Scenario: Aluno já tem submissão
- **WHEN** aluno chama `POST /tasks/{id}/submissions` e já possui submissão para essa tarefa
- **THEN** sistema retorna 409 (Conflict)

---

### Requirement: Aluno edita submissão antes do prazo
O sistema SHALL permitir que o aluno atualize texto e/ou arquivos de uma submissão existente enquanto o prazo não tiver expirado e o status for `SUBMITTED`.

#### Scenario: Edição bem-sucedida
- **WHEN** aluno chama `PUT /tasks/{id}/submissions/{submissionId}` com prazo não expirado e status `SUBMITTED`
- **THEN** sistema atualiza texto e/ou arquivos, publica `TaskSubmittedEvent` e retorna 200

#### Scenario: Edição após prazo
- **WHEN** aluno chama `PUT /tasks/{id}/submissions/{submissionId}` e prazo expirou
- **THEN** sistema retorna 422 com mensagem indicando prazo expirado

#### Scenario: Edição de submissão avaliada
- **WHEN** aluno chama `PUT /tasks/{id}/submissions/{submissionId}` e status é `EVALUATED`
- **THEN** sistema retorna 422

#### Scenario: Edição de submissão de outro aluno
- **WHEN** aluno chama `PUT /tasks/{id}/submissions/{submissionId}` e `submission.studentId != jwt.sub`
- **THEN** sistema retorna 403

---

### Requirement: Resposta do aluno inclui dados de avaliação
O sistema SHALL retornar `grade` (BigDecimal, nullable) e `feedback` (String, nullable) em todos os endpoints que retornam `TaskSubmission`, refletindo o resultado da avaliação quando `status == EVALUATED`.

#### Scenario: Submissão ainda não avaliada
- **WHEN** aluno ou professor acessa uma submissão com status `SUBMITTED`
- **THEN** resposta inclui `grade: null` e `feedback: null`

#### Scenario: Submissão avaliada
- **WHEN** aluno ou professor acessa uma submissão com status `EVALUATED`
- **THEN** resposta inclui `grade` e `feedback` preenchidos (feedback MUST ser não-nulo; grade pode ser null se tarefa não tiver pontuação)
