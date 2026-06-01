# LMS — Learning Management System

> Plataforma educacional desenvolvida como Trabalho de Conclusão de Curso (TCC).  
> Inspirada no Google Classroom, com foco em organização de conteúdos, gestão de turmas e apoio ao ensino.

---

## Stack

| Camada | Tecnologia |
|---|---|
| Front-end | React 18 + TypeScript + Vite |
| Back-end | Quarkus 3.x + Java 21 |
| Banco | MySQL 8 + Redis 7 |
| Infra | Docker + Compose |
| Arquitetura | Monolito Modular + DDD + Clean Arch + Hexagonal |

---

## Estrutura do Monorepo

```
lms/
├── apps/
│   ├── web/              # React SPA (Vite)
│   └── api/              # Quarkus Monolito Modular
├── packages/
│   └── shared-types/     # Contratos TypeScript compartilhados
├── infra/
│   ├── docker/
│   ├── docker-compose.yml
│   └── docker-compose.prod.yml
└── docs/
    ├── architecture/     # Decisões arquiteturais e ADRs
    ├── requirements/     # Requisitos funcionais
    └── decisions/        # Changelog de decisões
```

---

## Documentação

| Documento | Descrição |
|---|---|
| [Requisitos de Código](./docs/architecture/DECISIONS.md) | Stack, padrões, regras e decisões técnicas |
| [ADRs](./docs/architecture/adrs/README.md) | Architecture Decision Records |
| [Requisitos Funcionais](./docs/requirements/RF.md) | 26 RFs detalhados com fluxos e critérios de aceite |
| [Changelog](./docs/decisions/CHANGELOG.md) | Histórico de decisões do projeto |

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

## Rodando localmente

```bash
# Clone o repositório
git clone https://github.com/{owner}/lms.git
cd lms

# Copie as variáveis de ambiente
cp .env.example .env
# Edite .env com seus valores

# Suba todos os serviços
docker compose up -d

# API disponível em: http://localhost:8080
# Web disponível em: http://localhost:5173
# Swagger UI em:     http://localhost:8080/q/swagger-ui
```

---

## Convenções

- **Commits:** [Conventional Commits](https://www.conventionalcommits.org/)
- **Branches:** `feature/{módulo}-{descricao}` | `fix/{descricao}`
- **PRs:** Obrigatório para `main` e `develop`
- **SDD:** Especificação precede implementação — toda mudança rastreável a um RF ou ADR

---

## Metodologia — SDD

Este projeto utiliza **Specification-Driven Development**: os documentos de requisitos e decisões arquiteturais são contratos técnicos. Toda implementação deve ser rastreável a uma regra definida em `docs/`.
