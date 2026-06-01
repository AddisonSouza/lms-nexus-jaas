# ADR-007 — Banco Único Multi-Organization

**Status:** Aceito  
**Data:** Maio 2026

## Contexto
O sistema suporta múltiplas organizações. Precisávamos decidir entre banco único compartilhado ou isolamento por schema/banco.

## Decisão
Banco único (MySQL) com isolamento por `organization_id` na camada de aplicação.

## Justificativa
- Simplifica operação, backup e evolução do schema
- Adequado para o escopo do TCC e para um produto SaaS em estágio inicial
- O sistema é multi-organization, não multi-tenant com isolamento de infraestrutura
- `organization_id` extraído sempre do JWT — nunca do request body

## Consequências
- Toda tabela organizacional DEVE ter `organization_id NOT NULL FK`
- Todo Repository organizacional DEVE incluir `organization_id` no WHERE
- Ver regras DB-MT-01 a DB-MT-04 em DECISIONS.md
