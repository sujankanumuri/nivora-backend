package com.nivora.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

import java.util.List;

public record GuidanceRequest(
        @NotBlank @Size(max = 2000) String message,
        @Size(max = 4) List<@NotBlank @Size(max = 2000) String> previousMessages,
        @Size(max = 8) List<@Valid ConversationTurn> conversationHistory
) {
    public GuidanceRequest {
        previousMessages = previousMessages == null ? List.of() : previousMessages;
        conversationHistory = conversationHistory == null ? List.of() : conversationHistory;
    }
}
