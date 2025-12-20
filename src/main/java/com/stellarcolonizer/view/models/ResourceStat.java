package com.stellarcolonizer.view.models;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ResourceStat {
    private final StringProperty name;
    private final FloatProperty production;
    private final FloatProperty consumption;
    private final FloatProperty net;
    private final FloatProperty stockpile;

    public ResourceStat(String name, float production, float consumption, float net, float stockpile) {
        this.name = new SimpleStringProperty(name);
        this.production = new SimpleFloatProperty(production);
        this.consumption = new SimpleFloatProperty(consumption);
        this.net = new SimpleFloatProperty(net);
        this.stockpile = new SimpleFloatProperty(stockpile);
    }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public float getProduction() { return production.get(); }
    public FloatProperty productionProperty() { return production; }

    public float getConsumption() { return consumption.get(); }
    public FloatProperty consumptionProperty() { return consumption; }

    public float getNet() { return net.get(); }
    public FloatProperty netProperty() { return net; }

    public float getStockpile() { return stockpile.get(); }
    public FloatProperty stockpileProperty() { return stockpile; }
}
