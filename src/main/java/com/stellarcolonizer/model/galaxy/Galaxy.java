package com.stellarcolonizer.model.galaxy;

import com.stellarcolonizer.model.faction.Faction;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;

public class Galaxy {

    private String name;
    private HexGrid hexGrid;
    private List<StarSystem> starSystems;
    private List<Faction> factions;
    
    // 存储星系之间的连接路径
    private Map<StarSystem, List<StarSystem>> starSystemConnections;

    public Galaxy() {
        this.starSystems = new ArrayList<>();
        this.factions = new ArrayList<>();
        this.starSystemConnections = new HashMap<>();
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
    
    /**
     * 创建连接标识符以避免重复（使用坐标排序确保一致性）
     */
    private String createConnectionId(Hex hex1, Hex hex2) {
        // 使用坐标排序确保一致性
        CubeCoord coord1 = hex1.getCoord();
        CubeCoord coord2 = hex2.getCoord();
        
        // 创建坐标字符串
        String coordStr1 = coord1.q + "," + coord1.r + "," + coord1.s;
        String coordStr2 = coord2.q + "," + coord2.r + "," + coord2.s;
        
        // 排序确保一致性
        return coordStr1.compareTo(coordStr2) < 0 ? 
            coordStr1 + "|" + coordStr2 : coordStr2 + "|" + coordStr1;
    }
    
    /**
     * 生成星系之间的随机连接路径
     * 确保所有星系都在同一个连通分量中，每个星系至少可以通过一个其他星系到达
     * 同时也为没有星系的六边形创建连接路径
     */
    public void generateStarSystemConnections() {
        starSystemConnections.clear();
        
        if (starSystems.size() <= 1) {
            return; // 没有足够星系来创建连接
        }
        
        Random random = new Random();
        
        // 确保图是连通的 - 使用Prim算法构建最小生成树
        Set<StarSystem> inTree = new HashSet<>();
        List<StarSystem> remaining = new ArrayList<>(starSystems);
        
        // 随机选择起始节点
        StarSystem startNode = remaining.remove(random.nextInt(remaining.size()));
        inTree.add(startNode);
        
        // 构建最小生成树直到所有节点都加入
        while (!remaining.isEmpty()) {
            // 寻找连接已加入树的节点和未加入树的节点之间的最短边
            double minDistance = Double.MAX_VALUE;
            StarSystem bestFrom = null;
            StarSystem bestTo = null;
            
            for (StarSystem from : inTree) {
                Hex fromHex = getHexForStarSystem(from);
                if (fromHex == null) continue;
                
                for (StarSystem to : remaining) {
                    Hex toHex = getHexForStarSystem(to);
                    if (toHex == null) continue;
                    
                    double distance = calculateHexDistance(fromHex.getCoord(), toHex.getCoord());
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestFrom = from;
                        bestTo = to;
                    }
                }
            }
            
            // 添加找到的最佳边
            if (bestFrom != null && bestTo != null) {
                addConnection(bestFrom, bestTo);
                inTree.add(bestTo);
                remaining.remove(bestTo);
            } else {
                // 如果找不到合适的连接（理论上不应该发生），强制连接一个随机节点
                StarSystem to = remaining.remove(random.nextInt(remaining.size()));
                StarSystem from = new ArrayList<>(inTree).get(random.nextInt(inTree.size()));
                addConnection(from, to);
                inTree.add(to);
            }
        }
        
        // 添加额外的连接以增加连通性（大多数星系连接2-4个邻居）
        for (StarSystem system : starSystems) {
            // 获取当前星系所在的六边形
            Hex currentHex = getHexForStarSystem(system);
            if (currentHex == null) continue;
            
            // 获取相邻的六边形
            List<Hex> neighborHexes = hexGrid.getNeighbors(currentHex);
            
            // 找到相邻六边形中的星系
            List<StarSystem> nearbySystems = new ArrayList<>();
            for (Hex hex : neighborHexes) {
                if (hex.hasStarSystem() && hex.getStarSystem() != system) {
                    nearbySystems.add(hex.getStarSystem());
                }
            }
            
            // 随机打乱邻居列表
            Collections.shuffle(nearbySystems, random);
            
            // 为每个星系添加2-4个额外连接（除非已经连接）
            int extraConnections = 2 + random.nextInt(3); // 2, 3, 或 4
            int addedConnections = 0;
            
            for (StarSystem nearbySystem : nearbySystems) {
                if (addedConnections >= extraConnections) {
                    break;
                }
                
                if (!areSystemsConnected(system, nearbySystem)) {
                    addConnection(system, nearbySystem);
                    addedConnections++;
                }
            }
        }
        
        // 为所有相邻的六边形（包括空的）创建连接，但只连接一部分以确保约20%没有连线
        Map<Hex, Set<Hex>> hexConnections = new HashMap<>();
        
        // 先为所有六边形添加邻接关系
        for (Hex hex : hexGrid.getAllHexes()) {
            List<Hex> neighbors = hexGrid.getNeighbors(hex);
            hexConnections.put(hex, new HashSet<>(neighbors));
        }
        
        // 随机移除约10%的连接（保留90%的连接）
        List<Hex> allHexes = new ArrayList<>(hexGrid.getAllHexes());
        int totalPossibleConnections = 0;
        for (Hex hex : allHexes) {
            totalPossibleConnections += hexConnections.get(hex).size();
        }
        
        // 计算需要移除的连接数（约10%）
        int connectionsToRemove = (int) (totalPossibleConnections * 0.1);
        
        for (int i = 0; i < connectionsToRemove; i++) {
            // 随机选择一个六边形
            Hex fromHex = allHexes.get(random.nextInt(allHexes.size()));
            Set<Hex> neighbors = hexConnections.get(fromHex);
            
            if (!neighbors.isEmpty()) {
                // 随机选择一个邻居
                List<Hex> neighborList = new ArrayList<>(neighbors);
                Hex toHex = neighborList.get(random.nextInt(neighborList.size()));
                
                // 移除双向连接
                hexConnections.get(fromHex).remove(toHex);
                hexConnections.get(toHex).remove(fromHex);
            }
        }
        
        // 将这个信息存储起来供视图使用
        this.hexConnections = hexConnections;
    }
    
