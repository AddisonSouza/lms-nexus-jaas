## 1. Backend — Migration e modelo de domínio

- [ ] 1.1 [INFRA] Criar `V020__create_announcements_table.sql` (tabela `announcements`)
- [ ] 1.2 [INFRA] Criar `V021__create_announcement_attachments_table.sql` (tabela `announcement_attachments`)
- [ ] 1.3 [BE] Criar `Announcement`, `AnnouncementId`, `AnnouncementAttachment` em `communication/domain/model/`
- [ ] 1.4 [BE] Criar `AnnouncementPostedEvent` em `communication/domain/event/`
- [ ] 1.5 [BE] Criar exceções `AnnouncementNotFoundException`, `UnauthorizedAnnouncementOperationException` em `communication/domain/exception/`

## 2. Backend — Ports

- [ ] 2.1 [BE] Criar porta de entrada `PostAnnouncementUseCase`, `EditAnnouncementUseCase`, `DeleteAnnouncementUseCase`, `ListAnnouncementsUseCase` em `communication/domain/port/in/`
- [ ] 2.2 [BE] Criar porta de saída `AnnouncementRepository` em `communication/domain/port/out/`
- [ ] 2.3 [BE] Criar porta de saída `ClassroomQueryPort` (`isMember(userId, classroomId, organizationId, role)`) em `communication/domain/port/out/`

## 3. Backend — Infraestrutura

- [ ] 3.1 [BE] Criar `AnnouncementJpaEntity` e `AnnouncementAttachmentJpaEntity` em `communication/infrastructure/persistence/`
- [ ] 3.2 [BE] Implementar `AnnouncementRepositoryImpl` (save, findById, findByClassroomOrderByCreatedAtDesc, soft delete)
- [ ] 3.3 [BE] Implementar `ClassroomQueryPortImpl` consultando `ClassroomMemberJpaEntity` do módulo `classroom` via JPQL com FQN (mesmo padrão de `curriculum.ClassroomQueryPortImpl`)
- [ ] 3.4 [BE] Configurar MapStruct mapper entre `Announcement` (domínio) e `AnnouncementJpaEntity`

## 4. Backend — Use Cases

- [ ] 4.1 [BE] Implementar `PostAnnouncementService`: valida `ClassroomQueryPort.isMember(professorId, classroomId, orgId, "PROFESSOR")`, persiste anexos (arquivo via `StoragePort` e/ou link), salva aviso, dispara `AnnouncementPostedEvent`
- [ ] 4.2 [BE] Implementar `ListAnnouncementsService`: valida vínculo do chamador com a turma (qualquer papel), retorna lista ordenada decrescente
- [ ] 4.3 [BE] Implementar `EditAnnouncementService`: valida `author_id == jwt.sub` (403 caso contrário), atualiza conteúdo/anexos
- [ ] 4.4 [BE] Implementar `DeleteAnnouncementService`: valida `author_id == jwt.sub` (403 caso contrário), aplica soft delete

## 5. Backend — Endpoints REST

- [ ] 5.1 [BE] Criar `AnnouncementResource` com `POST /classrooms/{classroomId}/announcements` (`@RolesAllowed("PROFESSOR")`)
- [ ] 5.2 [BE] Adicionar `GET /classrooms/{classroomId}/announcements` (`@RolesAllowed({"PROFESSOR","ALUNO"})`)
- [ ] 5.3 [BE] Adicionar `PUT /announcements/{id}` (`@RolesAllowed("PROFESSOR")`)
- [ ] 5.4 [BE] Adicionar `DELETE /announcements/{id}` (`@RolesAllowed("PROFESSOR")`)

## 6. Backend — Testes

- [ ] 6.1 [BE] Testes unitários dos use cases (`PostAnnouncementService`, `ListAnnouncementsService`, `EditAnnouncementService`, `DeleteAnnouncementService`) com Mockito cobrindo cenários de sucesso e 403/404/422 da spec
- [ ] 6.2 [BE] Testes de integração `@QuarkusTest` + Testcontainers para `AnnouncementResource` cobrindo os 4 endpoints e os cenários de autorização (professor não vinculado, aluno tentando publicar/editar/excluir, autor vs não-autor)

## 7. Frontend — Tipos e API

- [ ] 7.1 [FE] Criar tipos `Announcement`, `AnnouncementAttachment` em `features/communication/types.ts`
- [ ] 7.2 [FE] Criar `features/communication/api/announcements.ts` com `listAnnouncements`, `createAnnouncement`, `updateAnnouncement`, `deleteAnnouncement`
- [ ] 7.3 [FE] Criar `features/communication/schemas/announcementSchema.ts` (Zod: `content` obrigatório, `attachments` opcional)

## 8. Frontend — Hooks e componentes

- [ ] 8.1 [FE] Criar hook `useAnnouncements(classroomId)` (TanStack Query, query key `["announcements", classroomId]`)
- [ ] 8.2 [FE] Criar hooks de mutação `useCreateAnnouncement`, `useUpdateAnnouncement`, `useDeleteAnnouncement` com invalidação da query key acima
- [ ] 8.3 [FE] Criar `AnnouncementCard.tsx` (item do feed; menu editar/excluir visível apenas quando `author_id === currentUser.id`)
- [ ] 8.4 [FE] Criar `AnnouncementFeed.tsx` (lista ordenada, estado vazio, loading)
- [ ] 8.5 [FE] Criar `AnnouncementForm.tsx` (React Hook Form + Zod; upload de arquivo e/ou link; usado em criar e editar)
- [ ] 8.6 [FE] Integrar seção "Mural" na página da turma existente (`features/classroom`), exibindo `AnnouncementForm` só para PROFESSOR vinculado e `AnnouncementFeed` para ambos os papéis

## 9. Frontend — Testes

- [ ] 9.1 [FE] Testes Vitest + Testing Library + MSW para `AnnouncementFeed` e `AnnouncementForm` (renderização, submissão, erro de validação, controle de visibilidade por papel)
