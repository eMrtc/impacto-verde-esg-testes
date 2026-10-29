package br.com.impactoverde.api.coleta;

import br.com.impactoverde.api.credito.CreditoServico;
import br.com.impactoverde.api.erro.ApiException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ColetaServico {

    private final Map<UUID, Coleta> coletas = new ConcurrentHashMap<>();
    private final CreditoServico creditos;

    public ColetaServico(CreditoServico creditos) {
        this.creditos = creditos;
    }

    public Coleta criar(ColetaRequisicao pedido) {
        Coleta nova = new Coleta(
                UUID.randomUUID(),
                pedido.familia().trim(),
                pedido.tipoResiduo(),
                pedido.quantidadeUnidades(),
                pedido.pesoKg(),
                pedido.pontoColeta().trim(),
                StatusColeta.PENDENTE,
                calcularPontos(pedido.tipoResiduo(), pedido.pesoKg()),
                Instant.now(),
                null);
        coletas.put(nova.id(), nova);
        return nova;
    }

    public List<Coleta> listar(StatusColeta status, String familia) {
        return coletas.values().stream()
                .filter(c -> status == null || c.status() == status)
                .filter(c -> familia == null || c.familia().equals(familia))
                .sorted(Comparator.comparing(Coleta::criadaEm))
                .toList();
    }

    public Coleta buscar(UUID id) {
        Coleta coleta = coletas.get(id);
        if (coleta == null) {
            throw ApiException.naoEncontrado("Coleta não encontrada");
        }
        return coleta;
    }

    public Coleta atualizar(UUID id, ColetaAtualizacao pedido) {
        Coleta atual = buscar(id);
        exigirPendente(atual, "Coleta já confirmada não pode ser alterada (rastreabilidade)");
        Coleta nova = atual.atualizar(
                pedido.quantidadeUnidades(),
                pedido.pesoKg(),
                pedido.pontoColeta().trim(),
                calcularPontos(atual.tipoResiduo(), pedido.pesoKg()));
        coletas.put(id, nova);
        return nova;
    }

    /** Confirma a coleta e gera os créditos alimentares correspondentes. */
    public synchronized Coleta confirmar(UUID id) {
        Coleta atual = buscar(id);
        exigirPendente(atual, "Coleta já confirmada");
        Coleta confirmada = atual.confirmar(Instant.now());
        coletas.put(id, confirmada);
        creditos.gerar(confirmada.familia(), confirmada.pontosGerados(), "COLETA:" + confirmada.id());
        return confirmada;
    }

    public void remover(UUID id) {
        if (coletas.remove(id) == null) {
            throw ApiException.naoEncontrado("Coleta não encontrada");
        }
    }

    private static void exigirPendente(Coleta coleta, String mensagem) {
        if (coleta.status() != StatusColeta.PENDENTE) {
            throw ApiException.conflito(mensagem);
        }
    }

    private static int calcularPontos(TipoResiduo tipo, double pesoKg) {
        return (int) Math.round(pesoKg * tipo.getPontosPorKg());
    }
}
