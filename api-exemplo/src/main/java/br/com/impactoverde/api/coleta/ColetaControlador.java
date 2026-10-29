package br.com.impactoverde.api.coleta;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/coletas", produces = "application/json")
public class ColetaControlador {

    private final ColetaServico servico;

    public ColetaControlador(ColetaServico servico) {
        this.servico = servico;
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<Coleta> criar(@Valid @RequestBody ColetaRequisicao pedido) {
        Coleta criada = servico.criar(pedido);
        return ResponseEntity.created(URI.create("/api/v1/coletas/" + criada.id())).body(criada);
    }

    @GetMapping
    public List<Coleta> listar(
            @RequestParam(required = false) StatusColeta status, @RequestParam(required = false) String familia) {
        return servico.listar(status, familia);
    }

    @GetMapping("/{id}")
    public Coleta buscar(@PathVariable UUID id) {
        return servico.buscar(id);
    }

    @PutMapping(path = "/{id}", consumes = "application/json")
    public Coleta atualizar(@PathVariable UUID id, @Valid @RequestBody ColetaAtualizacao pedido) {
        return servico.atualizar(id, pedido);
    }

    @PatchMapping("/{id}/confirmacao")
    public Coleta confirmar(@PathVariable UUID id) {
        return servico.confirmar(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        servico.remover(id);
        return ResponseEntity.noContent().build();
    }
}
