package br.com.impactoverde.api.config;

import br.com.impactoverde.api.erro.ApiException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Autenticação por Bearer token + autorização por perfil, aplicadas ANTES do roteamento do Spring MVC.
 * Assim, quem não tem crachá recebe 401 em qualquer rota (e não um 415/404 que entrega detalhes da API).
 * Regras: sem token válido => 401; DELETE exige ADMIN => 403 para OPERADOR.
 * Os erros são delegados ao mesmo tratador global, mantendo o corpo JSON padronizado.
 */
@Component
public class AutenticacaoFiltro extends OncePerRequestFilter {

    private static final String PREFIXO_BEARER = "Bearer ";
    private static final String PREFIXO_API = "/api/";
    private static final String CAMINHO_PUBLICO = "/api/v1/saude";

    private final SegurancaProperties propriedades;
    private final HandlerExceptionResolver tratadorDeErros;

    public AutenticacaoFiltro(
            SegurancaProperties propriedades,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver tratadorDeErros) {
        this.propriedades = propriedades;
        this.tratadorDeErros = tratadorDeErros;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest requisicao) {
        String caminho = requisicao.getRequestURI();
        return !caminho.startsWith(PREFIXO_API) || caminho.equals(CAMINHO_PUBLICO);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        try {
            Perfil perfil = identificarPerfil(requisicao.getHeader("Authorization"));
            if (HttpMethod.DELETE.matches(requisicao.getMethod()) && perfil != Perfil.ADMIN) {
                throw ApiException.proibido("Perfil sem permissão para esta operação");
            }
        } catch (ApiException e) {
            tratadorDeErros.resolveException(requisicao, resposta, null, e);
            return;
        }
        cadeia.doFilter(requisicao, resposta);
    }

    private Perfil identificarPerfil(String cabecalho) {
        if (cabecalho == null || !cabecalho.startsWith(PREFIXO_BEARER)) {
            throw ApiException.naoAutenticado("Credenciais ausentes ou inválidas");
        }
        String token = cabecalho.substring(PREFIXO_BEARER.length()).trim();
        if (iguais(token, propriedades.tokenAdmin())) {
            return Perfil.ADMIN;
        }
        if (iguais(token, propriedades.tokenOperador())) {
            return Perfil.OPERADOR;
        }
        throw ApiException.naoAutenticado("Credenciais ausentes ou inválidas");
    }

    /** Comparação em tempo constante para não vazar informação por timing. */
    private static boolean iguais(String informado, String esperado) {
        if (esperado == null || esperado.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                informado.getBytes(StandardCharsets.UTF_8), esperado.getBytes(StandardCharsets.UTF_8));
    }
}
