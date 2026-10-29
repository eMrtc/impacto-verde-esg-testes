# ---------- Estágio 1: compilação ----------
FROM registry.redhat.io/ubi9/openjdk-21:latest AS build
# O COPY --chown cria /home/default/build já pertencente ao usuário 185 (não-root), que precisa gravar em target/.
COPY --chown=185:0 pom.xml /home/default/build/pom.xml
WORKDIR /home/default/build
RUN mvn -B -q dependency:go-offline
COPY --chown=185:0 src ./src
RUN mvn -B -q -DskipTests package

# ---------- Estágio 2: execução (imagem mínima, somente JRE, sem root) ----------
FROM registry.redhat.io/ubi9/openjdk-21-runtime:latest
WORKDIR /app
COPY --from=build --chown=185:0 /home/default/build/target/impacto-verde-api.jar /app/impacto-verde-api.jar

# Usuário não-root (UID 185 já existe na imagem)
USER 185

# Segurança: escuta só em loopback. No docker-compose os testes compartilham a rede desta API.
ENV SERVER_ADDRESS=127.0.0.1 \
    SERVER_PORT=8080

# Os tokens (IMPACTO_VERDE_TOKEN_OPERADOR / IMPACTO_VERDE_TOKEN_ADMIN) são injetados em tempo de execução.
ENTRYPOINT ["java", "-jar", "/app/impacto-verde-api.jar"]
