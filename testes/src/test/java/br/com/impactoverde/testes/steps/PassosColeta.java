package br.com.impactoverde.testes.steps;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.impactoverde.testes.config.ConfiguracaoTeste;
import br.com.impactoverde.testes.contexto.ContextoCenario;
import br.com.impactoverde.testes.dados.FabricaDados;
import io.cucumber.java.pt.Dado;
import io.restassured.response.Response;

/** Preparação de massa de dados de coletas (a API real é chamada, nada de mock: aqui o lixo é verdadeiro). */
public class PassosColeta {

    private final ContextoCenario contexto;

    public PassosColeta(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @Dado("que preparo uma coleta válida de {string} com {int} unidades e peso de {string} kg")
    public void prepararColeta(String tipo, int unidades, String peso) {
        contexto.setCorpoPreparado(FabricaDados.coleta(contexto.getFamilia(), tipo, unidades, Double.parseDouble(peso)));
    }

    @Dado("que preparo uma atualização para {int} unidades e peso de {string} kg no ponto {string}")
    public void prepararAtualizacao(int unidades, String peso, String ponto) {
        contexto.setCorpoPreparado(FabricaDados.atualizacao(unidades, Double.parseDouble(peso), ponto));
    }

    @Dado("que existe uma coleta pendente de {string} com {int} unidades e peso de {string} kg")
    public void existeColetaPendente(String tipo, int unidades, String peso) {
        criarColetaDeApoio(tipo, unidades, Double.parseDouble(peso));
    }

    @Dado("que a coleta está confirmada")
    public void coletaConfirmada() {
        confirmarColetaAtual();
    }

    @Dado("que a família possui {int} pontos de crédito")
    public void familiaComPontos(int pontos) {
        // PILHAS rendem 100 pontos/kg, então 0,01 kg = 1 ponto.
        criarColetaDeApoio("PILHAS", 1, pontos / 100.0);
        confirmarColetaAtual();
    }

    private void criarColetaDeApoio(String tipo, int unidades, double pesoKg) {
        Response criada = contexto.chamarComToken(
                "POST", "/api/v1/coletas", ConfiguracaoTeste.tokenOperador(),
                FabricaDados.coleta(contexto.getFamilia(), tipo, unidades, pesoKg));
        assertThat(criada.statusCode()).as("preparação: criar coleta (%s)", criada.asString()).isEqualTo(201);
        contexto.registrarColetaCriada(criada.jsonPath().getString("id"));
    }

    private void confirmarColetaAtual() {
        Response confirmada = contexto.chamarComToken(
                "PATCH", "/api/v1/coletas/" + contexto.getColetaAtualId() + "/confirmacao",
                ConfiguracaoTeste.tokenOperador(), null);
        assertThat(confirmada.statusCode()).as("preparação: confirmar coleta (%s)", confirmada.asString()).isEqualTo(200);
    }
}
