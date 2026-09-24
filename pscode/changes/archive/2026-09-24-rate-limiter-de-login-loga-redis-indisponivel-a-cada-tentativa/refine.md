# Rate limiter de login loga Redis indisponível a cada tentativa
## Summary
Cada login em produção grava um aviso falso de "Redis fora do ar". O motivo é
que o limitador trata como erro o caso normal: a origem não está bloqueada. A
correção cala esse aviso falso e mantém o aviso quando o Redis cai de verdade.
## Technical detail
- No Quarkus 3.12.3, `KeyCommands.ttl()` lança `RedisKeyNotFoundException` (uma `RuntimeException`) quando o Redis responde `-2`. Isso foi confirmado no bytecode de `AbstractKeyCommands.decodeExpireResponse`. Por isso o ramo `ttl > 0` do `remainingBlock` nunca vê o `-2`, e o `catch (RuntimeException)` loga o WARN.
- Correção em `identity/infrastructure/security/AuthRateLimiterRedisAdapter.remainingBlock`: um `catch (RedisKeyNotFoundException)` antes do genérico retorna `Optional.empty()` sem log. O comentário do `-2` passa a descrever a exceção.
- `registerFailure` e `reset` não usam `ttl()` (`incr`/`expire`/`set`/`del` não lançam para chave ausente), então não mudam.
- Teste em `AuthRateLimiterRedisAdapterIT`: um `java.util.logging.Handler` anexado ao logger `AuthRateLimiterRedisAdapter` (o JBoss LogManager estende o JUL) coleta os registros, e o teste verifica que `remainingBlock` de uma origem sem bloqueio não gera nenhum registro `WARNING`. O handler é removido no `@AfterEach`.
## Scope
### In
- Chave de bloqueio inexistente tratada como "sem bloqueio", sem log.
- IT que falha no código atual e passa com a correção.
### Out
- Política de fail-open, limites (5 / 60 s / 900 s) e identificação da origem.
- Testar o fail-open com o Redis realmente fora do ar.
## Subtasks
- [x] `remainingBlock`: `catch (RedisKeyNotFoundException)` → sem bloqueio e sem log; IT com handler JUL provando que a origem sem bloqueio não gera WARN
