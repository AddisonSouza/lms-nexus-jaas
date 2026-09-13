# [fix] atividades recentes do dashboard exibem enum de papel cru

## Summary
Nas "Últimas atividades" do dashboard, a entrada de um novo membro mostra o código
interno do papel (`ADMIN_ORG`). Passa a mostrar quem entrou e o papel por extenso:
"Maria Silva (Aluno) ingressou na organização".

## Technical detail
- Origem: `reporting/infrastructure/persistence/MemberMetricsQueryPortImpl.java:59`
  monta a descrição concatenando `m.role` cru. Não vem de avisos.
- `OrganizationMemberJpaEntity` tem `userId`; buscar `u.fullName` de `UserJpaEntity`
  (mesmo padrão já usado em `GestorDashboardQueryPortImpl.java:196`), via LEFT JOIN
  para não perder o item quando o usuário não existir.
- Rótulos em pt-BR no back (a descrição já é texto pronto): ADMIN_ORG → Administrador,
  GESTOR → Gestor, PROFESSOR → Professor, ALUNO → Aluno; papel desconhecido mantém o valor.
- Fallback sem nome: "Novo membro (<Papel>) ingressou na organização".
- Front (`ActivityFeed.tsx`) e PDF (`templates/reporting/dashboard.html`) só exibem
  `description` — ambos corrigidos sem mudança de contrato.

## Scope
### In
- Descrição do item `MEMBER_JOINED` (feed web e PDF do dashboard).
- Ajuste dos testes: `ReportingMetricsQueryPortsIT`, `GetAdminDashboardServiceTest`,
  `ActivityFeed.test.tsx` (fixture com texto antigo).
### Out
- Autoria nas atividades de turma, tarefa e avaliação.
- Mudança no contrato da API (`ActivityItemResponse`) ou no layout do card.
- Unificar os mapas de rótulo de papel duplicados no front.

## Subtasks
- [ ] Back: descrição de novo membro com nome + papel legível e fallback, com IT cobrindo nome e fallback
- [ ] Testes: atualizar `GetAdminDashboardServiceTest` e fixture de `ActivityFeed.test.tsx` para o novo texto
