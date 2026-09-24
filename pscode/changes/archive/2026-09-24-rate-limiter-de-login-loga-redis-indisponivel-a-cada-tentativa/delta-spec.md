# fix: rate limiter de login loga Redis indisponível a cada tentativa — Delta

## Changed
- **Origem sem bloqueio não é mais tratada como Redis fora.** No Quarkus,
  `KeyCommands.ttl()` lança `RedisKeyNotFoundException` para o `-2` (chave não
  existe), que é o caso de todo login. O `catch (RuntimeException)` do
  `AuthRateLimiterRedisAdapter.remainingBlock` tratava isso como falha e
  gravava `WARN "Rate limiter unavailable, letting the request through"` a cada
  tentativa. Antes: um warn por login, e a queda real do Redis ficava escondida
  no ruído. Agora: a exceção é capturada antes e vira "sem bloqueio", sem log.
  O warn e o fail-open ficam só para falha real do Redis.
- Contagem, janela, bloqueio e limites (5 / 60 s / 900 s) inalterados.

## Added
- `AuthRateLimiterRedisAdapterIT.unknownOrigin_isNotReportedAsAnOutage`: um
  `Handler` JUL no logger do adapter prova que consultar uma origem sem
  bloqueio não gera `WARNING`. O teste falhava antes da correção.
