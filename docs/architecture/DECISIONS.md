# LMS — Requisitos de Código e Decisões Arquiteturais

> **Versão:** 1.1.0 — Maio 2026  
> **Metodologia:** Specification-Driven Development (SDD)  
> **Arquitetura:** Monolito Modular  
> **Natureza:** Trabalho de Conclusão de Curso (TCC)

---

## Princípio Fundamental — SDD

No modelo SDD, a especificação **precede** a implementação. Cada decisão de design, padrão de código e regra arquitetural descrita neste documento constitui um **contrato** que deve ser honrado durante todo o ciclo de desenvolvimento.

- Toda implementação deve ser rastreável a um requisito deste documento.
- Qualquer desvio deve ser justificado, documentado e aprovado como novo ADR.
- Padrões não podem ser misturados: DDD é obrigatório em toda camada de domínio.
- SOLID e Clean Architecture não são sugestões — são contratos de código.

---

## Stack

| Camada | Tecnologia |
|---|---|
| Front-end | React + TypeScript (Vite) |
| Back-end | Quarkus 3.x (Java 21) |
| Banco principal | MySQL 8.x |
| Cache | Redis 7 |
| Arquitetura | Monolito Modular + DDD + Clean Arch + Hexagonal |
| Armazenamento | Local → S3/MinIO (abstrato via StoragePort) |
| Autenticação | OAuth2 + JWT (RS256) |
| Repo | Monorepo |
| Infra | Docker + Compose |

---

## 1. Modelo de Negócio e Fluxo de Entrada

> O ponto de entrada único do sistema é o cadastro de um **Usuário**. A partir deste usuário, tudo é criado: organizações, convites, turmas e permissões. Não existe hierarquia pré-existente antes do primeiro cadastro.

### 1.1 Hierarquia de Criação

```
Usuário cadastra-se no sistema
   |
   +-- cria Organização (torna-se ADMIN_ORG)
         |
         +-- convida usuários por e-mail com papel definido:
         |     ADMIN_ORG  → pode gerenciar tudo na organização
         |     GESTOR     → gerencia turmas específicas
         |     PROFESSOR  → cria tarefas, avalia, posta avisos
         |     ALUNO      → consome conteúdo, envia tarefas
         |
         +-- cria Turmas (vinculadas à organização)
               |
               +-- vincula Disciplinas
               +-- vincula Professores e Alunos
               +-- gera código/link de convite para Alunos
```

### 1.2 Regras de Negócio Estruturais

| ID | Regra |
|---|---|
| **RN-01** | Um usuário pode pertencer a múltiplas organizações com papéis diferentes em cada uma. |
| **RN-02** | Um usuário só pode criar recursos dentro de uma organização à qual pertence com papel adequado. |
| **RN-03** | O criador de uma organização é automaticamente ADMIN_ORG e não pode ser removido deste papel por outro usuário. |
| **RN-04** | Convites são enviados por e-mail com token de uso único e expiração de 7 dias. O papel é definido no momento do convite. |
| **RN-05** | Alunos podem ingressar em uma turma via link/código público sem necessidade de convite por e-mail. |
| **RN-06** | Um Professor pode estar vinculado a múltiplas disciplinas dentro da mesma organização. |
| **RN-07** | Um Aluno só visualiza turmas, disciplinas e tarefas das organizações/turmas às quais pertence. |
| **RN-08** | Toda ação de exclusão é soft delete — registros não são removidos fisicamente do banco. |

### 1.3 Banco de Dados Único — Schema Multi-Tenant Leve

O banco é único e compartilhado. Isolamento por organização é feito em nível de aplicação via `organization_id`, não por schema separado.

| ID | Regra |
|---|---|
| **DB-MT-01** | Toda tabela com dado específico de organização DEVE ter `organization_id NOT NULL` com FK para `organizations`. |
| **DB-MT-02** | Todo Repository que busca dados organizacionais DEVE incluir `organization_id` no WHERE. Proibido buscar dados cross-organization. |
| **DB-MT-03** | O `organization_id` é extraído do JWT (claim customizado) — nunca recebido via request body. |
| **DB-MT-04** | Tabela `users` é global. Tabela `organization_members` faz o vínculo N:M com a coluna `role`. |

