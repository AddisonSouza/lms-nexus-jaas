## ADDED Requirements

### Requirement: Criação automática de notificação por Domain Event
O sistema SHALL criar uma notificação in-app para cada destinatário resolvido, ao consumir os Domain Events `AnnouncementPostedEvent`, `TaskPublishedEvent`, `TaskSubmittedEvent` e `SubmissionEvaluatedEvent`. Cada notificação criada SHALL conter `title`, `message` e `action_link` (texto fixo por tipo, conforme tabela do design.md).

#### Scenario: Aviso publicado notifica os alunos da turma
- **WHEN** `AnnouncementPostedEvent` é publicado para uma turma com alunos vinculados
- **THEN** sistema cria uma notificação do tipo `ANNOUNCEMENT_POSTED` para cada ALUNO vinculado à turma, exceto o autor do aviso

#### Scenario: Tarefa publicada notifica os alunos das turmas da disciplina
- **WHEN** `TaskPublishedEvent` é publicado para uma disciplina vinculada a uma ou mais turmas
- **THEN** sistema cria uma notificação do tipo `TASK_PUBLISHED` para cada ALUNO de cada turma vinculada à disciplina

#### Scenario: Resposta enviada notifica os professores da disciplina
- **WHEN** `TaskSubmittedEvent` é publicado para uma tarefa de uma disciplina com professores vinculados
- **THEN** sistema cria uma notificação do tipo `TASK_SUBMITTED` para cada PROFESSOR vinculado à disciplina da tarefa

#### Scenario: Avaliação lançada notifica o aluno
- **WHEN** `SubmissionEvaluatedEvent` é publicado para a submissão de um aluno
- **THEN** sistema cria uma notificação do tipo `SUBMISSION_EVALUATED` para o aluno (`studentId`) do evento

#### Scenario: Evento sem destinatários resolvidos
- **WHEN** um evento é consumido e a resolução de destinatários retorna lista vazia (ex.: turma sem alunos)
- **THEN** sistema não cria nenhuma notificação e não lança erro

### Requirement: Contador de não lidas em Redis
O sistema SHALL manter um contador de notificações não lidas por usuário no Redis, incrementado a cada notificação criada e atualizado a cada leitura.

#### Scenario: Criação de notificação incrementa o contador
- **WHEN** uma notificação é criada para um usuário
- **THEN** o contador Redis `communication:unread-count:{userId}` é incrementado em 1

#### Scenario: Marcar como lida decrementa o contador
- **WHEN** uma notificação não lida é marcada como lida
- **THEN** o contador Redis do usuário é decrementado em 1, sem ficar negativo

#### Scenario: Marcar todas como lidas zera o contador
- **WHEN** o usuário marca todas as notificações como lidas
- **THEN** o contador Redis do usuário é zerado

### Requirement: Listagem de notificações do usuário autenticado
O sistema SHALL retornar as notificações do usuário autenticado ordenadas por data de criação decrescente, junto com o contador de não lidas.

#### Scenario: Listagem bem-sucedida
- **WHEN** usuário autenticado chama `GET /notifications`
- **THEN** sistema retorna 200 com a lista de notificações do usuário ordenada por `created_at` decrescente, o campo `unreadCount`, e cada item da lista contendo `title`, `message` e `actionLink`

#### Scenario: Usuário sem notificações
- **WHEN** usuário autenticado sem nenhuma notificação chama `GET /notifications`
- **THEN** sistema retorna 200 com lista vazia e `unreadCount` igual a 0

#### Scenario: Notificações de outro usuário não aparecem
- **WHEN** usuário autenticado chama `GET /notifications`
- **THEN** sistema retorna apenas notificações cujo `user_id` é o do usuário autenticado

### Requirement: Marcar notificação como lida
O sistema SHALL permitir que o destinatário de uma notificação a marque individualmente como lida.

#### Scenario: Marcação bem-sucedida
- **WHEN** o destinatário chama `PATCH /notifications/{id}/read` para uma notificação não lida sua
- **THEN** sistema define `read_at` e retorna 200

#### Scenario: Notificação de outro usuário
- **WHEN** um usuário chama `PATCH /notifications/{id}/read` para uma notificação que não é sua
- **THEN** sistema retorna 403

#### Scenario: Notificação inexistente
- **WHEN** usuário chama `PATCH /notifications/{id}/read` para um id inexistente
- **THEN** sistema retorna 404

#### Scenario: Notificação já lida
- **WHEN** o destinatário chama `PATCH /notifications/{id}/read` para uma notificação já lida
- **THEN** sistema retorna 200 sem alterar o contador novamente

### Requirement: Marcar todas as notificações como lidas
O sistema SHALL permitir que o usuário autenticado marque em lote todas as suas notificações não lidas como lidas.

#### Scenario: Marcação em lote bem-sucedida
- **WHEN** usuário autenticado com notificações não lidas chama `PATCH /notifications/read-all`
- **THEN** sistema define `read_at` em todas as notificações não lidas do usuário e retorna 200 com `unreadCount` igual a 0

#### Scenario: Usuário sem notificações não lidas
- **WHEN** usuário autenticado sem notificações não lidas chama `PATCH /notifications/read-all`
- **THEN** sistema retorna 200 com `unreadCount` igual a 0, sem erro
