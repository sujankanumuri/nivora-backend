package com.nivora.api;

import com.nivora.api.service.GuidanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StoryApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GuidanceService guidanceService;

    @Test
    void listsSeededStoriesAndFiltersByCategory() throws Exception {
        String visitorId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/stories").param("visitorId", visitorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].title").value("The conversation I needed"))
                .andExpect(jsonPath("$[0].likes").value(234))
                .andExpect(jsonPath("$[0].liked").value(false));

        mockMvc.perform(get("/api/stories")
                        .param("visitorId", visitorId)
                        .param("category", "PCOS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("PCOS"));
    }

    @Test
    void likingAndUnlikingAStoryIsIdempotent() throws Exception {
        String visitorId = UUID.randomUUID().toString();
        String request = "{\"visitorId\":\"" + visitorId + "\"}";

        mockMvc.perform(put("/api/stories/story-1/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes").value(235))
                .andExpect(jsonPath("$.liked").value(true));

        mockMvc.perform(put("/api/stories/story-1/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes").value(235));

        mockMvc.perform(delete("/api/stories/story-1/likes").param("visitorId", visitorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes").value(234))
                .andExpect(jsonPath("$.liked").value(false));
    }

    @Test
    void returnsStructuredErrorsForInvalidAndMissingRequests() throws Exception {
        mockMvc.perform(post("/api/health/guidance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\" \",\"previousMessages\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());

        mockMvc.perform(get("/api/stories")
                        .param("visitorId", UUID.randomUUID().toString())
                        .param("category", "missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(put("/api/stories/not-a-story/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"visitorId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returnsGuidanceForCoughMessages() throws Exception {
        when(guidanceService.getGuidance(anyString(), anyList(), anyList()))
                .thenReturn("A cough can have many causes. When did it begin?");

        mockMvc.perform(post("/api/health/guidance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"I have cough","conversationHistory":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guidance").value(
                        org.hamcrest.Matchers.containsString("A cough")))
                .andExpect(jsonPath("$.guidance").value(
                        org.hamcrest.Matchers.containsString("When did it begin")));
    }

    @Test
    void allowsAlternateLocalVitePortsButRejectsUntrustedOrigins() throws Exception {
        mockMvc.perform(options("/api/stories")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5174")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5174"));

        mockMvc.perform(options("/api/stories")
                        .header(HttpHeaders.ORIGIN, "https://untrusted.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }
}
