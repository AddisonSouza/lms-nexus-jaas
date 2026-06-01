# ADR-003 — Monorepo

**Status:** Aceito  
**Data:** Maio 2026

## Contexto
Decidir entre monorepo (front + back no mesmo repo) ou repos separados.

## Decisão
Monorepo com estrutura: `apps/web`, `apps/api`, `packages/shared-types`.

## Justificativa
- Facilita refatoração coordenada entre front e back
- Centraliza CI/CD em um único pipeline
- `packages/shared-types` garante contrato de tipos TypeScript entre as duas apps
- Simplifica o uso com Claude Code — contexto completo em uma sessão

## Consequências
- PRs podem tocar front e back ao mesmo tempo (positivo para features full-stack)
- CI precisa detectar quais apps foram alteradas para não buildar tudo sempre
