# ADR-005 — MapStruct para Mapeamento entre Camadas

**Status:** Aceito  
**Data:** Maio 2026

## Contexto
Precisávamos de uma estratégia para mapear entre Domain Entities, JPA Entities e DTOs HTTP.

## Decisão
MapStruct 1.5+ para todos os mapeamentos entre camadas.

## Justificativa
- Type-safe: erros de mapeamento em compile-time, não em runtime
- Sem reflection: gera código Java puro em tempo de compilação
- Compatível com GraalVM (nativo) para evolução futura
- Elimina erros humanos de mapeamento manual

## Consequências
- Proibido mapeamento manual entre camadas
- Cada módulo tem seu próprio mapper em `infrastructure/persistence/mapper/` e `interfaces/mapper/`
