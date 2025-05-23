package com.cupidmeet.chatservice.service;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    /**
     * Получить токен пользователя.
     *
     * @return Токен
     */
    Optional<Jwt> getToken();

    /**
     * Получить доменное имя пользователя.
     *
     * @return доменное имя
     */
    Optional<String> getDomainUsername();

    /**
     * Получить идентификатор текущего пользователя.
     *
     * @return идентификатор
     */
    Optional<UUID> getCurrentId();
}
