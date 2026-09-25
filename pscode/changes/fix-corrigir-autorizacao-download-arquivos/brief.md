# [fix] corrigir autorização do download de arquivos

## Objetivo
Hoje o `GET /files/{fileKey}` entrega qualquer arquivo a qualquer usuário autenticado, sem checar organização nem vínculo com o recurso dono do arquivo. Isso viola a regra STG-03 (ver ADR-013).

## Comportamento esperado
O download só é liberado para quem tem acesso ao recurso dono do arquivo (material de disciplina, anexo de tarefa, anexo de entrega, anexo de aviso) dentro da organização do JWT. Nos demais casos a API responde 404, sem revelar se o arquivo existe.

## Fora de escopo
- Trocar o provedor de storage
- URLs pré-assinadas
- Mudanças nas regras de upload
