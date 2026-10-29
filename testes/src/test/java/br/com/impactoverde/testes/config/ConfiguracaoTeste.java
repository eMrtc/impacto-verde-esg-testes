package br.com.impactoverde.testes.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Configuração da suíte. Precedência: propriedade -D  &gt;  variável de ambiente  &gt;  arquivo teste.properties.
 * Ex.: a chave "api.base-url" também pode vir da variável de ambiente API_BASE_URL.
 * Tokens NUNCA ficam no arquivo: só em variáveis de ambiente (segredo não é piada).
 */
public final class ConfiguracaoTeste {

    private static final Properties ARQUIVO = carregarArquivo();

    private ConfiguracaoTeste() {
    }

    public static String baseUrl() {
        return obter("api.base-url").orElseThrow(() -> new IllegalStateException("Configure api.base-url / API_BASE_URL"));
    }

    public static int timeoutConexaoMs() {
        return Integer.parseInt(obter("api.timeout-conexao-ms").orElse("5000"));
    }

    public static int timeoutRespostaMs() {
        return Integer.parseInt(obter("api.timeout-resposta-ms").orElse("10000"));
    }

    public static int esperaMaximaSegundos() {
        return Integer.parseInt(obter("api.espera-maxima-segundos").orElse("60"));
    }

    public static String tokenOperador() {
        return obrigatorio("api.token-operador");
    }

    public static String tokenAdmin() {
        return obrigatorio("api.token-admin");
    }

    private static String obrigatorio(String chave) {
        return obter(chave).orElseThrow(() -> new IllegalStateException(
                "Configuração obrigatória ausente: defina a variável de ambiente " + nomeVariavel(chave)
                        + " (veja scripts/configurar-ambiente.sh). O valor nunca é impresso nos logs."));
    }

    private static Optional<String> obter(String chave) {
        String propriedade = System.getProperty(chave);
        if (propriedade != null && !propriedade.isBlank()) {
            return Optional.of(propriedade);
        }
        String ambiente = System.getenv(nomeVariavel(chave));
        if (ambiente != null && !ambiente.isBlank()) {
            return Optional.of(ambiente);
        }
        return Optional.ofNullable(ARQUIVO.getProperty(chave)).filter(valor -> !valor.isBlank());
    }

    private static String nomeVariavel(String chave) {
        return chave.replace('.', '_').replace('-', '_').toUpperCase();
    }

    private static Properties carregarArquivo() {
        Properties propriedades = new Properties();
        try (InputStream entrada = ConfiguracaoTeste.class.getResourceAsStream("/config/teste.properties")) {
            if (entrada != null) {
                propriedades.load(entrada);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler config/teste.properties", e);
        }
        return propriedades;
    }
}
