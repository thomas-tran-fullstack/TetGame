package com.tetgame.modules.game.tienlen;

public enum CardRank {
    THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10),
    JACK(11), QUEEN(12), KING(13), ACE(14), TWO(15);

    private final int value;

    CardRank(int value) { this.value = value; }

    public int getValue() { return value; }
}
