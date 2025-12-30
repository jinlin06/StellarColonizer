# Stellar Colonizer 项目算法详细介绍

## 概述

Stellar Colonizer 是一款基于 JavaFX 的 4X 策略游戏，玩家在广阔的宇宙中建立和发展自己的星际文明。该项目使用了多种算法来处理游戏中的各种系统，包括六边形网格系统、银河系生成、路径查找、科技树管理、资源管理等。

## 1. 六边形网格算法

### 1.1 六边形坐标系统

游戏使用立方坐标系统（Cube Coordinates）来表示六边形网格，这是处理六边形网格最有效的系统之一。

- **坐标表示**: 使用 `(q, r, s)` 坐标，其中 `q + r + s = 0`
- **优点**: 简化了距离计算、邻居查找等操作

**CubeCoord.java 代码分析**:

```java
public class CubeCoord {
    public final int q;
    public final int r;
    public final int s;

    public CubeCoord(int q, int r, int s) {
        this.q = q;
        this.r = r;
        this.s = s;
    }

    // 坐标相加
    public CubeCoord add(CubeCoord other) {
        return new CubeCoord(q + other.q, r + other.r, s + other.s);
    }

    // 坐标相减
    public CubeCoord subtract(CubeCoord other) {
        return new CubeCoord(q - other.q, r - other.r, s - other.s);
    }

    // 计算到另一个坐标的距离
    public double distance(CubeCoord other) {
        return subtract(other).length();
    }

    // 计算坐标到原点的距离
    public double length() {
        return (Math.abs(q) + Math.abs(r) + Math.abs(s)) / 2.0;
    }
}
```

**算法解释**:
- `add()` 和 `subtract()` 方法用于在六边形网格上进行位置运算
- `distance()` 方法计算两个六边形之间的最短距离
- `length()` 方法计算坐标到原点的六边形距离，使用公式 `(abs(q) + abs(r) + abs(s)) / 2`

### 1.2 六边形操作算法

#### 邻居查找算法

```java
public static final CubeCoord[] CUBE_DIRECTIONS = {
    new CubeCoord(1, -1, 0), new CubeCoord(1, 0, -1), new CubeCoord(0, 1, -1),
    new CubeCoord(-1, 1, 0), new CubeCoord(-1, 0, 1), new CubeCoord(0, -1, 1)
};

public List<Hex> getNeighbors(Hex hex) {
    List<Hex> neighbors = new ArrayList<>();
    CubeCoord coord = hex.getCoord();

    for (CubeCoord dir : CUBE_DIRECTIONS) {
        CubeCoord neighborCoord = coord.add(dir);
        Hex neighbor = hexMap.get(neighborCoord);
        if (neighbor != null) {
            neighbors.add(neighbor);
        }
    }

    return neighbors;
}
```

**算法解释**:
- 定义了六个方向向量，分别对应六边形的六个邻居方向
- `getNeighbors()` 方法通过将中心六边形坐标与六个方向向量相加来找到所有邻居
- 使用哈希表快速查找邻居六边形是否存在

#### 范围查找算法

```java
public List<Hex> getHexesInRange(Hex center, int range) {
    List<Hex> results = new ArrayList<>();
    for (int dx = -range; dx <= range; dx++) {
        for (int dy = Math.max(-range, -dx - range); dy <= Math.min(range, -dx + range); dy++) {
            int dz = -dx - dy;
            CubeCoord coord = center.getCoord().add(new CubeCoord(dx, dy, dz));
            Hex hex = hexMap.get(coord);
            if (hex != null) {
                results.add(hex);
            }
        }
    }
    return results;
}
```

**算法解释**:
- 这个算法找到距离中心六边形在指定范围内的所有六边形
- 使用立方坐标系统，范围内的六边形满足 `abs(dx) + abs(dy) + abs(dz) <= 2 * range` 的条件
- 通过遍历所有可能的偏移量来找到范围内的六边形

