## Context

O backend `apps/api` viola a Regra de Dependência da Clean Architecture em 3 pontos críticos: (1) `application/` importa concretos de `infrastructure/`, (2) `interfaces/` (REST) acessa `domain/port/out/` diretamente sem passar por `application/`, (3) módulos de negócio importam exceções entre si. Além disso, entidades de domínio são POJOs passivos — todas as invariantes residem nos serviços, violando o princípio de Aggregate Root do DDD. O `GlobalExceptionMapper` centraliza o mapeamento de 30+ tipos de exceção de todos os módulos, criando acoplamento total à implementação interna de cada bounded context.

Estado atual dos fluxos problemáticos:

```
TaskResource → TaskRepository (DIRETO, sem Use Case)   ← viola RD-06
AuthenticateService → BcryptPasswordService (DIRETO)   ← viola RD-03
CreateTaskService → InvalidFileTypeException (curriculum) ← viola MOD-01
PublishTaskService.status != DRAFT check               ← fora do Aggregate Root
GlobalExceptionMapper: 30+ instanceof de todos módulos ← viola OCP
```

## Goals / Non-Goals

**Goals:**
- `application/` depende apenas de `domain/` (RD-04 compliant)
- `interfaces/` chama apenas `application/usecase/` (RD-06 compliant)
- Cada módulo encapsula suas próprias exceções (MOD-01 compliant)
- `Task` e `TaskSubmission` são Aggregate Roots com invariantes internas
- `GlobalExceptionMapper` é open/closed: novas exceções não exigem mudança no mapper
- Dev funciona sem MinIO para upload de arquivos
- Swagger UI invisível em produção
- Testes de integração isolados (Testcontainers), sem banco hardcoded

**Non-Goals:**
- Migrar módulos `identity`, `organization`, `classroom`, `curriculum` para DDD com comportamento (apenas `assessment`)
- Alterar contratos HTTP (endpoints, status codes, payloads permanecem)
- Implementar novos RFs

## Decisions

### D1 — `TokenGeneratorPort` em `identity/domain/port/out/`

**Escolha:** Criar interface `TokenGeneratorPort` com dois métodos:
```java
public interface TokenGeneratorPort {
    String generateAccessToken(String userId);
    String generateAccessToken(String userId, String orgId, String role);
}
```
`JwtTokenService` implementa a Port. `AuthenticateService` e `RefreshTokenService` injetam `TokenGeneratorPort`.

**Motivo:** Segue o mesmo padrão de `PasswordHasher` que já existe como Port em `identity/domain/port/out/`. A camada `application/` passa a depender apenas de `domain/`, nunca de `infrastructure/security/`.

**Alternativa descartada:** Mover `JwtTokenService` para `domain/` — incorreto; geração de JWT com Smallrye depende do framework, pertence a `infrastructure/`.

---

### D2 — Três novos Use Cases para listagem em `assessment/`

**Escolha:** Criar os três use cases ausentes seguindo o padrão estabelecido por `CreateTaskUseCase`:

```
assessment/domain/port/in/ListTasksUseCase.java
assessment/application/usecase/ListTasksService.java

assessment/domain/port/in/ListPublishedTasksUseCase.java
assessment/application/usecase/ListPublishedTasksService.java

assessment/domain/port/in/ListSubmissionsUseCase.java
assessment/application/usecase/ListSubmissionsService.java
```

`ListSubmissionsService` incorpora a lógica de autorização hoje inline no `TaskResource` (`task.getCreatedBy().equals(userId)`).

**Motivo:** RD-06 é inequívoco: `interfaces/` não pode tocar `domain/`. Os repositórios saem do `TaskResource`.

---

### D3 — Aggregate Root com comportamento em `Task` e `TaskSubmission`

**Escolha:** Adicionar métodos que encapsulam transições de estado:

```java
// Task.java
public Task publish() {
    if (this.status != TaskStatus.DRAFT) throw new InvalidTaskStateException(...)
    return this.toBuilder().status(TaskStatus.PUBLISHED).build();
}
public Task close() { ... }

// TaskSubmission.java
public TaskSubmission evaluate(BigDecimal grade, String feedback) {
    if (this.status == SubmissionStatus.EVALUATED) throw new AlreadyEvaluatedException(...)
    return this.toBuilder().grade(grade).feedback(feedback).status(EVALUATED).build();
}
public TaskSubmission markLate() {
    return this.toBuilder().status(SubmissionStatus.LATE).build();
}
```

