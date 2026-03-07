package com.jkingai.pokertutor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkingai.pokertutor.dto.ActionRequest;
import com.jkingai.pokertutor.dto.GameRequest;
import com.jkingai.pokertutor.model.PlayerAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String createGameAndGetId() throws Exception {
        GameRequest request = new GameRequest("TestPlayer", 1000, 5, 10);
        MvcResult result = mockMvc.perform(post("/api/v1/games")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").exists())
                .andExpect(jsonPath("$.phase").value("PRE_FLOP"))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("gameId").asText();
    }

    @Test
    void createGameReturns201() throws Exception {
        createGameAndGetId();
    }

    @Test
    void getGameReturnsState() throws Exception {
        String gameId = createGameAndGetId();
        mockMvc.perform(get("/api/v1/games/" + gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.players").isArray())
                .andExpect(jsonPath("$.players[0].holeCards").isArray())
                .andExpect(jsonPath("$.players[1].holeCards").doesNotExist()); // Hidden
    }

    @Test
    void getGameReturns404ForUnknown() throws Exception {
        mockMvc.perform(get("/api/v1/games/fake_id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void submitActionProcessesCorrectly() throws Exception {
        String gameId = createGameAndGetId();
        ActionRequest action = new ActionRequest(PlayerAction.CALL, null);
        mockMvc.perform(post("/api/v1/games/" + gameId + "/actions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(action)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId));
    }

    @Test
    void postWithoutCsrfTokenIsForbidden() throws Exception {
        GameRequest request = new GameRequest("TestPlayer", 1000, 5, 10);
        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void foldAndDealNextHand() throws Exception {
        String gameId = createGameAndGetId();

        // Fold
        ActionRequest fold = new ActionRequest(PlayerAction.FOLD, null);
        mockMvc.perform(post("/api/v1/games/" + gameId + "/actions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fold)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phase").value("SHOWDOWN"));

        // Deal next hand
        mockMvc.perform(post("/api/v1/games/" + gameId + "/next-hand")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phase").value("PRE_FLOP"))
                .andExpect(jsonPath("$.handNumber").value(2));
    }

    @Test
    void healthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/games/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
