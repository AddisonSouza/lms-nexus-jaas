# Atividades recentes do dashboard exibem enum de papel cru — Delta

## Changed

- **Item de novo membro no feed.** A descrição concatenava o enum cru
  ("Novo membro (ADMIN_ORG) ingressou na organização"). Agora mostra nome e papel
  legível: "Maria Silva (Aluno) ingressou na organização". O nome vem de um
  LEFT JOIN em `users` no `MemberMetricsQueryPortImpl`; os rótulos são
  Administrador, Gestor, Professor e Aluno. Sem nome, cai em
  "Novo membro (<Papel>) …"; papel desconhecido mantém o valor.
- Vale para o feed web e para o PDF exportado — ambos exibem `description`, sem
  mudança no contrato da API.

## Added

- Testes: caso de nome + rótulo em `ReportingMetricsQueryPortsIT`;
  `MemberMetricsQueryPortImplTest` cobre fallback e papel desconhecido. Fixtures
  de `GetAdminDashboardServiceTest` e `ActivityFeed.test.tsx` no novo texto.
- Validado no navegador (admin de "escola municipal de han han"): os quatro
  papéis aparecem com nome e rótulo.
- Spec viva `admin-dashboard`: cenário "Novo membro identificado por nome e
  papel legível".

## Unchanged

- Demais atividades (turma, tarefa, avaliação) seguem sem autor.
- Card "Membros" e gráfico "Membros por papel" ainda mostram o enum — card #257.
