package com.jkingai.pokertutor.service;

import com.jkingai.pokertutor.dto.ActionRequest;
import com.jkingai.pokertutor.dto.GameRequest;
import com.jkingai.pokertutor.exception.GameNotFoundException;
import com.jkingai.pokertutor.exception.InvalidActionException;
import com.jkingai.pokertutor.model.Game;
import com.jkingai.pokertutor.model.GamePhase;
import com.jkingai.pokertutor.model.PlayerAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService gameService;

    @BeforeEach
    void setUp() {
        HandEvaluatorService handEvaluatorService = new HandEvaluatorService();
        OpponentAgentService opponentAgentService = new OpponentAgentService(null);
        RateLimitService rateLimitService = new RateLimitService(200, 15, 3);
        gameService = new GameService(handEvaluatorService, opponentAgentService, rateLimitService);
    }

    private Game createTestGame() {
        return gameService.createGame(new GameRequest("TestPlayer", 1000, 5, 10));
    }

    @Test
    void createGameInitializesCorrectly() {
        Game game = createTestGame();

        assertNotNull(game.getGameId());
        assertEquals(GamePhase.PRE_FLOP, game.getPhase());
        assertEquals(2, game.getPlayers().size());
        assertEquals("TestPlayer", game.getHumanPlayer().getName());
        assertEquals("The House", game.getAiPlayer().getName());
        assertEquals(2, game.getHumanPlayer().getHoleCards().size());
        assertEquals(2, game.getAiPlayer().getHoleCards().size());
        assertEquals(1, game.getHandNumber());
        assertEquals(15, game.getPot()); // 5 SB + 10 BB
    }

    @Test
    void getGameReturnsExistingGame() {
        Game game = createTestGame();
        Game retrieved = gameService.getGame(game.getGameId());
        assertEquals(game.getGameId(), retrieved.getGameId());
    }

    @Test
    void getGameThrowsForNonexistent() {
        assertThrows(GameNotFoundException.class, () -> gameService.getGame("fake_id"));
    }

    @Test
    void validActionsPreFlopDealer() {
        Game game = createTestGame();
        // Dealer (SB) acts first pre-flop in heads-up
        // Dealer posted 5 (SB), opponent posted 10 (BB), so there's an outstanding bet
        List<PlayerAction> actions = gameService.getValidActions(game);
        assertTrue(actions.contains(PlayerAction.CALL));
        assertTrue(actions.contains(PlayerAction.RAISE));
        assertTrue(actions.contains(PlayerAction.FOLD));
    }

    @Test
    void foldEndsHand() {
        Game game = createTestGame();
        game = gameService.processAction(game.getGameId(),
                new ActionRequest(PlayerAction.FOLD, null));
        assertEquals(GamePhase.SHOWDOWN, game.getPhase());
        assertNotNull(game.getWinnerMessage());
    }

    @Test
    void callEqualizesAndOpponentResponds() {
        Game game = createTestGame();
        // Human is dealer (SB), calls the BB
        game = gameService.processAction(game.getGameId(),
                new ActionRequest(PlayerAction.CALL, null));
        // After call, opponent (BB) should auto-act (random opponent)
        // Game may advance phase or stay depending on opponent's action
        assertNotNull(game.getPhase());
    }

    @Test
    void invalidActionThrows() {
        Game game = createTestGame();
        // Pre-flop with outstanding bet, CHECK is not valid
        assertThrows(InvalidActionException.class, () ->
                gameService.processAction(game.getGameId(),
                        new ActionRequest(PlayerAction.CHECK, null)));
    }

    @Test
    void dealNextHandAlternatesDealer() {
        Game game = createTestGame();
        int firstDealer = game.getDealerIndex();

        // End the hand by folding
        gameService.processAction(game.getGameId(), new ActionRequest(PlayerAction.FOLD, null));

        game = gameService.dealNextHand(game.getGameId());
        assertEquals(1 - firstDealer, game.getDealerIndex());
        assertEquals(2, game.getHandNumber());
        assertEquals(GamePhase.PRE_FLOP, game.getPhase());
    }

    @Test
    void dealNextHandResetsCommunityCards() {
        Game game = createTestGame();
        gameService.processAction(game.getGameId(), new ActionRequest(PlayerAction.FOLD, null));
        game = gameService.dealNextHand(game.getGameId());
        assertTrue(game.getCommunityCards().isEmpty());
    }

    @Test
    void dealNextHandThrowsWhenGameOver() {
        Game game = createTestGame();
        // End the hand normally
        gameService.processAction(game.getGameId(),
                new ActionRequest(PlayerAction.FOLD, null));
        // Manually set gameOver to simulate elimination
        game.setGameOver(true);
        assertThrows(InvalidActionException.class, () ->
                gameService.dealNextHand(game.getGameId()));
    }

    @Test
    void gameNotOverAfterNormalFold() {
        Game game = createTestGame();
        game = gameService.processAction(game.getGameId(),
                new ActionRequest(PlayerAction.FOLD, null));
        assertFalse(game.isGameOver());
    }

    @Test
    void showdownAtEndActionThrows() {
        Game game = createTestGame();
        gameService.processAction(game.getGameId(), new ActionRequest(PlayerAction.FOLD, null));
        assertEquals(GamePhase.SHOWDOWN, game.getPhase());
        assertThrows(InvalidActionException.class, () ->
                gameService.processAction(game.getGameId(),
                        new ActionRequest(PlayerAction.CHECK, null)));
    }
}
