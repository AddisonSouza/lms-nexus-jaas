# fix-be-arch-breaks — Delta

## Added
- Capability `file-storage`: `LocalStorageAdapter` armazena arquivos no filesystem local, selecionado automaticamente no profile `dev` — dispensa MinIO/S3 em desenvolvimento.
- `TokenGeneratorPort` em `identity/domain/port/out/`, implementada por `JwtTokenService`.
- Use cases `ListTasksUseCase`, `ListPublishedTasksUseCase` e `ListSubmissionsUseCase` em `assessment/`.
- Domain exceptions em `assessment/domain/exception/`: `DeadlineNotInFutureException`, `EmptySubmissionException`, `GradeExceedsMaxScoreException`, `TaskHasNoMaxScoreException`.
- Interface `HttpMappable` em `shared/exception/`, implementada pelas domain exceptions de todos os módulos.

## Changed
- `task-publishing`: o evento disparado ao publicar passa a ser `TaskPublishedEvent` (antes documentado como `TaskCreatedEvent`).
- `AuthenticateService` e `RefreshTokenService` injetam `TokenGeneratorPort` em vez do concreto `JwtTokenService` — restaura a Regra de Dependência.
- `TaskResource` deixa de injetar `TaskRepository`/`SubmissionRepository` e passa a chamar os use cases.
- `Task` e `TaskSubmission` protegem as próprias invariantes (`publish()`, `close()`, `evaluate()`, `markLate()`); validações de estado saíram dos serviços para dentro das entidades.
- `GlobalExceptionMapper` resolve status e código via `HttpMappable`, substituindo a cadeia de 30+ `instanceof`.
- `quarkus.swagger-ui.always-include` restrito aos profiles `%dev` e `%test`.

## Removed
- Imports cruzados diretos entre módulos, que acoplavam bounded contexts.
