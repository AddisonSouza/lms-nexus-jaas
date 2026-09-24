# Rate limiter de login loga Redis indisponível a cada tentativa

## Objetivo
O `AuthRateLimiterRedisAdapter` precisa distinguir "origem sem bloqueio" de
"Redis indisponível". Hoje o `redis.key().ttl()` lança
`RedisKeyNotFoundException` quando a chave `auth-rl:block:*` não existe, que é o
caso normal. O `catch` trata isso como falha e grava o WARN "Rate limiter
unavailable" a cada login em produção. Esse ruído esconde uma queda real do
Redis.

## Comportamento esperado
- Chave inexistente: sem bloqueio e sem log.
- WARN e fail-open só quando a conexão com o Redis falhar de verdade.
- Contagem de falhas e bloqueio continuam iguais.

## Fora de escopo
- A política de fail-open, os limites (5 falhas / 60 s / 900 s) e a
  identificação da origem (`X-Forwarded-For`, já corrigida no #394).
