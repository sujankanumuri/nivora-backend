package com.nivora.api.story.repository;

import com.nivora.api.story.entity.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {
    long countByStory_Id(String storyId);

    boolean existsByStory_IdAndVisitorId(String storyId, String visitorId);

    long deleteByStory_IdAndVisitorId(String storyId, String visitorId);
}
