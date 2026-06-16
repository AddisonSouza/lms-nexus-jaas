## 1. Backend — Migration e modelo de domínio

- [ ] 1.1 [INFRA] Criar `V023__create_notifications_table.sql` (tabela `notifications`: `id`, `user_id`, `organization_id`, `type`, `reference_id`, `title`, `message`, `action_link`, `read_at`, `created_at`, índices `(user_id, created_at)` e `(user_id, read_at)`)
- [ ] 1.2 [BE] Criar `Notification`, `NotificationId`, `NotificationType` (enum) em `communication/domain/model/` — incluindo campos `title`, `message` e `actionLink` (textos fixos por tipo, ver Decisão 4 do design.md)
- [ ] 1.3 [BE] Criar exceções `NotificationNotFoundException`, `UnauthorizedNotificationOperationException` em `communication/domain/exception/`

## 2. Backend — Ports

- [ ] 2.1 [BE] Criar portas de entrada `ListNotificationsUseCase`, `MarkNotificationReadUseCase`, `MarkAllNotificationsReadUseCase` em `communication/domain/port/in/`
- [ ] 2.2 [BE] Criar porta de saída `NotificationRepository` (save, findById, findByUser, countUnreadByUser, markRead, markAllReadByUser) em `communication/domain/port/out/`
- [ ] 2.3 [BE] Criar porta de saída `NotificationUnreadCounterPort` (increment, decrement, reset, get) em `communication/domain/port/out/`
- [ ] 2.4 [BE] Adicionar `listMemberUserIds(classroomId, role)` à porta existente `ClassroomQueryPort` (`communication/domain/port/out/`)
- [ ] 2.5 [BE] Criar porta de saída `SubjectQueryPort` (`findClassroomIdsBySubject`, `findTeacherUserIdsBySubject`) em `communication/domain/port/out/`
- [ ] 2.6 [BE] Criar porta de saída `TaskQueryPort` (`findSubjectIdByTask`) em `communication/domain/port/out/`

## 3. Backend — Infraestrutura

- [ ] 3.1 [BE] Criar `NotificationJpaEntity` em `communication/infrastructure/persistence/` e implementar `NotificationRepositoryImpl`
- [ ] 3.2 [BE] Implementar `NotificationUnreadCounterRedisAdapter` (`communication/infrastructure/cache/`) usando `RedisDataSource`, chave `communication:unread-count:{userId}`
- [ ] 3.3 [BE] Implementar `listMemberUserIds` em `ClassroomQueryPortImpl` (JPQL contra `ClassroomMemberJpaEntity` filtrando por `role`)
- [ ] 3.4 [BE] Implementar `SubjectQueryPortImpl` em `communication/infrastructure/persistence/` (JPQL contra `SubjectClassroomJpaEntity` e join `SubjectTeacherJpaEntity`→`OrganizationMemberJpaEntity` para resolver `memberId`→`userId`)
- [ ] 3.5 [BE] Implementar `TaskQueryPortImpl` em `communication/infrastructure/persistence/` (JPQL contra `TaskJpaEntity` retornando `subjectId`)

## 4. Backend — Listeners de Domain Events

- [ ] 4.1 [BE] Implementar `CreateNotificationOnAnnouncementPosted` (`@Observes AnnouncementPostedEvent`): resolve ALUNOs da turma via `ClassroomQueryPort`, exclui o autor, cria notificação `ANNOUNCEMENT_POSTED` para cada destinatário
- [ ] 4.2 [BE] Implementar `CreateNotificationOnTaskPublished` (`@Observes TaskPublishedEvent`): resolve turmas via `SubjectQueryPort.findClassroomIdsBySubject`, resolve ALUNOs de cada turma via `ClassroomQueryPort`, cria notificação `TASK_PUBLISHED`
- [ ] 4.3 [BE] Implementar `CreateNotificationOnTaskSubmitted` (`@Observes TaskSubmittedEvent`): resolve `subjectId` via `TaskQueryPort`, resolve PROFESSORes via `SubjectQueryPort.findTeacherUserIdsBySubject`, cria notificação `TASK_SUBMITTED`
- [ ] 4.4 [BE] Implementar `CreateNotificationOnSubmissionEvaluated` (`@Observes SubmissionEvaluatedEvent`): cria notificação `SUBMISSION_EVALUATED` diretamente para `studentId`
- [ ] 4.5 [BE] Em cada listener, após persistir a(s) notificação(ões), incrementar o contador Redis de cada destinatário via `NotificationUnreadCounterPort`

