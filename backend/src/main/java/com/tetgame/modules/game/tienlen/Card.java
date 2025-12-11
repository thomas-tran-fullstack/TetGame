package com.tetgame.modules.game.tienlen;

public record Card(CardSuit suit, CardRank rank) {
    @Override
    public String toString() {
        return rank.name() + "_of_" + suit.name();
    }
}