### 1.3 像素与六边形坐标转换

#### 像素转六边形坐标

```java
public Point2D cubeToPixel(CubeCoord coord) {
    double spacingFactor = 1.2; // 20%的间隙
    double x = hexSize * spacingFactor * (Math.sqrt(3) * coord.q + Math.sqrt(3)/2 * coord.r);
    double y = hexSize * spacingFactor * (3.0/2 * coord.r);
    return new Point2D(x, y);
}

public CubeCoord pixelToCube(double x, double y) {
    double spacingFactor = 1.2;
    double q = (Math.sqrt(3)/3 * x - 1.0/3 * y) / (hexSize * spacingFactor);
    double r = (2.0/3 * y) / (hexSize * spacingFactor);
    return roundCube(q, r, -q - r);
}

private CubeCoord roundCube(double q, double r, double s) {
    int rq = (int) Math.round(q);
    int rr = (int) Math.round(r);
    int rs = (int) Math.round(s);

    double qDiff = Math.abs(rq - q);
    double rDiff = Math.abs(rr - r);
    double sDiff = Math.abs(rs - s);

    if (qDiff > rDiff && qDiff > sDiff) {
        rq = -rr - rs;
    } else if (rDiff > sDiff) {
        rr = -rq - rs;
    } else {
        rs = -rq - rr;
    }

    return new CubeCoord(rq, rr, rs);
}
```

**算法解释**:
- `cubeToPixel()`: 将立方坐标转换为屏幕像素坐标，使用六边形的几何特性
- `pixelToCube()`: 将屏幕像素坐标转换为立方坐标，使用逆向转换公式
- `roundCube()`: 处理浮点数精度问题，将浮点坐标四舍五入到最近的整数立方坐标
- 最后一个函数使用差值比较来修正由于浮点精度问题导致的坐标不满足 `q + r + s = 0` 约束的情况

## 2. 银河系生成算法

### 2.1 六边形网格生成

```java
private void generateGrid() {
    for (int q = -radius; q <= radius; q++) {
        int r1 = Math.max(-radius, -q - radius);
        int r2 = Math.min(radius, -q + radius);
        for (int r = r1; r <= r2; r++) {
            CubeCoord coord = new CubeCoord(q, r, -q - r);
            Hex hex = new Hex(coord);
            hexMap.put(coord, hex);
        }
    }
}
```

**算法解释**:
- 这个算法生成一个六边形网格，其中每个六边形的坐标满足立方坐标约束 `q + r + s = 0`
- `r1` 和 `r2` 确定了在给定 `q` 值下 `r` 的有效范围，以保持网格为六边形形状
- 生成的网格是六边形而不是矩形，这是通过限制 `r` 的范围来实现的

### 2.2 恒星系生成

```java
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
```

**算法解释**:
- `getRandomStarType()`: 使用加权随机算法模拟真实恒星分布，M型恒星最常见(40%)，O型恒星最罕见(1%)
- `selectPlanetType()`: 基于计算出的温度带选择行星类型，模拟真实行星形成过程
- `calculateTemperature()`: 使用平方反比定律计算行星温度，`T ∝ 1/d²`，其中d是轨道距离

### 2.3 银河系连接算法

```java
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
    // ... 其他连接逻辑
}

private double calculateHexDistance(CubeCoord a, CubeCoord b) {
    return (Math.abs(a.q - b.q) + Math.abs(a.r - b.r) + Math.abs(a.s - b.s)) / 2.0;
}
```

**算法解释**:
- 这是Prim算法的实现，用于创建连通的星系网络
- 算法确保所有星系都至少通过一条路径连接到网络中
- `calculateHexDistance()`: 使用六边形网格的距离公式计算星系间的距离
- 算法首先创建一个连通的最小生成树，然后添加额外连接以提高网络连通性

## 3. 路径查找算法

