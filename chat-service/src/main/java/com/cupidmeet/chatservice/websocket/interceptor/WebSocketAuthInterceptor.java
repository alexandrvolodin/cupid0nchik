package com.cupidmeet.chatservice.websocket.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List; // Добавьте импорт List
import java.util.Map; // Добавьте импорт Map
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    Authentication authentication = getAuthentication(token);
                    accessor.setUser(authentication);
                } catch (Exception e) { // Обработка ошибок при декодировании/валидации токена
                    throw new IllegalArgumentException("Authentication failed: " + e.getMessage(), e);
                }
            } else {
                throw new IllegalArgumentException("Invalid or missing Authorization header");
            }
        }
        return message;
    }

    private Authentication getAuthentication(String token) {
        Jwt jwt = jwtDecoder.decode(token); // Выбросит исключение, если токен невалиден

        // Извлекаем роли из realm_access.roles, как и в SecurityConfig
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        return new JwtAuthenticationToken(jwt, authorities);
    }

    // Вынес логику извлечения ролей в отдельный метод для ясности и переиспользования
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())) // Добавляем префикс ROLE_ для единообразия, если в Keycloak роли без него
                    .collect(Collectors.toList());
        }
        return List.of(); // Возвращаем пустой список, если ролей нет
    }
}