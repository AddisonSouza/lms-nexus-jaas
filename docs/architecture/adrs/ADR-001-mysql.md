# ADR-001 — MySQL como Banco Principal

**Status:** Aceito  
**Data:** Maio 2026

## Contexto
Precisávamos escolher um banco relacional. As opções consideradas foram MySQL 8 e PostgreSQL.

## Decisão
MySQL 8.x como banco principal.

## Justificativa
- Familiaridade da equipe com MySQL
- Suporte nativo no Quarkus sem overhead adicional de driver
- Facilidade de containerização e uso no ecossistema do projeto
- O banco é acessado exclusivamente via Port (`UserRepository`, etc.) — a troca futura não afeta o domínio

## Consequências
- Queries específicas de PostgreSQL (ex: `JSONB`, `ARRAY`) não estarão disponíveis
- Para evoluir para PostgreSQL no futuro, apenas os adapters de infraestrutura precisam mudar