### 3.1 广度优先搜索 (BFS)

游戏使用广度优先搜索算法来计算舰队的可移动范围：

```java
private List<Hex> getReachableHexes(Hex startHex, int range) {
    List<Hex> reachableHexes = new ArrayList<>();
    
    // 使用广度优先搜索(BFS)找到范围内所有可到达的六边形
    Queue<Hex> queue = new LinkedList<>();
    Set<Hex> visited = new HashSet<>();
    Map<Hex, Integer> distances = new HashMap<>();
    
    queue.offer(startHex);
    visited.add(startHex);
    distances.put(startHex, 0);
    reachableHexes.add(startHex);
    
    while (!queue.isEmpty()) {
        Hex current = queue.poll();
        int currentDistance = distances.get(current);
        
        if (currentDistance >= range) {
            continue; // 如果已达到最大距离，不再扩展
        }
        
        // 检查所有邻居六边形
        List<Hex> neighbors = hexGrid.getNeighbors(current);
        for (Hex neighbor : neighbors) {
            // 检查银河系中是否有连接
            if (galaxy != null && !galaxy.getHexConnections().isEmpty()) {
                Set<Hex> connectedHexes = galaxy.getHexConnections().get(current);
                // 检查连接是否双向存在（确保两个六边形之间确实有连接）
                Set<Hex> neighborConnections = galaxy.getHexConnections().get(neighbor);
                boolean hasConnection = connectedHexes != null && connectedHexes.contains(neighbor);
                
                if (hasConnection && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    distances.put(neighbor, currentDistance + 1);
                    reachableHexes.add(neighbor);
                    queue.offer(neighbor);
                }
            } else {
                // 如果没有连接信息，假设所有邻居都可到达
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    distances.put(neighbor, currentDistance + 1);
                    reachableHexes.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
    }
    
    return reachableHexes;
}
```

**算法解释**:
- 这是标准的广度优先搜索(BFS)算法实现
- 使用队列存储待访问的六边形，使用集合跟踪已访问的六边形
- 使用距离映射来跟踪到达每个六边形的距离
- 考虑了银河系连接信息，只有有连接的六边形才能移动到
- 算法的时间复杂度是 O(V + E)，其中 V 是可达的六边形数量，E 是连接数量

### 3.2 舰队移动算法

```java
public boolean moveTo(Hex destination) {
    if (destination == null || destination.equals(currentHex.get())) return false;
    
    // 检查是否已经在此回合移动过
    if (movedThisTurn) {
        System.out.println("舰队 " + name.get() + " 本回合已移动过，无法再次移动");
        return false;
    }

    // 从当前六边形中移除舰队
    if (currentHex.get() != null) {
        currentHex.get().removeEntity(this);
    }
    
    this.destination.set(destination);
    
    // 立即更新舰队位置
    currentHex.set(destination);
    
    // 确保舰队被添加到新的六边形中
    if (destination != null) {
        destination.addEntity(this);
    }
    
    isMoving.set(false);
    currentMission.set(FleetMission.STANDBY);
    
    // 标记为已移动
    movedThisTurn = true;
    
    return true;
}

private int calculateFleetMoveRange(Fleet fleet) {
    if (fleet == null || fleet.getShips().isEmpty()) {
        return 1; // 默认移动范围
    }
    
    // 计算舰队中最高等级舰船的等级
    int highestTechLevel = fleet.getShips().stream()
        .mapToInt(ship -> ship.getDesign().getShipClass().getTechLevel())
        .max()
        .orElse(1);
    
    // 根据舰船等级确定移动范围
    if (highestTechLevel <= 3) {
        return 2; // 低级舰船可移动2格
    } else {
        return 1; // 高级舰船只能移动1格
    }
}
```

