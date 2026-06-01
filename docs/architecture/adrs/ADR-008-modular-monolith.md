# ADR-008 — Monolito Modular em vez de Microserviços

**Status:** Aceito  
**Data:** Maio 2026

## Contexto
Decidir entre Monolito Modular e Microserviços para a arquitetura back-end.

## Decisão
Monolito Modular: única unidade de deploy com módulos internos bem delimitados.

## Justificativa
- Para o escopo do TCC, oferece toda separação de responsabilidades necessária
- Elimina complexidade operacional de microserviços (service discovery, tracing distribuído)
- Módulos desacoplados via Ports permitem extração futura para microserviços sem reescrita do domínio
- Comunicação via interfaces Java e Domain Events — sem chamadas HTTP internas

## Módulos
`identity` | `organization` | `classroom` | `curriculum` | `assessment` | `communication` | `reporting` | `storage`

## Consequências
- Um único JAR/container em produção
- Comunicação assíncrona entre módulos via CDI Events
- Proibido acesso direto ao repositório de outro módulo
