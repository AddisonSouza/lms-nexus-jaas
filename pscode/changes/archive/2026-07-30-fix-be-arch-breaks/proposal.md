## Why

A análise de arquitetura do backend identificou 14 violações ativas das decisões documentadas em `DECISIONS.md` (excluído o item 1 — chave RSA, tratado em hotfix separado). As mais críticas violam a Regra de Dependência da Clean Architecture: `application/` importa diretamente classes de `infrastructure/`, e `interfaces/` (REST) acessa repositórios de domínio sem passar por Use Cases. Adicionalmente, entidades de domínio não protegem suas próprias invariantes (DDD), imports cruzados entre módulos acoplam bounded contexts, e o `GlobalExceptionMapper` acumula 30+ `instanceof` acoplado a todos os módulos. Esta change resolve a dívida técnica antes do ciclo de implementação de novos RFs.

## What Changes

**Clean Architecture (Regra de Dependência):**
- Criar `TokenGeneratorPort` em `identity/domain/port/out/`; `JwtTokenService` passa a implementá-la; `AuthenticateService` e `RefreshTokenService` injetam a Port (não o concreto)
- Criar `ListTasksUseCase` + `ListTasksService`, `ListPublishedTasksUseCase` + `ListPublishedTasksService`, `ListSubmissionsUseCase` + `ListSubmissionsService` em `assessment/`; remover injeção de `TaskRepository`/`SubmissionRepository` de `TaskResource`

**DDD — Entidades com Comportamento:**
- Adicionar `Task.publish()`, `Task.close()` e `TaskSubmission.evaluate(grade, feedback)`, `TaskSubmission.markLate()` com invariantes internas; mover validações de estado dos serviços para dentro das entidades

**Domain Exceptions:**
- Criar `DeadlineNotInFutureException`, `EmptySubmissionException`, `GradeExceedsMaxScoreException`, `TaskHasNoMaxScoreException` em `assessment/domain/exception/`; substituir `IllegalArgumentException` nos serviços
- Criar `UserNotMemberOfOrganizationException` em `identity/domain/exception/`; remover import de `organization` de `RefreshTokenService`
- Criar `InvalidAttachmentTypeException` em `assessment/domain/exception/`; remover import de `curriculum` de `CreateTaskService`

**MapStruct:**
- `TaskRepositoryImpl` e `SubmissionRepositoryImpl` usam `TaskMapper` existente e novo `SubmissionMapper` (elimina `toEntity()`/`toDomain()` manuais)
- Criar `TaskResponseMapper`; eliminar `toResponse()` estáticos de `CreateTaskService` e `SubmitTaskService`

**Domain Events:**
- Criar `TaskPublishedEvent`; `PublishTaskService` dispara `TaskPublishedEvent` (não `TaskCreatedEvent`)

**GlobalExceptionMapper:**
- Criar interface `HttpMappable` em `shared/exception/`; todas as domain exceptions implementam a interface; `GlobalExceptionMapper` reduz para ~20 linhas com `instanceof HttpMappable`

**Infraestrutura:**
- Criar `LocalStorageAdapter` para dev (filesystem local, sem MinIO)
- Remover `quarkus.swagger-ui.always-include=true`; adicionar `%dev` e `%test` profile-scoped
- Adicionar `quarkus-test-containers` ao `pom.xml`; criar `@QuarkusTestResource` para MySQL e Redis

**Testes:**
- Criar `TaskTest.java` e `TaskSubmissionTest.java` com testes de domínio para os novos métodos comportamentais

## Capabilities

### New Capabilities
- (nenhuma — esta change é inteiramente refatoração de código de produção existente e infraestrutura de teste)

### Modified Capabilities
- `task-publishing`: evento disparado ao publicar muda de `TaskCreatedEvent` para `TaskPublishedEvent`
- `file-storage`: `LocalStorageAdapter` habilita dev sem MinIO; cenário de dev sem S3 passa a ser suportado

## Impact

- **Backend apenas** (`apps/api/src/`)
- Módulos afetados: `identity` (application, infrastructure/security), `assessment` (domain, application, infrastructure, interfaces), `storage` (infrastructure), `shared` (exception)
- Sem alteração de schema de banco (sem migration Flyway)
- Sem mudança de contratos de API (endpoints, payloads, status HTTP permanecem idênticos)
- Sem impacto no frontend

## Non-goals

- Item 1 (chave RSA commitada) — hotfix separado
- Implementar novos RFs ou features
- Migrar outros módulos para DDD com comportamento (apenas `assessment`)
- Adicionar testes de integração além de Testcontainers para MySQL e Redis
