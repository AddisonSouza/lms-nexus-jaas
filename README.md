# LMS — Learning Management System

> Plataforma educacional desenvolvida como Trabalho de Conclusão de Curso (TCC).  
> Inspirada no Google Classroom, com foco em organização de conteúdos, gestão de turmas e apoio ao ensino.

---

## Stack

| Camada | Tecnologia |
|---|---|
| Front-end | React 18 + TypeScript + Vite |
| Back-end | Quarkus 3.12 + Java 21 |
| Banco | MySQL 8 + Redis 7 |
| Infra | Docker + Compose |
| Arquitetura | Monolito Modular + DDD + Clean Arch + Hexagonal |

---

## Estrutura do Monorepo

```
lms-nexus-jaas/
├── apps/
│   ├── web/              # React SPA (Vite)
│   └── api/              # Quarkus Monolito Modular
├── infra/
│   ├── docker/
│   │   ├── api/          # Dockerfile.dev da API
│   │   └── web/          # Dockerfile.dev do front
│   └── docker-compose.yml
└── docs/
    ├── architecture/     # Decisões arquiteturais e ADRs
    └── requirements/     # Requisitos funcionais
```

---

## Documentação

| Documento | Descrição |
|---|---|
| [Requisitos de Código](./docs/architecture/DECISIONS.md) | Stack, padrões, regras e decisões técnicas |
| [ADRs](./docs/architecture/adrs/) | Architecture Decision Records |
| [Requisitos Funcionais](./docs/requirements/RF.md) | RFs detalhados com fluxos e critérios de aceite |

---

## Módulos (Bounded Contexts)

| Módulo | Responsabilidade |
|---|---|
| `identity` | Autenticação, usuários, JWT |
| `organization` | Organizações, membros, convites |
| `classroom` | Turmas, vínculos, códigos de ingresso |
| `curriculum` | Disciplinas, conteúdo complementar, tópicos |
| `assessment` | Tarefas, submissões, avaliações, notas |
| `communication` | Mural de avisos, notificações in-app |
| `reporting` | Dashboards por perfil, relatórios PDF |
| `storage` | Abstração de upload/download de arquivos |

---

## Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/) + [Docker Compose](https://docs.docker.com/compose/)
- Java 21 + Maven 3.9+ _(apenas para rodar a API localmente sem Docker)_
- Node.js 20+ _(apenas para rodar o front localmente sem Docker)_

---

## Rodando com Docker (recomendado)

```bash
# Clone o repositório
git clone git@github.com:AddisonSouza/lms-nexus-jaas.git
cd lms-nexus-jaas

# Copie e configure as variáveis de ambiente
cp .env.example .env
# Edite .env: defina pelo menos MYSQL_ROOT_PASSWORD

# Suba todos os serviços a partir da pasta infra/
cd infra
docker compose up -d
```

| Serviço | URL |
|---|---|
| API REST | http://localhost:8080 |
| Swagger UI | http://localhost:8080/api/swagger-ui |
| Front-end | http://localhost:5173 |
| Mailpit (e-mail dev) | http://localhost:8025 |
| MySQL | localhost:3306 |
| Redis | localhost:6379 |

```bash
# Acompanhar logs
docker compose -f infra/docker-compose.yml logs -f api
docker compose -f infra/docker-compose.yml logs -f web

# Parar tudo
docker compose -f infra/docker-compose.yml down
```

---

## Rodando localmente (sem Docker)

### Infra (MySQL, Redis, Mailpit)

```bash
cd infra
docker compose up -d mysql redis mailpit
```

### API (Quarkus)

```bash
cd apps/api
mvn quarkus:dev
# API disponível em: http://localhost:8080
# Swagger UI em:     http://localhost:8080/api/swagger-ui
# Hot reload ativo por padrão no modo dev
```

### Front-end (Vite)

```bash
cd apps/web
npm install
npm run dev
# Front disponível em: http://localhost:5173
```

---

## Variáveis de ambiente relevantes

| Variável | Padrão (dev) | Descrição |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | _(obrigatório)_ | Senha root do MySQL |
| `MYSQL_DATABASE` | `lms_db` | Nome do banco |
| `REDIS_PASSWORD` | `redis` | Senha do Redis |
| `MAIL_FROM` | `noreply@lms.local` | Remetente dos e-mails |
| `VITE_API_URL` | `http://localhost:8080` | URL da API para o front |

> No perfil `dev` do Quarkus: JWT usa os arquivos `publicKey.pem`/`privateKey.pem` em `apps/api/src/main/resources/` (não versionados — gere o par localmente, veja `.env.example`) e e-mails são mockados (sem necessidade de SMTP real).

---

## Convenções

- **Commits:** [Conventional Commits](https://www.conventionalcommits.org/)
- **Branches:** `feat/{módulo}-{descricao}` | `fix/{descricao}`
- **PRs:** Obrigatório para `main`
- **SDD:** Especificação precede implementação — toda mudança é rastreável a um RF ou ADR

---

## Metodologia — SDD

Este projeto utiliza **Specification-Driven Development**: os documentos de requisitos e decisões arquiteturais são contratos técnicos. Toda implementação deve ser rastreável a uma regra definida em `docs/`.
