## ADDED Requirements

### Requirement: Saúde das turmas da organização
O sistema SHALL retornar, para todas as turmas da organização do `GESTOR` autenticado, os indicadores de saúde de cada turma: status, percentual médio de entrega (média das taxas submissões/alunos elegíveis por tarefa) e média de notas das submissões avaliadas.

#### Scenario: Consulta bem-sucedida do dashboard do gestor
- **WHEN** `GESTOR` autenticado chama `GET /organizations/{id}/gestor-dashboard` para a própria organização
- **THEN** sistema retorna 200 com a lista de turmas da organização, cada uma com status, percentual de entrega e média de notas

#### Scenario: Turma sem nenhuma submissão avaliada
- **WHEN** uma turma da organização não possui nenhuma submissão com status `EVALUATED`
- **THEN** a média de notas dessa turma é retornada como ausente (`null`), sem erro

#### Scenario: Organização sem turmas
- **WHEN** a organização do `GESTOR` não possui nenhuma turma cadastrada
- **THEN** sistema retorna 200 com lista de turmas vazia, sem erro

### Requirement: Alunos com tarefas pendentes ou atrasadas por turma
O sistema SHALL identificar, para cada turma, os até 5 alunos com mais tarefas pendentes ou atrasadas, onde uma tarefa é considerada pendente/atrasada para um aluno quando o prazo (`deadline`) já passou e não existe submissão do aluno, ou a submissão existente foi criada após o prazo.

#### Scenario: Turma com alunos em risco
- **WHEN** uma turma possui tarefas com prazo expirado sem submissão de alguns alunos
- **THEN** sistema retorna, para essa turma, até 5 alunos ordenados pela quantidade de pendências/atrasos em ordem decrescente

#### Scenario: Turma sem nenhuma pendência
- **WHEN** todos os alunos de uma turma estão em dia com as tarefas (sem prazo expirado pendente)
- **THEN** sistema retorna a lista de alunos em risco dessa turma como vazia, sem erro

### Requirement: Isolamento por organização (multi-tenant)
O sistema SHALL filtrar os dados do dashboard do gestor exclusivamente pelo `organization_id` extraído do JWT do usuário autenticado, nunca por valor recebido na URL ou no corpo da requisição.

#### Scenario: Tentativa de acessar dashboard de outra organização
- **WHEN** `GESTOR` da organização A chama `GET /organizations/{id-da-organizacao-B}/gestor-dashboard`
- **THEN** sistema retorna 403 e nenhum dado de outra organização é exposto

### Requirement: Restrição de acesso ao papel GESTOR
O sistema SHALL restringir o acesso ao dashboard do gestor e à exportação em PDF exclusivamente a usuários com papel `GESTOR`.

#### Scenario: Usuário sem papel GESTOR tenta acessar
- **WHEN** um usuário com papel `ADMIN_ORG`, `PROFESSOR` ou `ALUNO` chama `GET /organizations/{id}/gestor-dashboard`
- **THEN** sistema retorna 403

### Requirement: Exportação do dashboard do gestor em PDF
O sistema SHALL gerar um arquivo PDF com as mesmas informações de saúde das turmas exibidas no dashboard do gestor, restrito ao mesmo controle de acesso (`GESTOR`, isolamento por `organization_id`).

#### Scenario: Exportação bem-sucedida
- **WHEN** `GESTOR` autenticado chama `GET /organizations/{id}/gestor-dashboard/pdf` para a própria organização
- **THEN** sistema retorna 200 com `Content-Type: application/pdf` contendo os indicadores de saúde das turmas

#### Scenario: Exportação sem permissão
- **WHEN** um usuário sem papel `GESTOR`, ou `GESTOR` de outra organização, chama o endpoint de exportação
- **THEN** sistema retorna 403
