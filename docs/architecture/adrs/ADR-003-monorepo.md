# ADR-003 — Monorepo

**Status:** Aceito  
**Data:** Maio 2026

## Contexto
Decidir entre monorepo (front + back no mesmo repo) ou repos separados.

## Decisão
Monorepo com estrutura: `apps/web`, `apps/api`, `infra/`, `docs/`.

## Justificativa
- Facilita refatoração coordenada entre front e back
- Centraliza CI/CD em um único pipeline
- Simplifica o uso com Claude Code — contexto completo em uma sessão

## Consequências
- PRs podem tocar front e back ao mesmo tempo (positivo para features full-stack)
- O workflow único (`.github/workflows/deploy.yml`) filtra por caminho (`apps/**`, `infra/**`) mas roda os testes das duas apps a cada disparo — simples e suficiente para o volume do projeto

## Revisão — Setembro 2026
O pacote `packages/shared-types` previsto originalmente não foi criado. O back-end é Java, então tipos TypeScript compartilhados só serviriam ao front e duplicariam o que já existe. O contrato entre as apps é mantido por:
- `API_CONTRACT.md` — fonte da verdade dos endpoints e payloads;
- schemas Zod por feature em `apps/web/src/features/*/`, que validam as respostas em runtime (mais forte que tipos gerados, que somem na compilação).
