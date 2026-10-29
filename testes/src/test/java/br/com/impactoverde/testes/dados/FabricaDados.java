package br.com.impactoverde.testes.dados;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Gera massa de dados única por cenário, garantindo testes independentes e repetíveis. */
public final class FabricaDados {

    private FabricaDados() {
    }

    /** Nome de família exclusivo (ex.: "familia-qa-a1b2c3d4"), para um cenário nunca pisar no outro. */
    public static String familiaUnica() {
        return "familia-qa-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static Map<String, Object> coleta(String familia, String tipoResiduo, int unidades, double pesoKg) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("familia", familia);
        corpo.put("tipoResiduo", tipoResiduo);
        corpo.put("quantidadeUnidades", unidades);
        corpo.put("pesoKg", pesoKg);
        corpo.put("pontoColeta", "EcoPonto Vila Mariana");
        return corpo;
    }

    public static Map<String, Object> atualizacao(int unidades, double pesoKg, String pontoColeta) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("quantidadeUnidades", unidades);
        corpo.put("pesoKg", pesoKg);
        corpo.put("pontoColeta", pontoColeta);
        return corpo;
    }

    public static Map<String, Object> resgate(String familia, int pontos, String parceiro, String item) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("familia", familia);
        corpo.put("parceiro", parceiro);
        corpo.put("item", item);
        corpo.put("pontos", pontos);
        return corpo;
    }

    /**
     * Aplica uma mutação de campo usada nos cenários negativos.
     * "(ausente)" remove o campo; campos numéricos são convertidos para número; o resto vai como texto.
     */
    public static void aplicarValor(Map<String, Object> corpo, String campo, String valor) {
        if ("(ausente)".equals(valor)) {
            corpo.remove(campo);
            return;
        }
        corpo.put(campo, switch (campo) {
            case "quantidadeUnidades", "pontos" -> Integer.valueOf(valor);
            case "pesoKg" -> Double.valueOf(valor);
            default -> valor;
        });
    }
}
