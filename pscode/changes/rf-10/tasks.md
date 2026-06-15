## 1. Storage Module

- [x] 1.1 [INFRA] Criar módulo `storage`: pacotes `domain/model/`, `domain/port/out/`, `application/usecase/`, `infrastructure/`, `interfaces/rest/`
- [x] 1.2 [BE] Criar VOs `StoredFile` e enum `StorageContext` (`LESSON_MATERIAL`, `TASK_ATTACHMENT`) em `storage/domain/model/`
- [x] 1.3 [BE] Criar interface `StoragePort` em `storage/domain/port/out/` com métodos `store`, `retrieve`, `delete`, `getPublicUrl`
- [x] 1.4 [INFRA] Adicionar dependência `quarkus-amazon-s3` ao `pom.xml`; adicionar serviço `minio` ao `docker-compose.yml` com volume `minio_data` e healthcheck
- [x] 1.5 [BE] Implementar `S3StorageAdapter` em `storage/infrastructure/` — usa Quarkus S3 Client; key pattern `{context}/{ano}/{mes}/{uuid}-{filename}`; em dev aponta para MinIO via `quarkus.s3.endpoint-override`
- [x] 1.6 [BE] Criar `FileResource` em `storage/interfaces/rest/` — `GET /api/files/{fileKey}` com autenticação; chama `ServeFileUseCase`
- [x] 1.7 [INFRA] Adicionar configurações MinIO/S3 ao `application.properties` (endpoint-override, credentials, bucket) e variáveis ao `.env.example`

## 2. Flyway Migrations

- [x] 2.1 [INFRA] Criar `V013__create_subject_topics_table.sql` — tabela `subject_topics` (`id`, `subject_id`, `organization_id`, `title`, `position INT NOT NULL`, `created_at`, `updated_at`, `deleted_at`; FK para `subjects`)
- [x] 2.2 [INFRA] Criar `V014__create_subject_contents_table.sql` — tabela `subject_contents` (`id`, `topic_id`, `organization_id`, `title`, `content_type VARCHAR(20)`, `external_url TEXT NULL`, `file_key VARCHAR(512) NULL`, `description TEXT NULL`, `position INT NOT NULL`, `created_at`, `updated_at`, `deleted_at`; FK para `subject_topics`)

## 3. Domain — Topic

- [x] 3.1 [BE] Criar VO `TopicId` e domain model `Topic` em `curriculum/domain/model/` (`@Getter @Builder @EqualsAndHashCode`)
- [x] 3.2 [BE] Criar ports de entrada em `curriculum/domain/port/in/`: `CreateTopicUseCase`, `UpdateTopicUseCase`, `DeleteTopicUseCase`, `ReorderTopicsUseCase`, `ListTopicsUseCase`
- [x] 3.3 [BE] Criar `TopicRepository` em `curriculum/domain/port/out/`
- [x] 3.4 [BE] Criar exceptions `TopicNotFoundException`, `TopicAccessDeniedException` em `curriculum/domain/exception/`

## 4. Domain — SubjectContent

- [x] 4.1 [BE] Criar enum `ContentType` (`VIDEO`, `DOCUMENTO`, `LINK`, `ARQUIVO`) e VO `SubjectContentId` em `curriculum/domain/model/`
- [x] 4.2 [BE] Criar domain model `SubjectContent` em `curriculum/domain/model/`
- [x] 4.3 [BE] Criar ports de entrada: `CreateContentUseCase`, `UpdateContentUseCase`, `DeleteContentUseCase`, `ListSubjectContentsUseCase`
- [x] 4.4 [BE] Criar `ContentRepository` em `curriculum/domain/port/out/`
- [x] 4.5 [BE] Criar exceptions `ContentNotFoundException`, `ContentAccessDeniedException`, `InvalidFileTypeException`

## 5. Infrastructure — Persistence (curriculum)

- [x] 5.1 [BE] Criar `TopicJpaEntity` + `TopicRepositoryImpl` + `TopicMapper` (MapStruct) em `curriculum/infrastructure/persistence/`
- [x] 5.2 [BE] Criar `SubjectContentJpaEntity` + `SubjectContentRepositoryImpl` + `SubjectContentMapper` (MapStruct) em `curriculum/infrastructure/persistence/`

## 6. Application — Use Cases Topic

- [x] 6.1 [BE] Implementar `CreateTopicService`: valida que `subjectId` pertence à org do JWT, atribui `position = max + 1`, persiste
- [x] 6.2 [BE] Implementar `UpdateTopicService`: atualiza `title`; valida ownership
- [x] 6.3 [BE] Implementar `DeleteTopicService`: soft delete do topic + soft delete em cascata de todos os `SubjectContent` do tópico
- [x] 6.4 [BE] Implementar `ReorderTopicsService`: valida que todos os ids pertencem ao `subjectId`; atualiza `position` de cada topic
- [x] 6.5 [BE] Implementar `ListTopicsService`: filtra por `subjectId` + `organizationId` + `deletedAt IS NULL`, ordena por `position`

