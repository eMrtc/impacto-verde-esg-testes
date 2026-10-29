package br.com.impactoverde.api.coleta;

import java.time.Instant;
import java.util.UUID;

/** Registro de um descarte de resíduo perigoso feito por uma família (pilar Ambiental). */
public record Coleta(
        UUID id,
        String familia,
        TipoResiduo tipoResiduo,
        int quantidadeUnidades,
        double pesoKg,
        String pontoColeta,
        StatusColeta status,
        int pontosGerados,
        Instant criadaEm,
        Instant confirmadaEm) {

    public Coleta atualizar(int novaQuantidade, double novoPeso, String novoPonto, int novosPontos) {
        return new Coleta(id, familia, tipoResiduo, novaQuantidade, novoPeso, novoPonto, status, novosPontos, criadaEm, confirmadaEm);
    }

    public Coleta confirmar(Instant quando) {
        return new Coleta(id, familia, tipoResiduo, quantidadeUnidades, pesoKg, pontoColeta, StatusColeta.CONFIRMADA, pontosGerados, criadaEm, quando);
    }
}
