## MODIFIED Requirements

### Requirement: Aluno lista tarefas publicadas
O sistema SHALL retornar todas as tarefas com status `PUBLISHED` da organização do aluno autenticado via `GET /tasks/published`.

#### Scenario: Listagem bem-sucedida
- **WHEN** aluno autenticado chama `GET /tasks/published`
- **THEN** sistema retorna 200 com lista de tarefas publicadas da organização extraída do JWT

#### Scenario: Organização sem tarefas publicadas
- **WHEN** aluno chama `GET /tasks/published` e não há tarefas publicadas
- **THEN** sistema retorna 200 com lista vazia

### Requirement: SubmissionRepository suporta consulta por aluno
O `SubmissionRepository` SHALL expor método `findByStudentAndOrganization(studentId, orgId)` que retorna todas as submissões ativas do aluno na organização.

#### Scenario: Aluno com submissões existentes
- **WHEN** `findByStudentAndOrganization` é chamado com studentId e orgId válidos
- **THEN** retorna lista de `TaskSubmission` com `deleted_at IS NULL`

#### Scenario: Aluno sem submissões
- **WHEN** `findByStudentAndOrganization` é chamado e aluno não tem submissões
- **THEN** retorna lista vazia
