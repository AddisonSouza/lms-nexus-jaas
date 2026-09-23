# Imagem de produção da API. O build roda na própria VM (Ampere A1), então tudo
# aqui precisa existir para linux/arm64 — as duas imagens base do Temurin têm.
# Contexto de build: apps/api

# ─── build ────────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# O pom sozinho primeiro: enquanto as dependências não mudarem, esta camada fica
# em cache e o build seguinte não baixa o mundo de novo.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Os testes usam Testcontainers e precisariam de Docker dentro do Docker; a suíte
# roda fora daqui, no fluxo de review.
RUN mvn -B -q -DskipTests package

# ─── runtime ──────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy AS runtime

# curl existe por um motivo só: é o healthcheck do compose batendo em /q/health.
# A imagem JRE não traz nenhum cliente HTTP e o Java não tem equivalente de CLI.
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

RUN groupadd --system --gid 1001 lms \
 && useradd --system --uid 1001 --gid lms lms

WORKDIR /app

# A ordem importa: `lib/` é grande e muda pouco, `app/` é o nosso código e muda
# a cada commit. Copiar do mais estável para o mais volátil preserva o cache.
COPY --from=build --chown=1001:1001 /build/target/quarkus-app/lib/     ./lib/
COPY --from=build --chown=1001:1001 /build/target/quarkus-app/*.jar    ./
COPY --from=build --chown=1001:1001 /build/target/quarkus-app/app/     ./app/
COPY --from=build --chown=1001:1001 /build/target/quarkus-app/quarkus/ ./quarkus/

USER 1001

EXPOSE 8080

ENV QUARKUS_PROFILE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# `exec` para o java virar PID 1 e receber o SIGTERM do `docker stop` — sem isso
# o shell segura o sinal e o container só morre no timeout.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/quarkus-run.jar"]
