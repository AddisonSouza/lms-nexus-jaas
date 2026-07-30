## Context

RF-15 introduz o primeiro recurso do módulo `communication`, ainda inexistente no monólito. Os módulos vizinhos mais próximos em forma são `curriculum` (RF-10, conteúdo por tópico com anexos via `StoragePort` ou link externo) e `assessment` (RF-11/12, eventos de domínio via CDI `Event<T>` sem consumidor síncrono). O mural pertence à turma (`classroom_id`), não à disciplina — qualquer PROFESSOR vinculado à turma (via `classroom_members`, papel `PROFESSOR`) publica avisos; qualquer ALUNO vinculado à mesma turma os lê.

## Goals / Non-Goals

**Goals:**
- CRUD de avisos (criar, editar, excluir-soft, listar) restrito por papel e por vínculo com a turma.
- Suporte a anexos de arquivo (via `StoragePort` existente) e/ou link externo, mistos no mesmo aviso.
- Publicar `AnnouncementPostedEvent` para consumo futuro pelo módulo `notification` (RF-16, fora de escopo aqui).
- Listagem ordenada por `created_at DESC`, sem paginação (consistente com o restante do projeto).

**Non-Goals:**
- Notificações in-app / contador de não lidas (RF-16 — capability futura, apenas o evento é publicado).
- Edição colaborativa, comentários ou reações em avisos.
- Moderação por ADMIN_ORG/GESTOR sobre avisos de outro professor (RF-15 restringe edição/exclusão ao autor).
- Paginação ou cache do feed (reavaliar quando houver dados de uso real).

## Decisions

**1. Novo módulo `communication` com estrutura padrão hexagonal.**
Segue exatamente os outros módulos: `domain/{model,event,exception,port/in,port/out}`, `application/{usecase,dto}`, `infrastructure/persistence`, `interfaces/rest`.

**2. Verificação de vínculo com a turma via Port próprio (`ClassroomQueryPort`), não reuso direto do `ClassroomQueryPort` do `curriculum`.**
Alternativa considerada: importar o port do `curriculum`. Rejeitada — viola a regra "módulos comunicam via interfaces Java, nunca import direto entre bounded contexts dono". Em vez disso, `communication` define seu próprio `ClassroomQueryPort` (domain/port/out) com a forma mínima necessária; o adapter em `communication/infrastructure/persistence` consulta a entidade JPA `ClassroomMemberJpaEntity` do módulo `classroom` via JPQL com FQN — mesmo padrão usado por `curriculum.infrastructure.persistence.ClassroomQueryPortImpl` e por `assessment.infrastructure.persistence.SubjectQueryAdapter`.
```java
public interface ClassroomQueryPort {
    boolean isMember(String userId, String classroomId, String organizationId, String role); // role nullable = qualquer papel
}
```

**3. Anexos: tabela própria `announcement_attachments`, espelhando `submission_attachments`/`task_attachments`.**
Cada attachment é OU um arquivo armazenado via `StoragePort` (`file_key`, `original_name`, `mime_type`, `size_bytes` preenchidos) OU um link externo (`external_url`, `link_title` preenchidos) — mesma dualidade já usada em `subject_contents` (RF-10), implementada como colunas nullable em vez de subtipos separados, para reaproveitar o padrão existente.

**4. Evento publicado mesmo sem consumidor (CDI `Event<AnnouncementPostedEvent>`).**
Mesma decisão já tomada em `TaskSubmittedEvent`/`SubmissionEvaluatedEvent`: o evento é disparado de forma síncrona via `Event<T>.fire()` no use case; a ausência de um `@Observes` listener hoje não impede a publicação. Quando RF-16 for implementado, basta adicionar o listener.

**5. Soft delete e edição restritos ao autor (`author_id`), verificado na aplicação — não há papel de moderação neste RF.**
Tentar editar/excluir aviso de outro professor retorna 403 (`UnauthorizedAnnouncementOperationException`).

**6. Sem paginação no `GET /classrooms/{id}/announcements`.**
Decisão confirmada com o usuário: segue o padrão atual do projeto (nenhuma listagem usa paginação ainda). Reavaliar como trabalho futuro se o volume de avisos crescer.

## Estrutura de pacotes (backend)

```
apps/api/src/main/java/br/edu/lms/module/communication/
  domain/
    model/Announcement.java, AnnouncementId.java, AnnouncementAttachment.java
    event/AnnouncementPostedEvent.java
    exception/AnnouncementNotFoundException.java, UnauthorizedAnnouncementOperationException.java
    port/in/PostAnnouncementUseCase.java, EditAnnouncementUseCase.java, DeleteAnnouncementUseCase.java, ListAnnouncementsUseCase.java
    port/out/AnnouncementRepository.java, ClassroomQueryPort.java
  application/
    usecase/PostAnnouncementService.java, EditAnnouncementService.java, DeleteAnnouncementService.java, ListAnnouncementsService.java
    dto/PostAnnouncementCommand.java, EditAnnouncementCommand.java, AnnouncementResponse.java, AttachmentInput.java
  infrastructure/
    persistence/AnnouncementJpaEntity.java, AnnouncementAttachmentJpaEntity.java, AnnouncementRepositoryImpl.java, ClassroomQueryPortImpl.java
  interfaces/
    rest/AnnouncementResource.java
```

