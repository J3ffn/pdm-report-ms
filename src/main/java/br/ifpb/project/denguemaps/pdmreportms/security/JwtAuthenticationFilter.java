package br.ifpb.project.denguemaps.pdmreportms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

// Classe que intercepta as requisições e valida o token antes que chegue ao controlador
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
            throws ServletException, IOException {

        // Verifica se a requisição possui um cabeçalho HTTP chamado Authorization
        String header = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);

        /* Se o header estiver ausente ou mal formatado, o filtro apenas passa adiante a requisição, não bloqueando, mas ela será exigida mais à frente
           caso o endpoint exigir autenticação */
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        String token = header.substring(7);

        try {

            // Valida o token
            if (!jwtUtil.isTokenValid(token)) {
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String userName = jwtUtil.extractUsername(token);

            // Criando a autenticação no contexto do Spring
            // Qualquer endpoint protegido reconhecerá a requisição como autenticada
            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                new User(userName, "", Collections.emptyList()),
                                null,
                                Collections.emptyList()
                            );

                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }

        // Tratamento se o token for inválido, devolve 401 Unauthotrized e encerra o fluxo
        } catch (Exception error) {
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Continuando o filtro, passando a requisição para o próximo filtro ou controlador se tudo estiver correto
        filterChain.doFilter(httpServletRequest, httpServletResponse);

    }

}
