package br.com.impactoverde.testes.http;

import br.com.impactoverde.testes.config.ConfiguracaoTeste;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** Camada fina sobre o REST Assured: monta a requisição, injeta o token e aplica os timeouts. */
public class ClienteApi {

    private final RestAssuredConfig configuracao = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                    .setParam("http.connection.timeout", ConfiguracaoTeste.timeoutConexaoMs())
                    .setParam("http.socket.timeout", ConfiguracaoTeste.timeoutRespostaMs()));

    /**
     * @param token token Bearer; {@code null} significa "sem credenciais"
     * @param corpo {@code Map}/objeto serializado como JSON, {@code String} enviada como está, ou {@code null}
     */
    public Response enviar(String metodo, String caminho, String token, Object corpo) {
        RequestSpecification requisicao = RestAssured.given()
                .baseUri(ConfiguracaoTeste.baseUrl())
                .config(configuracao)
                .accept(ContentType.JSON);
        if (token != null) {
            requisicao.header("Authorization", "Bearer " + token);
        }
        if (corpo != null) {
            requisicao.contentType(ContentType.JSON).body(corpo);
        }
        return requisicao.request(Method.valueOf(metodo.toUpperCase()), caminho);
    }
}
