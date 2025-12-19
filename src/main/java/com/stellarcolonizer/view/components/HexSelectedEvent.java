package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.galaxy.Hex;

// 六边形选择事件
public class HexSelectedEvent extends javafx.event.Event {
    public static final javafx.event.EventType<HexSelectedEvent> HEX_SELECTED =
            new javafx.event.EventType<>(javafx.event.Event.ANY, "HEX_SELECTED");

    private final Hex selectedHex;

    public HexSelectedEvent(javafx.event.EventType<? extends javafx.event.Event> eventType, Hex selectedHex) {
        super(eventType);
        this.selectedHex = selectedHex;
    }

    public Hex getSelectedHex() {
        return selectedHex;
    }
}