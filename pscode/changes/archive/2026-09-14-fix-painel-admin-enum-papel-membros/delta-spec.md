# Painel do admin exibe enum de papel no card de membros e no gráfico — Delta

## Changed

- **Card "Membros" e gráfico "Membros por papel".** Mostravam o enum cru
  (`GESTOR: 1 · ADMIN_ORG: 1`, eixo `ADMIN_ORG`). Agora usam Administrador,
  Gestor, Professor e Aluno — no texto do card, no eixo e no tooltip. O mapa
  vive em `@lib/roles.ts` (`roleLabel`), pois FE-11 impede importar
  `features/organization/roles.ts`.
- **PDF do dashboard.** As tabelas "Membros por papel" e "Turmas por status"
  imprimiam as chaves cruas; agora mostram os rótulos acima e Ativa/Arquivada.
  Chave desconhecida é impressa como veio.
- **Rótulos do back unificados** em `reporting/infrastructure/ReportingLabels`,
  usado pelo PDF e pela descrição de novo membro do feed (#228), que antes tinha
  o próprio mapa.
- Contrato da API inalterado: `membersByRole`/`classroomsByStatus` seguem com as
  chaves do enum.

## Added

- Testes: `roles.test.ts`, `DashboardCharts.test.tsx` (eixo com rótulos),
  `DashboardPdfRendererIT` (HTML do PDF com rótulos e fallback);
  `MetricsCards.test.tsx` passa a esperar "Aluno: 10 · Professor: 2".
- Validado no navegador (admin de "escola municipal de han han"): card e eixo
  com Gestor/Professor/Aluno/Administrador; exportação do PDF retorna 200.
- Spec viva `admin-dashboard`: cenários de rótulos legíveis no dashboard e no PDF.

## Unchanged

- Cópias de rótulo de papel em `organization/roles.ts`, `OrganizationSwitcher` e
  `AcceptInvitePage`.
- Dashboards de Gestor, Professor e Aluno.
