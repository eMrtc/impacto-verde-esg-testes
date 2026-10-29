package br.com.impactoverde.api.erro;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Traduz qualquer falha em JSON padronizado com mensagens genéricas (detalhes só no log do servidor). */
@RestControllerAdvice
public class TratadorGlobalDeErros extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorGlobalDeErros.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> tratarApiException(ApiException ex, HttpServletRequest requisicao) {
        ErroResposta corpo = montar(ex.getStatus(), ex.getMessage(), requisicao.getRequestURI(), ex.getDetalhes());
        ResponseEntity.BodyBuilder resposta = ResponseEntity.status(ex.getStatus()).contentType(MediaType.APPLICATION_JSON);
        if (ex.getStatus() == HttpStatus.UNAUTHORIZED) {
            resposta.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        }
        return resposta.body(corpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> tratarInesperado(Exception ex, HttpServletRequest requisicao) {
        LOG.error("Falha inesperada ao processar a requisição", ex);
        ErroResposta corpo = montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", requisicao.getRequestURI(), List.of());
        return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).body(corpo);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders cabecalhos, HttpStatusCode status, WebRequest requisicao) {
        List<DetalheErro> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new DetalheErro(erro.getField(), erro.getDefaultMessage()))
                .toList();
        ErroResposta corpo = montar(HttpStatus.BAD_REQUEST, "Dados inválidos", caminho(requisicao), detalhes);
        return handleExceptionInternal(ex, corpo, cabecalhos, status, requisicao);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object corpo, HttpHeaders cabecalhos, HttpStatusCode status, WebRequest requisicao) {
        if (!(corpo instanceof ErroResposta)) {
            corpo = montar(HttpStatus.valueOf(status.value()), mensagemGenerica(status), caminho(requisicao), List.of());
        }
        HttpHeaders saida = new HttpHeaders();
        saida.putAll(cabecalhos);
        saida.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(corpo, saida, status);
    }

    private static ErroResposta montar(HttpStatus status, String mensagem, String caminho, List<DetalheErro> detalhes) {
        return new ErroResposta(Instant.now(), status.value(), status.getReasonPhrase(), mensagem, caminho, detalhes);
    }

    private static String caminho(WebRequest requisicao) {
        return requisicao instanceof ServletWebRequest web ? web.getRequest().getRequestURI() : "";
    }

    private static String mensagemGenerica(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Requisição inválida ou malformada";
            case 404 -> "Recurso não encontrado";
            case 405 -> "Método HTTP não suportado";
            case 415 -> "Tipo de mídia não suportado";
            default -> "Não foi possível processar a requisição";
        };
    }
}
