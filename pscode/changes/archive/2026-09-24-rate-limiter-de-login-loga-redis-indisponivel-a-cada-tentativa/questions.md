# Questions

- [x] Como tratar a chave inexistente? → `catch (RedisKeyNotFoundException)` específico antes do `RuntimeException`; mantém 1 round-trip e sem race (descartados: `exists()` antes do `ttl()`, e o comando `TTL` cru).
- [x] Como provar que o WARN sumiu? → No `AuthRateLimiterRedisAdapterIT`, um `Handler` JUL anexado ao logger do adapter; sem dependência nova.
