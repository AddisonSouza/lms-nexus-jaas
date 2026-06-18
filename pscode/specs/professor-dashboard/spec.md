## Requirements

### Requirement: Indicadores do dashboard do professor por disciplina
O sistema SHALL retornar, para a disciplina informada, os seguintes indicadores agregados: contagem de submissões pendentes de avaliação (status `SUBMITTED`) em todas as tarefas da disciplina, distribuição de notas das submissões avaliadas da última tarefa (mais recente por data de criação), lista de alunos sem entrega nessa última tarefa, e média de notas por aluno na disciplina (considerando todas as submissões avaliadas).

#### Scenario: Consulta bem-sucedida do dashboard do professor
- **WHEN** `PROFESSOR` vinculado à disciplina chama `GET /subjects/{id}/dashboard`
- **THEN** sistema retorna 200 com a contagem de pendências, a distribuição de notas da última tarefa, os alunos sem entrega e a média de notas por aluno

#### Scenario: Disciplina sem nenhuma tarefa criada
- **WHEN** a disciplina não possui nenhuma tarefa cadastrada
- **THEN** sistema retorna 200 com contagem de pendências igual a zero, distribuição de notas vazia, lista de alunos sem entrega vazia e média de notas por aluno vazia, sem erro

#### Scenario: Última tarefa sem nenhuma submissão avaliada
- **WHEN** a última tarefa da disciplina não possui nenhuma submissão com status `EVALUATED`
- **THEN** a distribuição de notas dessa tarefa é retornada vazia, sem erro

### Requirement: Alunos sem entrega na última tarefa
O sistema SHALL identificar, para a última tarefa da disciplina (mais recente por data de criação), todos os alunos elegíveis (matriculados em turmas vinculadas à disciplina) que não possuem nenhuma submissão registrada para essa tarefa.

#### Scenario: Existem alunos sem entrega
- **WHEN** parte dos alunos elegíveis da última tarefa não possui submissão registrada
- **THEN** sistema retorna a lista desses alunos no dashboard

#### Scenario: Todos os alunos entregaram a última tarefa
- **WHEN** todos os alunos elegíveis da última tarefa possuem submissão registrada
- **THEN** sistema retorna a lista de alunos sem entrega como vazia, sem erro

### Requirement: Restrição de acesso ao professor vinculado à disciplina
O sistema SHALL restringir o acesso ao dashboard do professor exclusivamente a usuários com papel `PROFESSOR` que estejam vinculados à disciplina consultada (`subject_teachers`).

#### Scenario: Usuário sem papel PROFESSOR tenta acessar
- **WHEN** um usuário com papel `ADMIN_ORG`, `GESTOR` ou `ALUNO` chama `GET /subjects/{id}/dashboard`
- **THEN** sistema retorna 403

#### Scenario: Professor não vinculado à disciplina tenta acessar
- **WHEN** um usuário com papel `PROFESSOR` que não está vinculado à disciplina chama `GET /subjects/{id}/dashboard`
- **THEN** sistema retorna 403

### Requirement: Isolamento por organização (multi-tenant)
O sistema SHALL filtrar os dados do dashboard do professor exclusivamente por disciplinas pertencentes à `organization_id` extraída do JWT do usuário autenticado.

#### Scenario: Tentativa de acessar dashboard de disciplina de outra organização
- **WHEN** `PROFESSOR` autenticado chama `GET /subjects/{id}/dashboard` para uma disciplina de outra organização
- **THEN** sistema retorna 403 e nenhum dado de outra organização é exposto
