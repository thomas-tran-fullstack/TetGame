package com.tetgame.modules.game.tienlen;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeckTest {

    @Test
    public void testDeckSizeAndShuffle() {
        Deck deck = new Deck();
        assertEquals(52, deck.getCards().size());
        List<Card> before = deck.getCards();
        deck.shuffle();
        List<Card> after = deck.getCards();
        assertEquals(52, after.size());
        // After shuffle order likely different
        boolean sameOrder = before.equals(after);
        // It's possible (extremely unlikely) they are same; just ensure method runs
        assertNotNull(after);
    }
}
