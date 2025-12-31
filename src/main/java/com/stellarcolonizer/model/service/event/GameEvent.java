package com.stellarcolonizer.model.service.event;

public class GameEvent {
    private final String type;
    private final Object data;

    public GameEvent(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
    
    public String getMessage() {
        return data != null ? data.toString() : "";
    }
}