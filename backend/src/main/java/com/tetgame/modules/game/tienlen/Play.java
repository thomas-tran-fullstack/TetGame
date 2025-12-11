package com.tetgame.modules.game.tienlen;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Play {
    private final PlayType type;
    private final List<Card> cards;
    private final int primaryRankValue; // for comparison

    public Play(PlayType type, List<Card> cards, int primaryRankValue) {
        this.type = type;
        this.cards = cards;
        this.primaryRankValue = primaryRankValue;
    }

    public PlayType getType() { return type; }
    public List<Card> getCards() { return cards; }
    public int getPrimaryRankValue() { return primaryRankValue; }

    public static Play fromCards(List<Card> cards) {
        // Map HandType (validator) to PlayType
        HandType ht = HandValidator.classify(cards);
        PlayType t = mapHandTypeToPlayType(ht);
        int primary = computePrimary(cards, t);
        return new Play(t, cards, primary);
    }

    private static PlayType mapHandTypeToPlayType(HandType ht) {
        return switch (ht) {
            case SINGLE -> PlayType.SINGLE;
            case PAIR -> PlayType.PAIR;
            case TRIPLE -> PlayType.TRIPLE;
            case STRAIGHT -> PlayType.STRAIGHT;
            case CONSECUTIVE_PAIRS -> PlayType.CONSECUTIVE_PAIRS;
            case FOUR_OF_KIND -> PlayType.FOUR_OF_KIND;
            default -> PlayType.INVALID;
        };
    }

    private static int computePrimary(List<Card> cards, PlayType t) {
        if (cards == null || cards.isEmpty()) return -1;
        switch (t) {
            case SINGLE: return cards.get(0).rank().getValue();
            case PAIR:
            case TRIPLE:
            case FOUR_OF_KIND:
                return cards.get(0).rank().getValue();
            case STRAIGHT:
            case CONSECUTIVE_PAIRS:
                return cards.stream().map(c -> c.rank().getValue()).max(Integer::compareTo).orElse(-1);
            default: return -1;
        }
    }

    public boolean beats(Play other) {
        if (other == null) return true;
        if (this.type == PlayType.INVALID) return false;
        if (this.type == other.type && this.cards.size() == other.cards.size()) {
            return this.primaryRankValue > other.primaryRankValue;
        }
        // bombs beat non-bombs
        if (isBomb(this.type) && !isBomb(other.type)) return true;
        if (!isBomb(this.type) && isBomb(other.type)) return false;
        // two bombs: compare by primary
        if (isBomb(this.type) && isBomb(other.type) && this.cards.size() == other.cards.size()) {
            return this.primaryRankValue > other.primaryRankValue;
        }
        return false;
    }

    private boolean isBomb(PlayType t) {
        return t == PlayType.FOUR_OF_KIND || t == PlayType.CONSECUTIVE_PAIRS || t == PlayType.BOMB;
    }
}
