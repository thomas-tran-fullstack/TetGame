package com.tetgame.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MessagePayload<T> {
    private String type;      // lobby.user-join, room.message, game.turn-start, etc.
    private String sender;    // username
    private long timestamp;
    private T data;           // Flexible data payload

    public MessagePayload(String type, String sender, T data) {
        this.type = type;
        this.sender = sender;
        this.timestamp = System.currentTimeMillis();
        this.data = data;
    }
}
