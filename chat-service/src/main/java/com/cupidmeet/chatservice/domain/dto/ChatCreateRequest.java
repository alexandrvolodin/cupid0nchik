package com.cupidmeet.chatservice.domain.dto;

import com.cupidmeet.chatservice.domain.enumeration.ChatType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCreateRequest {

    @Schema(description = "Тип чата")
    private ChatType chatType;

    @Schema(description = "Название чата")
    private String name;

    @Schema(description = "Список участников чата")
    private Set<UUID> participants;
}