## Migration Flyway

- `V020__create_announcements_table.sql`:
  ```sql
  CREATE TABLE announcements (
      id               VARCHAR(36)  NOT NULL,
      classroom_id     VARCHAR(36)  NOT NULL,
      organization_id  VARCHAR(36)  NOT NULL,
      author_id        VARCHAR(36)  NOT NULL,
      content          LONGTEXT     NOT NULL,
      created_at       DATETIME     NOT NULL,
      updated_at       DATETIME     NOT NULL,
      deleted_at       DATETIME     NULL,
      PRIMARY KEY (id),
      CONSTRAINT fk_announcements_classroom FOREIGN KEY (classroom_id)     REFERENCES classrooms(id),
      CONSTRAINT fk_announcements_org       FOREIGN KEY (organization_id) REFERENCES organizations(id),
      CONSTRAINT fk_announcements_author    FOREIGN KEY (author_id)       REFERENCES users(id),
      INDEX idx_announcements_classroom (classroom_id, created_at),
      INDEX idx_announcements_deleted   (deleted_at)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
  ```
- `V021__create_announcement_attachments_table.sql`:
  ```sql
  CREATE TABLE announcement_attachments (
      id               VARCHAR(36)  NOT NULL,
      announcement_id  VARCHAR(36)  NOT NULL,
      file_key         VARCHAR(512) NULL,
      original_name    VARCHAR(255) NULL,
      mime_type        VARCHAR(127) NULL,
      size_bytes       BIGINT       NULL,
      external_url     TEXT         NULL,
      link_title       VARCHAR(255) NULL,
      created_at       DATETIME     NOT NULL,
      PRIMARY KEY (id),
      CONSTRAINT fk_announcement_attachments_announcement FOREIGN KEY (announcement_id) REFERENCES announcements(id),
      INDEX idx_announcement_attachments_announcement (announcement_id)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
  ```

## Endpoints REST

- `POST /classrooms/{classroomId}/announcements` — `@RolesAllowed("PROFESSOR")`. Cria aviso; valida vínculo PROFESSOR↔turma via `ClassroomQueryPort`; publica `AnnouncementPostedEvent`. Retorna 201.
- `GET /classrooms/{classroomId}/announcements` — `@RolesAllowed({"PROFESSOR","ALUNO"})`. Lista avisos ativos da turma ordenados por `created_at DESC`; valida vínculo do chamador (qualquer papel) com a turma. Retorna 200.
- `PUT /announcements/{id}` — `@RolesAllowed("PROFESSOR")`. Edita conteúdo/anexos; 403 se `author_id != jwt.sub`. Retorna 200.
- `DELETE /announcements/{id}` — `@RolesAllowed("PROFESSOR")`. Soft delete; 403 se `author_id != jwt.sub`. Retorna 204.

## Frontend

- **Feature:** `apps/web/src/features/communication/`
  - `types.ts`: `Announcement`, `AnnouncementAttachment`
  - `api/announcements.ts`: `listAnnouncements`, `createAnnouncement`, `updateAnnouncement`, `deleteAnnouncement`
  - `hooks/useAnnouncements.ts`, `hooks/useAnnouncementMutations.ts` (TanStack Query; query key `["announcements", classroomId]`)
  - `schemas/announcementSchema.ts` (Zod: `content` obrigatório, `attachments` opcional)
  - `components/AnnouncementFeed.tsx` (lista ordenada, somente leitura)
  - `components/AnnouncementForm.tsx` (criar/editar, React Hook Form + Zod, visível só a PROFESSOR)
  - `components/AnnouncementCard.tsx` (item do feed com menu editar/excluir quando `author_id === currentUser.id`)
- **Integração:** nova seção "Mural" na página da turma existente (`features/classroom`), sem rota própria — segue o padrão de abas/seções já usado por conteúdo (RF-10).

## Risks / Trade-offs

- [Risco] Conteúdo `LONGTEXT` sem sanitização HTML poderia permitir XSS se o editor rich-text gerar HTML cru → Mitigação: reutilizar o mesmo componente/sanitização de texto rico já adotado em RF-10/RF-11 (mesma biblioteca, mesma política de sanitização no backend antes de persistir).
- [Risco] Falta de paginação pode degradar performance em turmas com histórico longo de avisos → Mitigação: índice composto `(classroom_id, created_at)`; reavaliar paginação como melhoria futura caso necessário.
- [Trade-off] Eventos publicados sem consumidor (RF-16 ainda não existe) → aceito conscientemente, mesmo padrão já em produção para `TaskSubmittedEvent`.
