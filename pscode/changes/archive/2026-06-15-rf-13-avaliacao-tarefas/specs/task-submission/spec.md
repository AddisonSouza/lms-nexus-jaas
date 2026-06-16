## MODIFIED Requirements

### Requirement: Resposta do aluno inclui dados de avaliação
O sistema SHALL retornar `grade` (BigDecimal, nullable) e `feedback` (String, nullable) em todos os endpoints que retornam `TaskSubmission`, refletindo o resultado da avaliação quando `status == EVALUATED`.

#### Scenario: Submissão ainda não avaliada
- **WHEN** aluno ou professor acessa uma submissão com status `SUBMITTED`
- **THEN** resposta inclui `grade: null` e `feedback: null`

#### Scenario: Submissão avaliada
- **WHEN** aluno ou professor acessa uma submissão com status `EVALUATED`
- **THEN** resposta inclui `grade` e `feedback` preenchidos (feedback MUST ser não-nulo; grade pode ser null se tarefa não tiver pontuação)
