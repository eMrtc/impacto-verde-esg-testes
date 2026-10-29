package br.com.impactoverde.api.esg;

import br.com.impactoverde.api.coleta.Coleta;
import br.com.impactoverde.api.coleta.ColetaServico;
import br.com.impactoverde.api.coleta.StatusColeta;
import br.com.impactoverde.api.coleta.TipoResiduo;
import br.com.impactoverde.api.credito.CreditoServico;
import br.com.impactoverde.api.credito.Lancamento;
import br.com.impactoverde.api.credito.TipoLancamento;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/esg", produces = "application/json")
public class IndicadoresControlador {

    private final ColetaServico coletas;
    private final CreditoServico creditos;

    public IndicadoresControlador(ColetaServico coletas, CreditoServico creditos) {
        this.coletas = coletas;
        this.creditos = creditos;
    }

    @GetMapping("/indicadores")
    public IndicadoresResposta indicadores() {
        List<Coleta> todas = coletas.listar(null, null);
        List<Coleta> confirmadas = todas.stream().filter(c -> c.status() == StatusColeta.CONFIRMADA).toList();
        List<Lancamento> lancamentos = creditos.todos();

        List<IndicadorTipo> porTipo = Arrays.stream(TipoResiduo.values())
                .map(tipo -> consolidar(tipo, confirmadas))
                .filter(i -> i.coletas() > 0)
                .toList();

        return new IndicadoresResposta(
                todas.size(),
                confirmadas.size(),
                todas.size() - confirmadas.size(),
                confirmadas.stream().mapToDouble(Coleta::pesoKg).sum(),
                lancamentos.stream().filter(l -> l.tipo() == TipoLancamento.GERADO).mapToLong(Lancamento::valorPontos).sum(),
                -lancamentos.stream().filter(l -> l.tipo() == TipoLancamento.RESGATADO).mapToLong(Lancamento::valorPontos).sum(),
                confirmadas.stream().map(Coleta::familia).collect(Collectors.toSet()).size(),
                porTipo,
                Instant.now());
    }

    private static IndicadorTipo consolidar(TipoResiduo tipo, List<Coleta> confirmadas) {
        List<Coleta> doTipo = confirmadas.stream().filter(c -> c.tipoResiduo() == tipo).toList();
        return new IndicadorTipo(
                tipo,
                doTipo.size(),
                doTipo.stream().mapToDouble(Coleta::pesoKg).sum(),
                doTipo.stream().mapToLong(Coleta::pontosGerados).sum());
    }
}
