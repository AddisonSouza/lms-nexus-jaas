## Context

RF-09 entregou CRUD de disciplinas (`Subject`). As disciplinas existem mas estão vazias — sem tópicos nem materiais. RF-10 acrescenta dois novos aggregates ao módulo `curriculum` e cria o módulo `storage` com a abstração de armazenamento de arquivos.

Estado atual: não existem `StoragePort`, módulo `storage`, entidade `Topic` ou `SubjectContent` em nenhuma camada.

## Goals / Non-Goals

**Goals:**
- CRUD de tópicos por disciplina com ordenação explícita (`position`)
- CRUD de conteúdo por tópico: tipos VIDEO, DOCUMENTO, LINK, ARQUIVO
- Upload de arquivo via `StoragePort` com `LocalStorageAdapter` (dev)
- Listagem agrupada por tópico para PROFESSOR e ALUNO
- Controle de acesso: aluno só acessa conteúdo de disciplinas às quais pertence

**Non-Goals:**
- Adapter S3/MinIO (implementação futura)
- Streaming de vídeo nativo
- Busca full-text em materiais
- Preview inline de documentos

## Decisions

### D1 — Tópicos como aggregate independente em `curriculum`

`Topic` é um aggregate com `id`, `subjectId`, `title`, `position`, `organizationId`. CRUD próprio via `/subjects/{id}/topics`. Faz sentido porque o professor gerencia tópicos antes de adicionar materiais — são entidades de primeira classe, não apenas metadata de conteúdo.

### D2 — `SubjectContent` pertence a `Topic`, não diretamente a `Subject`

`SubjectContent` tem FK para `topic_id`. A listagem de conteúdo por disciplina (`GET /subjects/{id}/contents`) retorna conteúdos agrupados por tópico (JOIN). Isso reflete a hierarquia Subject → Topic → Content.

### D3 — Tipos de conteúdo via enum `ContentType` no domínio

`ContentType`: `VIDEO | DOCUMENTO | LINK | ARQUIVO`. Armazenado como string na coluna `content_type`. Para VIDEO e LINK, o campo `externalUrl` armazena a URL. Para tipos com arquivo, `fileKey` referencia o objeto no `StoragePort`.

### D4 — `StoragePort` definido no módulo `storage`, consumido via Port em `curriculum`

Para evitar dependência direta entre módulos, o módulo `curriculum` acessa storage via interface `StorageQueryPort` (leitura) definida em `curriculum/domain/port/out/`. O módulo `storage` expõe um `StoragePort` público consumido pelo use case de criação de conteúdo (injeção CDI). Sem referência direta ao adapter no domínio.

### D4a — `S3StorageAdapter` com MinIO para dev (S3-compatível)

Ao invés de armazenar em disco (LocalStorageAdapter), o adapter usa a Quarkus Amazon S3 Extension (`quarkus-amazon-s3`). Em dev, apontado para um container **MinIO** via `quarkus.s3.endpoint-override`. Em produção, bastará remover o override e configurar credenciais reais da AWS. Mesma interface `StoragePort` — troca de target sem alterar nenhum use case. MinIO adicionado ao `docker-compose.yml` como serviço `minio`.

### D5 — Endpoint de serving de arquivos em `storage/interfaces/rest/`

`GET /api/files/{fileKey}` — validação de permissão (usuário autenticado, pertence à organização). Serve o stream do arquivo com `Content-Type` correto.

### D6 — Controle de acesso por query port em `curriculum`

Reutiliza `OrganizationMemberQueryPort` já existente. Para ALUNO: valida matrícula em turma que contenha a disciplina antes de retornar conteúdo.

---

## Estrutura de Pacotes

### Backend — módulo `curriculum`

```
curriculum/
  domain/
    model/
      Topic.java                        ← novo aggregate
      TopicId.java                      ← VO
      SubjectContent.java               ← novo aggregate
      SubjectContentId.java             ← VO
      ContentType.java                  ← enum
    exception/
      TopicNotFoundException.java
      ContentNotFoundException.java
      ContentAccessDeniedException.java
    port/in/
      CreateTopicUseCase.java
      UpdateTopicUseCase.java
      DeleteTopicUseCase.java
      ReorderTopicsUseCase.java
      CreateContentUseCase.java
      UpdateContentUseCase.java
      DeleteContentUseCase.java
      ListSubjectContentsUseCase.java
    port/out/
      TopicRepository.java
      ContentRepository.java
  application/
    dto/
      CreateTopicCommand.java
      UpdateTopicCommand.java
      ReorderTopicsCommand.java
      CreateContentCommand.java
      UpdateContentCommand.java
      TopicResponse.java
      SubjectContentResponse.java
      SubjectContentsGroupedResponse.java
    usecase/
      CreateTopicService.java
      UpdateTopicService.java
      DeleteTopicService.java
      ReorderTopicsService.java
      CreateContentService.java           ← chama StoragePort quando há arquivo
      UpdateContentService.java
      DeleteContentService.java
      ListSubjectContentsService.java
  infrastructure/
    persistence/
      TopicJpaEntity.java
      TopicRepositoryImpl.java
      TopicMapper.java                    ← MapStruct
      SubjectContentJpaEntity.java
      SubjectContentRepositoryImpl.java
      SubjectContentMapper.java           ← MapStruct
  interfaces/rest/
    TopicResource.java
    ContentResource.java
    dto/
      CreateTopicRequest.java
      UpdateTopicRequest.java
      ReorderTopicsRequest.java
      CreateContentRequest.java
      UpdateContentRequest.java
```

### Backend — módulo `storage` (novo)

