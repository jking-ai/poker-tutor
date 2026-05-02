package com.jkingai.pokertutor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkingai.pokertutor.dto.GameRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises {@link com.jkingai.pokertutor.config.GameCreationRateLimitFilter}.
 * The test sets the per-IP cap to 3/hour (matching the production default) and
 * verifies the 4th creation attempt from the same IP is rejected with HTTP 429.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.limits.games-created-per-hour-per-ip=3",
        "app.limits.max-concurrent-games=20"
})
class GameCreationRateLimitTest {

    private static final String CLIENT_IP = "203.0.113.42";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fourthGameCreationFromSameIpReturns429() throws Exception {
        GameRequest request = new GameRequest("RateLimitedPlayer", 1000, 5, 10);
        String body = objectMapper.writeValueAsString(request);

        // First three creations from the same IP should succeed.
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(createGameRequest(body))
                    .andExpect(status().isCreated());
        }

        // The fourth attempt from that same IP must be rate-limited.
        mockMvc.perform(createGameRequest(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("GAME_CREATION_RATE_LIMITED"))
                .andExpect(jsonPath("$.retryAfterSeconds").exists());
    }

    @Test
    void getRequestsAreNotRateLimited() throws Exception {
        // GETs against /api/v1/games/* must bypass the filter entirely; we
        // only need to confirm the filter does not produce a 429 here.
        mockMvc.perform(get("/api/v1/games/does-not-exist")
                        .with(req -> { req.setRemoteAddr(CLIENT_IP); return req; }))
                .andExpect(status().isNotFound());
    }

    private MockHttpServletRequestBuilder createGameRequest(String body) {
        return post("/api/v1/games")
                .with(csrf())
                .with(req -> { req.setRemoteAddr(CLIENT_IP); return req; })
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }
}
