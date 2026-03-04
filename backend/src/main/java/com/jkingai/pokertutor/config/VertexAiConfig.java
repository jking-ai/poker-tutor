package com.jkingai.pokertutor.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Configuration for Spring AI ChatClient beans connected to Vertex AI Gemini.
 * Creates two separate ChatClient instances: one for the Opponent Agent and one for the Coach Agent,
 * each with distinct system prompts and temperature settings.
 */
@Configuration
public class VertexAiConfig {

    @Value("classpath:prompts/opponent_persona.txt")
    private Resource opponentPromptResource;

    @Value("classpath:prompts/coach_persona.txt")
    private Resource coachPromptResource;

    @Bean
    @Qualifier("opponentChatClient")
    public ChatClient opponentChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(opponentPromptResource)
                .build();
    }

    @Bean
    @Qualifier("coachChatClient")
    public ChatClient coachChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(coachPromptResource)
                .build();
    }
}