```
storage/
  domain/
    model/
      StoredFile.java                    ← VO: fileKey, originalName, mimeType, sizeBytes
      StorageContext.java                ← enum: LESSON_MATERIAL, TASK_ATTACHMENT
    port/out/
      StoragePort.java                   ← interface pública
  application/usecase/
    ServeFileUseCase.java
  infrastructure/
    S3StorageAdapter.java                ← implementa StoragePort via Quarkus Amazon S3 Extension
                                           dev: MinIO via quarkus.s3.endpoint-override
                                           prod: AWS S3 (só muda config, zero código)
  interfaces/rest/
    FileResource.java                    ← GET /api/files/{fileKey}
```

---

## Migrations Flyway

| Arquivo | Descrição |
|---|---|
| `V013__create_subject_topics_table.sql` | Tabela `subject_topics` (id, subject_id, organization_id, title, position, deleted_at) |
| `V014__create_subject_contents_table.sql` | Tabela `subject_contents` (id, topic_id, organization_id, title, content_type, external_url, file_key, description, position, deleted_at) |

---

## Endpoints REST

### Tópicos
| Método | Path | Role | Descrição |
|---|---|---|---|
| `POST` | `/subjects/{subjectId}/topics` | PROFESSOR | Criar tópico |
| `PUT` | `/subjects/{subjectId}/topics/{id}` | PROFESSOR | Atualizar tópico |
| `DELETE` | `/subjects/{subjectId}/topics/{id}` | PROFESSOR | Excluir tópico (soft delete) |
| `PUT` | `/subjects/{subjectId}/topics/reorder` | PROFESSOR | Reordenar tópicos (array de ids) |
| `GET` | `/subjects/{subjectId}/topics` | PROFESSOR, ALUNO | Listar tópicos |

### Conteúdo
| Método | Path | Role | Descrição |
|---|---|---|---|
| `POST` | `/subjects/{subjectId}/contents` | PROFESSOR | Criar conteúdo (multipart para arquivos) |
| `PUT` | `/subjects/{subjectId}/contents/{id}` | PROFESSOR | Atualizar conteúdo |
| `DELETE` | `/subjects/{subjectId}/contents/{id}` | PROFESSOR | Excluir conteúdo (soft delete) |
| `GET` | `/subjects/{subjectId}/contents` | PROFESSOR, ALUNO | Listar conteúdo agrupado por tópico |

### Storage
| Método | Path | Role | Descrição |
|---|---|---|---|
| `GET` | `/api/files/{fileKey}` | Autenticado | Servir arquivo com validação de permissão |

---

## Frontend — `features/curriculum`

```
features/curriculum/
  api/
    topic-api.ts           ← CRUD de tópicos
    content-api.ts         ← CRUD de conteúdos (FormData para upload)
    query-keys.ts          ← estendido com topics e contents
  components/
    SubjectDetailPage.tsx  ← nova página de detalhe de disciplina
    TopicList.tsx          ← lista de tópicos com collapse (professor e aluno)
    TopicFormDialog.tsx    ← criar/editar tópico
    ContentFormDialog.tsx  ← criar/editar conteúdo por tipo
    ContentCard.tsx        ← card de material (ícone por tipo, link, download)
  hooks/
    useTopics.ts
    useCreateTopic.ts
    useUpdateTopic.ts
    useDeleteTopic.ts
    useReorderTopics.ts
    useSubjectContents.ts
    useCreateContent.ts
    useUpdateContent.ts
    useDeleteContent.ts
  schemas/
    topicSchema.ts         ← Zod
    contentSchema.ts       ← Zod (discriminated union por ContentType)
  types.ts                 ← estendido com Topic, SubjectContent, ContentType
```

Rota nova em `routes.tsx`: `/subjects/:subjectId` → `SubjectDetailPage`.

---

## Risks / Trade-offs

| Risco | Mitigação |
|---|---|
| Upload multipart com limite 50MB pode gerar timeout em conexões lentas | Configurar `quarkus.http.limits.max-body-size=50M`; feedback visual de progresso no FE |
| MinIO container em dev — reinicialização perde arquivos se volume não configurado | `docker-compose.yml` com volume `minio_data`; documentado no README |
| Credenciais MinIO dev expostas em docker-compose | Valores default (`minioadmin/minioadmin`) aceitáveis para dev; prod usa secrets reais via env |
| Reordenação de tópicos: race condition se dois professores reordenam simultaneamente | Aceito para MVP — tópico por disciplina é editado por um professor de cada vez na prática |

### Configuração MinIO no docker-compose.yml

```yaml
minio:
  image: minio/minio:latest
  command: server /data --console-address ":9001"
  ports:
    - "9000:9000"   # S3 API
    - "9001:9001"   # Console Web
  environment:
    MINIO_ROOT_USER: ${MINIO_ACCESS_KEY:-minioadmin}
    MINIO_ROOT_PASSWORD: ${MINIO_SECRET_KEY:-minioadmin}
  volumes:
    - minio_data:/data
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
    interval: 10s
    retries: 3
```

Configuração Quarkus (`application.properties` dev):
```properties
quarkus.s3.endpoint-override=http://minio:9000
quarkus.s3.path-style-access=true
quarkus.s3.aws.region=us-east-1
quarkus.s3.aws.credentials.type=static
quarkus.s3.aws.credentials.static-provider.access-key-id=${MINIO_ACCESS_KEY:minioadmin}
quarkus.s3.aws.credentials.static-provider.secret-access-key=${MINIO_SECRET_KEY:minioadmin}
storage.bucket=${STORAGE_BUCKET:lms-dev}
```
