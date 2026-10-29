package br.com.impactoverde.api.coleta;

/** Tipos de resíduo perigoso aceitos e quantos pontos cada quilo rende. */
public enum TipoResiduo {
    PILHAS(100),
    ISQUEIROS(100),
    ELETRONICOS_PEQUENOS(50),
    OLEO_DE_COZINHA(30),
    LAMPADAS_FLUORESCENTES(80),
    BATERIAS_DE_CELULAR(140);

    private final int pontosPorKg;

    TipoResiduo(int pontosPorKg) {
        this.pontosPorKg = pontosPorKg;
    }

    public int getPontosPorKg() {
        return pontosPorKg;
    }
}
