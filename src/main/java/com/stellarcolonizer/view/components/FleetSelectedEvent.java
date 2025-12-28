package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.fleet.Fleet;
import javafx.event.Event;
import javafx.event.EventType;

public class FleetSelectedEvent extends Event {
    public static final EventType<FleetSelectedEvent> FLEET_SELECTED = 
        new EventType<>("FLEET_SELECTED");
    
    private final Fleet selectedFleet;
    
    public FleetSelectedEvent(EventType<? extends FleetSelectedEvent> eventType, Fleet selectedFleet) {
        super(eventType);
        this.selectedFleet = selectedFleet;
    }
    
    public Fleet getSelectedFleet() {
        return selectedFleet;
    }
}