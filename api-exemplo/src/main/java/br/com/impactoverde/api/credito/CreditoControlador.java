package br.com.impactoverde.api.credito;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/creditos", produces = "application/json")
public class CreditoControlador {

    private final CreditoServico servico;

    public CreditoControlador(CreditoServico servico) {
        this.servico = servico;
    }

    @GetMapping("/{familia}/saldo")
    public SaldoResposta saldo(@PathVariable String familia) {
        return servico.saldo(familia);
    }

    @GetMapping("/{familia}/extrato")
    public List<Lancamento> extrato(@PathVariable String familia) {
        return servico.extrato(familia);
    }

    @PostMapping(path = "/resgates", consumes = "application/json")
    public ResponseEntity<Lancamento> resgatar(@Valid @RequestBody ResgateRequisicao pedido) {
        return ResponseEntity.status(201).body(servico.resgatar(pedido));
    }
}
