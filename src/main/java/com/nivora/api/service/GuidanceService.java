package com.nivora.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nivora.api.dto.ConversationTurn;
import com.nivora.api.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GuidanceService {
    private static final Logger logger = LoggerFactory.getLogger(GuidanceService.class);

    private static final String SYSTEM_PROMPT = """
            You are NIVORA, a warm, conversational women's health information assistant.
            Understand and respond to the user's actual message and the conversation context.
            Answer direct questions directly. For symptoms, share relevant general health information,
            safe self-care when appropriate, and ask only useful follow-up questions. Handle greetings,
            emotional concerns, general questions, unrelated questions, and unclear messages naturally;
            when useful, explain that NIVORA specializes in health information.
            Never diagnose, claim certainty about a condition, prescribe prescription medication, or
            replace a clinician. Be clear about uncertainty. If the user describes serious warning
            signs, recommend appropriate urgent or emergency care. Do not make every reply a generic
            disclaimer; address the user's concern first. Keep replies clear, compassionate, and concise.
            """;

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GuidanceService(
            RestClient.Builder restClientBuilder,
            @Value("${nivora.ai.base-url}") String baseUrl,
            @Value("${nivora.ai.api-key:}") String apiKey,
            @Value("${nivora.ai.model}") String model) {
        this.restClient = restClientBuilder.clone()
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public String getGuidance(
            String message, List<String> previousMessages, List<ConversationTurn> conversationHistory) {
        if (apiKey.isBlank()) {
            throw new AiServiceException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "The guidance service is not configured. Set the OPENAI_API_KEY environment variable.");
        }

        List<Map<String, String>> input = new ArrayList<>();
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            conversationHistory.forEach(turn -> input.add(Map.of(
                    "role", turn.role(),
                    "content", turn.content())));
        } else if (previousMessages != null) {
            previousMessages.forEach(previous -> input.add(Map.of(
                    "role", "user",
                    "content", previous)));
        }
        input.add(Map.of("role", "user", "content", message));

        JsonNode response;
        try {
            response = restClient.post()
                    .uri("/responses")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(Map.of(
                            "model", model,
                            "instructions", SYSTEM_PROMPT,
                            "input", input))
                    .retrieve()
                    .onStatus(status -> status.isError(), (request, providerResponse) -> {
                        int statusCode = providerResponse.getStatusCode().value();
                        logger.warn("OpenAI Responses API returned HTTP {}", statusCode);
                        throw providerFailure(statusCode);
                    })
                    .body(JsonNode.class);
        } catch (ResourceAccessException exception) {
            logger.warn("OpenAI Responses API could not be reached ({})",
                    exception.getClass().getSimpleName());
            throw new AiServiceException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "The guidance provider could not be reached. Please try again.");
        } catch (RestClientException exception) {
            logger.error("OpenAI Responses API request failed ({})",
                    exception.getClass().getSimpleName());
            throw new AiServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "The guidance provider returned an invalid response. Please try again.");
        }

        String guidance = extractText(response);
        if (guidance.isBlank()) {
            logger.error("OpenAI Responses API returned no text output");
            throw new AiServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "The guidance provider returned an empty response. Please try again.");
        }
        return guidance;
    }

    private AiServiceException providerFailure(int statusCode) {
        if (statusCode == 401) {
            return new AiServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "The guidance provider rejected its credentials. Check OPENAI_API_KEY.");
        }
        if (statusCode == 403) {
            return new AiServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "The guidance provider denied access. Check the API project's permissions.");
        }
        if (statusCode == 429) {
            return new AiServiceException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "The guidance service is rate limited. Please try again shortly.");
        }
        if (statusCode >= 500) {
            return new AiServiceException(
                    HttpStatus.BAD_GATEWAY,
                    "The guidance provider is temporarily unavailable. Please try again.");
        }
        return new AiServiceException(
                HttpStatus.BAD_GATEWAY,
                "The guidance provider rejected the request. Please check the backend configuration.");
    }

    private String extractText(JsonNode response) {
        if (response == null || !response.path("output").isArray()) {
            return "";
        }
        List<String> textParts = new ArrayList<>();
        response.path("output").forEach(item -> item.path("content").forEach(content -> {
            if ("output_text".equals(content.path("type").asText())) {
                String text = content.path("text").asText("");
                if (!text.isBlank()) {
                    textParts.add(text);
                }
            }
        }));
        return String.join("\n", textParts);
    }
}
