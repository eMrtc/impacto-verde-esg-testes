package br.com.impactoverde.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * API de exemplo do Impacto Verde ESG: separa o lixo perigoso do lixo de código.
 * Serve de alvo para a suíte de testes automatizados deste repositório.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ImpactoVerdeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImpactoVerdeApiApplication.class, args);
    }
}
