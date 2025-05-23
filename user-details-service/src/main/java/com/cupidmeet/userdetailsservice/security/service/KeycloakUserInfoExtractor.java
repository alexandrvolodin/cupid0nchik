package com.cupidmeet.userdetailsservice.security.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt; // Импортируем Jwt
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // Добавляем логирование

public final class KeycloakUserInfoExtractor {

    private static final Logger log = LoggerFactory.getLogger(KeycloakUserInfoExtractor.class);

    private KeycloakUserInfoExtractor() {
    }

    // Удаляем getKeycloakAccessToken, он больше не нужен
    // public static Optional<AccessToken> getKeycloakAccessToken(Authentication authentication) { ... }

    /**
     * Получить доменное имя пользователя из SecurityContext.
     * @param authentication объект Authentication из SecurityContextHolder
     * @return доменное имя пользователя.
     */
    public static Optional<String> getDomainUsername(Authentication authentication) {
        log.debug("Attempting to extract domain username from Authentication object.");

        // Проверяем, является ли Principal экземпляром Jwt
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            log.warn("Authentication object is null or its principal is not a Jwt instance. Actual principal type: {}",
                    authentication != null ? authentication.getPrincipal().getClass().getName() : "null");
            return Optional.empty();
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        log.debug("Found Jwt principal. Claims: {}", jwt.getClaims());

        // Извлекаем 'preferred_username' из JWT-токена
        Optional<String> usernameOptional = Optional.ofNullable(jwt.getClaimAsString("preferred_username"))
                .map(String::toUpperCase)
                .filter(name -> !name.isBlank());

        usernameOptional.ifPresentOrElse(
                username -> log.info("Extracted domain username: {}", username),
                () -> log.warn("Domain username could not be extracted ('preferred_username' claim missing, null, or empty in JWT).")
        );
        return usernameOptional;
    }
}