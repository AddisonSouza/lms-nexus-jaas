<div align="center">

<img src="apps/web/public/favicon.svg" width="72" alt="LMS Nexus" />

# LMS Nexus

**Plataforma de gestão de aprendizado multi-tenant para escolas**: turmas,
disciplinas, tarefas, correção e notas, cada organização no seu espaço isolado.

[![Deploy](https://github.com/AddisonSouza/lms-nexus-jaas/actions/workflows/deploy.yml/badge.svg)](https://github.com/AddisonSouza/lms-nexus-jaas/actions/workflows/deploy.yml)
![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Quarkus](https://img.shields.io/badge/Quarkus-3.12-4695EB?logo=quarkus)
![React 18](https://img.shields.io/badge/React-18-61DAFB?logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-strict-3178C6?logo=typescript)
![MySQL 8](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)

**[🌐 lmsnexus.com.br](https://lmsnexus.com.br)**

</div>

---

Projeto desenvolvido como Trabalho de Conclusão de Curso (TCC). Inspirado no
Google Classroom, mas pensado para a **instituição**: uma escola cria sua
organização, convida gestores, professores e alunos, e tudo o que acontece lá
dentro (turmas, conteúdo, tarefas, notas) fica isolado das demais.

## Funcionalidades

| Área | O que faz |
|---|---|
| **Conta** | Cadastro, confirmação de e-mail, login com JWT + refresh token, recuperação de senha |
| **Organização** | Criação da escola, gestão de membros e convites por e-mail com papel definido |
| **Turmas** | Criação de turmas e ingresso por link ou código |
| **Disciplinas** | Disciplinas vinculadas a turma e professor, com conteúdo complementar (arquivos e links) |
| **Tarefas** | Criação com prazo e anexos, envio de resposta pelo aluno, fechamento automático no prazo |
| **Avaliação** | Correção com nota e feedback pelo professor; aluno acompanha suas notas |
| **Comunicação** | Mural de avisos e notificações in-app |
| **Dashboards** | Um painel por papel: administrador, gestor, professor e aluno |

**Papéis:** `ADMIN_ORG` · `GESTOR` · `PROFESSOR` · `ALUNO`

Os 20 requisitos do MVP estão implementados. O [roadmap](./docs/requirements/RF.md)
prevê gamificação (pontos, badges, ranking) e recursos de IA (correção automática,
geração de exercícios, assistente de estudos).

## Stack

| Camada | Tecnologia |
|---|---|
| **Back-end** | Java 21, Quarkus 3.12, Hibernate Panache, Flyway, MapStruct, SmallRye JWT, Qute (e-mails) |
| **Front-end** | React 18, TypeScript strict, Vite, TanStack Query, Zustand, React Hook Form + Zod, Tailwind + shadcn/ui, Recharts |
| **Dados** | MySQL 8, Redis 7 (refresh tokens, rate limit), Object Storage compatível com S3 |
| **Testes** | JUnit + Testcontainers (API), Vitest + Testing Library (web) |
| **Infra** | Docker Compose, nginx + Let's Encrypt, Oracle Cloud (ARM), deploy contínuo via GitHub Actions |

## Arquitetura

**Monolito modular** com DDD, Clean Architecture e Hexagonal (Ports & Adapters):
uma única unidade de deploy, mas cada módulo com fronteiras próprias.

```
apps/api/src/main/java/br/edu/lms/module/{modulo}/
├── domain/          # modelo, eventos, exceções e ports — sem framework
├── application/     # casos de uso e DTOs
├── infrastructure/  # persistência, segurança, e-mail, storage
└── interfaces/      # REST
```

| Módulo | Responsabilidade |
|---|---|
| `identity` | Autenticação, usuários, JWT |
| `organization` | Organizações, membros, convites |
| `classroom` | Turmas, vínculos, códigos de ingresso |
| `curriculum` | Disciplinas e conteúdo complementar |
| `assessment` | Tarefas, submissões, avaliações, notas |
| `communication` | Mural de avisos, notificações in-app |
| `reporting` | Dashboards por perfil |
| `storage` | Abstração de upload/download de arquivos |

Os módulos conversam só por interfaces (ports) ou eventos de domínio, nunca pelo
repositório de outro módulo. O `organization_id` vem sempre do JWT, nunca do
corpo da requisição. As decisões e o *porquê* de cada uma estão nos
[ADRs](./docs/architecture/adrs/).

## Estrutura do monorepo

```
lms-nexus-jaas/
├── apps/
│   ├── api/          # Quarkus (monolito modular)
│   └── web/          # React SPA (Vite)
├── infra/
│   ├── docker/       # Dockerfiles de dev e prod
│   ├── nginx/        # reverse proxy + TLS
│   ├── scripts/      # deploy e backup
│   ├── docker-compose.yml       # desenvolvimento
│   └── docker-compose.prod.yml  # produção
├── docs/             # arquitetura, requisitos, deploy, design
└── pscode/           # specs vivas e histórico de mudanças (SDD)
```

## Rodando localmente

**Pré-requisitos:** Docker + Docker Compose. Java 21/Maven 3.9 e Node 20 só são
necessários para rodar API e front fora do Docker.

```bash
git clone git@github.com:AddisonSouza/lms-nexus-jaas.git
cd lms-nexus-jaas

cp .env.example .env        # defina pelo menos MYSQL_ROOT_PASSWORD
cd infra && docker compose up -d
```

| Serviço | URL |
|---|---|
| Front-end | http://localhost:5173 |
| API REST | http://localhost:8080 |
| Swagger UI | http://localhost:8080/api/swagger-ui |
| Mailpit (e-mails de dev) | http://localhost:8025 |

<details>
<summary><b>Sem Docker para API e front</b></summary>

```bash
# infra de apoio
cd infra && docker compose up -d mysql redis mailpit

# API, com hot reload
cd apps/api && mvn quarkus:dev

# front
cd apps/web && npm install && npm run dev
```

No perfil `dev`, o JWT usa `publicKey.pem`/`privateKey.pem` em
`apps/api/src/main/resources/`. Esses arquivos não são versionados: gere o par
localmente (instruções no `.env.example`).

</details>

<details>
<summary><b>Testes</b></summary>

```bash
cd apps/api && mvn verify                             # unitários + integração (Testcontainers, precisa de Docker)
cd apps/web && npm run lint && npx vitest run && npm run build
```

</details>

## Deploy

Todo merge na `main` que toca `apps/`, `infra/` ou o workflow roda os testes da
API e do front e, se passarem, publica em produção por SSH. O passo a passo
completo (VM, TLS, backup, acesso ao banco) está no [`docs/DEPLOY.md`](./docs/DEPLOY.md).

## Como o projeto é desenvolvido

Specification-Driven Development: requisitos e decisões arquiteturais são
**contrato**, e toda mudança é rastreável a um RF ou ADR. Cada mudança passa por
`draft → refine → dev → complete` no board do GitHub e deixa sua spec em
[`pscode/`](./pscode/).

- **Commits:** [Conventional Commits](https://www.conventionalcommits.org/) em inglês
- **Branches:** `feat/…`, `fix/…`, `chore/…`, `docs/…`
- **Sem commit direto na `main`:** toda mudança entra por Pull Request

## Documentação

| Documento | Conteúdo |
|---|---|
| [`DECISIONS.md`](./docs/architecture/DECISIONS.md) | Stack, camadas, convenções e checklist de conformidade |
| [ADRs](./docs/architecture/adrs/) | ADR-001 a ADR-010: o porquê de cada decisão |
| [`RF.md`](./docs/requirements/RF.md) | Os 26 requisitos funcionais, com fluxos e critérios de aceite |
| [`API_CONTRACT.md`](./API_CONTRACT.md) | Endpoints, payloads e códigos de erro |
| [`DEPLOY.md`](./docs/DEPLOY.md) | Runbook de produção |
| [Specs](./pscode/specs/) | Comportamento implementado, por capability |
| [`CHANGELOG`](./docs/decisions/CHANGELOG.md) | Registro cronológico das decisões do projeto |
| [Design](./docs/design/) | Brief de produto e auditoria do front-end |
