package com.nivora.api.story.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "story_likes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_story_like_visitor", columnNames = {"story_id", "visitor_id"})
})
public class StoryLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(name = "visitor_id", nullable = false, length = 36)
    private String visitorId;

    protected StoryLike() {
    }

    public StoryLike(Story story, String visitorId) {
        this.story = story;
        this.visitorId = visitorId;
    }
}
