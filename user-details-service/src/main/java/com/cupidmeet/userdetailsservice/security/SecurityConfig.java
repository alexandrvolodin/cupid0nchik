package com.cupidmeet.userdetailsservice.security;

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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Убрал securedEnabled = true, так как EnableMethodSecurity уже включает это
class SecurityConfig {

    private static final String[] SWAGGER_ENDPOINTS = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"};
    private static final String ACTUATOR_ENDPOINTS = "/actuator/**";

    // Эти роли не совсем нужны для oauth2ResourceServer, если вы используете @PreAuthorize
    // Но оставим их, если они используются где-то еще.
    @Value("${application.authRoles}")
    private String editorAuthRole;

    @Value("${application.authRolesViewer}")
    private String viewerAuthRole;

    @Value("${application.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri; // Это значение теперь будет использоваться

    // Удалены @Autowired configureGlobal и KeycloakAuthenticationProvider. Они не нужны.

    // В OAuth2 Resource Server вам обычно не нужна SessionAuthenticationStrategy
    // Если она не используется явно для других целей, можно убрать.
    // @Bean
    // public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    //    return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    // }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                        .requestMatchers(ACTUATOR_ENDPOINTS).permitAll()
                        // Раскомментируйте, если хотите использовать ролевую авторизацию здесь
                        // .requestMatchers(HttpMethod.GET).hasAnyAuthority(viewerAuthRole, editorAuthRole)
                        // .requestMatchers(HttpMethod.POST).hasAuthority(editorAuthRole)
                        // .anyRequest().authenticated()
                        .anyRequest().authenticated() // Все остальные запросы требуют аутентификации
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwkSetUri(jwkSetUri)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()) // Добавляем конвертер
                ))
                .cors(cors -> cors.configurationSource(request -> getCorsConfiguration()))
                .build();
    }

    // НОВЫЙ БИН: Конвертер для преобразования JWT в Authentication объект Spring Security
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access.roles");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtConverter;
    }


    private CorsConfiguration getCorsConfiguration() {
        CorsConfiguration cors = new CorsConfiguration();
        List<String> allowedAllValues = List.of("*");

        cors.setAllowedOrigins(allowedOrigins);
        cors.setAllowedMethods(allowedAllValues);
        cors.setExposedHeaders(allowedAllValues);
        cors.setAllowedHeaders(allowedAllValues);
        return cors;
    }
}