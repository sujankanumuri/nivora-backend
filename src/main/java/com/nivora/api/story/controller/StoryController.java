package com.nivora.api.story.controller;

import com.nivora.api.story.dto.StoryLikeRequest;
import com.nivora.api.story.dto.StoryResponse;
import com.nivora.api.story.service.StoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stories")
public class StoryController {
    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @GetMapping
    public List<StoryResponse> getStories(
            @RequestParam(required = false) String category,
            @RequestParam UUID visitorId) {
        return storyService.getStories(category, visitorId);
    }

    @PutMapping("/{storyId}/likes")
    public StoryResponse like(
            @PathVariable String storyId, @Valid @RequestBody StoryLikeRequest request) {
        return storyService.like(storyId, UUID.fromString(request.visitorId()));
    }

    @DeleteMapping("/{storyId}/likes")
    public StoryResponse unlike(
            @PathVariable String storyId, @RequestParam UUID visitorId) {
        return storyService.unlike(storyId, visitorId);
    }
}
