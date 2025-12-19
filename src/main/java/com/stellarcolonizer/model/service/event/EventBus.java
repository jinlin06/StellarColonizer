package com.stellarcolonizer.model.service.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private final List<GameEventListener> listeners = new CopyOnWriteArrayList<>();

    public void register(GameEventListener listener) {
        listeners.add(listener);
    }

    public void unregister(GameEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
