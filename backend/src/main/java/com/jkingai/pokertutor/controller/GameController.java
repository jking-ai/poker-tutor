package com.jkingai.pokertutor.controller;

import com.jkingai.pokertutor.dto.ActionRequest;
import com.jkingai.pokertutor.dto.GameRequest;
import com.jkingai.pokertutor.dto.GameResponse;
import com.jkingai.pokertutor.model.Game;
import com.jkingai.pokertutor.model.PlayerAction;
import com.jkingai.pokertutor.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody GameRequest request) {
        Game game = gameService.createGame(request);
        List<PlayerAction> validActions = gameService.getValidActions(game);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GameResponse.fromGame(game, validActions, gameService.isAiEnabled()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGame(@PathVariable String id) {
        Game game = gameService.getGame(id);
        List<PlayerAction> validActions = gameService.getValidActions(game);
        return ResponseEntity.ok(GameResponse.fromGame(game, validActions, gameService.isAiEnabled()));
    }

    @PostMapping("/{id}/actions")
    public ResponseEntity<GameResponse> submitAction(
            @PathVariable String id,
            @Valid @RequestBody ActionRequest request) {
        Game game = gameService.processAction(id, request);
        List<PlayerAction> validActions = gameService.getValidActions(game);
        return ResponseEntity.ok(GameResponse.fromGame(game, validActions, gameService.isAiEnabled()));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<?> getHistory(@PathVariable String id) {
        Game game = gameService.getGame(id);
        return ResponseEntity.ok(Map.of(
                "gameId", game.getGameId(),
                "handNumber", game.getHandNumber(),
                "actions", game.getActionHistory()
        ));
    }

    @PostMapping("/{id}/next-hand")
    public ResponseEntity<GameResponse> nextHand(@PathVariable String id) {
        Game game = gameService.dealNextHand(id);
        List<PlayerAction> validActions = gameService.getValidActions(game);
        return ResponseEntity.ok(GameResponse.fromGame(game, validActions, gameService.isAiEnabled()));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "activeGames", gameService.getActiveGameCount(),
                "aiEnabled", gameService.isAiEnabled()
        ));
    }
}
