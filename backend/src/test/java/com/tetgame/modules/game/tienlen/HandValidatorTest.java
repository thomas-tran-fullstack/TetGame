package com.tetgame.modules.game.tienlen;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HandValidatorTest {

    @Test
    public void testSingleAndPair() {
        Card c1 = new Card(CardSuit.HEARTS, CardRank.THREE);
        assertEquals(HandType.SINGLE, HandValidator.classify(List.of(c1)));
        Card c2 = new Card(CardSuit.SPADES, CardRank.THREE);
        assertEquals(HandType.PAIR, HandValidator.classify(List.of(c1, c2)));
    }

    @Test
    public void testLa6DoiTuong() {
        List<Card> cards = List.of(
            new Card(CardSuit.CLUBS, CardRank.THREE), new Card(CardSuit.HEARTS, CardRank.THREE),
            new Card(CardSuit.CLUBS, CardRank.FOUR), new Card(CardSuit.HEARTS, CardRank.FOUR),
            new Card(CardSuit.CLUBS, CardRank.FIVE), new Card(CardSuit.HEARTS, CardRank.FIVE),
            new Card(CardSuit.CLUBS, CardRank.SIX), new Card(CardSuit.HEARTS, CardRank.SIX),
            new Card(CardSuit.CLUBS, CardRank.SEVEN), new Card(CardSuit.HEARTS, CardRank.SEVEN),
            new Card(CardSuit.CLUBS, CardRank.EIGHT), new Card(CardSuit.HEARTS, CardRank.EIGHT)
        );
        assertTrue(HandValidator.laToiTrang(cards));
    }

    @Test
    public void testLaTuQuyHeo() {
        List<Card> cards = List.of(
            new Card(CardSuit.CLUBS, CardRank.TWO),
            new Card(CardSuit.HEARTS, CardRank.TWO),
            new Card(CardSuit.SPADES, CardRank.TWO),
            new Card(CardSuit.DIAMONDS, CardRank.TWO)
        );
        assertTrue(HandValidator.laToiTrang(cards));
    }

    @Test
    public void testLaSanhRong() {
        List<Card> cards = List.of(
            new Card(CardSuit.CLUBS, CardRank.THREE), new Card(CardSuit.CLUBS, CardRank.FOUR),
            new Card(CardSuit.CLUBS, CardRank.FIVE), new Card(CardSuit.CLUBS, CardRank.SIX),
            new Card(CardSuit.CLUBS, CardRank.SEVEN), new Card(CardSuit.CLUBS, CardRank.EIGHT),
            new Card(CardSuit.CLUBS, CardRank.NINE), new Card(CardSuit.CLUBS, CardRank.TEN),
            new Card(CardSuit.CLUBS, CardRank.JACK), new Card(CardSuit.CLUBS, CardRank.QUEEN),
            new Card(CardSuit.CLUBS, CardRank.KING), new Card(CardSuit.CLUBS, CardRank.ACE)
        );
        assertTrue(HandValidator.laToiTrang(cards));
    }
}