Os serviços (`PublishTaskService`, `EvaluateSubmissionService`) passam a chamar `task.publish()` e `submission.evaluate(...)`.

**Motivo:** DDD diz que o Aggregate Root é o guardião das suas próprias invariantes. A validação de estado em serviços externo é um sintoma de Anemic Domain Model.

---

### D4 — `HttpMappable` interface em `shared/exception/`

**Escolha:** Criar interface no pacote compartilhado:

```java
// shared/exception/HttpMappable.java
public interface HttpMappable {
    int httpStatus();
    String errorCode();
}
```

Todas as `DomainException` implementam `HttpMappable`. O `GlobalExceptionMapper` reduz para:

```java
if (e instanceof HttpMappable m)
    return Response.status(m.httpStatus()).entity(Map.of("error", m.errorCode())).build();
```

**Motivo:** Open/Closed Principle — novas exceções de qualquer módulo não exigem toque no `GlobalExceptionMapper`. O mapper deixa de importar exceções de 6 módulos.

**Alternativa considerada:** Usar anotações JAX-RS `@ResponseStatus` — requer biblioteca externa e vincula domínio ao framework HTTP. Rejeitado.

---

### D5 — `LocalStorageAdapter` com seleção por `@IfBuildProfile`

**Escolha:** Usar `@IfBuildProfile("dev")` e `@IfBuildProfile("prod")` do Quarkus CDI para selecionar o adaptador:

```java
@IfBuildProfile("dev")
public class LocalStorageAdapter implements StoragePort { ... }

@IfBuildProfile(anyOf = {"prod", "staging"})
public class S3StorageAdapter implements StoragePort { ... }
```

Sem configuração extra — o profile de build já existente (`%dev`, `%prod`) seleciona automaticamente.

**Alternativa descartada:** `@ConfigProperty(name = "storage.provider")` + `Instance<StoragePort>` — mais verboso e requer configuração adicional por ambiente.

---

### D6 — Testcontainers com `@QuarkusTestResource`

**Escolha:** Criar `MySQLTestResource` e `RedisTestResource` usando `io.quarkus:quarkus-test-containers`. Adicionar `@QuarkusTestResource` às classes `*ResourceIT`.

**Motivação:** Remove dependência de MySQL na porta 3307 hardcoded. Cada execução tem banco isolado.

---

## Risks / Trade-offs

| Risco | Mitigação |
|---|---|
| `HttpMappable` exige modificar TODAS as domain exceptions existentes | Fazer de forma mecânica: cada exception recebe `httpStatus()` e `errorCode()` copiados do switch atual do `GlobalExceptionMapper` antes de removê-los |
| `@IfBuildProfile` não funciona se o adaptor S3 não tiver MinIO configurado em `%test` | Criar `LocalStorageAdapter` sem profile annotation para `test` também — usar `@IfBuildProfile(noneOf = "prod")` |
| Mover invariantes para entidades pode quebrar chamadas existentes nos serviços | `Task.publish()` retorna nova instância (imutável via `@Builder`) — substituição direta de `task.toBuilder().status(PUBLISHED).build()` por `task.publish()` |
| Testcontainers aumenta tempo de build dos ITs | Esperado e aceitável — garante isolamento |

## Migration Plan

1. **Sem migration Flyway** — nenhuma mudança de schema
2. **Sem mudança de contrato HTTP** — endpoints, payloads e status codes idênticos
3. **Ordem recomendada** (por dependência):
   1. `HttpMappable` + refatorar todas as domain exceptions (base para os novos use cases)
   2. Domain exceptions de `assessment` (items 8)
   3. DDD behavior em `Task` e `TaskSubmission` (item 7)
   4. Novos Use Cases em `assessment` (items 3, 4)
   5. MapStruct mappers (item 5) — pode ser paralelo ao passo 4
   6. `TokenGeneratorPort` em `identity` (item 2)
   7. Cross-module exceptions (`InvalidAttachmentTypeException`, `UserNotMemberOfOrganizationException`)
   8. `TaskPublishedEvent` (item 9)
   9. `LocalStorageAdapter` (item 12) — independente
   10. Swagger config (item 11) — trivial, pode ser primeiro commit
   11. Testcontainers + testes de domínio (itens 13, 14) — ao final
4. **Rollback:** Revert do branch — sem side effects em produção

## Open Questions

- Nenhuma — todas as decisões foram resolvidas na fase de grill.
