package br.com.impactoverde.testes.contexto;

import br.com.impactoverde.testes.dados.FabricaDados;
import br.com.impactoverde.testes.http.ClienteApi;
import io.cucumber.java.Scenario;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Estado compartilhado entre os passos de UM cenário (o PicoContainer cria uma instância nova por cenário,
 * então nada vaza de um teste para o outro, como deve ser com lixo bem separado).
 */
public class ContextoCenario {

    private static final String CAMINHO_COLETAS = "/api/v1/coletas";

    private final ClienteApi cliente = new ClienteApi();
    private final List<String> coletasCriadas = new ArrayList<>();
    private final Map<String, Double> fotografiaIndicadores = new HashMap<>();

    private Scenario cenario;
    private String token;
    private Response resposta;
    private String familia;
    private String coletaAtualId;
    private Map<String, Object> corpoPreparado;

    public void iniciar(Scenario cenario) {
        this.cenario = cenario;
        this.familia = FabricaDados.familiaUnica();
    }

    /** Executa a chamada do cenário, guarda a resposta e registra no relatório uma linha de evidência. */
    public Response chamar(String metodo, String caminho, Object corpo) {
        resposta = cliente.enviar(metodo, resolver(caminho), token, corpo);
        registrar("%s %s -> HTTP %d".formatted(metodo.toUpperCase(), resolver(caminho), resposta.statusCode()));
        if ("POST".equalsIgnoreCase(metodo) && CAMINHO_COLETAS.equals(caminho) && resposta.statusCode() == 201) {
            registrarColetaCriada(resposta.jsonPath().getString("id"));
        }
        return resposta;
    }

    /** Chamada de apoio (preparação/limpeza) com token explícito; não altera a resposta do cenário. */
    public Response chamarComToken(String metodo, String caminho, String tokenExplicito, Object corpo) {
        return cliente.enviar(metodo, resolver(caminho), tokenExplicito, corpo);
    }

    /** Troca os marcadores {familia} e {coletaId} pelos valores do cenário atual. */
    public String resolver(String texto) {
        return texto
                .replace("{familia}", familia == null ? "" : familia)
                .replace("{coletaId}", coletaAtualId == null ? "" : coletaAtualId);
    }

    public void registrar(String mensagem) {
        if (cenario != null) {
            cenario.log(mensagem);
        }
    }

    public void registrarColetaCriada(String id) {
        coletasCriadas.add(id);
        coletaAtualId = id;
    }

    public List<String> getColetasCriadas() {
        return coletasCriadas;
    }

    public Map<String, Double> getFotografiaIndicadores() {
        return fotografiaIndicadores;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Response getResposta() {
        return resposta;
    }

    public String getFamilia() {
        return familia;
    }

    public String getColetaAtualId() {
        return coletaAtualId;
    }

    public Map<String, Object> getCorpoPreparado() {
        return corpoPreparado;
    }

    public void setCorpoPreparado(Map<String, Object> corpoPreparado) {
        this.corpoPreparado = corpoPreparado;
    }
}
