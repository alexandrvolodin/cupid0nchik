package com.cupidmeet.chatservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Регистрируем модуль для поддержки Java 8 Date and Time API (ZonedDateTime)
        mapper.registerModule(new JavaTimeModule());
        // Отключаем запись дат как временных меток (timestamp),
        // чтобы они сериализовались в ISO 8601 строки (например, "2025-05-23T10:30:15.000Z")
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}