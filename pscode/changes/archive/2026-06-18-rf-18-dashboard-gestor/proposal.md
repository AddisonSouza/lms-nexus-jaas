## Why

RF-18 (módulo `reporting`) dá ao `GESTOR` uma visão de saúde das turmas da organização — hoje ele precisa abrir cada turma individualmente para entender entregas e desempenho. O módulo `reporting` já existe desde o RF-17 (Admin Dashboard); este RF estende o padrão para o papel `GESTOR`.

## What Changes

- Novo endpoint `GET /organizations/{id}/gestor-dashboard` (`@RolesAllowed("GESTOR")`) retorna, para **todas as turmas da organização** (no modelo atual `GESTOR` é um papel org-wide — `organization_members.role`, sem vínculo turma-a-turma; `ListClassroomsService` já trata `GESTOR` igual a `ADMIN_ORG`), por turma: status, % de entregas (submissões / alunos elegíveis das tarefas da turma), média de notas das tarefas avaliadas, e os top 5 alunos com mais tarefas pendentes/atrasadas.
- Sem filtro de período (`from`/`to`) — os indicadores de saúde refletem o estado atual de todas as tarefas já criadas na turma, diferente do RF-17 (que é uma métrica de atividade num intervalo).
- Exportação em PDF do mesmo relatório (`GET /organizations/{id}/gestor-dashboard/pdf`), reaproveitando a infraestrutura já criada no RF-17 (`DashboardPdfRenderer`, OpenHTMLtoPDF, template Qute).
- Frontend: nova feature `features/dashboard` ganha um `GestorDashboard` (cards comparativos por turma + lista de alunos em risco), reaproveitando os componentes/hooks do RF-17 (`PeriodSelector` não se aplica aqui, sem período); `OrganizationDashboardPage` passa a renderizar `<GestorDashboard organizationId={id} />` quando `role === 'GESTOR'`, preservando o comportamento atual para `PROFESSOR`/`ALUNO`.
- **Fora de escopo:** dashboards de PROFESSOR/ALUNO (RF-19/RF-20); qualquer vínculo gestor↔turma específico (modelo atual mantém `GESTOR` org-wide); cache Redis da agregação; paginação da lista de turmas/alunos em risco.

## Capabilities

### New Capabilities
- `gestor-dashboard`: agregação por turma (status, % de entrega, média de notas, alunos com mais pendências/atraso) para todas as turmas da organização do `GESTOR` autenticado, com exportação em PDF.

### Modified Capabilities

(nenhuma — RF-18 introduz uma nova capability sobre o módulo `reporting` já existente; nenhum contrato de `classroom`, `organization` ou `assessment` muda)

## Impact

- **Backend:** módulo `reporting` ganha um novo Query Port (`GestorDashboardQueryPort` ou equivalente, cross-module contra `classroom`, `organization` e `assessment`), um novo Use Case (`GetGestorDashboardService`) e reaproveita `DashboardPdfRenderer` com um novo template Qute; novo endpoint REST em `AdminDashboardResource` ou um novo `GestorDashboardResource`; sem migration Flyway (somente leitura, sem entidade própria, mesmo padrão do RF-17).
- **Frontend:** nova feature `features/dashboard` ganha `GestorDashboard.tsx` e hook `useGestorDashboard`; `OrganizationDashboardPage` passa a ter uma terceira ramificação condicional por papel (`ADMIN_ORG` → `AdminDashboard`, `GESTOR` → `GestorDashboard`, demais → placeholder atual).
- Sem impacto em contratos REST existentes.
