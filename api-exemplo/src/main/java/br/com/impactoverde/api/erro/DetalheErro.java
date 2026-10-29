package br.com.impactoverde.api.erro;

/** Um problema de validação em um campo específico. */
public record DetalheErro(String campo, String mensagem) {
}
