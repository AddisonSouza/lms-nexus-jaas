# [feat] acessos de professor para admin e gestor

## Objetivo
Avaliar a viabilidade de `ADMIN_ORG` e `GESTOR` acumularem os acessos de
`PROFESSOR` na mesma organização (ex.: coordenador de curso que cria turmas e
disciplinas e também leciona).

## Comportamento esperado
Um gestor/admin consegue executar as ações de professor (lecionar disciplinas,
criar e corrigir tarefas) sem precisar de outra conta ou trocar de papel.

## Fora de escopo
- Implementar antes da análise de viabilidade.
- Mudar os acessos de `ALUNO`.
- Papéis entre organizações diferentes.
