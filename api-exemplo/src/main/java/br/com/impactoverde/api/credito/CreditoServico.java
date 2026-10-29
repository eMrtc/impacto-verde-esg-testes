package br.com.impactoverde.api.credito;

import br.com.impactoverde.api.erro.ApiException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Extrato em memória (dados de exemplo; num projeto real seria o MongoDB da atividade anterior). */
@Service
public class CreditoServico {

    private final List<Lancamento> lancamentos = new ArrayList<>();

    public synchronized Lancamento gerar(String familia, int pontos, String origem) {
        return registrar(familia, TipoLancamento.GERADO, pontos, origem);
    }

    public synchronized Lancamento resgatar(ResgateRequisicao pedido) {
        int saldo = saldoDe(pedido.familia());
        if (pedido.pontos() > saldo) {
            throw ApiException.regraDeNegocio("Saldo insuficiente para o resgate");
        }
        String origem = pedido.parceiro() + " - " + pedido.item();
        return registrar(pedido.familia(), TipoLancamento.RESGATADO, -pedido.pontos(), origem);
    }

    public synchronized SaldoResposta saldo(String familia) {
        if (extrato(familia).isEmpty()) {
            throw ApiException.naoEncontrado("Família sem lançamentos");
        }
        return new SaldoResposta(familia, saldoDe(familia));
    }

    public synchronized List<Lancamento> extrato(String familia) {
        return lancamentos.stream().filter(l -> l.familia().equals(familia)).toList();
    }

    public synchronized List<Lancamento> todos() {
        return List.copyOf(lancamentos);
    }

    private int saldoDe(String familia) {
        return lancamentos.stream().filter(l -> l.familia().equals(familia)).mapToInt(Lancamento::valorPontos).sum();
    }

    private Lancamento registrar(String familia, TipoLancamento tipo, int valor, String origem) {
        int saldoApos = saldoDe(familia) + valor;
        Lancamento novo = new Lancamento(UUID.randomUUID(), familia, tipo, valor, origem, saldoApos, Instant.now());
        lancamentos.add(novo);
        return novo;
    }
}
