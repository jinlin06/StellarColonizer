package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.fleet.Fleet;
import javafx.event.Event;
import javafx.event.EventType;

import java.util.List;

public class FleetListSelectedEvent extends Event {
    public static final EventType<FleetListSelectedEvent> FLEET_LIST_SELECTED = 
        new EventType<>("FLEET_LIST_SELECTED");
    
    private final List<Fleet> fleetList;
    private final Object source;
    
    public FleetListSelectedEvent(EventType<? extends FleetListSelectedEvent> eventType, 
                                  List<Fleet> fleetList, Object source) {
        super(eventType);
        this.fleetList = fleetList;
        this.source = source;
    }
    
    public List<Fleet> getFleetList() {
        return fleetList;
    }
    
    public Object getSource() {
        return source;
    }
}