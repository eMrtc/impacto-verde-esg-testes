package br.com.impactoverde.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Tokens vindos de variáveis de ambiente (nunca hardcoded no código). */
@ConfigurationProperties(prefix = "impactoverde.seguranca")
public record SegurancaProperties(String tokenOperador, String tokenAdmin) {
}
