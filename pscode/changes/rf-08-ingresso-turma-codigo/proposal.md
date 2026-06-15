## Why

RF-08 completa o ciclo de ingresso em turmas iniciado no RF-07: turmas já são criadas com código de convite, mas ainda não existe um fluxo para que ALUNOs utilizem esse código de forma autônoma — sem depender de convite individual por e-mail.

## What Changes

- Novo endpoint `POST /classrooms/join` que aceita um código de 6 caracteres e vincula o usuário autenticado como `ALUNO` na turma correspondente (fluxo idempotente).
- Novo endpoint `POST /classrooms/{id}/invite-code/regenerate` para que `PROFESSOR` ou `GESTOR` invalide o código atual e gere um novo.
- Extensão do `ClassroomRepository` com método `findByInviteCode`.
- Nova exception `InvalidInviteCodeException` (código inexistente ou turma arquivada).
- Frontend: página/componente de ingresso por código com campo de input e validação Zod.

## Capabilities

### New Capabilities

- `classroom-join-by-code`: Ingresso de ALUNO em turma via código de 6 caracteres. Inclui join idempotente, bloqueio para turma arquivada, e regeneração do código pelo PROFESSOR/GESTOR.

### Modified Capabilities

<!-- nenhuma -->

## Impact

- **Backend:** módulo `classroom` — novos use cases, extensão do `ClassroomRepository`, dois novos endpoints REST.
- **Frontend:** módulo `features/classroom` — componente `JoinClassroomForm`, hook `useJoinClassroom`, rota pública `/join/:code` ou formulário de código.
- **Sem migration Flyway:** o schema de `classrooms` e `classroom_members` já existe desde o RF-07.
- **Non-goals:** convite por e-mail (coberto pelo RF-06); geração do link de compartilhamento (visual, responsabilidade do FE usando o código existente); autenticação prévia obrigatória (ALUNO já deve estar logado).
