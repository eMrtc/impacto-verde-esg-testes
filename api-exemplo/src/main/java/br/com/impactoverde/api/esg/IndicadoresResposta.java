package br.com.impactoverde.api.esg;

import java.time.Instant;
import java.util.List;

/**
 * Indicadores ESG consolidados. Só coletas CONFIRMADAS entram nos números ambientais
 * (governança: o que não foi auditado não conta).
 */
public record IndicadoresResposta(
        long totalColetas,
        long coletasConfirmadas,
        long coletasPendentes,
        double pesoTotalKg,
        long pontosDistribuidos,
        long pontosResgatados,
        long familiasAtendidas,
        List<IndicadorTipo> porTipoResiduo,
        Instant geradoEm) {
}
