### Requirement: Métricas agregadas da organização por período
O sistema SHALL retornar, para um período informado (`from`/`to`), as métricas agregadas da organização do `ADMIN_ORG` autenticado: contagem de turmas ativas e arquivadas, contagem de membros por papel, contagem de tarefas criadas e avaliadas no período, e taxa média de entrega (submissões / total de alunos elegíveis) das tarefas do período.

#### Scenario: Consulta bem-sucedida do dashboard
- **WHEN** `ADMIN_ORG` autenticado chama `GET /organizations/{id}/dashboard?from=<data>&to=<data>` para a própria organização
- **THEN** sistema retorna 200 com turmas ativas/arquivadas, membros por papel, tarefas criadas/avaliadas no período e taxa média de entrega

#### Scenario: Organização sem dados no período
- **WHEN** `ADMIN_ORG` consulta um período sem turmas, tarefas ou membros criados
- **THEN** sistema retorna 200 com todas as métricas zeradas, sem erro

#### Scenario: Período inválido
- **WHEN** `ADMIN_ORG` chama `GET /organizations/{id}/dashboard` com `from` posterior a `to`
- **THEN** sistema retorna 400

### Requirement: Isolamento por organização (multi-tenant)
O sistema SHALL filtrar todas as métricas do dashboard exclusivamente pelo `organization_id` extraído do JWT do usuário autenticado, nunca por valor recebido na URL ou no corpo da requisição.

#### Scenario: Tentativa de acessar dashboard de outra organização
- **WHEN** `ADMIN_ORG` da organização A chama `GET /organizations/{id-da-organizacao-B}/dashboard`
- **THEN** sistema retorna 403 e nenhuma métrica de outra organização é exposta

### Requirement: Restrição de acesso ao papel ADMIN_ORG
O sistema SHALL restringir o acesso ao dashboard e à exportação em PDF exclusivamente a usuários com papel `ADMIN_ORG`.

#### Scenario: Usuário sem papel ADMIN_ORG tenta acessar
- **WHEN** um usuário com papel `GESTOR`, `PROFESSOR` ou `ALUNO` chama `GET /organizations/{id}/dashboard`
- **THEN** sistema retorna 403

### Requirement: Feed de últimas atividades da organização
O sistema SHALL incluir no dashboard um feed das últimas atividades da organização no período consultado, composto por: turmas criadas/arquivadas, tarefas criadas/avaliadas e membros que ingressaram na organização — ordenado por data decrescente.

#### Scenario: Feed com atividades de múltiplos tipos
- **WHEN** o período consultado contém turmas criadas, tarefas avaliadas e novos membros
- **THEN** sistema retorna o feed com todos os eventos ordenados por data decrescente, identificando o tipo de cada atividade

#### Scenario: Feed vazio
- **WHEN** o período consultado não contém nenhuma atividade
- **THEN** sistema retorna o feed como lista vazia, sem erro

### Requirement: Exportação do dashboard em PDF
O sistema SHALL gerar um arquivo PDF com as mesmas métricas e o mesmo período exibidos no dashboard, restrito ao mesmo controle de acesso (`ADMIN_ORG`, isolamento por `organization_id`).

#### Scenario: Exportação bem-sucedida
- **WHEN** `ADMIN_ORG` autenticado chama `GET /organizations/{id}/reports/pdf?from=<data>&to=<data>` para a própria organização
- **THEN** sistema retorna 200 com `Content-Type: application/pdf` contendo as métricas do período

#### Scenario: Exportação sem permissão
- **WHEN** um usuário sem papel `ADMIN_ORG`, ou `ADMIN_ORG` de outra organização, chama o endpoint de exportação
- **THEN** sistema retorna 403
