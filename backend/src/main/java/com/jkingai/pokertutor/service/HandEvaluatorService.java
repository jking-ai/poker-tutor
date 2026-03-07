package com.jkingai.pokertutor.service;

import com.jkingai.pokertutor.model.Card;
import com.jkingai.pokertutor.model.Card.Rank;
import com.jkingai.pokertutor.model.HandRank;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HandEvaluatorService {

    public HandEvaluation evaluateBestHand(List<Card> holeCards, List<Card> communityCards) {
        List<Card> allCards = new ArrayList<>(holeCards);
        allCards.addAll(communityCards);

        if (allCards.size() < 5) {
            List<Card> sorted = new ArrayList<>(allCards);
            sorted.sort(Comparator.comparingInt((Card c) -> c.rank().getValue()).reversed());
            HandRank rank = allCards.size() == 5 ? evaluateHand(sorted) : evaluatePartialHand(sorted);
            return new HandEvaluation(rank, sorted, rank.getDisplayName());
        }

        HandEvaluation best = null;
        List<List<Card>> combos = combinations(allCards, 5);
        for (List<Card> combo : combos) {
            HandRank rank = evaluateHand(combo);
            List<Card> sorted = sortHandForRank(combo, rank);
            HandEvaluation eval = new HandEvaluation(rank, sorted, rank.getDisplayName());
            if (best == null || compareEvaluations(eval, best) > 0) {
                best = eval;
            }
        }
        return best;
    }

    public HandRank evaluateHand(List<Card> fiveCards) {
        if (fiveCards.size() != 5) {
            throw new IllegalArgumentException("Hand must contain exactly 5 cards");
        }

        boolean isFlush = isFlush(fiveCards);
        boolean isStraight = isStraight(fiveCards);
        Map<Rank, Long> rankCounts = getRankCounts(fiveCards);
        List<Long> counts = rankCounts.values().stream().sorted(Comparator.reverseOrder()).toList();

        if (isFlush && isStraight) {
            List<Card> sorted = fiveCards.stream()
                    .sorted(Comparator.comparingInt((Card c) -> c.rank().getValue()).reversed())
                    .toList();
            if (sorted.getFirst().rank() == Rank.ACE && sorted.get(1).rank() == Rank.KING) {
                return HandRank.ROYAL_FLUSH;
            }
            return HandRank.STRAIGHT_FLUSH;
        }
        if (counts.getFirst() == 4) return HandRank.FOUR_OF_A_KIND;
        if (counts.getFirst() == 3 && counts.get(1) == 2) return HandRank.FULL_HOUSE;
        if (isFlush) return HandRank.FLUSH;
        if (isStraight) return HandRank.STRAIGHT;
        if (counts.getFirst() == 3) return HandRank.THREE_OF_A_KIND;
        if (counts.getFirst() == 2 && counts.get(1) == 2) return HandRank.TWO_PAIR;
        if (counts.getFirst() == 2) return HandRank.PAIR;
        return HandRank.HIGH_CARD;
    }

    public int compareHands(List<Card> hand1, List<Card> hand2, List<Card> communityCards) {
        HandEvaluation eval1 = evaluateBestHand(hand1, communityCards);
        HandEvaluation eval2 = evaluateBestHand(hand2, communityCards);
        return compareEvaluations(eval1, eval2);
    }

    public int compareEvaluations(HandEvaluation eval1, HandEvaluation eval2) {
        int rankCompare = Integer.compare(eval2.rank().ordinal(), eval1.rank().ordinal());
        if (rankCompare != 0) return rankCompare;
        return compareKickers(eval1.bestFiveCards(), eval2.bestFiveCards(), eval1.rank());
    }

    public String identifyNutHand(List<Card> communityCards) {
        if (communityCards == null || communityCards.isEmpty()) {
            return "Royal Flush";
        }

        Set<Card> communitySet = new HashSet<>(communityCards);
        List<Card> remainingCards = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Rank rank : Rank.values()) {
                Card c = new Card(rank, suit);
                if (!communitySet.contains(c)) {
                    remainingCards.add(c);
                }
            }
        }

        HandEvaluation bestNut = null;
        List<List<Card>> holeCombos = combinations(remainingCards, 2);
        for (List<Card> hole : holeCombos) {
            HandEvaluation eval = evaluateBestHand(hole, communityCards);
            if (bestNut == null || compareEvaluations(eval, bestNut) > 0) {
                bestNut = eval;
            }
        }

        return bestNut != null ? bestNut.description() : "Unknown";
    }

    private boolean isFlush(List<Card> cards) {
        Card.Suit suit = cards.getFirst().suit();
        return cards.stream().allMatch(c -> c.suit() == suit);
    }

    private boolean isStraight(List<Card> cards) {
        List<Integer> values = cards.stream()
                .map(c -> c.rank().getValue())
                .sorted()
                .toList();

        // Normal straight check
        boolean normal = true;
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) - values.get(i - 1) != 1) {
                normal = false;
                break;
            }
        }
        if (normal) return true;

        // Ace-low straight (A-2-3-4-5): Ace=14, so values would be [2,3,4,5,14]
        return values.equals(List.of(2, 3, 4, 5, 14));
    }

    private int getStraightHighCard(List<Card> cards) {
        List<Integer> values = cards.stream()
                .map(c -> c.rank().getValue())
                .sorted()
                .toList();
        // Ace-low straight: high card is 5, not Ace
        if (values.equals(List.of(2, 3, 4, 5, 14))) {
            return 5;
        }
        return values.getLast();
    }

    private Map<Rank, Long> getRankCounts(List<Card> cards) {
        return cards.stream().collect(Collectors.groupingBy(Card::rank, Collectors.counting()));
    }

    private List<Card> sortHandForRank(List<Card> cards, HandRank rank) {
        Map<Rank, Long> counts = getRankCounts(cards);

        return switch (rank) {
            case FOUR_OF_A_KIND, FULL_HOUSE, THREE_OF_A_KIND, TWO_PAIR, PAIR -> {
                // Sort by count desc, then by rank value desc within same count
                yield cards.stream()
                        .sorted(Comparator.comparingLong((Card c) -> counts.get(c.rank())).reversed()
                                .thenComparingInt((Card c) -> c.rank().getValue()).reversed())
                        .toList();
            }
            case STRAIGHT, STRAIGHT_FLUSH -> {
                List<Integer> values = cards.stream().map(c -> c.rank().getValue()).sorted().toList();
                if (values.equals(List.of(2, 3, 4, 5, 14))) {
                    // Ace-low: sort as 5,4,3,2,A
                    yield cards.stream()
                            .sorted((a, b) -> {
                                int aVal = a.rank() == Rank.ACE ? 1 : a.rank().getValue();
                                int bVal = b.rank() == Rank.ACE ? 1 : b.rank().getValue();
                                return Integer.compare(bVal, aVal);
                            })
                            .toList();
                }
                yield cards.stream()
                        .sorted(Comparator.comparingInt((Card c) -> c.rank().getValue()).reversed())
                        .toList();
            }
            default -> cards.stream()
                    .sorted(Comparator.comparingInt((Card c) -> c.rank().getValue()).reversed())
                    .toList();
        };
    }

    private int compareKickers(List<Card> hand1, List<Card> hand2, HandRank rank) {
        List<Integer> vals1 = getComparisonValues(hand1, rank);
        List<Integer> vals2 = getComparisonValues(hand2, rank);

        for (int i = 0; i < Math.min(vals1.size(), vals2.size()); i++) {
            int cmp = Integer.compare(vals1.get(i), vals2.get(i));
            if (cmp != 0) return cmp;
        }
        return 0;
    }

    private List<Integer> getComparisonValues(List<Card> sortedCards, HandRank rank) {
        Map<Rank, Long> counts = getRankCounts(sortedCards);

        return switch (rank) {
            case STRAIGHT, STRAIGHT_FLUSH -> {
                yield List.of(getStraightHighCard(sortedCards));
            }
            case FOUR_OF_A_KIND -> {
                Rank quadRank = getGroupRank(counts, 4);
                Rank kicker = getKickerRank(counts, quadRank);
                yield List.of(quadRank.getValue(), kicker != null ? kicker.getValue() : 0);
            }
            case FULL_HOUSE -> {
                Rank tripRank = getGroupRank(counts, 3);
                Rank pairRank = getGroupRank(counts, 2);
                yield List.of(tripRank.getValue(), pairRank != null ? pairRank.getValue() : 0);
            }
            case THREE_OF_A_KIND -> {
                Rank tripRank = getGroupRank(counts, 3);
                List<Integer> result = new ArrayList<>();
                result.add(tripRank.getValue());
                counts.entrySet().stream()
                        .filter(e -> e.getValue() == 1)
                        .map(e -> e.getKey().getValue())
                        .sorted(Comparator.reverseOrder())
                        .forEach(result::add);
                yield result;
            }
            case TWO_PAIR -> {
                List<Rank> pairs = counts.entrySet().stream()
                        .filter(e -> e.getValue() == 2)
                        .map(Map.Entry::getKey)
                        .sorted(Comparator.comparingInt((Rank r) -> r.getValue()).reversed())
                        .toList();
                List<Integer> result = new ArrayList<>();
                pairs.forEach(r -> result.add(r.getValue()));
                counts.entrySet().stream()
                        .filter(e -> e.getValue() == 1)
                        .map(e -> e.getKey().getValue())
                        .forEach(result::add);
                yield result;
            }
            case PAIR -> {
                Rank pairRank = getGroupRank(counts, 2);
                List<Integer> result = new ArrayList<>();
                result.add(pairRank.getValue());
                counts.entrySet().stream()
                        .filter(e -> e.getValue() == 1)
                        .map(e -> e.getKey().getValue())
                        .sorted(Comparator.reverseOrder())
                        .forEach(result::add);
                yield result;
            }
            default -> sortedCards.stream()
                    .map(c -> c.rank().getValue())
                    .sorted(Comparator.reverseOrder())
                    .toList();
        };
    }

    private Rank getGroupRank(Map<Rank, Long> counts, long groupSize) {
        return counts.entrySet().stream()
                .filter(e -> e.getValue() == groupSize)
                .map(Map.Entry::getKey)
                .max(Comparator.comparingInt(Rank::getValue))
                .orElse(null);
    }

    private Rank getKickerRank(Map<Rank, Long> counts, Rank excludeRank) {
        return counts.entrySet().stream()
                .filter(e -> e.getKey() != excludeRank)
                .map(Map.Entry::getKey)
                .max(Comparator.comparingInt(Rank::getValue))
                .orElse(null);
    }

    private HandRank evaluatePartialHand(List<Card> cards) {
        Map<Rank, Long> counts = getRankCounts(cards);
        List<Long> sortedCounts = counts.values().stream().sorted(Comparator.reverseOrder()).toList();
        if (sortedCounts.getFirst() >= 4) return HandRank.FOUR_OF_A_KIND;
        if (sortedCounts.getFirst() == 3 && sortedCounts.size() > 1 && sortedCounts.get(1) == 2) return HandRank.FULL_HOUSE;
        if (sortedCounts.getFirst() == 3) return HandRank.THREE_OF_A_KIND;
        if (sortedCounts.getFirst() == 2 && sortedCounts.size() > 1 && sortedCounts.get(1) == 2) return HandRank.TWO_PAIR;
        if (sortedCounts.getFirst() == 2) return HandRank.PAIR;
        return HandRank.HIGH_CARD;
    }

    private List<List<Card>> combinations(List<Card> cards, int k) {
        List<List<Card>> result = new ArrayList<>();
        combinationsHelper(cards, k, 0, new ArrayList<>(), result);
        return result;
    }

    private void combinationsHelper(List<Card> cards, int k, int start, List<Card> current, List<List<Card>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < cards.size(); i++) {
            current.add(cards.get(i));
            combinationsHelper(cards, k, i + 1, current, result);
            current.removeLast();
        }
    }

    public record HandEvaluation(HandRank rank, List<Card> bestFiveCards, String description) {}
}
