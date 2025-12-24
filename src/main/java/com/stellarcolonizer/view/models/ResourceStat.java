package com.stellarcolonizer.view.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;

public class ResourceStat {
    private final StringProperty name;
    private final SimpleDoubleProperty production;
    private final SimpleDoubleProperty consumption;
    private final SimpleDoubleProperty net;
    private final SimpleDoubleProperty stockpile;

    public ResourceStat(String name, float production, float consumption, float net, float stockpile) {
        this.name = new SimpleStringProperty(name);
        this.production = new SimpleDoubleProperty(production);
        this.consumption = new SimpleDoubleProperty(consumption);
        this.net = new SimpleDoubleProperty(net);
        this.stockpile = new SimpleDoubleProperty(stockpile);
    }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public double getProduction() { return production.get(); }
    public void setProduction(double production) { this.production.set(production); }
    public SimpleDoubleProperty productionProperty() { return production; }

    public double getConsumption() { return consumption.get(); }
    public void setConsumption(double consumption) { this.consumption.set(consumption); }
    public SimpleDoubleProperty consumptionProperty() { return consumption; }

    public double getNet() { return net.get(); }
    public void setNet(double net) { this.net.set(net); }
    public SimpleDoubleProperty netProperty() { return net; }

    public double getStockpile() { return stockpile.get(); }
    public void setStockpile(double stockpile) { this.stockpile.set(stockpile); }
    public SimpleDoubleProperty stockpileProperty() { return stockpile; }
}