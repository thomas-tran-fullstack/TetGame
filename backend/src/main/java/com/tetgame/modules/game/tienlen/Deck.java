package com.tetgame.modules.game.tienlen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Deck {
    private final List<Card> cards = new ArrayList<>();

    public Deck() {
        for (CardSuit s : CardSuit.values()) {
            for (CardRank r : CardRank.values()) {
                cards.add(new Card(s, r));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<List<Card>> deal(int players, int cardsPerPlayer) {
        if (players * cardsPerPlayer > cards.size()) throw new IllegalArgumentException("Not enough cards");
        List<List<Card>> hands = new ArrayList<>();
        for (int i = 0; i < players; i++) hands.add(new ArrayList<>());
        for (int c = 0; c < cardsPerPlayer; c++) {
            for (int p = 0; p < players; p++) {
                hands.get(p).add(cards.get(c * players + p));
            }
        }
        return hands;
    }

    public List<Card> getCards() { return Collections.unmodifiableList(cards); }
}
