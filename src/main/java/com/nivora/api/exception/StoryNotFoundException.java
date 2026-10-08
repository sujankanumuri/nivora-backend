package com.nivora.api.exception;

public class StoryNotFoundException extends RuntimeException {
    public StoryNotFoundException(String storyId) {
        super("Story '" + storyId + "' was not found.");
    }
}
