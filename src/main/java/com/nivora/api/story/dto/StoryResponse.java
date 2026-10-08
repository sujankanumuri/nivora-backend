package com.nivora.api.story.dto;

public record StoryResponse(
        String id,
        String category,
        String title,
        String excerpt,
        String content,
        String color,
        long likes,
        boolean liked
) {
}
