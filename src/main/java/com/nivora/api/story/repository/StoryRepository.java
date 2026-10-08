package com.nivora.api.story.repository;

import com.nivora.api.story.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryRepository extends JpaRepository<Story, String> {
    List<Story> findAllByOrderByIdAsc();

    List<Story> findAllByCategoryIgnoreCaseOrderByIdAsc(String category);
}
