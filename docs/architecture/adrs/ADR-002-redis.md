# ADR-002 — Redis para Refresh Tokens e Cache

**Status:** Aceito  
**Data:** Maio 2026

## Contexto
Precisávamos de um mecanismo para armazenar Refresh Tokens com TTL e para rate limiting.

## Decisão
Redis 7 para Refresh Tokens, rate limiting e contadores de notificações não lidas.

## Justificativa
- Tokens têm TTL natural — Redis é a ferramenta certa para dados efêmeros com expiração
- Operações simples: get/set/del com TTL automático
- Reduz carga no MySQL para a operação crítica de autenticação
- Suporte nativo no Quarkus via Lettuce client

## Consequências
- Adiciona um serviço ao Docker Compose
- Refresh Tokens são perdidos se o Redis for reiniciado sem persistência (aceitável — usuário faz login novamente)
- Chave padrão: `{módulo}:{tipo}:{id}` — ex: `auth:refresh_token:user_123`
