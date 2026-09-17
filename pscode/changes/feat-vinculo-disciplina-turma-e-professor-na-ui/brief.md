# Vínculo disciplina↔turma e professor na UI

**Severidade:** crítico · RF-09 · origem: bateria E2E 15/09/2026

## Objetivo
Expor na página da disciplina o vínculo com turmas e a atribuição de professores.
`LinkClassroomDialog` e `AssignTeacherDialog` existem mas não são usados em
nenhuma página; sem o vínculo o professor recebe 403 ao criar tarefa e o aluno
não alcança materiais.

## Comportamento esperado
- ADMIN_ORG e GESTOR veem em `/curriculum/:subjectId` as turmas vinculadas e os
  professores atribuídos, com vincular/desvincular e atribuir/remover.
- Seleção por lista (turmas ativas; membros PROFESSOR/GESTOR/ADMIN_ORG), nunca
  por UUID digitado.
- Erros da API (422 turma arquivada, 422 membro não é professor) exibidos.

## Fora do escopo
- Reordenação de tópicos.
- Painel do professor.
