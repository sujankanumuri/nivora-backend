package com.nivora.api.story.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stories")
public class Story {
    @Id
    @Column(length = 32, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 500)
    private String excerpt;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false, length = 16)
    private String color;

    @Column(name = "initial_likes", nullable = false)
    private int initialLikes;

    protected Story() {
    }

    public Story(
            String id, String category, String title, String excerpt, String content,
            String color, int initialLikes) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.excerpt = excerpt;
        this.content = content;
        this.color = color;
        this.initialLikes = initialLikes;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public String getContent() {
        return content;
    }

    public String getColor() {
        return color;
    }

    public int getInitialLikes() {
        return initialLikes;
    }
}