**算法解释**:
- `moveTo()`: 实现舰队移动逻辑，包括从旧位置移除、添加到新位置、设置移动标志
- `calculateFleetMoveRange()`: 基于舰队中最高等级舰船确定移动范围，低级舰船(≤3级)可移动2格，高级舰船(>3级)只能移动1格
- 使用回合制移动限制，每回合每支舰队只能移动一次

## 4. 科技树算法

### 4.1 科技层级计算

```java
private int calculateTechTier(Technology tech, Map<String, Technology> techMap, Set<String> visited) {
    // 避免循环依赖
    if (visited.contains(tech.getId())) {
        return 1; // 如果出现循环依赖，返回基础层级
    }

    // 如果没有前置科技，层级为1
    if (tech.getPrerequisites().isEmpty()) {
        return 1;
    }

    // 递归计算前置科技中的最高层级
    int maxPrereqTier = 0;
    visited.add(tech.getId());
    
    for (String prereqId : tech.getPrerequisites()) {
        Technology prereq = techMap.get(prereqId);
        if (prereq != null) {
            int prereqTier = calculateTechTier(prereq, techMap, visited);
            maxPrereqTier = Math.max(maxPrereqTier, prereqTier);
        }
    }
    
    visited.remove(tech.getId());
    return maxPrereqTier + 1;
}
```

**算法解释**:
- 这是递归算法，用于计算科技的层级
- 使用访问集合避免循环依赖导致的无限递归
- 科技层级等于其所有前置科技中的最高层级 + 1
- 时间复杂度在最坏情况下是 O(V + E)，其中 V 是科技数量，E 是依赖关系数量

### 4.2 科技研究进度算法

```java
public boolean progress(int points) {
    // 将当前回合的研究点数添加到累积进度中
    researchPoints.set(points); // 更新每回合投入的点数
    int newProgress = progress.get() + points;
    progress.set(newProgress);
    
    if (newProgress >= getTotalCost()) {
        // 研究完成
        technology.get().setResearched(true);
        return true;
    }
    
    return false;
}

public float getProgressPercentage() {
    if (getTotalCost() <= 0) return 0;
    return (float) getProgress() / getTotalCost() * 100;
}
```

**算法解释**:
- `progress()` 方法累加研究点数，当累积点数达到科技成本时，研究完成
- `getProgressPercentage()` 计算当前进度百分比，用于UI显示
- 算法简单但有效，支持中断和恢复研究

### 4.3 科技研究队列算法

```java
public void processResearch(int baseResearchPoints) {
    this.baseResearchPointsPerRound = baseResearchPoints; // 保存每回合的基础科研产出
    this.baseResearchPointsPerRoundProperty.set(baseResearchPoints); // 更新JavaFX属性
    currentResearchPoints.set(baseResearchPoints);

    if (researchQueue.isEmpty()) {
        return;
    }

    ResearchProject currentProject = researchQueue.get(0);
    // 使用所有可用的研究点数推进当前项目
    float effectivePoints = baseResearchPoints * researchSpeedBonus.get();
    int intEffectivePoints = Math.round(effectivePoints);

    boolean completed = currentProject.progress(intEffectivePoints);

    if (completed) {
        Technology completedTech = currentProject.getTechnology();
        researchQueue.remove(0);

        // 通知所有监听器科技已完成
        notifyResearchCompleted(completedTech.getId());

        if (!researchQueue.isEmpty()) {
            // 可以发送通知
        }
    }
}
```

**算法解释**:
- 只有队列中的第一个项目（当前研究项目）会获得研究点数
- 使用研究速度加成计算实际研究效率
- 当项目完成时，从队列中移除并触发完成事件
- 支持队列中的下一个项目自动开始研究

## 5. 资源管理算法

### 5.1 资源库存管理

