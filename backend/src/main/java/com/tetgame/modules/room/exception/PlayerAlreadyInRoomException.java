package com.tetgame.modules.room.exception;

public class PlayerAlreadyInRoomException extends RuntimeException {
    public PlayerAlreadyInRoomException(String message) {
        super(message);
    }
}