---

## 2. Arquitetura — Monolito Modular

> **Definição:** Uma única unidade de deploy (1 JAR/container) composta por módulos internos com fronteiras bem definidas. Cada módulo possui seu próprio domínio, casos de uso e adaptadores. Comunicação entre módulos ocorre via interfaces Java — nunca chamadas HTTP internas.

### 2.1 Módulos Mapeados

| Módulo | Bounded Context | Responsabilidades Principais |
|---|---|---|
| **identity** | Identidade | Cadastro de usuário, autenticação (login/logout), JWT, refresh token, recuperação de senha, confirmação de e-mail |
| **organization** | Organização | CRUD de organização, convites por e-mail com token, gestão de membros (organization_members), papéis por organização |
| **classroom** | Turmas | CRUD de turmas, vínculo professor-aluno, geração de link/código de convite, listagem de membros da turma |
| **curriculum** | Currículo | CRUD de disciplinas, vínculo disciplina-turma, associação disciplina-professor, conteúdo complementar (videoaulas, materiais), organização por tópico |
| **assessment** | Avaliação | Criação de tarefas com prazo, upload de materiais, submissão de respostas pelos alunos, avaliação com nota e feedback, histórico de submissões |
| **communication** | Comunicação | Mural de avisos (feed cronológico), publicação por professor, leitura por alunos, notificações in-app |
| **reporting** | Relatórios | Dashboards por perfil (admin, gestor, professor, aluno), relatórios exportáveis em PDF |
| **storage** | Armazenamento | Abstração de upload/download via StoragePort, implementação local para dev, preparado para S3/MinIO |

### 2.2 Comunicação entre Módulos

| ID | Regra |
|---|---|
| **MOD-01** | Módulos se comunicam exclusivamente via interfaces Java (Ports). Proibido instanciar classes concretas de outro módulo. |
| **MOD-02** | Domain Events via CDI são o mecanismo preferencial para comunicação assíncrona. Ex: `identity` publica `UserRegisteredEvent`; `communication` escuta e cria notificação. |
| **MOD-03** | Proibido acesso direto ao repositório de outro módulo. |
| **MOD-04** | Dependências entre módulos devem ser unidirecionais. Dependências circulares são proibidas. |
| **MOD-05** | Cada módulo possui seu próprio pacote de persistência. Proibido compartilhar JPA Entities entre módulos. |

### 2.3 Estrutura de Pacotes

```
apps/api/src/main/java/br/edu/lms/
├── module/
│   ├── identity/
│   │   ├── domain/
│   │   │   ├── model/           # User, UserId, Email, UserRole
│   │   │   ├── event/           # UserRegisteredEvent
│   │   │   ├── exception/       # InvalidCredentialsException
│   │   │   └── port/
│   │   │       ├── in/          # RegisterUserUseCase, AuthenticateUseCase
│   │   │       └── out/         # UserRepository, EmailPort
│   │   ├── application/
│   │   │   ├── usecase/         # RegisterUserService, AuthenticateService
│   │   │   └── dto/             # RegisterUserCommand, AuthResponse
│   │   ├── infrastructure/
│   │   │   ├── persistence/     # UserJpaEntity, UserRepositoryImpl
│   │   │   ├── security/        # JwtTokenProvider, BCryptPasswordService
│   │   │   └── mail/            # QuarkusMailAdapter
│   │   └── interfaces/
│   │       └── rest/            # AuthResource, UserResource
│   │
│   ├── organization/
│   ├── classroom/
│   ├── curriculum/
│   ├── assessment/
│   ├── communication/
│   ├── reporting/
│   └── storage/
│
└── shared/
    ├── domain/                  # DomainEvent (interface), AggregateRoot (base)
    ├── security/                # SecurityContext — extrai userId/orgId do JWT
    └── exception/               # GlobalExceptionMapper
```

