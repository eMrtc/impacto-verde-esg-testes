package br.com.impactoverde.api.erro;

import java.util.List;
import org.springframework.http.HttpStatus;

/** Exceção de negócio/HTTP com o status já decidido; o handler global monta o corpo padronizado. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final List<DetalheErro> detalhes;

    private ApiException(HttpStatus status, String mensagem, List<DetalheErro> detalhes) {
        super(mensagem);
        this.status = status;
        this.detalhes = detalhes;
    }

    public static ApiException naoAutenticado(String mensagem) {
        return new ApiException(HttpStatus.UNAUTHORIZED, mensagem, List.of());
    }

    public static ApiException proibido(String mensagem) {
        return new ApiException(HttpStatus.FORBIDDEN, mensagem, List.of());
    }

    public static ApiException naoEncontrado(String mensagem) {
        return new ApiException(HttpStatus.NOT_FOUND, mensagem, List.of());
    }

    public static ApiException conflito(String mensagem) {
        return new ApiException(HttpStatus.CONFLICT, mensagem, List.of());
    }

    public static ApiException regraDeNegocio(String mensagem) {
        return new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, mensagem, List.of());
    }

    public static ApiException requisicaoInvalida(String campo, String mensagem) {
        return new ApiException(
                HttpStatus.BAD_REQUEST, "Dados inválidos", List.of(new DetalheErro(campo, mensagem)));
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<DetalheErro> getDetalhes() {
        return detalhes;
    }
}