```java
public class ResourceStockpile {
    private final Map<ResourceType, Float> resources;
    private final Map<ResourceType, Float> capacity; // 各种资源的容量

    public float getResource(ResourceType type) {
        return resources.getOrDefault(type, 0f);
    }

    public void addResource(ResourceType type, float amount) {
        float current = getResource(type);
        float maxCapacity = capacity.getOrDefault(type, Float.MAX_VALUE);

        float newAmount = current + amount;
        
        if (newAmount > maxCapacity) {
            newAmount = maxCapacity; // 超过容量部分丢弃
        }
        
        // 不允许资源为负数，最小为0
        if (newAmount < 0) {
            newAmount = 0;
        }

        resources.put(type, newAmount);
    }

    public boolean consumeResource(ResourceType type, float amount) {
        float current = getResource(type);

        if (current >= amount) {
            resources.put(type, current - amount);
            return true;
        }
        return false;
    }
}
```

**算法解释**:
- 使用枚举映射存储不同类型的资源
- `addResource()`: 添加资源但不超过容量限制
- `consumeResource()`: 消耗资源，只有在资源足够时才执行，返回操作是否成功
- 防止资源数量为负数

### 5.2 资源价值计算

```java
private float getResourceValue(ResourceType type) {
    // 基础价值（可以调整）
    switch (type) {
        case ENERGY: return 1.0f;
        case METAL: return 2.0f;
        case FOOD: return 1.5f;
        case SCIENCE: return 5.0f;
        case EXOTIC_MATTER: return 50.0f;
        case NEUTRONIUM: return 100.0f;
        case CRYSTAL: return 30.0f;
        case DARK_MATTER: return 200.0f;
        case ANTI_MATTER: return 150.0f;
        case LIVING_METAL: return 80.0f;
        default: return 1.0f;
    }
}

public float getTotalValue() {
    // 计算总价值（使用基础价值）
    float total = 0;
    for (Map.Entry<ResourceType, Float> entry : resources.entrySet()) {
        total += entry.getValue() * getResourceValue(entry.getKey());
    }
    return total;
}
```

**算法解释**:
- 为不同类型的资源分配不同的价值权重
- `getTotalValue()` 计算所有资源的总经济价值
- 稀有资源（如暗物质、反物质）有更高的价值权重

## 6. 胜利条件算法

### 6.1 完全胜利条件

```java
private boolean checkControlRate(Faction faction) {
    // 获取所有行星
    List<Planet> allPlanets = galaxy.getStarSystems().stream()
            .flatMap(system -> system.getPlanets().stream())
            .collect(java.util.stream.Collectors.toList());
    
    int totalPlanets = allPlanets.size();
    
    // 避免除零错误
    if (totalPlanets == 0) {
        return false;
    }
    
    // 计算达到80%所需的最少行星数
    int minPlanetsNeeded = (totalPlanets * 4 + 5) / 5; // 等价于 Math.ceil(totalPlanets * 0.8)，但避免了浮点运算
    
    // 统计由该派系控制的行星数，一旦达到所需数量立即返回true以提高效率
    long controlledPlanets = allPlanets.stream()
            .filter(planet -> planet.getColony() != null && planet.getColony().getFaction() == faction)
            .limit(minPlanetsNeeded)
            .count();
    
    return controlledPlanets >= minPlanetsNeeded;
}

private boolean checkTechCompletion(TechTree techTree) {
    // 科技完成度应该是所有科技都已研究
    // 即没有未研究的科技
    return techTree.getTechnologies().stream()
            .noneMatch(tech -> !tech.isResearched());
}
```

**算法解释**:
- `checkControlRate()`: 检查派系是否控制了80%的行星，使用整数运算避免浮点误差
- 使用 `limit()` 优化，一旦找到足够数量的行星就停止计算
- `checkTechCompletion()`: 检查是否研究了所有科技
- 完全胜利需要同时满足控制率和科技完成度条件

## 7. AI 决策算法

### 7.1 AI 控制器

