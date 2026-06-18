## Requirements

### Requirement: Indicadores do dashboard do aluno
O sistema SHALL retornar, para o aluno autenticado, os seguintes indicadores agregados: lista das próximas tarefas pendentes (publicadas, sem submissão do aluno) ordenada por `deadline` ascendente (mais urgente primeiro); contagem de tarefas entregues vs pendentes nas turmas em que o aluno está matriculado; lista das últimas notas e feedbacks recebidos (submissões avaliadas, ordenadas pela mais recente primeiro); e média geral de notas por disciplina, calculada apenas sobre submissões avaliadas do aluno.

#### Scenario: Consulta bem-sucedida do dashboard do aluno
- **WHEN** `ALUNO` autenticado chama `GET /students/me/dashboard`
- **THEN** sistema retorna 200 com a lista de próximas tarefas pendentes ordenada por urgência, a contagem de entregues vs pendentes, as últimas notas/feedbacks e a média de notas por disciplina

#### Scenario: Aluno sem nenhuma tarefa publicada nas suas turmas
- **WHEN** o aluno não possui nenhuma tarefa publicada nas turmas em que está matriculado
- **THEN** sistema retorna 200 com a lista de próximas tarefas vazia, contagem de entregues e pendentes igual a zero, últimas notas vazias e média por disciplina vazia, sem erro

### Requirement: Próximas tarefas ordenadas por urgência de prazo
O sistema SHALL listar apenas tarefas com status `PUBLISHED` para as quais o aluno autenticado não possui submissão registrada, ordenadas pelo campo `deadline` em ordem ascendente (a mais próxima do vencimento aparece primeiro).

#### Scenario: Existem tarefas pendentes com prazos diferentes
- **WHEN** o aluno possui múltiplas tarefas publicadas sem submissão, com deadlines distintos
- **THEN** sistema retorna a lista ordenada da tarefa com deadline mais próximo para a mais distante

#### Scenario: Tarefa já submetida não aparece como pendente
- **WHEN** o aluno já possui uma submissão registrada para uma tarefa publicada
- **THEN** essa tarefa não aparece na lista de próximas tarefas pendentes

### Requirement: Média geral de notas por disciplina
O sistema SHALL calcular a média geral de notas do aluno autenticado agrupada por disciplina, considerando exclusivamente submissões com status `EVALUATED`.

#### Scenario: Aluno com submissões avaliadas em mais de uma disciplina
- **WHEN** o aluno possui submissões avaliadas em duas ou mais disciplinas distintas
- **THEN** sistema retorna uma média por disciplina, calculada apenas com as submissões avaliadas daquela disciplina

#### Scenario: Disciplina sem nenhuma submissão avaliada do aluno
- **WHEN** o aluno não possui nenhuma submissão avaliada em uma disciplina das suas turmas
- **THEN** essa disciplina não aparece na lista de médias por disciplina, sem erro

### Requirement: Restrição de acesso aos próprios dados do aluno
O sistema SHALL restringir o acesso ao dashboard exclusivamente a usuários com papel `ALUNO`, retornando apenas dados associados ao próprio `userId` extraído do JWT (`sub`), nunca de outro aluno.

#### Scenario: Usuário sem papel ALUNO tenta acessar
- **WHEN** um usuário com papel `ADMIN_ORG`, `GESTOR` ou `PROFESSOR` chama `GET /students/me/dashboard`
- **THEN** sistema retorna 403

#### Scenario: Aluno autenticado só vê os próprios dados
- **WHEN** dois alunos distintos chamam `GET /students/me/dashboard` em momentos diferentes
- **THEN** cada um recebe exclusivamente os dados das tarefas, submissões e notas associados ao seu próprio `userId`

### Requirement: Isolamento por organização (multi-tenant)
O sistema SHALL filtrar os dados do dashboard do aluno exclusivamente por turmas e disciplinas pertencentes à `organization_id` extraída do JWT do usuário autenticado.

#### Scenario: Aluno matriculado em turmas de uma única organização
- **WHEN** `ALUNO` autenticado chama `GET /students/me/dashboard`
- **THEN** sistema retorna apenas dados de turmas e disciplinas da `organization_id` do JWT, sem expor dados de outras organizações
