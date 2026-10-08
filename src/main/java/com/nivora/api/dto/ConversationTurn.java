package com.nivora.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConversationTurn(
        @NotBlank
        @Pattern(regexp = "user|assistant", message = "must be user or assistant")
        String role,
        @NotBlank @Size(max = 2000) String content
) {
}
