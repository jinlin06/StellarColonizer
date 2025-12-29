package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.fleet.Fleet;

@FunctionalInterface
public interface FleetSelectionCallback {
    void onFleetSelected(Fleet fleet);
}