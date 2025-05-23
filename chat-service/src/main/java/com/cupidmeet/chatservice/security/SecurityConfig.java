package com.cupidmeet.chatservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity // Необязательно с Boot 3.x, но можно оставить
@EnableMethodSecurity // Включает @PreAuthorize, @PostAuthorize, @Secured
@RequiredArgsConstructor // Для инъекции allowedOrigins и jwkSetUri через конструктор
class SecurityConfig {

    /**
     * Документация.
     */
    private static final String[] SWAGGER_ENDPOINTS = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"};

    /**
     * Актуатор.
     */
    private static final String ACTUATOR_ENDPOINTS = "/actuator/**";

    @Value("${application.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // Отключаем CSRF для stateless REST API
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Без HTTP-сессий, только JWT
                .httpBasic(AbstractHttpConfigurer::disable) // Отключаем Basic Auth
                .formLogin(AbstractHttpConfigurer::disable) // Отключаем аутентификацию по формам
                .logout(AbstractHttpConfigurer::disable) // Отключаем обработку выхода
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(SWAGGER_ENDPOINTS).permitAll() // Разрешаем доступ к Swagger
                        .requestMatchers(ACTUATOR_ENDPOINTS).permitAll() // Разрешаем доступ к Actuator
                        .requestMatchers("/ws/**").permitAll() // Разрешаем доступ к WebSocket эндпоинту (аутентификация будет в WebSocketAuthInterceptor)
                        .anyRequest().authenticated() // Все остальные запросы требуют аутентификации
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .jwkSetUri(jwkSetUri) // URI для получения публичных ключей Keycloak
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()) // Наш конвертер для ролей
                ))
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Настройка CORS
                .build();
    }

    /**
     * Бин для настройки извлечения прав доступа (ролей) из JWT.
     * Мы ожидаем, что роли находятся в клейме "realm_access.roles" в JWT от Keycloak.
     * По умолчанию Spring Security добавляет префикс "SCOPE_" к правам доступа, если нет префикса.
     * Если вы хотите, чтобы ваши роли выглядели как "ROLE_ADMIN", "ROLE_USER" и т.д.,
     * и в Keycloak они просто "admin", "user", то используйте setAuthorityPrefix("ROLE_").
     * Если ваши роли уже содержат "ROLE_" или вы не хотите префикс, используйте setAuthorityPrefix("").
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Указываем, что роли находятся в клейме "realm_access.roles"
        grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access.roles");
        // Не добавляем префикс "SCOPE_", если роли уже имеют "ROLE_" или не нужны префиксы
        // Если хотите "ROLE_", а в Keycloak "admin", то setAuthorityPrefix("ROLE_");
        // Если хотите просто "admin", а в Keycloak "admin", то setAuthorityPrefix("");
        // По умолчанию будет "SCOPE_admin"
        grantedAuthoritiesConverter.setAuthorityPrefix(""); // Чтобы не добавлять префикс "SCOPE_"

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    /**
     * Конфигурация CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins); // Разрешенные источники из properties
        configuration.setAllowedMethods(List.of("*")); // Разрешаем все HTTP-методы
        configuration.setAllowedHeaders(List.of("*")); // Разрешаем все заголовки
        configuration.setExposedHeaders(List.of("*")); // Выставляем все заголовки
        configuration.setAllowCredentials(true); // Разрешаем куки и заголовки авторизации
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Применяем ко всем путям
        return source;
    }
}