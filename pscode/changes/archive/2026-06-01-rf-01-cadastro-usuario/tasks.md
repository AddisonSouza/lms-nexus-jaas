## 1. Infraestrutura e Migrations

- [ ] 1.1 [INFRA] Criar `V001__create_users_table.sql` com colunas `id`, `full_name`, `email`, `password_hash`, `status`, `created_at`, `updated_at`, `deleted_at`
- [ ] 1.2 [INFRA] Criar `V002__create_email_confirmation_tokens_table.sql` com colunas `id`, `user_id`, `token`, `expires_at`, `used_at`, `created_at`
- [ ] 1.3 [INFRA] Validar migrations rodando `quarkus dev` localmente e confirmar tabelas criadas no MySQL

## 2. Domínio — `module/identity/domain/`

- [ ] 2.1 [BE] Criar `UserStatus.java` (enum: `PENDING_CONFIRMATION`, `ACTIVE`, `SUSPENDED`)
- [ ] 2.2 [BE] Criar Value Object `UserId.java` (`@Value`, gera UUID no factory `UserId.generate()`)
- [ ] 2.3 [BE] Criar Value Object `Email.java` (`@Value`, valida formato no construtor, lança `DomainException`)
- [ ] 2.4 [BE] Criar Value Object `FullName.java` (`@Value`, valida não-blank e máx 150 chars)
- [ ] 2.5 [BE] Criar Aggregate Root `User.java` (`@Getter @Builder @EqualsAndHashCode(onlyExplicitlyIncluded=true)`)
- [ ] 2.6 [BE] Criar `UserRegisteredEvent.java` (record com `userId` e `email`)
- [ ] 2.7 [BE] Criar `EmailAlreadyInUseException.java`
- [ ] 2.8 [BE] Criar Port de entrada `RegisterUserUseCase.java` (interface com `execute(RegisterUserCommand)`)
- [ ] 2.9 [BE] Criar Port de saída `UserRepository.java` (interface: `save(User)`, `findByEmail(Email)`)
- [ ] 2.10 [BE] Criar Port de saída `EmailPort.java` (interface: `sendConfirmationEmail(Email, String token)`)

## 3. Aplicação — `module/identity/application/`

- [ ] 3.1 [BE] Criar `RegisterUserCommand.java` (`@Builder`: `fullName`, `email`, `rawPassword`)
- [ ] 3.2 [BE] Criar `RegisterUserResponse.java` (`@Builder`: `userId`, `email`, `status`)
- [ ] 3.3 [BE] Criar `RegisterUserService.java` (`@ApplicationScoped @RequiredArgsConstructor @Slf4j`, implementa `RegisterUserUseCase`)
- [ ] 3.4 [BE] Implementar lógica em `RegisterUserService`: validar e-mail único → hash senha → salvar usuário → emitir e-mail e evento

## 4. Infraestrutura — `module/identity/infrastructure/`

- [ ] 4.1 [BE] Criar `UserJpaEntity.java` (`@Data @Entity @Table(name="users") @EqualsAndHashCode(onlyExplicitlyIncluded=true)`)
- [ ] 4.2 [BE] Criar `UserMapper.java` (MapStruct: `User ↔ UserJpaEntity`)
- [ ] 4.3 [BE] Criar `UserRepositoryImpl.java` (`@ApplicationScoped`, injeta `EntityManager`, implementa `UserRepository`)
- [ ] 4.4 [BE] Criar `BcryptPasswordService.java` (`@ApplicationScoped`, BCrypt custo configurável via `@ConfigProperty`, padrão 12)
- [ ] 4.5 [BE] Criar `EmailConfirmationTokenJpaEntity.java` e migration correspondente
- [ ] 4.6 [BE] Criar `QuarkusMailAdapter.java` (`@ApplicationScoped`, implementa `EmailPort`, usa Quarkus Mailer)

## 5. Interface REST — `module/identity/interfaces/rest/`

- [ ] 5.1 [BE] Criar `RegisterRequest.java` com Bean Validation (`@NotBlank`, `@Email`, `@Size(min=8)`)
- [ ] 5.2 [BE] Criar `RegisterResponse.java`
- [ ] 5.3 [BE] Criar `AuthResource.java` (`@Path("/auth")`, endpoint `POST /register`, anotações OpenAPI `@Operation` e `@APIResponse`)

## 6. Testes — Backend

- [ ] 6.1 [BE] Testes unitários para Value Objects (`Email`, `FullName`, `UserId`) — casos válidos e inválidos
- [ ] 6.2 [BE] Testes unitários para `RegisterUserService` com mocks de `UserRepository`, `EmailPort` e `BcryptPasswordService` via Mockito
- [ ] 6.3 [BE] Teste de integração `@QuarkusTest` para `POST /auth/register` com Testcontainers (MySQL): cadastro com sucesso, e-mail duplicado (409), senha fraca (422)

## 7. Frontend — `features/auth`

- [ ] 7.1 [FE] Criar `registerSchema.ts` (Zod: `fullName` não-vazio, `email` formato válido, `password` mín 8 chars)
- [ ] 7.2 [FE] Criar `auth-api.ts` com função `registerUser(data)` → `POST /auth/register`
- [ ] 7.3 [FE] Criar `query-keys.ts` com `authKeys.register`
- [ ] 7.4 [FE] Criar `useRegister.ts` (hook com `useMutation`, trata 409 e 422)
- [ ] 7.5 [FE] Criar `RegisterForm.tsx` (React Hook Form + Zod resolver, campos: nome completo, e-mail, senha, botão com loading state)
- [ ] 7.6 [FE] Criar `EmailConfirmationPage.tsx` (página pós-cadastro: instrução de confirmar e-mail)
- [ ] 7.7 [FE] Adicionar rota `/register` em `routes.tsx` (pública, sem `ProtectedRoute`)
- [ ] 7.8 [FE] Adicionar link "Criar conta" na tela de login (a ser criada em RF-02)

## 8. Testes — Frontend

- [ ] 8.1 [FE] Testes de componente para `RegisterForm`: submissão com sucesso, erro 409, erro 422, validação de campos vazios (Testing Library + MSW)
- [ ] 8.2 [FE] Teste do hook `useRegister` com `renderHook` (Testing Library)

## 9. Ajustes de Infraestrutura

- [ ] 9.1 [INFRA] Adicionar variáveis `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM`, `MAIL_USERNAME`, `MAIL_PASSWORD` ao `.env.example`
- [ ] 9.2 [INFRA] Configurar `quarkus.mailer.*` em `application.properties` usando `${MAIL_*}` e perfil `%test` com Mailpit ou similar
