package com.stellarcolonizer.model.service.event;

public class GameEvent {
    private final String type;
    private final String message;
    private final long timestamp;

    public GameEvent(String type, String message) {
        this.type = type;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public GameEvent(String type) {
        this(type, "");
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
