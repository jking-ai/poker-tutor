package com.jkingai.pokertutor.service;

import com.jkingai.pokertutor.dto.ActionRequest;
import com.jkingai.pokertutor.dto.GameRequest;
import com.jkingai.pokertutor.exception.GameNotFoundException;
import com.jkingai.pokertutor.exception.InvalidActionException;
import com.jkingai.pokertutor.model.*;
import com.jkingai.pokertutor.model.Game.ActionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final HandEvaluatorService handEvaluatorService;
    private final ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final RateLimitService rateLimitService;

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    private final OpponentAgentService opponentAgentService;

    public GameService(HandEvaluatorService handEvaluatorService, OpponentAgentService opponentAgentService,
                       RateLimitService rateLimitService) {
        this.handEvaluatorService = handEvaluatorService;
        this.opponentAgentService = opponentAgentService;
        this.rateLimitService = rateLimitService;
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public int getActiveGameCount() {
        return games.size();
    }

    public Game createGame(GameRequest request) {
        rateLimitService.checkGameCreationAllowed(games.size());
        Game game = new Game();
        game.setGameId("game_" + UUID.randomUUID().toString().substring(0, 8));

        Player human = new Player(request.playerName(), request.startingChips());
        Player ai = new Player("The House", request.startingChips());

        game.setPlayers(List.of(human, ai));
        game.setSmallBlind(request.smallBlind());
        game.setBigBlind(request.bigBlind());
        game.setDealerIndex(0); // Human starts as dealer
        game.setHandNumber(1);

        Deck deck = new Deck();
        game.setDeck(deck);

        dealNewHand(game);

        games.put(game.getGameId(), game);
        log.info("Created game {} for player '{}'", game.getGameId(), request.playerName());

        // If AI acts first (AI is dealer/SB pre-flop), auto-process
        if (!game.isHumanTurn() && game.getPhase() != GamePhase.SHOWDOWN) {
            processOpponentTurn(game);
        }

        return game;
    }

    public Game getGame(String gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }
        // Self-healing: if it's AI's turn and game isn't over, auto-process
        if (game.getPhase() != GamePhase.SHOWDOWN && !game.isHumanTurn() && !game.isGameOver()) {
            log.info("Auto-processing stuck AI turn for game {}", gameId);
            processOpponentTurn(game);
        }
        return game;
    }

    public Game processAction(String gameId, ActionRequest request) {
        Game game = getGame(gameId);

        if (game.getPhase() == GamePhase.SHOWDOWN) {
            throw new InvalidActionException("Hand is over. Use next-hand to deal a new hand.",
                    request.action(), List.of());
        }

        if (!game.isHumanTurn()) {
            throw new InvalidActionException("It is not your turn.",
                    request.action(), List.of());
        }

        List<PlayerAction> validActions = getValidActions(game);
        if (!validActions.contains(request.action())) {
            throw new InvalidActionException(
                    "Action " + request.action() + " is not valid in the current state",
                    request.action(), validActions);
        }

        applyAction(game, game.getCurrentPlayerIndex(), request.action(), request.amount(), null, null);

        // If game didn't end and it's opponent's turn, auto-process opponent
        if (game.getPhase() != GamePhase.SHOWDOWN && !game.isHumanTurn()) {
            processOpponentTurn(game);
        }

        return game;
    }

    public Game dealNextHand(String gameId) {
        Game game = getGame(gameId);

        if (game.isGameOver()) {
            throw new InvalidActionException("Game is over. Start a new game.",
                    null, List.of());
        }

        // Alternate dealer
        game.setDealerIndex(1 - game.getDealerIndex());
        game.setHandNumber(game.getHandNumber() + 1);

        // Reset players
        for (Player p : game.getPlayers()) {
            p.resetForNewHand();
        }

        // Reset deck
        game.getDeck().reset();
        game.setCommunityCards(new ArrayList<>());
        game.setPot(0);
        // Keep previous hand log entries for scrollback
        game.setLastAction(null);
        game.setWinnerMessage(null);
        game.resetCoachingCallsThisHand();

        dealNewHand(game);

        // If AI acts first (AI is dealer/SB pre-flop), auto-process
        if (!game.isHumanTurn() && game.getPhase() != GamePhase.SHOWDOWN) {
            processOpponentTurn(game);
        }

        return game;
    }

    public List<PlayerAction> getValidActions(Game game) {
        if (game.getPhase() == GamePhase.SHOWDOWN) {
            return List.of();
        }

        Player current = game.getCurrentPlayer();
        Player opponent = game.getOpponent();

        if (current.isFolded() || current.isAllIn()) {
            return List.of();
        }

        List<PlayerAction> actions = new ArrayList<>();
        int outstandingBet = opponent.getCurrentBet() - current.getCurrentBet();

        if (outstandingBet > 0) {
            // There's a bet to respond to
            actions.add(PlayerAction.CALL);
            if (current.getChips() > outstandingBet) {
                actions.add(PlayerAction.RAISE);
            }
            actions.add(PlayerAction.FOLD);
            actions.add(PlayerAction.ALL_IN);
        } else {
            // No outstanding bet
            actions.add(PlayerAction.CHECK);
            if (current.getChips() > 0) {
                actions.add(PlayerAction.BET);
            }
            actions.add(PlayerAction.FOLD);
            actions.add(PlayerAction.ALL_IN);
        }

        return actions;
    }

    private void dealNewHand(Game game) {
        Deck deck = game.getDeck();
        deck.shuffle();
        game.setPhase(GamePhase.PRE_FLOP);

        game.addLogEntry("--- Hand #" + game.getHandNumber() + " ---");
        game.addLogEntry("Dealer shuffles and deals.");

        // Deal 2 hole cards to each player
        for (Player p : game.getPlayers()) {
            p.setHoleCards(deck.deal(2));
        }

        // Update dealer status
        for (int i = 0; i < game.getPlayers().size(); i++) {
            game.getPlayers().get(i).setDealer(i == game.getDealerIndex());
        }

        // Post blinds (heads-up: dealer = small blind, other = big blind)
        int sbIndex = game.getDealerIndex();
        int bbIndex = 1 - sbIndex;

        Player sbPlayer = game.getPlayers().get(sbIndex);
        Player bbPlayer = game.getPlayers().get(bbIndex);

        int sbAmount = Math.min(game.getSmallBlind(), sbPlayer.getChips());
        sbPlayer.placeBet(sbAmount);
        game.addToPot(sbAmount);
        game.addLogEntry(sbPlayer.getName() + " posts small blind $" + sbAmount);

        int bbAmount = Math.min(game.getBigBlind(), bbPlayer.getChips());
        bbPlayer.placeBet(bbAmount);
        game.addToPot(bbAmount);
        game.addLogEntry(bbPlayer.getName() + " posts big blind $" + bbAmount);

        // Pre-flop: action starts with dealer (small blind) in heads-up
        game.setCurrentPlayerIndex(sbIndex);
        game.setPlayersActedThisRound(new HashSet<>());
    }

    private void applyAction(Game game, int playerIndex, PlayerAction action, Integer amount, String reasoning, String tableTalk) {
        Player player = game.getPlayers().get(playerIndex);
        Player opponent = game.getPlayers().get(1 - playerIndex);

        switch (action) {
            case FOLD -> {
                player.fold();
                // Opponent wins the pot
                opponent.addChips(game.getPot());
                game.setWinnerMessage(opponent.getName() + " wins $" + game.getPot() + " (opponent folded)");
                game.setPot(0);
                game.setPhase(GamePhase.SHOWDOWN);
                // Check if folder is eliminated (had all chips in pot)
                if (player.getChips() <= 0) {
                    game.setGameOver(true);
                    game.addLogEntry("=== GAME OVER === " + opponent.getName() + " wins the game!");
                }
            }
            case CHECK -> {
                // No chips change
            }
            case CALL -> {
                int callAmount = opponent.getCurrentBet() - player.getCurrentBet();
                callAmount = Math.min(callAmount, player.getChips());
                player.placeBet(callAmount);
                game.addToPot(callAmount);
            }
            case BET -> {
                int betAmount = amount != null ? amount : game.getBigBlind();
                betAmount = Math.min(betAmount, player.getChips());
                player.placeBet(betAmount);
                game.addToPot(betAmount);
                // Reset acted set since opponent must respond
                game.getPlayersActedThisRound().clear();
            }
            case RAISE -> {
                // First call the outstanding bet
                int callAmount = opponent.getCurrentBet() - player.getCurrentBet();
                callAmount = Math.min(callAmount, player.getChips());
                player.placeBet(callAmount);
                game.addToPot(callAmount);

                // Then raise by the specified amount
                int raiseAmount = amount != null ? amount : game.getBigBlind();
                raiseAmount = Math.min(raiseAmount, player.getChips());
                player.placeBet(raiseAmount);
                game.addToPot(raiseAmount);

                // Reset acted set since opponent must respond
                game.getPlayersActedThisRound().clear();
            }
            case ALL_IN -> {
                int allInAmount = player.getChips();
                player.placeBet(allInAmount);
                game.addToPot(allInAmount);
                // If this raises, reset acted set
                if (player.getCurrentBet() > opponent.getCurrentBet()) {
                    game.getPlayersActedThisRound().clear();
                }
            }
        }

        // Build log message for the action
        String logMsg = switch (action) {
            case FOLD -> player.getName() + " folds.";
            case CHECK -> player.getName() + " checks.";
            case CALL -> player.getName() + " calls $" + (opponent.getCurrentBet() > 0 ? Math.min(opponent.getCurrentBet(), player.getCurrentBet()) : player.getCurrentBet()) + ".";
            case BET -> player.getName() + " bets $" + player.getCurrentBet() + ".";
            case RAISE -> player.getName() + " raises to $" + player.getCurrentBet() + ".";
            case ALL_IN -> player.getName() + " goes all-in for $" + player.getCurrentBet() + "!";
        };
        ActionRecord record = new ActionRecord(player.getName(), action,
                player.getCurrentBet(), reasoning, tableTalk, game.getPhase());
        game.getActionHistory().add(record);
        game.setLastAction(record);

        if (action != PlayerAction.FOLD) {
            game.getPlayersActedThisRound().add(playerIndex);
            checkRoundComplete(game);
        }
    }

    private void checkRoundComplete(Game game) {
        if (game.getPhase() == GamePhase.SHOWDOWN) return;

        Player p0 = game.getPlayers().get(0);
        Player p1 = game.getPlayers().get(1);

        boolean betsEqualized = p0.getCurrentBet() == p1.getCurrentBet();
        boolean bothActed = game.getPlayersActedThisRound().size() >= 2;
        boolean bothAllIn = p0.isAllIn() && p1.isAllIn();
        boolean oneAllIn = p0.isAllIn() || p1.isAllIn();

        // Round is complete when:
        // 1) Bets equalized and both acted (or one all-in), OR
        // 2) Both players are all-in (bets may differ due to chip difference), OR
        // 3) Both acted and one is all-in (called with fewer chips)
        if ((betsEqualized && (bothActed || oneAllIn)) || bothAllIn || (bothActed && oneAllIn)) {
            // Return excess bet if bets don't match (side pot scenario in heads-up)
            if (!betsEqualized) {
                Player bigger = p0.getCurrentBet() > p1.getCurrentBet() ? p0 : p1;
                int excess = Math.abs(p0.getCurrentBet() - p1.getCurrentBet());
                bigger.addChips(excess);
                game.addToPot(-excess);
            }
            transitionToNextPhase(game);
        } else {
            // Switch turn to other player
            game.setCurrentPlayerIndex(1 - game.getCurrentPlayerIndex());
        }
    }

    private void transitionToNextPhase(Game game) {
        // Collect bets into pot is already done during action
        // Reset current bets
        for (Player p : game.getPlayers()) {
            p.resetBet();
        }

        game.advancePhase();
        game.setPlayersActedThisRound(new HashSet<>());

        boolean bothAllIn = game.getPlayers().get(0).isAllIn() && game.getPlayers().get(1).isAllIn();
        boolean oneAllIn = game.getPlayers().get(0).isAllIn() || game.getPlayers().get(1).isAllIn();

        switch (game.getPhase()) {
            case FLOP -> {
                List<Card> flop = game.getDeck().deal(3);
                game.getCommunityCards().addAll(flop);
                game.addLogEntry("*** FLOP *** [" + formatCards(flop) + "]");
                if (bothAllIn) {
                    runOutRemainingCards(game);
                } else {
                    game.setCurrentPlayerIndex(1 - game.getDealerIndex());
                }
            }
            case TURN -> {
                List<Card> turn = game.getDeck().deal(1);
                game.getCommunityCards().addAll(turn);
                game.addLogEntry("*** TURN *** [" + formatCards(turn) + "]");
                if (bothAllIn) {
                    runOutRemainingCards(game);
                } else {
                    game.setCurrentPlayerIndex(1 - game.getDealerIndex());
                }
            }
            case RIVER -> {
                List<Card> river = game.getDeck().deal(1);
                game.getCommunityCards().addAll(river);
                game.addLogEntry("*** RIVER *** [" + formatCards(river) + "]");
                if (bothAllIn) {
                    resolveShowdown(game);
                } else {
                    game.setCurrentPlayerIndex(1 - game.getDealerIndex());
                }
            }
            case SHOWDOWN -> resolveShowdown(game);
            default -> {}
        }
    }

    private void runOutRemainingCards(Game game) {
        while (game.getCommunityCards().size() < 5) {
            game.advancePhase();
            int cardsNeeded = switch (game.getPhase()) {
                case FLOP -> 3;
                case TURN, RIVER -> 1;
                default -> 0;
            };
            if (cardsNeeded > 0) {
                List<Card> dealt = game.getDeck().deal(cardsNeeded);
                game.getCommunityCards().addAll(dealt);
                game.addLogEntry("*** " + game.getPhase() + " *** [" + formatCards(dealt) + "]");
            }
        }
        game.setPhase(GamePhase.SHOWDOWN);
        resolveShowdown(game);
    }

    private void resolveShowdown(Game game) {
        game.setPhase(GamePhase.SHOWDOWN);

        Player human = game.getHumanPlayer();
        Player ai = game.getAiPlayer();

        if (human.isFolded()) {
            ai.addChips(game.getPot());
            game.setWinnerMessage(ai.getName() + " wins $" + game.getPot());
            game.addLogEntry(ai.getName() + " wins $" + game.getPot() + " (opponent folded).");
        } else if (ai.isFolded()) {
            human.addChips(game.getPot());
            game.setWinnerMessage(human.getName() + " wins $" + game.getPot());
            game.addLogEntry(human.getName() + " wins $" + game.getPot() + " (opponent folded).");
        } else {
            int result = handEvaluatorService.compareHands(
                    human.getHoleCards(), ai.getHoleCards(), game.getCommunityCards());

            HandEvaluatorService.HandEvaluation humanEval =
                    handEvaluatorService.evaluateBestHand(human.getHoleCards(), game.getCommunityCards());
            HandEvaluatorService.HandEvaluation aiEval =
                    handEvaluatorService.evaluateBestHand(ai.getHoleCards(), game.getCommunityCards());

            game.addLogEntry("*** SHOWDOWN ***");
            game.addLogEntry(human.getName() + " shows [" + formatCards(human.getHoleCards()) + "] - " + humanEval.rank().getDisplayName());
            game.addLogEntry(ai.getName() + " shows [" + formatCards(ai.getHoleCards()) + "] - " + aiEval.rank().getDisplayName());

            if (result > 0) {
                human.addChips(game.getPot());
                String msg = human.getName() + " wins $" + game.getPot() + " with " + humanEval.rank().getDisplayName();
                game.setWinnerMessage(msg);
                game.addLogEntry(msg);
            } else if (result < 0) {
                ai.addChips(game.getPot());
                String msg = ai.getName() + " wins $" + game.getPot() + " with " + aiEval.rank().getDisplayName();
                game.setWinnerMessage(msg);
                game.addLogEntry(msg);
            } else {
                int half = game.getPot() / 2;
                human.addChips(half);
                ai.addChips(game.getPot() - half);
                String msg = "Split pot! Both have " + humanEval.rank().getDisplayName();
                game.setWinnerMessage(msg);
                game.addLogEntry(msg);
            }
        }
        game.setPot(0);

        // Check if any player is eliminated
        for (Player p : game.getPlayers()) {
            if (p.getChips() <= 0) {
                game.setGameOver(true);
                Player winner = game.getPlayers().stream()
                        .filter(pl -> pl.getChips() > 0)
                        .findFirst().orElse(null);
                if (winner != null) {
                    game.addLogEntry("=== GAME OVER === " + winner.getName() + " wins the game!");
                }
                break;
            }
        }
    }

    private String formatCards(List<Card> cards) {
        return cards.stream()
                .map(c -> formatRank(c.rank()) + formatSuit(c.suit()))
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }

    private String formatRank(Card.Rank rank) {
        return switch (rank) {
            case ACE -> "A";
            case KING -> "K";
            case QUEEN -> "Q";
            case JACK -> "J";
            case TEN -> "T";
            default -> String.valueOf(rank.getValue());
        };
    }

    private String formatSuit(Card.Suit suit) {
        return switch (suit) {
            case HEARTS -> "\u2665";
            case DIAMONDS -> "\u2666";
            case CLUBS -> "\u2663";
            case SPADES -> "\u2660";
        };
    }

    @Scheduled(fixedRate = 600000) // every 10 minutes
    public void cleanupStaleGames() {
        Instant cutoff = Instant.now().minus(Duration.ofHours(2));
        int before = games.size();
        games.entrySet().removeIf(entry -> entry.getValue().getCreatedAt().isBefore(cutoff));
        int removed = before - games.size();
        if (removed > 0) {
            log.info("Cleaned up {} stale games (older than 2 hours)", removed);
        }
    }

    private void processOpponentTurn(Game game) {
        if (game.getPhase() == GamePhase.SHOWDOWN) return;

        List<PlayerAction> validActions = getValidActions(game);
        if (validActions.isEmpty()) return;

        // Try AI agent if enabled and within rate limit
        if (aiEnabled && opponentAgentService != null) {
            if (!rateLimitService.isAiCallAllowed(game)) {
                log.warn("AI call limit reached for game {}, using random fallback", game.getGameId());
            } else {
                try {
                    OpponentAgentService.OpponentDecision decision = opponentAgentService.getOpponentAction(game);
                    game.incrementAiCallCount();
                    if (validActions.contains(decision.action())) {
                        applyAction(game, game.getCurrentPlayerIndex(), decision.action(),
                                decision.amount(), decision.reasoning(), decision.tableTalk());
                        return;
                    }
                } catch (Exception e) {
                    log.warn("AI opponent failed, falling back to random: {}", e.getMessage());
                }
            }
        }

        // Random fallback opponent
        PlayerAction action;
        Integer amount = null;

        int roll = random.nextInt(100);
        if (validActions.contains(PlayerAction.CHECK)) {
            if (roll < 60) {
                action = PlayerAction.CHECK;
            } else if (roll < 85) {
                action = PlayerAction.BET;
                amount = game.getBigBlind() * (1 + random.nextInt(3));
            } else {
                action = PlayerAction.FOLD;
            }
        } else {
            // Facing a bet
            if (roll < 50) {
                action = PlayerAction.CALL;
            } else if (roll < 75) {
                action = PlayerAction.RAISE;
                amount = game.getBigBlind() * (1 + random.nextInt(3));
            } else {
                action = PlayerAction.FOLD;
            }
        }

        // Ensure chosen action is valid
        if (!validActions.contains(action)) {
            action = validActions.getFirst();
        }

        String reasoning = switch (action) {
            case CHECK -> "I'll check and see what happens.";
            case CALL -> "I'll call your bet.";
            case BET -> "Time to put some pressure on.";
            case RAISE -> "Let's make this interesting!";
            case FOLD -> "Not worth it this time.";
            case ALL_IN -> "All in!";
        };

        applyAction(game, game.getCurrentPlayerIndex(), action, amount, reasoning, null);

        // If round completed and it's still opponent's turn (e.g. after phase transition), continue
        if (game.getPhase() != GamePhase.SHOWDOWN && !game.isHumanTurn()) {
            processOpponentTurn(game);
        }
    }
}