    /**
     * 获取六边形的唯一标识符
     */
    private String getHexIdentifier(Hex hex) {
        CubeCoord coord = hex.getCoord();
        return coord.q + "," + coord.r + "," + coord.s;
    }
    
    // 添加字段来存储六边形连接
    private Map<Hex, Set<Hex>> hexConnections = new HashMap<>();
    
    /**
     * 获取六边形连接信息
     */
    public Map<Hex, Set<Hex>> getHexConnections() {
        return hexConnections;
    }
    
    /**
     * 计算两个六边形坐标之间的距离
     */
    private double calculateHexDistance(CubeCoord a, CubeCoord b) {
        return (Math.abs(a.q - b.q) + Math.abs(a.r - b.r) + Math.abs(a.s - b.s)) / 2.0;
    }
    
    /**
     * 添加两个星系之间的连接（确保不会重复添加）
     */
    private void addConnection(StarSystem from, StarSystem to) {
        // 检查是否已经存在连接
        List<StarSystem> fromConnections = starSystemConnections.computeIfAbsent(from, k -> new ArrayList<>());
        List<StarSystem> toConnections = starSystemConnections.computeIfAbsent(to, k -> new ArrayList<>());
        
        // 只添加不存在的连接
        if (!fromConnections.contains(to)) {
            fromConnections.add(to);
        }
        
        if (!toConnections.contains(from)) {
            toConnections.add(from);
        }
    }
    
    /**
     * 检查两个星系之间是否有直接连接（任一方向）
     */
    public boolean areSystemsConnected(StarSystem from, StarSystem to) {
        List<StarSystem> connections = starSystemConnections.get(from);
        return connections != null && connections.contains(to);
    }
    
    /**
     * 获取指定星系所在的六边形
     */
    public Hex getHexForStarSystem(StarSystem system) {
        for (Hex hex : hexGrid.getAllHexes()) {
            if (hex.hasStarSystem() && hex.getStarSystem() == system) {
                return hex;
            }
        }
        return null;
    }
    
    /**
     * 获取星系的所有直接连接
     */
    public List<StarSystem> getConnectedSystems(StarSystem system) {
        return starSystemConnections.getOrDefault(system, new ArrayList<>());
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

    private static class Edge implements Comparable<Edge> {
        StarSystem from;
        StarSystem to;
        double weight;
        
        Edge(StarSystem from, StarSystem to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
        
        @Override
        public int compareTo(Edge other) {
            return Double.compare(this.weight, other.weight);
        }
    }
}