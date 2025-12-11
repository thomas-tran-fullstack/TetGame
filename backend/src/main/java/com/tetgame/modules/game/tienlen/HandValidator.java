package com.tetgame.modules.game.tienlen;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HandValidator {

    public static HandType classify(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return HandType.OTHER;
        if (cards.size() == 1) return HandType.SINGLE;
        Map<CardRank, Long> counts = cards.stream().collect(Collectors.groupingBy(Card::rank, Collectors.counting()));
        if (counts.size() == 1 && counts.values().contains((long)3)) return HandType.TRIPLE;
        if (counts.size() == 1 && counts.values().contains((long)4)) return HandType.FOUR_OF_KIND;
        if (cards.size() == 2 && counts.size() == 1) return HandType.PAIR;
        if (isConsecutivePairs(cards)) return HandType.CONSECUTIVE_PAIRS;
        if (isStraight(cards)) return HandType.STRAIGHT;
        return HandType.OTHER;
    }

    private static boolean isStraight(List<Card> cards) {
        if (cards.size() < 3) return false;
        List<Integer> vals = cards.stream().map(c -> c.rank().getValue()).sorted().collect(Collectors.toList());
        for (int i = 1; i < vals.size(); i++) {
            if (vals.get(i) != vals.get(i-1) + 1) return false;
        }
        return true;
    }

    private static boolean isConsecutivePairs(List<Card> cards) {
        if (cards.size() < 6 || cards.size() % 2 != 0) return false; // at least 3 pairs
        Map<CardRank, Long> counts = cards.stream().collect(Collectors.groupingBy(Card::rank, Collectors.counting()));
        // each rank must appear exactly twice
        if (counts.values().stream().anyMatch(c -> c != 2L)) return false;
        List<Integer> ranks = counts.keySet().stream().map(CardRank::getValue).sorted().collect(Collectors.toList());
        for (int i = 1; i < ranks.size(); i++) {
            if (ranks.get(i) != ranks.get(i-1) + 1) return false;
        }
        return true;
    }

    private static boolean isFourOfKind(List<Card> cards) {
        if (cards.size() != 4) return false;
        Map<CardRank, Long> counts = cards.stream().collect(Collectors.groupingBy(Card::rank, Collectors.counting()));
        return counts.size() == 1 && counts.values().contains(4L);
    }

    /**
     * Kiểm tra "tới trắng" theo quy ước hiện tại:
     * - 6 đôi bất kỳ (>=6 cặp trong 13 lá)
     * - tứ quý heo (4 lá có rank = TWO)
     * - sảnh rồng: có ít nhất 1 lá của mỗi rank từ THREE .. ACE (3->A)
     */
    public static boolean laToiTrang(List<Card> cards) {
        if (cards == null || cards.isEmpty()) return false;
        // thường chỉ kiểm tra trên bài chia ban đầu (13 lá)
        if (la6DoiBatKy(cards)) return true;
        if (laTuQuyHeo(cards)) return true;
        if (laSanhRong(cards)) return true;
        return false;
    }

    // 6 đôi bất kỳ: có ít nhất 6 rank có count >= 2
    private static boolean la6DoiBatKy(List<Card> cards) {
        Map<CardRank, Long> counts = cards.stream().collect(Collectors.groupingBy(Card::rank, Collectors.counting()));
        long pairCount = counts.values().stream().filter(c -> c >= 2).count();
        return pairCount >= 6;
    }

    // Tứ quý heo: có 4 lá rank TWO
    private static boolean laTuQuyHeo(List<Card> cards) {
        Map<CardRank, Long> counts = cards.stream().collect(Collectors.groupingBy(Card::rank, Collectors.counting()));
        return counts.getOrDefault(CardRank.TWO, 0L) == 4L;
    }

    // Sảnh rồng: có ít nhất 1 lá cho mỗi rank từ THREE .. ACE (3->A)
    private static boolean laSanhRong(List<Card> cards) {
        Map<CardRank, Long> counts = cards.stream().collect(Collectors.groupingBy(Card::rank, Collectors.counting()));
        for (CardRank r : CardRank.values()) {
            if (r == CardRank.TWO) break; // dừng trước TWO
            if (counts.getOrDefault(r, 0L) < 1) return false;
        }
        return true;
    }
}
