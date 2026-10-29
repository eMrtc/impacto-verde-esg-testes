package br.com.impactoverde.api.esg;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint público usado pelo Docker/CI para saber se a API já acordou. */
@RestController
@RequestMapping(path = "/api/v1/saude", produces = "application/json")
public class SaudeControlador {

    @GetMapping
    public Map<String, String> saude() {
        return Map.of("status", "UP", "servico", "impacto-verde-api-exemplo");
    }
}
