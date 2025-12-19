// GalaxyGenerator.java - 星系生成器
package com.stellarcolonizer.model.galaxy;

import com.stellarcolonizer.model.galaxy.enums.HexType;
import com.stellarcolonizer.model.galaxy.enums.PlanetType;
import com.stellarcolonizer.model.galaxy.enums.StarType;

import javafx.geometry.Point2D;

import java.util.*;

import static com.stellarcolonizer.model.galaxy.enums.HexType.EMPTY;

public class GalaxyGenerator {

    private Random random;
    private NameGenerator nameGenerator;

    public GalaxyGenerator() {
        this.random = new Random();
        this.nameGenerator = new NameGenerator();
    }

    public Galaxy generateGalaxy(int starCount) {
        System.out.println("生成星系，包含 " + starCount + " 个恒星系...");

        Galaxy galaxy = new Galaxy();

        // 生成六边形网格
        int radius = calculateGridRadius(starCount);
        HexGrid hexGrid = new HexGrid(radius, 50.0); // 六边形大小50像素
        galaxy.setHexGrid(hexGrid);

        // 获取所有可用的六边形
        List<Hex> availableHexes = new ArrayList<>(hexGrid.getAllHexes());
        Collections.shuffle(availableHexes, random);

        // 生成恒星系
        int placedStars = 0;
        for (int i = 0; i < Math.min(starCount, availableHexes.size()); i++) {
            Hex hex = availableHexes.get(i);
            StarSystem starSystem = generateStarSystem();
            hex.setStarSystem(starSystem);

            // 在六边形内随机位置放置星系
            double x = random.nextDouble() * 0.6 - 0.3; // -0.3 到 0.3
            double y = random.nextDouble() * 0.6 - 0.3;
            starSystem.setPosition(new Point2D(x, y));

            galaxy.addStarSystem(starSystem);
            placedStars++;
        }

        System.out.println("成功生成 " + placedStars + " 个恒星系");

        // 生成星云和小行星带
        generateNebulas(galaxy, placedStars / 4);
        generateAsteroidFields(galaxy, placedStars / 5);

        return galaxy;
    }

    private StarSystem generateStarSystem() {
        // 随机选择恒星类型（加权）
        StarType starType = getRandomStarType();

        // 生成星系名
        String name = nameGenerator.generateStarSystemName();

        StarSystem system = new StarSystem(name, starType);

        // 生成行星数量（1-8颗）
        int planetCount = 1 + random.nextInt(8);

        for (int i = 0; i < planetCount; i++) {
            Planet planet = generatePlanet(system, i);
            system.addPlanet(planet);
        }

        return system;
    }

    private Planet generatePlanet(StarSystem system, int orbitIndex) {
        // 轨道距离（基于轨道序号）
        float orbitDistance = 0.2f + orbitIndex * 0.3f + random.nextFloat() * 0.2f;

        // 选择行星类型（基于轨道距离和恒星类型）
        PlanetType planetType = selectPlanetType(system.getStarType(), orbitDistance);

        // 行星大小（1-10）
        int size = 3 + random.nextInt(8);

        // 行星名
        String name = nameGenerator.generatePlanetName(system.getName());

        Planet planet = new Planet(name, planetType, size, orbitDistance);
        planet.setOrbitIndex(orbitIndex);

        return planet;
    }

    private StarType getRandomStarType() {
        // 权重分布：M > K > G > F > A > B > O
        double roll = random.nextDouble();
        if (roll < 0.40) return StarType.M;     // 40%
        else if (roll < 0.65) return StarType.K; // 25%
        else if (roll < 0.80) return StarType.G; // 15%
        else if (roll < 0.90) return StarType.F; // 10%
        else if (roll < 0.96) return StarType.A; // 6%
        else if (roll < 0.99) return StarType.B; // 3%
        else return StarType.O;                 // 1%
    }

    private PlanetType selectPlanetType(StarType starType, float orbitDistance) {
        // 基于恒星类型和轨道距离选择行星类型
        float temperature = calculateTemperature(starType, orbitDistance);

        // 温度带决定行星类型
        if (temperature > 1.2f) {
            // 热区
            return random.nextFloat() < 0.7f ? PlanetType.LAVA : PlanetType.DESERT;
        } else if (temperature > 0.8f && temperature <= 1.2f) {
            // 宜居带
            double roll = random.nextDouble();
            if (roll < 0.3) return PlanetType.TERRA;
            else if (roll < 0.5) return PlanetType.ARID;
            else if (roll < 0.7) return PlanetType.OCEAN;
            else if (roll < 0.85) return PlanetType.JUNGLE;
            else if (roll < 0.95) return PlanetType.DESERT;
            else return PlanetType.LAVA;
        } else if (temperature > 0.4f) {
            // 温带
            return random.nextFloat() < 0.6f ? PlanetType.TUNDRA : PlanetType.ICE;
        } else {
            // 冷区或气态巨行星区
            if (orbitDistance > 2.0f && random.nextFloat() < 0.7f) {
                return PlanetType.GAS_GIANT;
            } else {
                return random.nextFloat() < 0.5f ? PlanetType.ICE : PlanetType.BARREN;
            }
        }
    }

    private float calculateTemperature(StarType starType, float orbitDistance) {
        // 简化温度计算
        float baseTemp = 1.0f / starType.getHabitabilityMultiplier(); // 恒星温度倒数为基数
        return baseTemp / (orbitDistance * orbitDistance);
    }

    private void generateNebulas(Galaxy galaxy, int count) {
        HexGrid grid = galaxy.getHexGrid();
        List<Hex> hexes = grid.getAllHexes().stream()
                .filter(h -> h.getType() == EMPTY && h.getStarSystem() == null)
                .toList();

        Collections.shuffle(hexes, random);

        for (int i = 0; i < Math.min(count, hexes.size()); i++) {
            hexes.get(i).setType(HexType.NEBULA);
        }
    }

    private void generateAsteroidFields(Galaxy galaxy, int count) {
        HexGrid grid = galaxy.getHexGrid();
        List<Hex> hexes = grid.getAllHexes().stream()
                .filter(h -> h.getType() == EMPTY && h.getStarSystem() == null)
                .toList();

        Collections.shuffle(hexes, random);

        for (int i = 0; i < Math.min(count, hexes.size()); i++) {
            hexes.get(i).setType(HexType.ASTEROID_FIELD);
        }
    }

    private int calculateGridRadius(int starCount) {
        // 估算需要的网格大小
        // 每个六边形平均包含0.75个单元（密集包装）
        int hexesNeeded = (int) (starCount / 0.75 * 1.5); // 加上50%空余
        return (int) Math.sqrt(hexesNeeded / Math.PI) + 2;
    }
}

