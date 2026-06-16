## Context

RF-16 adiciona o primeiro consumidor real de Domain Events no módulo `communication` (criado em RF-15, que já publica `AnnouncementPostedEvent` sem listener). Os eventos a consumir já existem e seguem o mesmo padrão CDI síncrono (`Event<T>.fire()`): `AnnouncementPostedEvent` (communication), `TaskPublishedEvent`, `TaskSubmittedEvent`, `SubmissionEvaluatedEvent` (assessment). O issue do RF-16 cita `TaskCreatedEvent` e `InvitationCreatedEvent`, que não existem no código — mapeados para `TaskPublishedEvent` e descartados (`MemberInvitedEvent`), respectivamente (ver Decisão 6).

Os eventos carregam apenas IDs (`subjectId`, `classroomId`, `studentId`...), nunca a lista de destinatários — resolver "quem recebe" exige consultas cross-module, replicando o padrão já aprovado em RF-15 (`ClassroomQueryPort` do `communication` consulta via JPQL com FQN a entidade `ClassroomMemberJpaEntity` do módulo `classroom`).

## Goals / Non-Goals

**Goals:**
- Consumir os 4 eventos mapeados e criar notificações persistidas para os destinatários corretos.
- Contador de não lidas por usuário em Redis (chave `communication:unread-count:{userId}`, regra `DB-06`).
- Listagem, marcação individual e em lote ("marcar todas como lidas").
- Sino + painel no frontend com polling de 30s.

**Non-Goals:**
- `MemberInvitedEvent` não gera notificação in-app nesta change (sem `userId` no momento do evento — ver Decisão 6).
- Tempo real (WebSocket/SSE) — evolução futura, conforme o próprio RF-16.
- Paginação no `GET /notifications` (consistente com o padrão atual do projeto — nenhuma listagem usa paginação ainda; reavaliar como melhoria futura).
- Preferências de notificação (silenciar tipos, e-mail digest, etc.).

## Decisions

**1. Novo agregado `Notification` dentro do módulo `communication` existente (não um módulo `notification` separado).**
O issue menciona "módulo notification" no contexto, mas a estrutura de módulos do projeto (`pscode/config.yaml`) já define `communication` como o módulo correto — `AnnouncementPostedEvent` já vive lá. Criar um módulo novo só para isso duplicaria infraestrutura sem necessidade.

**2. Notificação é genérica (`type` + IDs de referência), não uma subclasse por evento.**
Tabela única `notifications` com colunas `type` (enum string: `ANNOUNCEMENT_POSTED`, `TASK_PUBLISHED`, `TASK_SUBMITTED`, `SUBMISSION_EVALUATED`), `reference_id` (id da entidade de origem — announcementId/taskId/submissionId) e `metadata` (JSON curto, ex.: `classroomId`/`subjectId`, usado pelo frontend para montar o link). Alternativa considerada: tabela por tipo de evento — rejeitada por multiplicar trabalho de migration/repository sem ganho, já que a listagem e o contador tratam todas como a mesma entidade.

**3. Resolução de destinatários via novos Ports cross-module no `communication`, seguindo o padrão FQN-JPQL de RF-15.**
- `ClassroomQueryPort` (já existe) ganha `List<String> listMemberUserIds(String classroomId, String role)` — JPQL contra `ClassroomMemberJpaEntity` (módulo `classroom`), mesmo padrão de `isMember`.
- Novo `SubjectQueryPort` (out) com:
  - `List<String> findClassroomIdsBySubject(String subjectId)` — JPQL contra `SubjectClassroomJpaEntity` (módulo `curriculum`).
  - `List<String> findTeacherUserIdsBySubject(String subjectId)` — JPQL join `SubjectTeacherJpaEntity` → `OrganizationMemberJpaEntity` (resolve `memberId` → `userId`, pois `SubjectTeacherJpaEntity.id.memberId` referencia `organization_members.id`, não `users.id` diretamente).
  Alternativa considerada: expor esses métodos como porta pública do `curriculum`/`classroom` para outros módulos consumirem diretamente — rejeitada pela regra do projeto "módulos comunicam via interfaces Java, nunca import direto entre bounded contexts dono"; cada módulo consumidor define seu próprio port com a forma mínima necessária (mesma decisão já tomada em RF-15).

