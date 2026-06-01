## Context

O módulo `identity` não existe ainda. Esta é a primeira implementação do sistema — define os padrões que todos os demais módulos deverão seguir. A arquitetura é Monolito Modular + DDD + Clean Architecture + Hexagonal (ADR-008). Toda comunicação entre módulos é via Ports Java, nunca HTTP.

Restrições:
- `domain/` não pode importar Jakarta Persistence, Quarkus ou Lombok `@Data`/`@Entity`
- Senha: BCrypt custo mínimo 12 (SEC-01)
- Tabela `users` é global — sem `organization_id` (DB-MT-04)
- Soft delete obrigatório: coluna `deleted_at` (DB-05)
- Flyway obrigatório, `database.generation=update` proibido (DB-01)

## Goals / Non-Goals

**Goals:**
- Implementar `POST /auth/register` com criação de usuário e envio de e-mail de confirmação
- Estabelecer a estrutura de pacotes do módulo `identity` como template para os demais módulos
- Definir `UserRegisteredEvent` como primeiro Domain Event do sistema

**Non-Goals:**
- Login/logout (RF-02), recuperação de senha (RF-03), confirmação do token (RF-04)
- Qualquer lógica de organização ou papel

## Decisions

### 1. Estrutura de pacotes do módulo `identity`

```
apps/api/src/main/java/br/edu/lms/module/identity/
├── domain/
│   ├── model/
│   │   ├── User.java              # Aggregate Root — @Getter @Builder @EqualsAndHashCode
│   │   ├── UserId.java            # Value Object — @Value
│   │   ├── Email.java             # Value Object — @Value (valida formato no construtor)
│   │   ├── FullName.java          # Value Object — @Value
│   │   └── UserStatus.java        # Enum: PENDING_CONFIRMATION, ACTIVE, SUSPENDED
│   ├── event/
│   │   └── UserRegisteredEvent.java  # Record ou @Value
│   ├── exception/
│   │   └── EmailAlreadyInUseException.java
│   └── port/
│       ├── in/
│       │   └── RegisterUserUseCase.java   # interface: execute(RegisterUserCommand)
│       └── out/
│           ├── UserRepository.java        # interface: save(User), findByEmail(Email)
│           └── EmailPort.java             # interface: sendConfirmationEmail(Email, String token)
├── application/
│   ├── usecase/
│   │   └── RegisterUserService.java   # @ApplicationScoped @RequiredArgsConstructor @Slf4j
│   └── dto/
│       ├── RegisterUserCommand.java   # @Builder: fullName, email, rawPassword
│       └── RegisterUserResponse.java  # @Builder: userId, email, status
├── infrastructure/
│   ├── persistence/
│   │   ├── UserJpaEntity.java         # @Data @Entity @Table(name="users")
│   │   ├── UserRepositoryImpl.java    # @ApplicationScoped implements UserRepository
│   │   └── UserMapper.java            # @Mapper (MapStruct): User ↔ UserJpaEntity
│   ├── security/
│   │   └── BcryptPasswordService.java # @ApplicationScoped: hash + verify
│   └── mail/
│       └── QuarkusMailAdapter.java    # @ApplicationScoped implements EmailPort
└── interfaces/
    └── rest/
        ├── AuthResource.java          # @Path("/auth") @POST("/register")
        └── dto/
            ├── RegisterRequest.java   # Bean Validation: @NotBlank, @Email, @Size(min=8)
            └── RegisterResponse.java
```

### 2. Fluxo de cadastro

```
POST /auth/register
  → AuthResource.register(RegisterRequest)
  → RegisterUserUseCase.execute(RegisterUserCommand)
    → RegisterUserService:
        1. Email email = new Email(command.email())          # valida formato
        2. userRepository.findByEmail(email) → 409 se existe
        3. String hash = bcrypt.hash(command.rawPassword())
        4. User user = User.builder().id(UserId.generate())
                           .email(email).status(PENDING_CONFIRMATION)
                           .passwordHash(hash).build()
        5. userRepository.save(user)
        6. String token = UUID.randomUUID().toString()
           emailPort.sendConfirmationEmail(email, token)     # async via CDI Event
        7. Event.fire(new UserRegisteredEvent(user.id(), email))
        8. return RegisterUserResponse
```

### 3. Migration Flyway

Arquivo: `apps/api/src/main/resources/db/migration/V001__create_users_table.sql`

```sql
CREATE TABLE users (
    id          VARCHAR(36)  NOT NULL,
    full_name   VARCHAR(150) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password_hash VARCHAR(72) NOT NULL,
    status      ENUM('PENDING_CONFIRMATION','ACTIVE','SUSPENDED') NOT NULL DEFAULT 'PENDING_CONFIRMATION',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP    NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
);
```

Arquivo: `V002__create_email_confirmation_tokens_table.sql`

```sql
CREATE TABLE email_confirmation_tokens (
    id         VARCHAR(36)  NOT NULL,
    user_id    VARCHAR(36)  NOT NULL,
    token      VARCHAR(36)  NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    used_at    TIMESTAMP    NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_token (token),
    CONSTRAINT fk_ect_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 4. Endpoint e contrato REST

| Método | Path | Auth | Descrição |
|---|---|---|---|
| `POST` | `/auth/register` | Público | Cadastro de usuário |

Request body:
```json
{ "fullName": "João Silva", "email": "joao@email.com", "password": "senha123" }
```

Respostas:
- `201 Created` → `{ "userId": "...", "email": "...", "status": "PENDING_CONFIRMATION" }`
- `409 Conflict` → `{ "error": "E-mail já em uso" }`
- `422 Unprocessable Entity` → `{ "errors": ["senha deve ter no mínimo 8 caracteres"] }`

### 5. Frontend — `features/auth`

```
apps/web/src/features/auth/
├── components/
│   ├── RegisterForm.tsx        # React Hook Form + Zod
│   └── EmailConfirmationPage.tsx
├── schemas/
│   └── registerSchema.ts      # z.object({ fullName, email, password })
├── api/
│   ├── auth-api.ts            # registerUser(data): POST /auth/register
│   └── query-keys.ts          # authKeys.register
└── hooks/
    └── useRegister.ts         # useMutation → authApi.registerUser
```

Rota adicionada em `routes.tsx`: `/register` → `<RegisterForm />` (pública, sem `ProtectedRoute`)

## Risks / Trade-offs

| Risco | Mitigação |
|---|---|
| Envio de e-mail síncrono bloqueia o response | Usar CDI `@Observes` assíncrono via `UserRegisteredEvent` — o response retorna antes do e-mail ser enviado |
| Token de confirmação armazenado em tabela relacional vs Redis | Tabela relacional para o MVP (auditável, persistente); Redis seria mais eficiente mas adiciona complexidade desnecessária agora |
| BCrypt custo 12 pode ser lento em testes | Usar `@ConfigProperty` para custo configurável; perfil `test` usa custo 4 |

## Migration Plan

1. Criar migration `V001` e `V002` antes de qualquer código Java
2. Validar migrations com `quarkus dev` localmente
3. Implementar camada `domain/` primeiro (sem dependências externas — testável isoladamente)
4. Implementar `application/` com mocks dos Ports
5. Implementar `infrastructure/` e conectar
6. Implementar `interfaces/rest/`
7. Frontend por último (depende do endpoint estar funcional ou mockado via MSW)
