# ADR-013 — Object Storage via API S3-compatible em Todos os Ambientes

**Status:** Aceito — revisa a ADR-004  
**Data:** Setembro 2026

## Contexto
A ADR-004 previa um `LocalStorageAdapter` em disco para o MVP e um adapter S3 no futuro, escolhidos por `storage.provider`. Em produção, disco local na VM (ADR-012) não tem redundância e cresceria sem controle; manter dois adapters dobraria o código e os testes.

## Decisão
Uma única implementação, `S3StorageAdapter` (`module/storage/infrastructure/`), usando o AWS SDK via `quarkus-amazon-s3`. Só o endpoint muda por ambiente:

| Ambiente | Serviço |
|---|---|
| dev | MinIO no `docker-compose.yml` |
| test | LocalStack via Quarkus Dev Services |
| prod | OCI Object Storage (endpoint S3-compatible, Customer Secret Key, `path-style-access`) |

Chave do objeto: `{contexto}/{ano}/{mes}/{uuid}-{nome-sanitizado}`; o nome original vai em metadado para o `Content-Disposition`. Arquivos são servidos pela API (`GET /files/{fileKey}`), que valida permissão antes de fazer o stream.

## Justificativa
- Um só caminho de código testado em todos os ambientes — o que roda no teste é o que roda em produção
- S3 é o protocolo de fato; qualquer provedor (AWS, OCI, MinIO, R2) serve sem mudar código
- O `StoragePort` da ADR-004 continua isolando domínio e application de tudo isso

## Consequências
- Desenvolvimento local depende do container MinIO
- Troca de provedor = variáveis `STORAGE_ENDPOINT`, `STORAGE_BUCKET`, `STORAGE_REGION` e credenciais
- `STG-02` (armazenamento em disco) deixa de valer
