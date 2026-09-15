# [fix] atividades recentes do dashboard exibem enum de papel cru

## Objective
As "Últimas atividades" do dashboard mostram o enum de papel cru (ex.: `ADMIN_ORG`)
no item de novo membro, o que causa estranheza ao usuário.

## Expected behavior
- Exibir nome + papel legível: "Maria Silva (Aluno) ingressou na organização".
- Papéis: `ADMIN_ORG` → Administrador, `GESTOR` → Gestor, `PROFESSOR` → Professor, `ALUNO` → Aluno.
- Sem nome disponível: "Novo membro (Aluno) ingressou na organização".

## Out of scope
- Redesenho do card de atividades.
- Mudanças em outras telas que exibem papéis.