## 5. Backend — Use Cases de leitura

- [ ] 5.1 [BE] Implementar `ListNotificationsService`: retorna notificações do `jwt.sub` ordenadas por `created_at DESC` + `unreadCount` do Redis
- [ ] 5.2 [BE] Implementar `MarkNotificationReadService`: valida `user_id == jwt.sub` (403 caso contrário), 404 se não existir, define `read_at`, decrementa contador Redis (idempotente se já lida)
- [ ] 5.3 [BE] Implementar `MarkAllNotificationsReadService`: define `read_at` em lote para todas as não lidas do usuário, zera contador Redis

## 6. Backend — Endpoints REST

- [ ] 6.1 [BE] Criar `NotificationResource` com `GET /notifications` (`@RolesAllowed({"ADMIN_ORG","GESTOR","PROFESSOR","ALUNO"})`)
- [ ] 6.2 [BE] Adicionar `PATCH /notifications/{id}/read` (mesmos papéis)
- [ ] 6.3 [BE] Adicionar `PATCH /notifications/read-all` (mesmos papéis)

## 7. Backend — Testes

- [ ] 7.1 [BE] Testes unitários dos 4 listeners (`CreateNotificationOnXxx`) com Mockito cobrindo resolução de destinatários, exclusão do autor (announcement) e lista vazia de destinatários
- [ ] 7.2 [BE] Testes unitários de `ListNotificationsService`, `MarkNotificationReadService`, `MarkAllNotificationsReadService` cobrindo sucesso e 403/404 da spec
- [ ] 7.3 [BE] Testes de integração `@QuarkusTest` para `NotificationResource` cobrindo os 3 endpoints e os cenários de autorização da spec
- [ ] 7.4 [BE] Teste de integração ponta a ponta: publicar `AnnouncementPostedEvent`/`TaskPublishedEvent`/`TaskSubmittedEvent`/`SubmissionEvaluatedEvent` via use case de origem e verificar notificação + contador Redis criados

## 8. Frontend — Tipos e API

- [ ] 8.1 [FE] Adicionar tipos `Notification`, `NotificationType` em `features/communication/types.ts`
- [ ] 8.2 [FE] Criar `features/communication/api/notifications.ts` com `listNotifications`, `markNotificationRead`, `markAllNotificationsRead`
- [ ] 8.3 [FE] Adicionar `["notifications"]` em `features/communication/api/query-keys.ts`

## 9. Frontend — Hooks e componentes

- [ ] 9.1 [FE] Criar hook `useNotifications()` (TanStack Query, `refetchInterval: 30_000`, query key `["notifications"]`)
- [ ] 9.2 [FE] Criar hooks de mutação `useMarkNotificationRead`, `useMarkAllNotificationsRead` com invalidação de `["notifications"]`
- [ ] 9.3 [FE] Criar `NotificationBell.tsx` (ícone Lucide `Bell` + badge com `unreadCount`)
- [ ] 9.4 [FE] Criar `NotificationPanel.tsx` (popover/dropdown Shadcn com a lista; clique marca como lida e navega via `actionLink`; botão "marcar todas como lidas")
- [ ] 9.5 [FE] Integrar `NotificationBell` no layout principal (header autenticado), visível para todos os papéis

## 10. Frontend — Testes

- [ ] 10.1 [FE] Testes Vitest + Testing Library + MSW para `NotificationBell` e `NotificationPanel` (badge de contagem, marcar uma como lida, marcar todas como lidas, polling)
