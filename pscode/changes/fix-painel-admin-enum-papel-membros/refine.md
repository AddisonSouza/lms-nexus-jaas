# [fix] painel do admin exibe enum de papel no card de membros e no gráfico

## Summary
No painel do Administrador, a contagem de membros, o gráfico por papel e o PDF
exportado mostram códigos internos (`ADMIN_ORG`, `ACTIVE`). Passam a mostrar
"Administrador", "Gestor", "Professor", "Aluno" e, no PDF, "Ativa"/"Arquivada".

## Technical detail
- Web: novo `apps/web/src/lib/roles.ts` (FE-11 impede importar
  `features/organization/roles.ts`) com o mapa e `roleLabel(role)` que devolve o
  valor cru se desconhecido.
- `MetricsCards.tsx:30-32` monta `${role}: ${count}` → usar `roleLabel(role)`.
- `DashboardCharts.tsx:10,23,25`: incluir `label` em `membersData` e usá-lo como
  `dataKey` do `XAxis`; o tooltip passa a mostrar o rótulo.
- PDF: `templates/reporting/dashboard.html:21-23,26-32` imprime `{entry.key}`.
  `DashboardPdfRenderer.render` passa mapas de rótulo (papel e status) ao template,
  que usa o rótulo com fallback para a chave.
- O contrato da API não muda: `membersByRole`/`classroomsByStatus` seguem com as
  chaves do enum.
- #228 (PR #247) cria um mapa de papel privado em `MemberMetricsQueryPortImpl`;
  se já estiver na `main`, reaproveitar em vez de duplicar no back.

## Scope
### In
- Card "Membros", eixo e tooltip de "Membros por papel".
- Tabelas de membros por papel e turmas por status no PDF do admin.
- Testes: `MetricsCards.test.tsx` (hoje espera `ALUNO: 10`), teste do gráfico e
  teste do HTML renderizado do PDF.
### Out
- Feed de últimas atividades (#228).
- Migrar `organization/roles.ts`, `OrganizationSwitcher` e `AcceptInvitePage`
  para o `@lib/roles.ts`.
- Dashboards de Gestor, Professor e Aluno.

## Subtasks
- [ ] Web: `@lib/roles.ts` + card "Membros" e gráfico com rótulos legíveis, com testes
- [ ] PDF: tabelas de papel e status de turma com rótulos legíveis, com teste do HTML renderizado
