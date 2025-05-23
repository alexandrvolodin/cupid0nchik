package com.cupidmeet.chatservice.service.impl;

import com.cupidmeet.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.Map; // Добавьте импорт Map

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public Optional<Jwt> getToken() { // Возвращаем Optional<Jwt>
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(authentication -> authentication instanceof JwtAuthenticationToken)
                .map(authentication -> ((JwtAuthenticationToken) authentication).getToken());
    }

    @Override
    public Optional<String> getDomainUsername() {
        return getToken().flatMap(this::getUsernameByToken);
    }

    @Override
    public Optional<UUID> getCurrentId() {
        return getToken().flatMap(token -> Optional.of(UUID.fromString(token.getSubject())));
    }

    private Optional<String> getUsernameByToken(Jwt jwt) { // Принимаем Jwt
        // Keycloak обычно использует "preferred_username" для логина пользователя
        return Optional.ofNullable(jwt.getClaimAsString("preferred_username"))
                .map(name -> name.split("\\\\", 2))
                .map(segments -> segments[segments.length - 1])
                .map(String::toUpperCase)
                .filter(name -> !name.isBlank());
    }
}