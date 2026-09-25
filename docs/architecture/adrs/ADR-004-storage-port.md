# ADR-004 — StoragePort Abstrato desde o MVP

**Status:** Aceito — revisado em Setembro 2026 (ver ADR-013)  
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
- `StoragePort` vive em `module/storage/domain/port/out/`; os demais módulos dependem só dele
- A troca de implementação não tocou domínio nem application — a abstração cumpriu o objetivo

## Revisão — Setembro 2026
O `LocalStorageAdapter` e a seleção por `storage.provider` não chegaram a ser implementados: o projeto adotou direto um único `S3StorageAdapter` (em `module/storage/infrastructure/`) apontando para qualquer serviço S3-compatible. A decisão e seus motivos estão na ADR-013.
