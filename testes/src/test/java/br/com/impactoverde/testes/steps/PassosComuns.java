package br.com.impactoverde.testes.steps;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import br.com.impactoverde.testes.config.ConfiguracaoTeste;
import br.com.impactoverde.testes.contexto.ContextoCenario;
import br.com.impactoverde.testes.dados.FabricaDados;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;

/** Passos genéricos reutilizados por todas as features: autenticação, envio de requisições e asserções de resposta. */
public class PassosComuns {

    private static final List<String> PISTAS_DE_VAZAMENTO =
            List.of("Exception", "stacktrace", "at br.com", "at org.", "java.lang", "springframework");

    private final ContextoCenario contexto;

    public PassosComuns(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    // ---------- Dado: pré-condições ----------

    @Dado("que a API do Impacto Verde está disponível")
    public void apiDisponivel() {
        Response saude = contexto.chamar("GET", "/api/v1/saude", null);
        assertThat(saude.statusCode()).as("a API deveria estar no ar").isEqualTo(200);
    }

    @Dado("que estou autenticado como {string}")
    public void autenticadoComo(String perfil) {
        switch (perfil) {
            case "operador" -> contexto.setToken(ConfiguracaoTeste.tokenOperador());
            case "administrador" -> contexto.setToken(ConfiguracaoTeste.tokenAdmin());
            default -> fail("Perfil desconhecido: " + perfil + " (use 'operador' ou 'administrador')");
        }
    }

    @Dado("que não envio credenciais")
    public void semCredenciais() {
        contexto.setToken(null);
    }

    @Dado("que envio um token inválido")
    public void tokenInvalido() {
        contexto.setToken("token-invalido-so-para-teste");
    }

    @Dado("que altero o campo {string} para {string}")
    public void alterarCampo(String campo, String valor) {
        FabricaDados.aplicarValor(contexto.getCorpoPreparado(), campo, valor);
    }

    // ---------- Quando: ações ----------

    @Quando("eu envio {string} para {string}")
    public void enviar(String metodo, String caminho) {
        contexto.chamar(metodo, caminho, null);
    }

    @Quando("eu envio {string} para {string} com o corpo preparado")
    public void enviarComCorpoPreparado(String metodo, String caminho) {
        contexto.chamar(metodo, caminho, contexto.getCorpoPreparado());
    }

    @Quando("eu envio {string} para {string} com o corpo bruto:")
    public void enviarComCorpoBruto(String metodo, String caminho, String corpoBruto) {
        contexto.chamar(metodo, caminho, corpoBruto);
    }

    // ---------- Então: asserções ----------

    @Então("o status da resposta deve ser {int}")
    public void statusDeveSer(int esperado) {
        assertThat(contexto.getResposta().statusCode())
                .as("status HTTP (corpo: %s)", contexto.getResposta().asString())
                .isEqualTo(esperado);
    }

    @Então("a resposta deve respeitar o schema {string}")
    public void respeitarSchema(String arquivo) {
        contexto.getResposta().then().body(matchesJsonSchemaInClasspath("schemas/" + arquivo));
        contexto.registrar("Contrato validado contra schemas/" + arquivo);
    }

    @Então("o campo {string} deve ser {string}")
    public void campoDeveSer(String caminho, String esperado) {
        Object atual = contexto.getResposta().jsonPath().get(caminho);
        assertThat(String.valueOf(atual)).as("campo '%s'", caminho).isEqualTo(contexto.resolver(esperado));
    }

    @Então("o campo {string} não deve ser nulo")
    public void campoNaoNulo(String caminho) {
        assertThat((Object) contexto.getResposta().jsonPath().get(caminho)).as("campo '%s'", caminho).isNotNull();
    }

    @Então("o campo {string} deve ser do tipo {string}")
    public void campoDeveSerDoTipo(String caminho, String tipo) {
        Object valor = contexto.getResposta().jsonPath().get(caminho);
        Class<?> esperado = switch (tipo) {
            case "texto" -> String.class;
            case "numero" -> Number.class;
            case "lista" -> List.class;
            case "objeto" -> Map.class;
            case "booleano" -> Boolean.class;
            default -> throw new IllegalArgumentException("Tipo desconhecido: " + tipo);
        };
        assertThat(valor).as("campo '%s'", caminho).isInstanceOf(esperado);
    }

    @Então("a lista deve ter exatamente {int} elemento(s)")
    public void listaComTamanho(int esperado) {
        assertThat(contexto.getResposta().jsonPath().getList("$")).hasSize(esperado);
    }

    @Então("a lista {string} deve conter o valor {string}")
    public void listaDeveConter(String caminho, String valor) {
        List<Object> itens = contexto.getResposta().jsonPath().getList(caminho);
        assertThat(itens).as("lista '%s'", caminho).contains(contexto.resolver(valor));
    }

    @Então("todos os itens da lista devem ter {string} igual a {string}")
    public void todosOsItensIguais(String campo, String esperado) {
        List<Object> valores = contexto.getResposta().jsonPath().getList(campo);
        assertThat(valores).isNotEmpty().allSatisfy(v -> assertThat(String.valueOf(v)).isEqualTo(contexto.resolver(esperado)));
    }

    @Então("o cabeçalho {string} deve conter {string}")
    public void cabecalhoDeveConter(String nome, String trecho) {
        assertThat(contexto.getResposta().getHeader(nome)).as("cabeçalho %s", nome).contains(contexto.resolver(trecho));
    }

    @Então("os detalhes do erro devem citar o campo {string}")
    public void detalhesCitamCampo(String campo) {
        assertThat(contexto.getResposta().jsonPath().getList("detalhes.campo"))
                .as("campos citados em 'detalhes'")
                .contains(campo);
    }

    @Então("o detalhe do campo {string} deve ser {string}")
    public void detalheDoCampo(String campo, String mensagem) {
        String atual = contexto.getResposta().jsonPath().getString("detalhes.find { it.campo == '" + campo + "' }.mensagem");
        assertThat(atual).as("mensagem de validação do campo '%s'", campo).isEqualTo(mensagem);
    }

    @Então("a mensagem de erro deve ser {string}")
    public void mensagemDeErro(String esperada) {
        assertThat(contexto.getResposta().jsonPath().getString("mensagem")).isEqualTo(esperada);
    }

    @Então("o corpo da resposta deve estar vazio")
    public void corpoVazio() {
        assertThat(contexto.getResposta().asString()).isEmpty();
    }

    @Então("o corpo não deve expor detalhes internos")
    public void semVazamento() {
        String corpo = contexto.getResposta().asString();
        assertThat(PISTAS_DE_VAZAMENTO).allSatisfy(pista -> assertThat(corpo).doesNotContain(pista));
    }
}
