## 1. [BE] Swagger e Config — Fixes Triviais

- [ ] 1.1 [BE] Remover `quarkus.swagger-ui.always-include=true` de `application.properties`; adicionar `%dev.quarkus.swagger-ui.always-include=true` e `%test.quarkus.swagger-ui.always-include=true`

## 2. [BE] HttpMappable — GlobalExceptionMapper Open/Closed

- [ ] 2.1 [BE] Criar interface `HttpMappable` em `shared/exception/HttpMappable.java` com métodos `int httpStatus()` e `String errorCode()`
- [ ] 2.2 [BE] Fazer todas as domain exceptions existentes em `identity/domain/exception/` implementar `HttpMappable` com os valores do switch atual do `GlobalExceptionMapper`
- [ ] 2.3 [BE] Fazer todas as domain exceptions existentes em `organization/domain/exception/` implementar `HttpMappable`
- [ ] 2.4 [BE] Fazer todas as domain exceptions existentes em `classroom/domain/exception/`, `curriculum/domain/exception/` e `assessment/domain/exception/` implementar `HttpMappable`
- [ ] 2.5 [BE] Refatorar `GlobalExceptionMapper` para usar `instanceof HttpMappable` em vez de 30+ branches individuais; remover imports de exceções de todos os módulos

## 3. [BE] Domain Exceptions — assessment

- [ ] 3.1 [BE] Criar `DeadlineNotInFutureException` em `assessment/domain/exception/` implementando `HttpMappable` (422); substituir `IllegalArgumentException` em `CreateTaskService` linha 54
- [ ] 3.2 [BE] Criar `EmptySubmissionException` em `assessment/domain/exception/` implementando `HttpMappable` (422); substituir `IllegalArgumentException` em `SubmitTaskService` linha 59
- [ ] 3.3 [BE] Criar `GradeExceedsMaxScoreException` e `TaskHasNoMaxScoreException` em `assessment/domain/exception/` implementando `HttpMappable` (422); substituir `IllegalArgumentException` em `EvaluateSubmissionService` linhas 48 e 53
- [ ] 3.4 [BE] Criar `InvalidAttachmentTypeException` em `assessment/domain/exception/` implementando `HttpMappable` (422); substituir uso de `InvalidFileTypeException` (do módulo `curriculum`) em `CreateTaskService` linha 16

## 4. [BE] Domain Exception — identity

- [ ] 4.1 [BE] Criar `UserNotMemberOfOrganizationException` em `identity/domain/exception/` implementando `HttpMappable` (403); substituir uso de `NotAnOrganizationMemberException` (do módulo `organization`) em `RefreshTokenService`

## 5. [BE] DDD — Aggregate Root com Comportamento

- [ ] 5.1 [BE] Adicionar `Task.publish()` em `assessment/domain/model/Task.java`: valida `status == DRAFT` (lança `InvalidTaskStateException`), retorna `this.toBuilder().status(PUBLISHED).build()`
- [ ] 5.2 [BE] Adicionar `Task.close()` em `Task.java`: valida `status == PUBLISHED`, retorna instância com status `CLOSED` (ou `DRAFT` conforme regra de negócio)
- [ ] 5.3 [BE] Adicionar `TaskSubmission.evaluate(BigDecimal grade, String feedback)` em `TaskSubmission.java`: valida `status != EVALUATED` (lança `AlreadyEvaluatedException`), retorna instância com grade, feedback e `status = EVALUATED`
- [ ] 5.4 [BE] Adicionar `TaskSubmission.markLate()` em `TaskSubmission.java`: retorna instância com `status = LATE`
- [ ] 5.5 [BE] Refatorar `PublishTaskService` para chamar `task.publish()` em vez de validar status e usar `toBuilder()` externamente
- [ ] 5.6 [BE] Refatorar `EvaluateSubmissionService` para chamar `submission.evaluate(grade, feedback)` em vez de validar e mutar externamente

## 6. [BE] Domain Event — TaskPublishedEvent

- [ ] 6.1 [BE] Criar `TaskPublishedEvent` em `assessment/domain/event/` com campos `taskId`, `subjectId`, `organizationId`
- [ ] 6.2 [BE] Substituir disparo de `TaskCreatedEvent` por `TaskPublishedEvent` em `PublishTaskService` (linha 46); injetar `Event<TaskPublishedEvent>` no lugar de `Event<TaskCreatedEvent>`

## 7. [BE] Clean Architecture — TokenGeneratorPort em identity

