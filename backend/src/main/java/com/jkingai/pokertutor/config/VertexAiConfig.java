package com.jkingai.pokertutor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring AI ChatClient beans connected to Vertex AI Gemini.
 * Creates two separate ChatClient instances: one for the Opponent Agent and one for the Coach Agent,
 * each with distinct system prompts and temperature settings.
 */
@Configuration
public class VertexAiConfig {

    // TODO: Inject Spring AI ChatModel bean (auto-configured by spring-ai-vertex-ai-gemini-spring-boot-starter)

    // TODO: Create "opponentChatClient" bean
    //   - Load system prompt from classpath:prompts/opponent_persona.txt
    //   - Set temperature to 0.8 (creative, unpredictable play)
    //   - Configure structured output for action responses

    // TODO: Create "coachChatClient" bean
    //   - Load system prompt from classpath:prompts/coach_persona.txt
    //   - Set temperature to 0.3 (consistent, analytical output)
    //   - Configure structured output for coaching responses
}