**4. Destinatários por evento:**
| Evento | Destinatários |
|---|---|
| `AnnouncementPostedEvent` | ALUNOs da `classroomId`, exceto o autor (já é o publicador) |
| `TaskPublishedEvent` | ALUNOs de todas as `classroomId`s vinculadas ao `subjectId` (via `findClassroomIdsBySubject` + `listMemberUserIds(..., "ALUNO")`) |
| `TaskSubmittedEvent` | PROFESSORes do `subjectId` da tarefa (via `findTeacherUserIdsBySubject`) — requer resolver `taskId → subjectId` (novo `TaskQueryPort.findSubjectIdByTask`, JPQL contra `TaskJpaEntity`) |
| `SubmissionEvaluatedEvent` | o próprio `studentId` do evento (notificação direta, sem fan-out) |

**5. Contador de não lidas em Redis, fonte de verdade para o badge; tabela MySQL é fonte de verdade para a lista.**
Cada criação de notificação faz `INCR communication:unread-count:{userId}`; cada leitura (individual ou "marcar todas") recalcula via `DECRBY`/`SET 0` a partir da contagem real não lida no MySQL pós-update, evitando drift entre Redis e banco em caso de falha parcial. Alternativa considerada: Redis como única fonte (sem persistir notificações) — rejeitada porque `GET /notifications` precisa listar histórico, não só o contador.

**6. `MemberInvitedEvent` fora de escopo — decisão confirmada com o usuário.**
Notificação in-app exige um `userId` existente; no momento do convite só há `email`/`token` (`InviteMemberService`). O canal de convite já é e-mail (RF-06). Revisitar apenas se um RF futuro vincular convite pendente → notificação após aceite.

**7. Eventos consumidos de forma síncrona (`@Observes`, não `@ObservesAsync`).**
Mesmo padrão usado por `InvitationMailService.onMemberInvited`. Se a criação de notificação falhar, a transação do evento de origem (ex.: publicar tarefa) também falha — aceitável no MVP; mover para `@ObservesAsync` é uma otimização futura caso o fan-out fique custoso.

**8. `GET /notifications` retorna `unreadCount` embutido na resposta (não há endpoint dedicado no RF-16).**
O contador vem do Redis; a lista vem do MySQL. Um único DTO de resposta (`NotificationListResponse { items, unreadCount }`) evita endpoint extra não pedido no RF.

## Estrutura de pacotes (backend)

```
apps/api/src/main/java/br/edu/lms/module/communication/
  domain/
    model/Notification.java, NotificationId.java, NotificationType.java (enum)
    exception/NotificationNotFoundException.java, UnauthorizedNotificationOperationException.java
    port/in/ListNotificationsUseCase.java, MarkNotificationReadUseCase.java, MarkAllNotificationsReadUseCase.java
    port/out/NotificationRepository.java, NotificationUnreadCounterPort.java, SubjectQueryPort.java, TaskQueryPort.java
  application/
    usecase/CreateNotificationOnAnnouncementPosted.java, CreateNotificationOnTaskPublished.java,
            CreateNotificationOnTaskSubmitted.java, CreateNotificationOnSubmissionEvaluated.java,
            ListNotificationsService.java, MarkNotificationReadService.java, MarkAllNotificationsReadService.java
    dto/NotificationResponse.java, NotificationListResponse.java
  infrastructure/
    persistence/NotificationJpaEntity.java, NotificationRepositoryImpl.java, SubjectQueryPortImpl.java, TaskQueryPortImpl.java
    cache/NotificationUnreadCounterRedisAdapter.java
  interfaces/
    rest/NotificationResource.java
```