---

## 3. Back-End — Quarkus

### 3.1 Stack Tecnológica

| Decisão | Tecnologia / Regra |
|---|---|
| Runtime | Java 21 com Virtual Threads (Project Loom) — obrigatório |
| Framework | Quarkus 3.x — última versão estável |
| Lombok | Obrigatório em todas as classes Java. Proibido gerar getters/setters manualmente. |
| ORM | Hibernate ORM com Panache — modo Active Record **proibido**; usar Repository Pattern |
| Banco | MySQL 8.x via JDBC |
| Cache | Redis via Quarkus Redis Client (Lettuce) |
| Auth | Quarkus OIDC + Smallrye JWT — tokens JWT assinados com RS256 |
| Senhas | BCrypt via Quarkus Security — fator de custo mínimo 12 |
| E-mail | Quarkus Mailer (SMTP configurável por variável de ambiente) |
| Mapeamento | MapStruct 1.5+ — proibido mapeamento manual entre camadas |
| Validação | Bean Validation (Jakarta) — todas as entradas validadas nos Ports de entrada |
| API Docs | SmallRye OpenAPI + Swagger UI (desativado em produção) |
| Migrations | Flyway — obrigatório. Proibido `quarkus.hibernate-orm.database.generation=update` |
| Testes | JUnit 5 + Mockito + @QuarkusTest + Testcontainers (MySQL + Redis) |
| Build | Maven com Quarkus Maven Plugin |

### 3.2 Lombok — Regras de Uso

| ID | Regra |
|---|---|
| **LOM-01** | `@Value` em Value Objects de domínio: garante imutabilidade. Ex: `Email`, `UserId`, `ClassroomId`. |
| **LOM-02** | `@Builder` em Domain Entities e Application DTOs. |
| **LOM-03** | `@Data` em JPA Entities (infrastructure): gera getters, setters, equals, hashCode. |
| **LOM-04** | `@RequiredArgsConstructor` em Application Services e Adapters: injeção via construtor — compatível com CDI. |
| **LOM-05** | `@Slf4j` em qualquer classe que precisa de logging. |
| **LOM-06** | `@EqualsAndHashCode(onlyExplicitlyIncluded=true)` + `@EqualsAndHashCode.Include` no campo `id` em JPA Entities. |
| **LOM-07** | **Proibido** usar `@Data` em Domain Entities puras — usar `@Value` (imutável) ou `@Getter + @Builder`. |

```java
// Value Object — imutável
@Value
public class Email {
    String value;
    public Email(String value) {
        if (value == null || !value.contains("@"))
            throw new DomainException("Email inválido: " + value);
        this.value = value.toLowerCase().trim();
    }
}

// Domain Entity — Aggregate Root
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @EqualsAndHashCode.Include
    private final UserId id;
    private Email email;
    private UserRole role;
    public void changeEmail(Email newEmail) { this.email = newEmail; }
}

// JPA Entity
@Data
@Entity
@Table(name = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserJpaEntity {
    @Id @EqualsAndHashCode.Include
    @Column(name = "id") private String id;
    @Column(name = "email", nullable = false) private String email;
}

// Application Service
@RequiredArgsConstructor
@ApplicationScoped
@Slf4j
public class RegisterUserService implements RegisterUserUseCase {
    private final UserRepository userRepository;
    private final EmailPort emailPort;
    private final PasswordEncoder passwordEncoder;
}
```

### 3.3 Regras de Dependência entre Camadas

| ID | Regra |
|---|---|
| **RD-01** | `domain/` não pode importar nenhuma classe de `infrastructure/`, `application/` ou `interfaces/` |
| **RD-02** | `domain/` não pode importar anotações Jakarta Persistence, Quarkus ou Lombok (`@Data`, `@Entity` etc.) |
| **RD-03** | `application/` não pode importar classes de `infrastructure/` ou `interfaces/` |
| **RD-04** | `application/` importa apenas `domain/` (entidades, ports, value objects, eventos) |
| **RD-05** | `infrastructure/` implementa as interfaces definidas em `domain/port/out/` |
| **RD-06** | `interfaces/` (REST) chama apenas `application/usecase/` — nunca `domain/` diretamente |
| **RD-07** | JPA Entities são **diferentes** das Domain Entities |
| **RD-08** | `@Data` e `@Entity` são permitidos **apenas** em `infrastructure/persistence/entity/` |

