package br.ifpb.project.denguemaps.pdmreportms.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

/**
 * Utilitários para extração segura de dados de autenticação e claims do JWT.
 */
public final class SecurityUtils {

    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

    private SecurityUtils() {}

    /**
     * Extrai com segurança o UUID do cidadão/usuário autenticado a partir do claim 'sub' do JWT.
     * Retorna Optional.empty() se o JWT for nulo ou se o subject não for um UUID válido.
     */
    public static Optional<UUID> extractUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(jwt.getSubject()));
        } catch (IllegalArgumentException e) {
            log.warn("Subject do JWT não pôde ser convertido para UUID: {}", jwt.getSubject());
            return Optional.empty();
        }
    }

    /**
     * Verifica se a autenticação possui perfil de administrador.
     */
    public static boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "admin");
    }

    /**
     * Verifica se a autenticação possui perfil de agente de saúde.
     */
    public static boolean isHealthAgent(Authentication authentication) {
        return hasRole(authentication, "health_agent");
    }

    /**
     * Verifica se a autenticação possui determinada role (com ou sem prefixo ROLE_).
     */
    public static boolean hasRole(Authentication authentication, String role) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase(role) || a.getAuthority().equalsIgnoreCase(roleWithPrefix));
    }
}