- [ ] 7.1 [BE] Criar `TokenGeneratorPort` em `identity/domain/port/out/` com métodos `generateAccessToken(String userId)` e `generateAccessToken(String userId, String orgId, String role)`
- [ ] 7.2 [BE] Fazer `JwtTokenService` implementar `TokenGeneratorPort`
- [ ] 7.3 [BE] Substituir injeção de `JwtTokenService` por `TokenGeneratorPort` em `AuthenticateService` e `RefreshTokenService`; remover imports de `infrastructure/security/`

## 8. [BE] Clean Architecture — Use Cases de Listagem em assessment

- [ ] 8.1 [BE] Criar `ListTasksUseCase` em `assessment/domain/port/in/` e `ListTasksService` em `assessment/application/usecase/` que chama `taskRepository.findByOrganizationAndCreatedBy(orgId, userId)`
- [ ] 8.2 [BE] Criar `ListPublishedTasksUseCase` em `assessment/domain/port/in/` e `ListPublishedTasksService` em `assessment/application/usecase/` que chama `taskRepository.findPublishedByOrganization(orgId)`
- [ ] 8.3 [BE] Criar `ListSubmissionsUseCase` em `assessment/domain/port/in/` e `ListSubmissionsService` em `assessment/application/usecase/` que valida ownership (`task.getCreatedBy().equals(userId)`) e chama `submissionRepository.findByTask(taskId, orgId)`
- [ ] 8.4 [BE] Substituir injeções de `TaskRepository` e `SubmissionRepository` em `TaskResource` pelos três novos use cases; remover chamadas diretas a repositórios do controller

## 9. [BE] MapStruct — Eliminar Mapeamento Manual

- [ ] 9.1 [BE] Criar `SubmissionMapper` em `assessment/infrastructure/persistence/` com `@Mapper(componentModel = "cdi")`; mapear `TaskSubmission ↔ SubmissionEntity`
- [ ] 9.2 [BE] Substituir `toEntity()` e `toDomain()` manuais em `SubmissionRepositoryImpl` pelo `SubmissionMapper` injetado
- [ ] 9.3 [BE] Fazer `TaskRepositoryImpl` injetar e usar o `TaskMapper` existente em vez dos métodos `toEntity()`/`toDomain()` manuais
- [ ] 9.4 [BE] Criar `TaskResponseMapper` (ou expandir `TaskMapper`) para mapear `Task → TaskResponse` e `TaskSubmission → SubmissionResponse`
- [ ] 9.5 [BE] Remover métodos estáticos `toResponse()` de `CreateTaskService` e `SubmitTaskService`; demais serviços que os chamam passam a usar `TaskResponseMapper`

## 10. [BE] Infraestrutura — LocalStorageAdapter

- [ ] 10.1 [BE] Criar `LocalStorageAdapter` em `module/storage/infrastructure/` implementando `StoragePort`; salva em `{project.root}/data/uploads/{context}/{ano}/{mes}/{uuid}-{filename}`; retorna `StoredFile` com `fileKey` relativo
- [ ] 10.2 [BE] Anotar `LocalStorageAdapter` com `@IfBuildProfile("dev")` e `S3StorageAdapter` com `@IfBuildProfile(anyOf = {"prod", "staging"})` para seleção automática por CDI
- [ ] 10.3 [BE] Adicionar endpoint de serving local em `StorageResource` para profile `dev` (ou verificar se o endpoint existente já suporta `LocalStorageAdapter`)

## 11. [BE] Testes — Testcontainers

- [ ] 11.1 [BE] Adicionar dependência `io.quarkus:quarkus-test-containers` e `org.testcontainers:mysql` ao `pom.xml`
- [ ] 11.2 [BE] Criar `MySQLTestResource` implementando `QuarkusTestResourceLifecycleManager` que sobe container MySQL e injeta URL no `%test.quarkus.datasource.jdbc.url`
- [ ] 11.3 [BE] Criar `RedisTestResource` analogamente para Redis Testcontainer
- [ ] 11.4 [BE] Anotar `TaskResourceIT` e `SubmissionResourceIT` com `@QuarkusTestResource(MySQLTestResource.class)` e `@QuarkusTestResource(RedisTestResource.class)`; remover URL hardcoded de `application.properties`

## 12. [BE] Testes — Domain Tests para assessment

- [ ] 12.1 [BE] Criar `TaskTest.java` em `assessment/domain/` (plain Java + Lombok, sem Quarkus): testar `publish()` em DRAFT (sucesso), `publish()` em PUBLISHED (lança `InvalidTaskStateException`), `close()` em PUBLISHED (sucesso)
- [ ] 12.2 [BE] Criar `TaskSubmissionTest.java` em `assessment/domain/`: testar `evaluate(grade, feedback)` em SUBMITTED (sucesso), `evaluate()` em EVALUATED (lança `AlreadyEvaluatedException`), `markLate()` em SUBMITTED (sucesso)
