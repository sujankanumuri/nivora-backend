package com.nivora.api.story.service;

import com.nivora.api.exception.StoryNotFoundException;
import com.nivora.api.story.dto.StoryResponse;
import com.nivora.api.story.entity.Story;
import com.nivora.api.story.entity.StoryLike;
import com.nivora.api.story.repository.StoryLikeRepository;
import com.nivora.api.story.repository.StoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StoryService {
    private final StoryRepository storyRepository;
    private final StoryLikeRepository storyLikeRepository;

    public StoryService(StoryRepository storyRepository, StoryLikeRepository storyLikeRepository) {
        this.storyRepository = storyRepository;
        this.storyLikeRepository = storyLikeRepository;
    }

    @Transactional(readOnly = true)
    public List<StoryResponse> getStories(String category, UUID visitorId) {
        List<Story> stories = category == null || category.isBlank()
                ? storyRepository.findAllByOrderByIdAsc()
                : storyRepository.findAllByCategoryIgnoreCaseOrderByIdAsc(category);
        return stories.stream().map(story -> toResponse(story, visitorId)).toList();
    }

    @Transactional
    public StoryResponse like(String storyId, UUID visitorId) {
        Story story = findStory(storyId);
        String visitor = visitorId.toString();
        if (!storyLikeRepository.existsByStory_IdAndVisitorId(storyId, visitor)) {
            storyLikeRepository.saveAndFlush(new StoryLike(story, visitor));
        }
        return toResponse(story, visitorId);
    }

    @Transactional
    public StoryResponse unlike(String storyId, UUID visitorId) {
        Story story = findStory(storyId);
        storyLikeRepository.deleteByStory_IdAndVisitorId(storyId, visitorId.toString());
        return toResponse(story, visitorId);
    }

    private Story findStory(String storyId) {
        return storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryNotFoundException(storyId));
    }

    private StoryResponse toResponse(Story story, UUID visitorId) {
        return new StoryResponse(
                story.getId(),
                story.getCategory(),
                story.getTitle(),
                story.getExcerpt(),
                story.getContent(),
                story.getColor(),
                story.getInitialLikes() + storyLikeRepository.countByStory_Id(story.getId()),
                visitorId != null
                        && storyLikeRepository.existsByStory_IdAndVisitorId(
                                story.getId(), visitorId.toString()));
    }
}
