package com.stellarcolonizer.model.galaxy;

import com.stellarcolonizer.model.faction.Faction;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Galaxy {

    private String name;
    private HexGrid hexGrid;
    private List<StarSystem> starSystems;
    private List<Faction> factions;

    public Galaxy() {
        this.starSystems = new ArrayList<>();
        this.factions = new ArrayList<>();
        this.name = generateGalaxyName();
    }

    private String generateGalaxyName() {
        String[] prefixes = {"螺旋", "椭圆", "棒旋", "不规则", "矮", "透镜状"};
        String[] suffixes = {"星云", "星系", "星团", "星域", "旋臂", "星区"};
        String[] names = {"仙女座", "猎户座", "英仙座", "半人马座", "大麦哲伦", "小麦哲伦"};

        Random random = new Random();
        int type = random.nextInt(3);

        switch (type) {
            case 0:
                return prefixes[random.nextInt(prefixes.length)] +
                        suffixes[random.nextInt(suffixes.length)];
            case 1:
                return names[random.nextInt(names.length)] +
                        suffixes[random.nextInt(suffixes.length)];
            case 2:
                return "NGC " + (random.nextInt(9999) + 1000);
            default:
                return "未命名星系";
        }
    }

    public void addStarSystem(StarSystem system) {
        starSystems.add(system);
    }

    public void removeStarSystem(StarSystem system) {
        starSystems.remove(system);
    }

    public StarSystem findStarSystem(String name) {
        return starSystems.stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<StarSystem> getStarSystemsInRange(Point2D center, double range) {
        List<StarSystem> result = new ArrayList<>();
        for (StarSystem system : starSystems) {
            // 需要实现距离计算
            // 这里简化处理
            result.add(system);
        }
        return result;
    }

    public int getColonizedPlanetCount() {
        return (int) starSystems.stream()
                .flatMap(s -> s.getPlanets().stream())
                .filter(p -> p.getColony() != null)
                .count();
    }

    public int getTotalPlanetCount() {
        return starSystems.stream()
                .mapToInt(s -> s.getPlanets().size())
                .sum();
    }

    // Getter 和 Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public HexGrid getHexGrid() { return hexGrid; }
    public void setHexGrid(HexGrid hexGrid) { this.hexGrid = hexGrid; }

    public List<StarSystem> getStarSystems() { return new ArrayList<>(starSystems); }

    public List<Faction> getFactions() { return new ArrayList<>(factions); }
    public void addFaction(Faction faction) { factions.add(faction); }
    public void removeFaction(Faction faction) { factions.remove(faction); }
}