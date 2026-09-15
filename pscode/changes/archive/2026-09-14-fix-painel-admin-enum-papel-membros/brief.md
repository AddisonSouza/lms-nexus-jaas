# [fix] painel do admin exibe enum de papel no card de membros e no gráfico

## Objective
No dashboard do Administrador, o card "Membros" (`GESTOR: 1 · ADMIN_ORG: 1`) e o
gráfico "Membros por papel" mostram o enum de papel cru.

## Expected behavior
- Card, eixo e tooltip do gráfico com rótulos legíveis: Administrador, Gestor,
  Professor, Aluno.
- PDF exportado: tabela de membros com os mesmos rótulos e tabela de turmas com
  status legível (Ativa, Arquivada).

## Out of scope
- Feed de últimas atividades (card #228).
- Outras telas e as cópias de rótulo de papel já existentes no front.