### 3.4 Regras SOLID Aplicadas

| Princípio | Aplicação |
|---|---|
| **SRP** | Cada classe tem uma única razão para mudar. Use Cases têm exatamente um método público principal (`execute` / `handle`). |
| **OCP** | Comportamentos extensíveis via interfaces (Ports). Para adicionar novo storage, implementar `StoragePort` sem alterar código existente. |
| **LSP** | Toda implementação de Port deve ser substituível sem quebrar o comportamento esperado pelo Use Case. |
| **ISP** | Ports são granulares. `UserRepository` não herda de `GenericRepository<T>`. |
| **DIP** | Use Cases dependem de abstrações (Ports/interfaces), nunca de implementações concretas. Injeção via CDI (`@Inject`). |

### 3.5 Segurança

| ID | Regra |
|---|---|
| **SEC-01** | Senhas armazenadas com BCrypt — fator de custo mínimo 12. |
| **SEC-02** | Tokens JWT assinados com RS256 (par de chaves RSA). Nunca usar HS256. |
| **SEC-03** | Access Token: validade de 15 minutos. Refresh Token: 7 dias, armazenado no Redis com TTL. |
| **SEC-04** | JWT contém claims customizados: `sub` (userId), `org` (organizationId), `roles` (lista de papéis). |
| **SEC-05** | RBAC implementado via `@RolesAllowed` no nível do Resource. Nunca verificar role na lógica de domínio. |
| **SEC-06** | Endpoints protegidos por padrão. Exceções: `/auth/login`, `/auth/register`, `/auth/refresh`, `/invites/{token}`. |
| **SEC-07** | `organization_id` extraído **sempre** do JWT — nunca do request body. |
| **SEC-08** | Rate limiting para `/auth/**` via Redis (contador por IP + TTL de 1 minuto). |

### 3.6 Persistência e Cache

| ID | Regra |
|---|---|
| **DB-01** | MySQL 8 como banco principal. Migrations via Flyway. Proibido `database.generation=update`. |
| **DB-02** | Scripts nomeados: `V{numero}__{descricao}.sql` — ex: `V001__create_users_table.sql`. |
| **DB-03** | Tabelas e colunas: `snake_case`. JPA Entities com `@Table(name=)` e `@Column(name=)` sempre explícitos. |
| **DB-04** | Toda tabela organizacional tem `organization_id NOT NULL FK`. |
| **DB-05** | Soft delete obrigatório: `User`, `Organization`, `Classroom`, `Subject`, `Task`. Coluna `deleted_at TIMESTAMP NULL`. |
| **DB-06** | Redis: Refresh Tokens, rate limiting, contador de notificações não lidas. Chaves: `{módulo}:{tipo}:{id}`. |
| **DB-07** | Nenhuma query JPQL ou nativa fora de `infrastructure/persistence/`. |
| **DB-08** | Relacionamentos N:M com tabela associativa explícita. |

### 3.7 Armazenamento de Arquivos

```java
// domain/port/out/StoragePort.java
public interface StoragePort {
    StoredFile store(InputStream content, String filename, String mimeType, StorageContext context);
    InputStream retrieve(String fileKey);
    void delete(String fileKey);
    String getPublicUrl(String fileKey);
}
// Implementações:
// infrastructure/storage/LocalStorageAdapter.java  (dev)
// infrastructure/storage/S3StorageAdapter.java     (produção — futuro)
// Seleção via @ConfigProperty(name = "storage.provider")
```

