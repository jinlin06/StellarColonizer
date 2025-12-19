// StarSystem.java - 恒星系
package com.stellarcolonizer.model.galaxy;

import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.colony.Colony;
import com.stellarcolonizer.model.galaxy.enums.StarType;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class StarSystem {

    private String name;
    private StarType starType;
    private List<Planet> planets;
    private Point2D position; // 在六边形内的相对位置
    private float habitability; // 总体宜居度 0-1

    public StarSystem(String name, com.stellarcolonizer.model.galaxy.enums.StarType starType) {
        this.name = name;
        this.starType = starType;
        this.planets = new ArrayList<>();
        this.habitability = calculateHabitability();
    }

    public void addPlanet(Planet planet) {
        planets.add(planet);
        planet.setStarSystem(this);
        habitability = calculateHabitability();
    }

    private float calculateHabitability() {
        if (planets.isEmpty()) return 0;

        float total = 0;
        for (Planet planet : planets) {
            total += planet.getHabitability();
        }
        return total / planets.size();
    }

    public List<Planet> getHabitablePlanets() {
        return planets.stream()
                .filter(p -> p.getHabitability() > 0.3f)
                .toList();
    }

    public List<Planet> getColonizablePlanets() {
        return planets.stream()
                .filter(p -> p.getType().isColonizable())
                .filter(p -> p.getColony() == null)
                .toList();
    }

    public boolean hasColony(com.stellarcolonizer.model.faction.Faction faction) {
        return planets.stream()
                .anyMatch(p -> {
                    com.stellarcolonizer.model.colony.Colony colony = p.getColony();
                    return colony != null && colony.getFaction().equals(faction);
                });
    }

    // Getter 和 Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public StarType getStarType() { return starType; }
    public void setStarType(com.stellarcolonizer.model.galaxy.enums.StarType starType) { this.starType = starType; }

    public List<Planet> getPlanets() { return new ArrayList<>(planets); }

    public Point2D getPosition() { return position; }
    public void setPosition(Point2D position) { this.position = position; }

    public float getHabitability() { return habitability; }
}


