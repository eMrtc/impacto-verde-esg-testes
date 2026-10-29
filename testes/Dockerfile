# Contêiner que executa a suíte Cucumber contra a API (imagem Red Hat com JDK 21 e Maven, usuário não-root).
FROM registry.redhat.io/ubi9/openjdk-21:latest

# Camada de dependências separada do código para aproveitar cache.
# O COPY --chown cria /home/default/build já pertencente ao usuário 185 (não-root).
COPY --chown=185:0 pom.xml /home/default/build/pom.xml
WORKDIR /home/default/build
RUN mvn -B -q dependency:go-offline

COPY --chown=185:0 src ./src

USER 185

# Configuração via variáveis de ambiente: API_BASE_URL, API_TOKEN_OPERADOR, API_TOKEN_ADMIN
CMD ["mvn", "-B", "clean", "test"]