| ID | Regra |
|---|---|
| **STG-01** | `StoragePort` definido em `domain/port/out/` — nenhuma referência a S3/disco no domínio. |
| **STG-02** | Implementação local armazena em `{project.root}/data/uploads/{contexto}/{ano}/{mes}/`. |
| **STG-03** | Arquivos servidos via endpoint `/api/files/{fileKey}` com validação de permissão. |
| **STG-04** | Tamanho máximo de upload: 50MB (configurável via `application.properties`). |
| **STG-05** | Tipos aceitos: `task_attachment` (pdf, doc, docx, zip, jpg, png), `lesson_material` (pdf, mp4, webm). |

---

## 4. Front-End — React SPA

### 4.1 Stack Tecnológica

| Decisão | Tecnologia / Regra |
|---|---|
| Bundler | Vite 5+ — `vite.config.ts` obrigatório |
| Linguagem | TypeScript 5+ em strict mode (`strict: true` no `tsconfig.json`) |
| React | React 18+ com Concurrent Mode. Proibido class components. |
| Roteamento | React Router v6 — rotas declarativas em `routes.tsx` centralizado |
| Server State | TanStack Query v5 — toda comunicação com API via queries/mutations |
| Client State | Zustand — apenas estado global verdadeiramente compartilhado |
| Formulários | React Hook Form + Zod — Zod é o schema de validação único |
| UI Base | Shadcn/ui (sobre Radix UI) — acessível sem vendor lock-in |
| Estilização | Tailwind CSS — utility-first. Proibido `style=` inline exceto casos dinâmicos justificados |
| HTTP Client | Axios com instância centralizada — interceptors para JWT e refresh token |
| Ícones | Lucide React — único pacote de ícones permitido |
| Testes | Vitest + Testing Library + MSW para mock de API |
| Linting | ESLint com `@typescript-eslint` + Prettier |

### 4.2 Estrutura de Pastas

```
apps/web/src/
├── app/
│   ├── routes.tsx              # Todas as rotas declaradas aqui
│   ├── providers.tsx           # QueryClient, Auth, Theme, OrgContext
│   └── main.tsx
├── features/
│   ├── auth/
│   │   ├── components/         # LoginForm, RegisterForm, ForgotPasswordForm
│   │   ├── hooks/              # useAuth, useLogin, useRegister
│   │   ├── api/
│   │   │   ├── auth-api.ts
│   │   │   └── query-keys.ts
│   │   ├── schemas/            # loginSchema.ts, registerSchema.ts (Zod)
│   │   ├── store/              # authStore.ts (Zustand)
│   │   └── types.ts
│   ├── organization/
│   ├── classroom/
│   ├── curriculum/
│   ├── assessment/
│   ├── communication/
│   └── dashboard/
├── components/
│   ├── ui/                     # Re-exports Shadcn/ui
│   ├── layout/                 # AppShell, Sidebar, Header, OrgSwitcher
│   └── shared/                 # DataTable, FileUpload, ConfirmDialog
├── lib/
│   ├── axios.ts
│   ├── query-client.ts
│   └── utils.ts
├── hooks/
└── types/
```

### 4.3 Regras de Código

| ID | Regra |
|---|---|
| **FE-01** | Todo componente é uma função: `function Component(): JSX.Element`. Proibido `React.FC<>` e class components. |
| **FE-02** | Separação obrigatória: apresentação (sem lógica de dados) vs containers (possuem hooks de query). |
| **FE-03** | Custom hooks extraem toda lógica de state/efeitos dos componentes. |
| **FE-04** | TanStack Query gerencia todo estado de servidor. Proibido `useState` para dados da API. |
| **FE-05** | Query keys tipados e centralizados em `features/{nome}/api/query-keys.ts`. |
| **FE-06** | Zustand em slices por feature. Proibido store global monolítico. |
| **FE-07** | Toda prop tem type explícito. Proibido `any` e `@ts-ignore`. |
| **FE-08** | Zod schemas validam dados de formulários **e** respostas da API. |
| **FE-09** | Rotas protegidas via `ProtectedRoute` com verificação de role do `authStore`. |
| **FE-10** | Tratamento de erro centralizado: Axios interceptor (401 → refresh), Error Boundary (render), TanStack Query `onError` global. |
| **FE-11** | Proibido importar diretamente entre features. Comunicação via Zustand ou URL params. |
| **FE-12** | Path aliases obrigatórios: `@features`, `@components`, `@lib`, `@hooks`, `@types`. |