```java
public class AIController {
    private final Faction faction;

    public AIController(Faction faction) {
        this.faction = faction;
    }

    public void makeDecision() {
        // AI决策逻辑
        // 1. 检查资源情况
        // 2. 决定建造什么
        // 3. 决定研究什么科技
        // 4. 决定是否扩张
    }

    public void makeDecision(Object gameState) {
        makeDecision();
    }
}
```

**算法解释**:
- 当前 AI 控制器是基础实现，计划中的算法包括:
  - 资源评估: 评估当前资源状况
  - 建造决策: 决定建造什么建筑或舰船
  - 科技选择: 选择研究的科技
  - 扩张策略: 决定是否扩张和扩张方向

## 8. 战斗系统算法

### 8.1 舰队战斗算法

```java
public void attackTarget(Ship target) {
    for (ShipModule module : getActiveModules()) {
        if (module instanceof WeaponModule) {
            WeaponModule weapon = (WeaponModule) module;
            
            float hitChance = calculateHitChance(weapon, target);
            if (Math.random() < hitChance) {
                float damage = weapon.getDamage();
                
                // 创建伤害对象
                Damage weaponDamage = new Damage(damage, mapWeaponTypeToDamageType(weapon.getWeaponType()), 0);
                
                // 对目标造成伤害
                target.takeDamage(weaponDamage);
            }
        }
    }
}

private float calculateHitChance(WeaponModule weapon, Ship target) {
    // 简化：100%命中
    return 1.0f;
}
```

**算法解释**:
- 遍历所有激活的武器模块
- 计算命中概率，如果命中则对目标造成伤害
- 将武器类型映射到相应的伤害类型
- 当前实现简化为100%命中率，但保留了扩展接口

## 9. 性能优化算法

### 9.1 可见性算法

```java
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
```

**算法解释**:
- 基于传感器强度和距离计算六边形的可见度
- 如果六边形内有己方单位则完全可见
- 使用 `1/(1+距离)` 公式计算距离衰减
- 限制最大可见度为1.0

## 10. 殖民地管理算法

### 10.1 殖民地产出计算

```java
public void processTurn() {
    // 计算基础资源产出
    for (ResourceType type : ResourceType.values()) {
        float baseProduction = calculateBaseProduction(type);
        float buildingBonus = calculateBuildingBonus(type);
        float governorBonus = calculateGovernorBonus(type);
        float totalProduction = baseProduction + buildingBonus + governorBonus;
        
        // 添加到派系资源库存
        faction.getResourceStockpile().addResource(type, totalProduction);
    }
    
    // 处理建筑升级
    processBuildingUpgrades();
    
    // 更新殖民地统计数据
    updateStats();
}

private float calculateBaseProduction(ResourceType type) {
    float baseProduction = 0;
    
    // 基于行星类型的基础产出
    switch (type) {
        case METAL:
            baseProduction = planet.getType().getBaseMetal() * planet.getSize();
            break;
        case ENERGY:
            baseProduction = planet.getType().getBaseEnergy() * planet.getSize();
            break;
        case FOOD:
            baseProduction = planet.getType().getBaseFood() * planet.getSize();
            break;
        // ... 其他资源类型
    }
    
    return baseProduction;
}
```

**算法解释**:
- 每回合处理殖民地产出，包括基础产出、建筑加成和总督加成
- 基于行星类型和大小计算基础资源产出
- 将产出添加到派系资源库存
- 处理建筑升级和更新统计数据

## 总结

Stellar Colonizer 项目使用了多种算法来实现其复杂的游戏系统，包括六边形网格操作、银河系生成、路径查找、科技树管理、资源管理等。这些算法共同构成了一个功能完整的 4X 策略游戏的核心逻辑。

项目特别注重六边形网格的处理，使用立方坐标系统来高效处理六边形网格上的各种操作。路径查找算法确保了舰队能够合理移动，而银河系生成算法则创建了丰富的游戏世界。科技树系统采用递归算法计算科技层级，资源管理系统使用高效的映射操作来管理多种资源类型。