// Hex.java - 六边形单元
package com.stellarcolonizer.model.galaxy;

import com.stellarcolonizer.model.faction.Faction;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.List;

public class Hex {

    private CubeCoord coord;
    private HexType type;
    private StarSystem starSystem; // 如果该六边形有星系
    private List<Entity> entities;
    private float visibility; // 0-1，战争迷雾
    private Faction exploredBy; // 探索过的派系

    public Hex(CubeCoord coord) {
        this.coord = coord;
        this.type = HexType.EMPTY;
        this.entities = new ArrayList<>();
        this.visibility = 0.0f;
    }

    public boolean hasStarSystem() {
        return starSystem != null;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
        entity.setCurrentHex(this);
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
        entity.setCurrentHex(null);
    }

    public boolean containsFleet(Faction faction) {
        return entities.stream()
                .filter(e -> e instanceof Fleet)
                .map(e -> (Fleet) e)
                .anyMatch(f -> f.getFaction().equals(faction));
    }

    public List<Fleet> getFleets() {
        return entities.stream()
                .filter(e -> e instanceof Fleet)
                .map(e -> (Fleet) e)
                .toList();
    }

    public void updateVisibility(Faction faction, float sensorStrength) {
        // 计算该派系对这个六边形的可见度
        // 基于距离、传感器科技、是否有单位等
        float newVisibility = calculateVisibility(faction, sensorStrength);
        this.visibility = Math.max(this.visibility, newVisibility);

        if (newVisibility > 0.5f && exploredBy == null) {
            exploredBy = faction;
        }
    }

    private float calculateVisibility(Faction faction, float sensorStrength) {
        // 简化版：如果六边形内有己方单位，完全可见
        if (containsFleet(faction)) {
            return 1.0f;
        }

        // 否则基于传感器范围和障碍物计算
        float distanceFactor = 1.0f / (1 + getDistanceToNearestFriendly(faction));
        return Math.min(1.0f, sensorStrength * distanceFactor);
    }

    private int getDistanceToNearestFriendly(Faction faction) {
        // 寻找最近的友方单位距离
        // 这里简化处理
        return 10; // 默认值
    }

    // Getter 和 Setter
    public CubeCoord getCoord() { return coord; }

    public HexType getType() { return type; }
    public void setType(HexType type) { this.type = type; }

    public StarSystem getStarSystem() { return starSystem; }
    public void setStarSystem(StarSystem starSystem) {
        this.starSystem = starSystem;
        if (starSystem != null) {
            this.type = HexType.STAR_SYSTEM;
        }
    }

    public List<Entity> getEntities() { return new ArrayList<>(entities); }

    public float getVisibility() { return visibility; }
    public void setVisibility(float visibility) { this.visibility = visibility; }

    public Faction getExploredBy() { return exploredBy; }
    public void setExploredBy(Faction exploredBy) { this.exploredBy = exploredBy; }

    public boolean isExplored() { return exploredBy != null; }
}

