# LMS Nexus

Plataforma de gestão de aprendizado (LMS) **multi-tenant**, desenvolvida como
TCC. Organiza turmas, disciplinas, tarefas e avaliações dentro de **Organizações**
educacionais isoladas. Monorepo: `apps/api` (Quarkus), `apps/web` (React),
`packages/shared-types`.

## Documentos de referência — leia antes de implementar

Estes documentos são **contrato**, não sugestão. Toda implementação deve ser
rastreável a uma regra ou requisito deles.

| Documento | O que responde |
|---|---|
| `docs/architecture/DECISIONS.md` | Regras de código e decisões arquiteturais. É a fonte da verdade: stack, módulos, camadas, convenções (`NOM-*`, `GIT-*`, `MOD-*`, `LOM-*`) e o Checklist de Conformidade da seção 8. |
| `docs/architecture/adrs/` | ADR-001 a ADR-010 — o *porquê* de cada decisão (MySQL, Redis, monorepo, StoragePort, MapStruct, shadcn, banco único, monolito modular, React SPA, Lombok). |
| `docs/requirements/RF.md` | Os 26 requisitos funcionais (RF-01 a RF-26): 20 do MVP, 6 de evolução futura. |
| `API_CONTRACT.md` | Contrato de API: endpoints, payloads, códigos de erro. |
| `pscode/specs/<capability>/spec.md` | Specs vivas por capability, no formato Requirement/Scenario. Refletem o comportamento **já implementado**. |

Antes de escrever código para um RF, leia a seção correspondente de `RF.md` e a
spec da capability em `pscode/specs/`. Ao concluir, o `/ps:complete` registra o
delta.

## Arquitetura

**Monolito Modular + DDD + Clean Architecture + Hexagonal.** Uma unidade de
deploy, módulos com fronteiras próprias.

**Módulos:** `identity`, `organization`, `classroom`, `curriculum`, `assessment`,
`communication`, `reporting`, `storage`.
**Papéis:** `ADMIN_ORG`, `GESTOR`, `PROFESSOR`, `ALUNO`.

Estrutura de cada módulo em `apps/api/src/main/java/br/edu/lms/module/{modulo}/`:

```
domain/          model/  event/  exception/  port/in/  port/out/
application/     usecase/  dto/
infrastructure/  persistence/  security/  mail/
interfaces/      rest/
```

## Regras invioláveis

Detalhadas em `DECISIONS.md`; resumidas aqui porque valem para toda mudança.

**Camadas e módulos**
- `domain/` não importa `jakarta.persistence`, Quarkus nem Lombok (`@Data`, `@Entity`).
- `application/` não importa `infrastructure/` nem `interfaces/`.
- Módulos se comunicam via interfaces Java (Ports) ou Domain Events CDI — nunca
  HTTP interno, nunca acesso ao repositório de outro módulo, nunca dependência
  circular.

**Back-end**
- `organization_id` sempre extraído do JWT, nunca do request body.
- Soft delete obrigatório em `User`, `Organization`, `Classroom`, `Subject` e
  `Task`: coluna `deleted_at TIMESTAMP NULL` (DB-05).
- Flyway obrigatório para qualquer mudança de schema.
  `quarkus.hibernate-orm.database.generation=update` é proibido.
- MapStruct em todo mapeamento entre camadas — mapeamento manual é proibido.
- Nenhum segredo hardcoded: tudo via `@ConfigProperty`.
- Testes de integração com Testcontainers. H2 é proibido.

**Front-end**
- TypeScript strict — sem `any` nem `@ts-ignore`.
- TanStack Query para server state, Zustand para client state. `useState` para
  dados de API é proibido; `useEffect` para fetch também.
- Zod para cada formulário e cada resposta de API.
- Sem import cruzado entre features. Rotas protegidas via `ProtectedRoute`.
- Único pacote de ícones: Lucide React.

**Git**
- Conventional Commits, mensagens em inglês, commits atômicos.
- Proibido commit direto em `main` — toda mudança via Pull Request.

<!-- PSCODE:START -->
## PSCode — Guided SDD

This project uses **PSCode**: a guided, spec-driven flow installed into your
coding agent. Every change moves through short, human-validated steps and lives
under `pscode/changes/<slug>/`.

**Flow (mirrors the board):** `/ps:draft` (Backlog) → `/ps:refine <card#>` (In
Refinement → Ready to Dev) → `/ps:dev <card#>` (In Development → In Code Review →
In Test → Ready to Deploy) → `/ps:complete <card#>` (Done). `/ps:cancel <card#>`
sends a card to Cancelled.

**Rules (non-negotiable):**
- Prefer the `AskUserQuestion` tool for any question — at every step — with a
  recommended option first. This includes **yes/no confirmations** (e.g. "can I
  mark `[x]` and close the sub-issue?"): pair them with an `AskUserQuestion`
  offering `Sim` / `Não` (recommended first), never plain prose. It makes
  answering a one-tap choice.
- Do not advance to the next step without explicit user approval.
- Implement one subtask at a time; never expand scope mid-subtask.
- Keep every artifact short — each step fits on one terminal screen.

Limits and settings live in `pscode/config.yaml`.
<!-- PSCODE:END -->
