package br.com.impactoverde.testes.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import br.com.impactoverde.testes.config.ConfiguracaoTeste;
import br.com.impactoverde.testes.contexto.ContextoCenario;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.restassured.response.Response;
import java.util.List;

/**
 * Os indicadores ESG são globais, então os testes comparam "antes e depois" (delta) em vez de valores absolutos:
 * assim a suíte continua repetível mesmo se a API já tiver dados de outras execuções.
 */
public class PassosIndicadores {

    private static final String CAMINHO = "/api/v1/esg/indicadores";
    private static final List<String> INDICADORES_NUMERICOS = List.of(
            "totalColetas", "coletasConfirmadas", "coletasPendentes", "pesoTotalKg",
            "pontosDistribuidos", "pontosResgatados", "familiasAtendidas");

    private final ContextoCenario contexto;

    public PassosIndicadores(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Dado("que registro a fotografia atual dos indicadores ESG")
    public void registrarFotografia() {
        Response resposta = contexto.chamarComToken("GET", CAMINHO, ConfiguracaoTeste.tokenOperador(), null);
        assertThat(resposta.statusCode()).isEqualTo(200);
        INDICADORES_NUMERICOS.forEach(
                nome -> contexto.getFotografiaIndicadores().put(nome, resposta.jsonPath().getDouble(nome)));
    }

    @Então("o indicador {string} deve ter aumentado em {string}")
    public void indicadorAumentouEm(String nome, String aumento) {
        double antes = contexto.getFotografiaIndicadores().get(nome);
        double depois = contexto.getResposta().jsonPath().getDouble(nome);
        assertThat(depois - antes).as("variação do indicador '%s'", nome).isCloseTo(Double.parseDouble(aumento), within(0.001));
    }

    @Então("o indicador {string} não deve ter mudado")
    public void indicadorNaoMudou(String nome) {
        indicadorAumentouEm(nome, "0");
    }
}
