## Why

RF-17 (módulo `reporting`) dá ao `ADMIN_ORG` uma visão consolidada da organização — hoje ele precisa navegar por turmas, membros e tarefas individualmente para entender o estado geral. O módulo `reporting` ainda não existe; este RF o introduz como um módulo de leitura agregada (read-model) sobre dados já produzidos pelos módulos `organization`, `classroom` e `assessment`.

## What Changes

- Novo módulo `reporting` com `AdminDashboardQuery`, agregando por `organization_id`: turmas ativas/arquivadas, membros por papel, tarefas criadas/avaliadas no período, taxa média de entrega e últimas atividades (turmas criadas/arquivadas, tarefas criadas/avaliadas, membros ingressados).
- Filtro de período obrigatório via query params (`from`/`to`) — sem período fixo; o frontend oferece atalhos (7/30/90 dias) e seleção customizada.
- Endpoint de exportação em PDF do mesmo relatório, renderizado a partir de template HTML (Qute) via OpenHTMLtoPDF — primeira geração de PDF no projeto (nova dependência).
- Frontend: `features/dashboard` com cards de métricas, gráficos (Recharts, padrão dos componentes de chart do shadcn/ui) e feed de últimas atividades, substituindo o placeholder atual em `OrganizationDashboardPage`.
- **Fora de escopo nesta change:** dashboards de GESTOR, PROFESSOR e ALUNO (RF-18/19/20, reaproveitarão o módulo `reporting` mas com queries e RBAC próprios); agendamento/exportação automática de relatórios; cache do resultado agregado (sem Redis nesta entrega — reavaliar se performance exigir).

## Capabilities

### New Capabilities
- `admin-dashboard`: agregação de métricas da organização (turmas, membros, tarefas, taxa de entrega, atividades recentes) filtradas por período e por `organization_id`, com visualização no frontend e exportação em PDF, restrita ao papel `ADMIN_ORG`.

### Modified Capabilities

(nenhuma — RF-17 introduz o módulo `reporting` como leitor cross-module via Query Ports, mesmo padrão já usado por `communication` em RF-15/16; nenhum contrato existente de `organization`, `classroom` ou `assessment` muda)

## Impact

- **Backend:** novo módulo `reporting` (domain/port/in, application/usecase, infrastructure/persistence, interfaces/rest); novos Query Ports cross-module (`ClassroomQueryPort`, `OrganizationMemberQueryPort`, `TaskQueryPort` — reaproveitando o padrão JPQL cross-module já aprovado em RF-15/16) implementados como adapters em `reporting/infrastructure/persistence`; nova dependência Maven para geração de PDF (OpenHTMLtoPDF); sem migration Flyway (módulo é somente leitura, sem entidade própria).
- **Frontend:** nova feature `features/dashboard` (componentes, hooks `useAdminDashboard`, query keys, seletor de período); nova dependência `recharts`; `OrganizationDashboardPage` passa a renderizar o dashboard completo em vez do placeholder.
- Sem impacto em contratos existentes — apenas leitura cross-module via Ports.
