# ADR-004 — StoragePort Abstrato desde o MVP

**Status:** Aceito  
**Data:** Maio 2026

## Contexto
No MVP, armazenamento de arquivos é local. No futuro, será S3 ou MinIO.

## Decisão
Criar `StoragePort` como interface em `domain/port/out/` desde o início, com implementação `LocalStorageAdapter`.

## Justificativa
- Custo zero de abstrair agora
- Trocar implementação no futuro sem tocar domínio ou application — OCP em prática
- `StorageContext` enum permite regras diferentes por tipo de arquivo

## Consequências
- `LocalStorageAdapter` armazena em `data/uploads/{contexto}/{ano}/{mes}/`
- `S3StorageAdapter` será implementado futuramente apenas em `infrastructure/storage/`
- Seleção via `@ConfigProperty(name = "storage.provider")`
