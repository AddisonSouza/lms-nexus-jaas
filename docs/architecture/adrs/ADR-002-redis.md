# ADR-002 — Redis para Refresh Tokens e Cache

**Status:** Aceito  
**Data:** Maio 2026

## Contexto
Precisávamos de um mecanismo para armazenar Refresh Tokens com TTL e para rate limiting.

## Decisão
Redis 7 para todo dado efêmero com TTL: Refresh Tokens, tokens de uso único (confirmação de e-mail e reset de senha), marcação de sessão obsoleta, rate limiting e contadores de notificações não lidas.

## Justificativa
- Tokens têm TTL natural — Redis é a ferramenta certa para dados efêmeros com expiração
- Operações simples: get/set/del com TTL automático
- Reduz carga no MySQL para a operação crítica de autenticação
- Suporte nativo no Quarkus via Lettuce client

## Consequências
- Adiciona um serviço ao Docker Compose
- Em produção o Redis roda com AOF (`--appendonly yes`): além de sessões, ele guarda tokens de confirmação e reset com validade de horas — perdê-los num restart invalidaria links já enviados por e-mail
- Chave padrão para chaves novas: `{módulo}:{tipo}:{id}` — ex: `identity:stale-since:{userId}`

## Revisão — Setembro 2026
O escopo cresceu durante a implementação. A tabela `email_confirmation_tokens` foi removida (V003) em favor do Redis. Chaves em uso:

| Prefixo | Conteúdo | Módulo |
|---|---|---|
| `rt:{token}` / `rt:user:{userId}` | Refresh token → sessão; índice de tokens por usuário | identity |
| `ect:{token}` / `ect-rl:` | Token de confirmação de e-mail; rate limit do reenvio | identity |
| `prt:{token}` | Token de reset de senha | identity |
| `auth-rl:fail:` / `auth-rl:block:` | Falhas de login por IP e bloqueio | identity |
| `identity:stale-since:{userId}` | Marca de sessão obsoleta (ver ADR-011) | identity |
| `communication:unread-count:{userId}` | Contador de notificações não lidas | communication |

Os prefixos curtos (`rt`, `ect`, `prt`, `auth-rl`) antecedem a convenção e foram mantidos: renomeá-los invalidaria sessões e links ativos sem ganho funcional.