---

## 5. Infraestrutura e Containerização

### 5.1 Estrutura do Monorepo

```
lms/
├── apps/
│   ├── web/                   # React SPA (Vite)
│   └── api/                   # Quarkus Monolito Modular
├── packages/
│   └── shared-types/          # Contratos TypeScript compartilhados
├── infra/
│   ├── docker/
│   │   ├── api/Dockerfile.dev
│   │   ├── api/Dockerfile.prod
│   │   └── web/Dockerfile.dev
│   ├── mysql/init/
│   ├── docker-compose.yml
│   └── docker-compose.prod.yml
├── docs/
│   ├── architecture/          # ADRs e este documento
│   └── requirements/          # Requisitos funcionais
├── .github/workflows/
├── .env.example
└── README.md
```

### 5.2 Docker Compose — Desenvolvimento

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: lms_db
    ports: ['3306:3306']
    volumes: [mysql_data:/var/lib/mysql]
    healthcheck:
      test: ['CMD', 'mysqladmin', 'ping', '-h', 'localhost']
      interval: 10s
      retries: 5

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
    ports: ['6379:6379']

  api:
    build:
      context: ../apps/api
      dockerfile: ../../infra/docker/api/Dockerfile.dev
    ports: ['8080:8080']
    environment:
      QUARKUS_DATASOURCE_JDBC_URL: jdbc:mysql://mysql:3306/lms_db
      QUARKUS_REDIS_HOSTS: redis://:${REDIS_PASSWORD}@redis:6379
      JWT_PRIVATE_KEY: ${JWT_PRIVATE_KEY}
      MAIL_HOST: ${MAIL_HOST}
    depends_on:
      mysql: { condition: service_healthy }
      redis: { condition: service_started }
    volumes:
      - ../apps/api:/workspace
      - ./data/uploads:/app/uploads

  web:
    build:
      context: ../apps/web
      dockerfile: ../../infra/docker/web/Dockerfile.dev
    ports: ['5173:5173']
    environment:
      VITE_API_URL: http://localhost:8080
    volumes:
      - ../apps/web:/workspace
