package com.nivora.api.story;

import com.nivora.api.story.entity.Story;
import com.nivora.api.story.repository.StoryRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class StoryDataInitializer {
    @Bean
    ApplicationRunner initializeStories(StoryRepository stories) {
        return args -> {
            List<Story> defaults = List.of(
                    new Story("story-1", "Periods", "The conversation I needed",
                            "I was fourteen when my first period came. I thought something was terribly wrong with me...",
                            "I was fourteen when my first period came. I thought something was terribly wrong with me because no one had ever explained what to expect. Years later, I try to be the person I needed then. A question is never something to be ashamed of.",
                            "rose", 234),
                    new Story("story-2", "PCOS", "Finally having a name for it",
                            "After three years of unexplained changes and irregular cycles, I finally got an answer...",
                            "After three years of unexplained changes and irregular cycles, I finally got an answer. The diagnosis was not the end of the story, but it was the beginning of understanding my body and finding a care team that listened.",
                            "lilac", 187),
                    new Story("story-3", "Mental health", "A softer kind of strength",
                            "Postpartum depression hit me like a wave I never saw coming. I felt guilty for not feeling happy...",
                            "Postpartum depression hit me like a wave I never saw coming. I felt guilty for not feeling happy. Asking for help was hard, and it changed everything. If this sounds familiar, you deserve support without having to earn it.",
                            "peach", 312),
                    new Story("story-4", "Pregnancy", "Grief deserves room, too",
                            "My miscarriage was the loneliest experience of my life. Nobody talked about it...",
                            "My miscarriage was the loneliest experience of my life. Nobody talked about it, so I thought I had to carry it quietly. I want someone else to know: there is no right timeline for grief, and you are not alone in it.",
                            "sage", 276),
                    new Story("story-5", "Endometriosis", "I trusted what my body was telling me",
                            "Being diagnosed with endometriosis at twenty-two changed how I saw my future...",
                            "Being diagnosed with endometriosis at twenty-two changed how I saw my future. It took time to find someone who took my pain seriously. I learned that being persistent about your care is not being difficult.",
                            "butter", 198),
                    new Story("story-6", "Periods", "Pain is worth talking about",
                            "I wish someone had told me that severe period pain is not something I had to just push through...",
                            "I wish someone had told me that severe period pain is not something I had to just push through. Tracking my symptoms helped me explain what was happening and have a better conversation with my doctor.",
                            "blue", 405)
            );
            defaults.stream()
                    .filter(story -> !stories.existsById(story.getId()))
                    .forEach(stories::save);
        };
    }
}