(`ClassroomQueryPort`/`ClassroomQueryPortImpl` já existem — só ganham o novo método `listMemberUserIds`.)

Os 4 listeners de evento (`CreateNotificationOnXxx`) ficam em `application/usecase/` como `@ApplicationScoped` com método `@Observes`, não implementam port `in/` (não são acionados via REST, só via CDI) — mesmo padrão de `InvitationMailService`.

## Migration Flyway

- `V023__create_notifications_table.sql`:
  ```sql
  CREATE TABLE notifications (
      id               VARCHAR(36)  NOT NULL,
      user_id          VARCHAR(36)  NOT NULL,
      organization_id  VARCHAR(36)  NOT NULL,
      type             VARCHAR(40)  NOT NULL,
      reference_id     VARCHAR(36)  NOT NULL,
      metadata         JSON         NULL,
      read_at          DATETIME(6)  NULL,
      created_at       DATETIME(6)  NOT NULL,
      PRIMARY KEY (id),
      CONSTRAINT fk_notifications_user FOREIGN KEY (user_id)         REFERENCES users(id),
      CONSTRAINT fk_notifications_org  FOREIGN KEY (organization_id) REFERENCES organizations(id),
      INDEX idx_notifications_user_created (user_id, created_at),
      INDEX idx_notifications_user_unread  (user_id, read_at)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
  ```

## Endpoints REST

- `GET /notifications` — `@RolesAllowed({"ADMIN_ORG","GESTOR","PROFESSOR","ALUNO"})`. Lista notificações do usuário autenticado (`jwt.sub`), ordenadas por `created_at DESC`, sem paginação; resposta inclui `unreadCount` (Redis). Retorna 200.
- `PATCH /notifications/{id}/read` — mesmos papéis. Marca uma notificação como lida; 403 se `user_id != jwt.sub`; decrementa contador Redis. Retorna 200.
- `PATCH /notifications/read-all` — mesmos papéis. Marca todas as notificações não lidas do usuário autenticado como lidas em lote; zera contador Redis. Retorna 200.

## Frontend

- **Feature:** `apps/web/src/features/communication/` (estende a feature criada em RF-15)
  - `types.ts`: adiciona `Notification`, `NotificationType`
  - `api/notifications.ts`: `listNotifications`, `markNotificationRead`, `markAllNotificationsRead`
  - `api/query-keys.ts`: adiciona `["notifications"]`
  - `hooks/useNotifications.ts` (TanStack Query, `refetchInterval: 30_000`)
  - `hooks/useNotificationMutations.ts` (mark read / mark all read, invalida `["notifications"]`)
  - `components/NotificationBell.tsx` (ícone Lucide `Bell` + badge com `unreadCount`)
  - `components/NotificationPanel.tsx` (dropdown/popover Shadcn com a lista; cada item navega para a referência via `metadata`)
- **Integração:** `NotificationBell` adicionado ao layout principal (header autenticado), visível para todos os papéis — sem rota própria.

## Risks / Trade-offs

- [Risco] Fan-out de `TaskPublishedEvent` para turmas com muitos alunos gera N inserts síncronos na mesma transação do `PublishTaskService` → pode aumentar a latência de publicar tarefa. Mitigação: aceitável no volume do MVP (turmas de sala de aula, não milhares de alunos); reavaliar `@ObservesAsync` ou processamento em lote se necessário.
- [Risco] Drift entre contador Redis e contagem real no MySQL (ex.: falha após `INCR` mas antes do commit da notificação, ou vice-versa) → Mitigação: contador é cosmético (badge); a lista paginada do MySQL é a fonte de verdade exibida ao abrir o painel, então drift se autocorrige na próxima leitura.
- [Trade-off] `MemberInvitedEvent` fora de escopo (Decisão 6) → aceito conscientemente; convite continua só por e-mail.
- [Trade-off] Sem paginação em `GET /notifications` → aceito, consistente com o padrão atual do projeto; reavaliar se o histórico crescer muito (mitigado parcialmente pelo índice `(user_id, created_at)`).