```

### 5.3 Variáveis de Ambiente

| ID | Regra |
|---|---|
| **ENV-01** | `.env.example` commitado com todas as variáveis (sem valores reais). `.env` no `.gitignore`. |
| **ENV-02** | Front: variáveis públicas prefixadas `VITE_`. Nenhum segredo no bundle. |
| **ENV-03** | Back: sensíveis via variáveis de ambiente injetadas pelo Docker ou `%prod.quarkus.*`. |
| **ENV-04** | Chaves RSA (JWT) geradas fora do repositório e injetadas via variável de ambiente. |
| **ENV-05** | Quarkus perfis: `dev` (desenvolvimento local), `test` (Testcontainers), `prod` (produção). |

---

## 6. Estratégia de Testes

### 6.1 Back-End

| ID | Regra |
|---|---|
| **TEST-BE-01** | Testes unitários para toda lógica de domínio. Sem Quarkus no contexto — plain Java + Lombok. |
| **TEST-BE-02** | Testes unitários para Use Cases com mocks dos Ports via Mockito. |
| **TEST-BE-03** | Testes de integração com `@QuarkusTest` + Testcontainers para MySQL e Redis. Proibido H2 em integração. |
| **TEST-BE-04** | Testes de API com `@QuarkusTest` + REST-assured validando contrato HTTP e RBAC. |
| **TEST-BE-05** | Cobertura mínima 70% em `domain/` e `application/`. Medida via JaCoCo. |

### 6.2 Front-End

| ID | Regra |
|---|---|
| **TEST-FE-01** | Testes unitários para custom hooks com Testing Library `renderHook`. |
| **TEST-FE-02** | Testes de componentes com Testing Library — comportamento, não implementação. |
| **TEST-FE-03** | Mock de API via MSW — nunca mock direto de módulos Axios. |
| **TEST-FE-04** | Testes de formulários: submissão, mensagens de erro, estados de loading. |
| **TEST-FE-05** | Snapshots proibidos. Apenas assertions comportamentais. |

---

## 7. Convenções de Código

### 7.1 Nomenclatura

| ID | Regra |
|---|---|
| **NOM-01 (Java)** | Classes: PascalCase. Métodos/variáveis: camelCase. Constantes: UPPER_SNAKE_CASE. Pacotes: lowercase. |
| **NOM-02 (Java)** | Use Cases nomeados como verbos: `CreateOrganizationService`, `InviteMemberService`. |
| **NOM-03 (Java)** | Ports sem prefixo I: `UserRepository` (não `IUserRepository`). Impl: `UserRepositoryImpl`. |
| **NOM-04 (Java)** | Domain Events no passado: `UserRegisteredEvent`, `TaskSubmittedEvent`. |
| **NOM-05 (TS)** | Componentes: PascalCase. Hooks: camelCase com prefixo `use`. Stores: camelCase com sufixo `Store`. |
| **NOM-06 (TS)** | Tipos/interfaces: PascalCase. Enums: PascalCase com valores UPPER_CASE. |
| **NOM-07 (DB)** | Tabelas snake_case plural: `users`, `classrooms`, `organization_members`. FKs: `{tabela_singular}_id`. |
| **NOM-08 (REST)** | Endpoints kebab-case plural: `/organizations`, `/classroom-members`, `/task-submissions`. |

### 7.2 Git e Versionamento

| ID | Regra |
|---|---|
| **GIT-01** | Conventional Commits: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `chore:`, `style:`. |
| **GIT-02** | Commits atômicos — uma mudança lógica por commit. |
| **GIT-03** | Branches: `main`, `develop`, `feature/{módulo}-{descricao}`, `fix/{descricao}`. |
| **GIT-04** | Proibido commit direto em `main` ou `develop`. Toda mudança via Pull Request. |
| **GIT-05** | Mensagens de commit em inglês. |

---

## 8. Checklist de Conformidade

### Back-End
- [ ] Nenhuma anotação JPA/Quarkus/Lombok no pacote `domain/`
- [ ] Nenhuma lógica de negócio fora de `domain/model/` ou `domain/service/`
- [ ] Todos os Ports de saída implementados em `infrastructure/`
- [ ] MapStruct em todos os mapeamentos entre camadas
- [ ] Flyway migration criada para qualquer mudança de schema
- [ ] Lombok usado corretamente por tipo de classe (LOM-01 a LOM-07)
- [ ] `organization_id` extraído do JWT — nunca do request body
- [ ] Testes unitários para todo Use Case novo
- [ ] Endpoints documentados via OpenAPI (`@Operation`, `@APIResponse`)
- [ ] Nenhum segredo hardcoded — tudo via `@ConfigProperty`

### Front-End
- [ ] TypeScript strict — sem `any` ou `@ts-ignore`
- [ ] Nenhum `useEffect` para fetch — usar TanStack Query
- [ ] Zod schema para cada formulário e resposta de API
- [ ] Query keys centralizadas em `query-keys.ts` da feature
- [ ] Nenhum import cruzado entre features
- [ ] Rotas protegidas com `ProtectedRoute` por perfil

### Infraestrutura
- [ ] `.env.example` atualizado com novas variáveis
- [ ] `docker-compose.yml` funcional após mudanças de serviço
- [ ] Nenhum dado sensível em logs ou variáveis expostas

---

*Este documento é um contrato técnico. Toda implementação deve ser rastreável às regras aqui definidas.*  
*Ver também: [ADRs](./adrs/) | [Requisitos Funcionais](../requirements/RF.md)*
