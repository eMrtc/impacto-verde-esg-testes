package br.com.impactoverde.api.credito;

import java.time.Instant;
import java.util.UUID;

/** Lançamento no extrato de créditos alimentares (pilar Social). Resgates têm valor negativo. */
public record Lancamento(
        UUID id,
        String familia,
        TipoLancamento tipo,
        int valorPontos,
        String origem,
        int saldoApos,
        Instant data) {
}
