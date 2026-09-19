package br.edu.lms.support;

import io.quarkus.arc.Arc;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;

/**
 * Zera o limitador de autenticação antes de cada teste.
 *
 * <p>Todos os ITs chegam de 127.0.0.1, então são uma única origem para o
 * limitador: sem isso, a primeira classe que acumula falhas de login bloqueia
 * `/auth` para todas as que rodarem depois, e a suíte quebra por ordem de
 * execução em vez de por comportamento.
 *
 * <p>É um callback do Quarkus, e não uma `Extension` do JUnit, porque só ele
 * roda dentro do classloader da aplicação — de fora, `Arc.container()` é nulo.
 * Registrado em `META-INF/services`, vale para toda a suíte sem anotar classe
 * por classe.
 */
public class ClearAuthRateLimit implements QuarkusTestBeforeEachCallback {

    @Override
    public void beforeEach(QuarkusTestMethodContext context) {
        var container = Arc.container();
        if (container == null) {
            return;
        }
        var redis = container.instance(RedisDataSource.class).get();
        var keys = redis.key().keys("auth-rl:*");
        if (!keys.isEmpty()) {
            redis.key().del(keys.toArray(new String[0]));
        }
    }
}
