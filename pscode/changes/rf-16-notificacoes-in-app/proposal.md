## Why

RF-16 (módulo `communication`) introduz notificações in-app para que usuários (ALUNO, PROFESSOR, GESTOR, ADMIN_ORG) saibam de eventos relevantes (tarefa publicada, resposta enviada, avaliação lançada, aviso publicado) sem depender de e-mail. O módulo `communication` já existe desde RF-15 e já publica `AnnouncementPostedEvent` sem nenhum consumidor — este RF cria o primeiro consumidor real desses Domain Events.

## What Changes

- Novo `CreateNotificationUseCase` que consome (via `@Observes` CDI) os Domain Events já publicados por outros módulos: `AnnouncementPostedEvent`, `TaskPublishedEvent`, `TaskSubmittedEvent`, `SubmissionEvaluatedEvent`.
- Persistência de notificações por usuário (`notifications` table), com tipo, payload mínimo (ids de referência) e `read_at`.
- Contador de não lidas mantido no Redis (`DB-06`), incrementado na criação e decrementado/zerado na leitura.
- Endpoints: `GET /notifications` (lista + contador de não lidas do usuário autenticado), `PATCH /notifications/{id}/read`, `PATCH /notifications/read-all`.
- Frontend: sino no layout principal com badge de não lidas, painel dropdown com a lista, polling leve de 30s (TanStack Query `refetchInterval`).
- **Fora de escopo nesta change:** `MemberInvitedEvent` não gera notificação in-app — o convidado ainda não possui `userId` no momento do evento (apenas e-mail/token); o canal de convite já é o e-mail. Tempo real (WebSocket/SSE) é evolução futura, mantendo o MVP por polling.

## Capabilities

### New Capabilities
- `in-app-notifications`: criação, listagem, marcação de leitura (individual e em lote) de notificações in-app geradas a partir de Domain Events, com contador de não lidas em Redis e exibição via sino no frontend.

### Modified Capabilities

(nenhuma — RF-16 apenas adiciona um consumidor aos eventos já publicados por `classroom-announcements`, `task-publishing`, `task-submission` e `task-evaluation`; nenhum contrato existente muda)

## Impact

- **Backend:** novo agregado `Notification` no módulo `communication` (domain/model, port/in, port/out), 4 listeners CDI (um por evento consumido), nova tabela `notifications` (migration Flyway), novo `NotificationUnreadCounterPort` sobre Redis, novos endpoints REST, novos ports de leitura entre módulos (`assessment`→`communication` para resolver destinatários via `subjectId`/`classroomId`, reaproveitando padrão de FQN JPQL já usado em `ClassroomQueryPortImpl`).
- **Frontend:** `features/communication` ganha `NotificationBell`, `NotificationPanel`, hooks (`useNotifications`, `useNotificationMutations`), schema/types e item no layout principal (header).
- Sem impacto em contratos existentes de `assessment`, `classroom` ou `curriculum` — apenas leitura via JPQL cross-module, mesmo padrão já aprovado em RF-15.
