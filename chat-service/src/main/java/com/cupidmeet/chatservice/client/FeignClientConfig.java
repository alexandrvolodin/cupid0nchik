package com.cupidmeet.chatservice.client;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestTokenInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication instanceof JwtAuthenticationToken) {
                    JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
                    String token = jwtAuthenticationToken.getToken().getTokenValue();
                    template.header("Authorization", "Bearer " + token);
                }
            }
        };
    }
}