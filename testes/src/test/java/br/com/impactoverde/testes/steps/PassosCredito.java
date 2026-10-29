package br.com.impactoverde.testes.steps;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.impactoverde.testes.config.ConfiguracaoTeste;
import br.com.impactoverde.testes.contexto.ContextoCenario;
import br.com.impactoverde.testes.dados.FabricaDados;
import io.cucumber.java.pt.Dado;
import io.restassured.response.Response;

/** Preparação do corpo de resgates de créditos alimentares. */
public class PassosCredito {

    private final ContextoCenario contexto;

    public PassosCredito(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Dado("que envio o resgate preparado")
    public void enviarResgatePreparado() {
        Response resposta = contexto.chamarComToken(
                "POST", "/api/v1/creditos/resgates", ConfiguracaoTeste.tokenOperador(), contexto.getCorpoPreparado());
        assertThat(resposta.statusCode()).as("preparação: resgate (%s)", resposta.asString()).isEqualTo(201);
    }

    @Dado("que preparo um resgate de {int} pontos no parceiro {string} para o item {string}")
    public void prepararResgate(int pontos, String parceiro, String item) {
        contexto.setCorpoPreparado(FabricaDados.resgate(contexto.getFamilia(), pontos, parceiro, item));
    }
}
