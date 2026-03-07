package com.jkingai.pokertutor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkingai.pokertutor.model.*;
import com.jkingai.pokertutor.model.Game.ActionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class OpponentAgentService {

    private static final Logger log = LoggerFactory.getLogger(OpponentAgentService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final ChatClient chatClient;

    public OpponentAgentService(@Autowired(required = false) @Qualifier("opponentChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    private static final int TIMEOUT_SECONDS = 15;
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public OpponentDecision getOpponentAction(Game game) {
        if (chatClient == null) {
            throw new UnsupportedOperationException("AI opponent requires Vertex AI configuration.");
        }

        String userPrompt = buildGameStatePrompt(game);
        log.info("AI opponent thinking... phase={}", game.getPhase());

        try {
            Future<String> future = executor.submit(() ->
                    chatClient.prompt().user(userPrompt).call().content()
            );
            String response = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("AI opponent responded: {}", response);
            return parseResponse(response, game);
        } catch (TimeoutException e) {
            log.warn("AI opponent timed out after {}s, falling back", TIMEOUT_SECONDS);
            throw new RuntimeException("AI timed out");
        } catch (Exception e) {
            log.warn("AI opponent error: {}", e.getMessage());
            throw new RuntimeException("AI call failed", e);
        }
    }

    private String buildGameStatePrompt(Game game) {
        Player ai = game.getAiPlayer();
        Player human = game.getHumanPlayer();
        List<PlayerAction> validActions = buildValidActionsList(game);

        StringBuilder sb = new StringBuilder();
        sb.append("Game State:\n");
        sb.append("- Phase: ").append(game.getPhase()).append("\n");
        sb.append("- Your hole cards: ").append(formatCards(ai.getHoleCards())).append("\n");

        if (!game.getCommunityCards().isEmpty()) {
            sb.append("- Community cards: ").append(formatCards(game.getCommunityCards())).append("\n");
        }

        sb.append("- Pot: $").append(game.getPot()).append("\n");
        sb.append("- Your chips: $").append(ai.getChips()).append("\n");
        sb.append("- Opponent chips: $").append(human.getChips()).append("\n");
        sb.append("- Your current bet: $").append(ai.getCurrentBet()).append("\n");
        sb.append("- Opponent current bet: $").append(human.getCurrentBet()).append("\n");
        sb.append("- Big blind: $").append(game.getBigBlind()).append("\n");
        sb.append("- You are ").append(ai.isDealer() ? "the dealer (small blind)" : "big blind").append("\n");

        sb.append("- Valid actions: ").append(
                validActions.stream().map(Enum::name).collect(Collectors.joining(", "))
        ).append("\n");

        // Recent action history
        List<ActionRecord> history = game.getActionHistory();
        if (!history.isEmpty()) {
            sb.append("- Recent actions this hand:\n");
            int start = Math.max(0, history.size() - 8);
            for (int i = start; i < history.size(); i++) {
                ActionRecord ar = history.get(i);
                if (ar.action() != null) {
                    sb.append("  ").append(ar.player()).append(": ").append(ar.action());
                    if (ar.amount() > 0) sb.append(" $").append(ar.amount());
                    sb.append("\n");
                }
            }
        }

        sb.append("\nChoose your action. Respond with ONLY a JSON object.");
        return sb.toString();
    }

    private List<PlayerAction> buildValidActionsList(Game game) {
        Player current = game.getAiPlayer();
        Player opponent = game.getHumanPlayer();

        if (current.isFolded() || current.isAllIn()) {
            return List.of();
        }

        java.util.ArrayList<PlayerAction> actions = new java.util.ArrayList<>();
        int outstandingBet = opponent.getCurrentBet() - current.getCurrentBet();

        if (outstandingBet > 0) {
            actions.add(PlayerAction.CALL);
            if (current.getChips() > outstandingBet) {
                actions.add(PlayerAction.RAISE);
            }
            actions.add(PlayerAction.FOLD);
            actions.add(PlayerAction.ALL_IN);
        } else {
            actions.add(PlayerAction.CHECK);
            if (current.getChips() > 0) {
                actions.add(PlayerAction.BET);
            }
            actions.add(PlayerAction.FOLD);
            actions.add(PlayerAction.ALL_IN);
        }
        return actions;
    }

    private OpponentDecision parseResponse(String response, Game game) {
        try {
            // Strip markdown code fences if present
            String json = response.strip();
            if (json.startsWith("```")) {
                json = json.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
            }

            JsonNode node = mapper.readTree(json);
            String actionStr = node.get("action").asText().toUpperCase();
            int amount = node.has("amount") ? node.get("amount").asInt(0) : 0;
            String reasoning = node.has("reasoning") ? node.get("reasoning").asText() : "";

            String tableTalk = node.has("table_talk") ? node.get("table_talk").asText() : "";

            PlayerAction action = PlayerAction.valueOf(actionStr);

            // Sanity check amount for BET/RAISE
            if ((action == PlayerAction.BET || action == PlayerAction.RAISE) && amount < game.getBigBlind()) {
                amount = game.getBigBlind();
            }

            return new OpponentDecision(action, amount, reasoning, tableTalk);
        } catch (Exception e) {
            log.warn("Failed to parse AI response '{}': {}", response, e.getMessage());
            throw new RuntimeException("Failed to parse AI opponent response", e);
        }
    }

    private String formatCards(List<Card> cards) {
        return cards.stream()
                .map(c -> formatRank(c.rank()) + formatSuit(c.suit()))
                .collect(Collectors.joining(" "));
    }

    private String formatRank(Card.Rank rank) {
        return switch (rank) {
            case ACE -> "A";
            case KING -> "K";
            case QUEEN -> "Q";
            case JACK -> "J";
            case TEN -> "10";
            default -> String.valueOf(rank.getValue());
        };
    }

    private String formatSuit(Card.Suit suit) {
        return switch (suit) {
            case HEARTS -> "h";
            case DIAMONDS -> "d";
            case CLUBS -> "c";
            case SPADES -> "s";
        };
    }

    public record OpponentDecision(PlayerAction action, int amount, String reasoning, String tableTalk) {}
}
