## ADDED Requirements

### Requirement: Frontend trata status LATE de submissões
O sistema SHALL reconhecer e exibir corretamente submissões com status `LATE` retornadas pela API. O tipo `SubmissionStatus` MUST incluir o valor `'LATE'` no union type TypeScript.

#### Scenario: Submissão com status LATE exibida na lista do professor
- **WHEN** professor visualiza a lista de submissões e a API retorna submissão com `status: 'LATE'`
- **THEN** sistema exibe a submissão com indicação visual de atraso (badge "Atrasada") e o botão "Avaliar" permanece disponível

#### Scenario: Submissão com status LATE na lista do aluno
- **WHEN** aluno visualiza suas tarefas e a API retorna submissão com `status: 'LATE'`
- **THEN** sistema exibe a submissão com indicação de "Enviada com atraso" em vez de "Aguardando avaliação"

#### Scenario: SubmissionStatus aceita LATE sem erro de tipo
- **WHEN** código TypeScript compara `submission.status === 'LATE'`
- **THEN** compilador TypeScript não reporta erro — `'LATE'` é membro do union type
