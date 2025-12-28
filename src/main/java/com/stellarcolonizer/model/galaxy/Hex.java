// Hex.java - 六边形单元
package com.stellarcolonizer.model.galaxy;

import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.fleet.Fleet;
import com.stellarcolonizer.model.galaxy.enums.HexType;

import java.util.ArrayList;
import java.util.List;

public class Hex {

    private CubeCoord coord;
    private HexType type;
    private StarSystem starSystem; // 如果该六边形有星系
    private List<Fleet> entities;
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

    public void addEntity(Fleet entity) {
        entities.add(entity);
        entity.setCurrentHex(this); // 设置舰队位置
    }

    public void removeEntity(Fleet entity) {
        entities.remove(entity);
        // 不再设置为null，保持舰队的最后位置信息
    }

    public boolean containsFleet(Faction faction) {
        return entities.stream()
                .anyMatch(f -> f.getFaction().equals(faction));
    }

    public List<Fleet> getFleets() {
        return new ArrayList<>(entities);
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

    public List<Fleet> getEntities() { return new ArrayList<>(entities); }

    public float getVisibility() { return visibility; }
    public void setVisibility(float visibility) { this.visibility = visibility; }

    public Faction getExploredBy() { return exploredBy; }
    public void setExploredBy(Faction exploredBy) { this.exploredBy = exploredBy; }

    public boolean isExplored() { return exploredBy != null; }
}

