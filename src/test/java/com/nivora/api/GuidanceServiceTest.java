package com.nivora.api;

import com.nivora.api.dto.ConversationTurn;
import com.nivora.api.exception.AiServiceException;
import com.nivora.api.service.GuidanceService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GuidanceServiceTest {
    private static final String BASE_URL = "https://api.openai.com/v1";
    private static final String API_KEY = "test-api-key";
    private static final String MODEL = "gpt-4.1-mini";

    @Test
    void sendsTheCompleteMessageAndConversationToOpenAi() {
        Fixture fixture = fixture(API_KEY);
        String message = "I've had lower abdominal discomfort for two days and it gets worse after eating.";
        String responseJson = """
                {"output":[{"type":"message","content":[
                  {"type":"output_text","text":"Here is general information about that discomfort."}
                ]}]}
                """;

        fixture.server().expect(requestTo(BASE_URL + "/responses"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + API_KEY))
                .andExpect(jsonPath("$.model").value(MODEL))
                .andExpect(jsonPath("$.instructions").value(containsString("actual message")))
                .andExpect(jsonPath("$.input[0].role").value("user"))
                .andExpect(jsonPath("$.input[0].content").value("I have stomach discomfort"))
                .andExpect(jsonPath("$.input[1].role").value("assistant"))
                .andExpect(jsonPath("$.input[1].content").value("Where is the discomfort?"))
                .andExpect(jsonPath("$.input[2].role").value("user"))
                .andExpect(jsonPath("$.input[2].content").value(message))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        String guidance = fixture.service().getGuidance(
                message,
                List.of(),
                List.of(
                        new ConversationTurn("user", "I have stomach discomfort"),
                        new ConversationTurn("assistant", "Where is the discomfort?")));

        assertEquals("Here is general information about that discomfort.", guidance);
        fixture.server().verify();
    }

    @Test
    void reportsProviderAuthorizationFailuresWithoutReturningProviderBody() {
        Fixture fixture = fixture(API_KEY);
        fixture.server().expect(requestTo(BASE_URL + "/responses"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"private provider response\"}"));

        AiServiceException exception = assertThrows(
                AiServiceException.class,
                () -> fixture.service().getGuidance("What is PCOS?", List.of(), List.of()));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatus());
        assertEquals(
                "The guidance provider denied access. Check the API project's permissions.",
                exception.getMessage());
        fixture.server().verify();
    }

    @Test
    void reportsMissingApiKeyAsServiceUnavailable() {
        Fixture fixture = fixture("");

        AiServiceException exception = assertThrows(
                AiServiceException.class,
                () -> fixture.service().getGuidance("Hi", List.of(), List.of()));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
        assertEquals(
                "The guidance service is not configured. Set the OPENAI_API_KEY environment variable.",
                exception.getMessage());
        fixture.server().verify();
    }

    private Fixture fixture(String apiKey) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GuidanceService service = new GuidanceService(builder, BASE_URL, apiKey, MODEL);
        return new Fixture(server, service);
    }

    private record Fixture(MockRestServiceServer server, GuidanceService service) {
    }
}
