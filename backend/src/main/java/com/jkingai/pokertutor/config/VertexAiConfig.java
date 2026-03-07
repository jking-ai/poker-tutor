package com.jkingai.pokertutor.config;

import com.google.cloud.vertexai.VertexAI;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true")
public class VertexAiConfig {

    @Bean
    VertexAI vertexAi(@Value("${app.ai.project-id}") String projectId,
                      @Value("${app.ai.location}") String location) throws IOException {
        return new VertexAI.Builder()
                .setProjectId(projectId)
                .setLocation(location)
                .build();
    }

    @Bean
    VertexAiGeminiChatModel chatModel(VertexAI vertexAi,
                                      @Value("${app.ai.model}") String model) {
        return new VertexAiGeminiChatModel(vertexAi,
                VertexAiGeminiChatOptions.builder()
                        .withModel(model)
                        .withTemperature(0.8)
                        .build());
    }

    @Bean
    ChatClient opponentChatClient(VertexAiGeminiChatModel chatModel,
                                  @Value("classpath:prompts/opponent_persona.txt") Resource promptResource)
            throws IOException {
        String systemPrompt = promptResource.getContentAsString(StandardCharsets.UTF_8);
        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .build();
    }

    @Bean
    VertexAiGeminiChatModel coachChatModel(VertexAI vertexAi,
                                           @Value("${app.ai.model}") String model) {
        return new VertexAiGeminiChatModel(vertexAi,
                VertexAiGeminiChatOptions.builder()
                        .withModel(model)
                        .withTemperature(0.4)
                        .build());
    }

    @Bean
    ChatClient coachChatClient(VertexAiGeminiChatModel coachChatModel,
                               @Value("classpath:prompts/coach_persona.txt") Resource promptResource)
            throws IOException {
        String systemPrompt = promptResource.getContentAsString(StandardCharsets.UTF_8);
        return ChatClient.builder(coachChatModel)
                .defaultSystem(systemPrompt)
                .build();
    }
}
