package br.com.impactoverde.testes.hooks;

import br.com.impactoverde.testes.config.ConfiguracaoTeste;
import br.com.impactoverde.testes.contexto.ContextoCenario;
import br.com.impactoverde.testes.http.ClienteApi;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;

/** Ciclo de vida: espera a API acordar, e depois de cada cenário recolhe o lixo de teste (coletas criadas). */
public class Hooks {

    private final ContextoCenario contexto;

    public Hooks(ContextoCenario contexto) {
        this.contexto = contexto;
    }

    @BeforeAll
    public static void aguardarApi() throws InterruptedException {
        ClienteApi cliente = new ClienteApi();
        long limite = System.currentTimeMillis() + ConfiguracaoTeste.esperaMaximaSegundos() * 1000L;
        Exception ultimaFalha = null;
        while (System.currentTimeMillis() < limite) {
            try {
                if (cliente.enviar("GET", "/api/v1/saude", null, null).statusCode() == 200) {
                    System.out.println("[QA-ESG] API no ar em " + ConfiguracaoTeste.baseUrl() + ". Bora separar o lixo do código!");
                    return;
                }
            } catch (Exception e) {
                ultimaFalha = e;
            }
            Thread.sleep(1000);
        }
        throw new IllegalStateException("A API não respondeu em " + ConfiguracaoTeste.baseUrl()
                + " dentro do tempo limite. Ela dormiu no ponto de coleta? "
                + (ultimaFalha != null ? ultimaFalha.getClass().getSimpleName() : ""));
    }

    @Before
    public void aoIniciar(Scenario cenario) {
        contexto.iniciar(cenario);
        contexto.registrar("Iniciando: " + cenario.getName());
    }

    @After
    public void aoTerminar(Scenario cenario) {
        String tokenAdmin = ConfiguracaoTeste.tokenAdmin();
        for (String id : contexto.getColetasCriadas()) {
            contexto.chamarComToken("DELETE", "/api/v1/coletas/" + id, tokenAdmin, null);
        }
        contexto.registrar("Limpeza concluída: %d coleta(s) de teste reciclada(s). Status final: %s"
                .formatted(contexto.getColetasCriadas().size(), cenario.getStatus()));
    }
}
