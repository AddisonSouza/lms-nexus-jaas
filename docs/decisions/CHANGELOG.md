# CHANGELOG — Decisões do Projeto

Registro cronológico de decisões tomadas durante o desenvolvimento.  
Decisões arquiteturais formais ficam em [ADRs](../architecture/adrs/).

---

## Maio 2026 — Setup Inicial

### Stack definida
- **Back-end:** Quarkus 3.x + Java 21 + MySQL 8 + Redis 7
- **Front-end:** React 18 + TypeScript + Vite + TanStack Query + Zustand + Shadcn/ui
- **Infra:** Docker + Compose + Monorepo

### Decisões arquiteturais formalizadas
- `ADR-001` MySQL como banco principal
- `ADR-002` Redis para Refresh Tokens
- `ADR-003` Monorepo
- `ADR-004` StoragePort abstrato
- `ADR-005` MapStruct
- `ADR-006` Shadcn/ui
- `ADR-007` Banco único multi-organization
- `ADR-008` Monolito Modular
- `ADR-009` React SPA em vez de Next.js
- `ADR-010` Lombok obrigatório

### Escopo MVP definido
- 20 Requisitos Funcionais (RF-01 a RF-20)
- 8 módulos: `identity`, `organization`, `classroom`, `curriculum`, `assessment`, `communication`, `reporting`, `storage`

### Escopo Futuro definido
- 6 Requisitos Funcionais (RF-21 a RF-26)
- 2 módulos futuros: `gamification`, `ai`

### Ferramentas de gestão
- GitHub Projects como board principal (Open Spec)
- Documentação versionada no monorepo em `docs/`
- Conventional Commits obrigatório

---

*Novas decisões devem ser registradas aqui com data e contexto.*