## 7. Application — Use Cases Content

- [x] 7.1 [BE] Implementar `CreateContentService`: valida `topicId`, determina se é URL ou arquivo, chama `StoragePort.store()` para tipos com arquivo, persiste
- [x] 7.2 [BE] Implementar `UpdateContentService`: permite alterar `title`, `description`, `externalUrl`; substituição de arquivo requer soft delete do fileKey antigo + novo store
- [x] 7.3 [BE] Implementar `DeleteContentService`: soft delete do conteúdo; se `fileKey` presente, chama `StoragePort.delete()`
- [x] 7.4 [BE] Implementar `ListSubjectContentsService`: retorna conteúdos agrupados por tópico; valida acesso do ALUNO via `OrganizationMemberQueryPort`

## 8. REST Resources (curriculum)

- [x] 8.1 [BE] Criar `TopicResource` em `curriculum/interfaces/rest/`: `POST`, `PUT`, `DELETE`, `PUT /reorder`, `GET` para `/subjects/{subjectId}/topics`; `@RolesAllowed` corretos
- [x] 8.2 [BE] Criar `ContentResource` em `curriculum/interfaces/rest/`: `POST` (multipart), `PUT`, `DELETE`, `GET` para `/subjects/{subjectId}/contents`; limite 50MB configurado
- [x] 8.3 [BE] Criar DTOs REST: `CreateTopicRequest`, `UpdateTopicRequest`, `ReorderTopicsRequest`, `UpdateContentRequest` e responses correspondentes

## 9. Testes Backend

- [ ] 9.1 [BE] Testes unitários: `CreateTopicServiceTest`, `DeleteTopicServiceTest`, `ReorderTopicsServiceTest` com Mockito
- [ ] 9.2 [BE] Testes unitários: `CreateContentServiceTest`, `DeleteContentServiceTest` com mocks de `StoragePort` e `TopicRepository`
- [ ] 9.3 [BE] Testes de integração `@QuarkusTest` + Testcontainers: `TopicResourceIT` (CRUD completo, RBAC, validações)
- [ ] 9.4 [BE] Testes de integração `@QuarkusTest`: `ContentResourceIT` (CRUD, upload multipart, controle de acesso aluno)

## 10. Frontend — Setup e Types

- [ ] 10.1 [FE] Estender `features/curriculum/types.ts` com interfaces `Topic`, `SubjectContent`, `ContentType`, `SubjectContentsGrouped`
- [ ] 10.2 [FE] Criar `topic-api.ts` e `content-api.ts` em `features/curriculum/api/`; estender `query-keys.ts` com chaves `topics` e `contents`
- [ ] 10.3 [FE] Criar schemas Zod `topicSchema.ts` e `contentSchema.ts` (discriminated union por `ContentType`) em `features/curriculum/schemas/`

## 11. Frontend — Hooks

- [ ] 11.1 [FE] Criar hooks de tópico: `useTopics`, `useCreateTopic`, `useUpdateTopic`, `useDeleteTopic`, `useReorderTopics`
- [ ] 11.2 [FE] Criar hooks de conteúdo: `useSubjectContents`, `useCreateContent`, `useUpdateContent`, `useDeleteContent`

## 12. Frontend — Componentes

- [ ] 12.1 [FE] Criar `SubjectDetailPage.tsx` — página de detalhe da disciplina; rota `/subjects/:subjectId` adicionada em `routes.tsx`
- [ ] 12.2 [FE] Criar `TopicList.tsx` — lista de tópicos com accordion (collapse); cada tópico mostra conteúdos; professor vê botões de ação
- [ ] 12.3 [FE] Criar `TopicFormDialog.tsx` — dialog de criar/editar tópico com React Hook Form + Zod
- [ ] 12.4 [FE] Criar `ContentFormDialog.tsx` — dialog de criar/editar conteúdo; campos variam por `ContentType` (discriminated union); `FileUpload` para tipos com arquivo
- [ ] 12.5 [FE] Criar `ContentCard.tsx` — card de material com ícone por tipo (Lucide), link externo ou download
- [ ] 12.6 [FE] Adicionar link "Ver disciplina" na `SubjectListPage.tsx` para navegar ao detalhe

## 13. Testes Frontend

- [ ] 13.1 [FE] Testes de `TopicFormDialog` com Testing Library + MSW: criação e validação de erros
- [ ] 13.2 [FE] Testes de `ContentFormDialog`: renderização por tipo, validação Zod, submit com FormData
- [ ] 13.3 [FE] Testes de `useCreateContent` e `useCreateTopic` com renderHook
