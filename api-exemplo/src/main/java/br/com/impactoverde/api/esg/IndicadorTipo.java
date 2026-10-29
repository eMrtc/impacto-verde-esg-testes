package br.com.impactoverde.api.esg;

import br.com.impactoverde.api.coleta.TipoResiduo;

/** Consolidado por tipo de resíduo (somente coletas confirmadas). */
public record IndicadorTipo(TipoResiduo tipoResiduo, long coletas, double pesoKg, long pontos) {
}
