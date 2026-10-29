package br.com.impactoverde.api.erro;

import java.time.Instant;
import java.util.List;

/** Corpo padrão de erro: nunca expõe stack trace nem detalhes internos. */
public record ErroResposta(
        Instant timestamp, int status, String erro, String mensagem, String caminho, List<DetalheErro> detalhes) {
}
