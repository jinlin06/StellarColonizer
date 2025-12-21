package com.stellarcolonizer.model.fleet;

import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.fleet.enums.FleetMission;
import com.stellarcolonizer.model.fleet.enums.ShipClass;
import com.stellarcolonizer.model.galaxy.Hex;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class Fleet {

    private final StringProperty name;
    private final ObjectProperty<Faction> faction;
    private final ObjectProperty<Hex> currentHex;

    // 舰队组成
    private final ObservableList<Ship> ships;
    private final Map<ShipDesign, Integer> shipCountByDesign;

    // 舰队属性
    private final FloatProperty totalCombatPower;
    private final FloatProperty averageSpeed;
    private final FloatProperty detectionRange;
    private final FloatProperty fuelConsumption;

    // 状态
    private final BooleanProperty isMoving;
    private final ObjectProperty<FleetMission> currentMission;
    private final ObjectProperty<Hex> destination;

    // 指挥官
    private final ObjectProperty<FleetCommander> commander;

    // 补给
    private final Map<ResourceType, Float> supplies;
    private final FloatProperty supplyEfficiency;

    public Fleet(String name, Faction faction, Hex initialHex) {
        this.name = new SimpleStringProperty(name);
        this.faction = new SimpleObjectProperty<>(faction);
        this.currentHex = new SimpleObjectProperty<>(initialHex);

        this.ships = FXCollections.observableArrayList();
        this.shipCountByDesign = new HashMap<>();

        this.totalCombatPower = new SimpleFloatProperty(0);
        this.averageSpeed = new SimpleFloatProperty(0);
        this.detectionRange = new SimpleFloatProperty(100);
        this.fuelConsumption = new SimpleFloatProperty(0);

        this.isMoving = new SimpleBooleanProperty(false);
        this.currentMission = new SimpleObjectProperty<>(FleetMission.STANDBY);
        this.destination = new SimpleObjectProperty<>(null);

        this.commander = new SimpleObjectProperty<>(null);

        this.supplies = new EnumMap<>(ResourceType.class);
        initializeSupplies();
        this.supplyEfficiency = new SimpleFloatProperty(1.0f);
    }

    private void initializeSupplies() {
        supplies.put(ResourceType.FOOD, 1000.0f);
        supplies.put(ResourceType.ENERGY, 5000.0f);
        supplies.put(ResourceType.METAL, 1000.0f);
        supplies.put(ResourceType.FUEL, 5000.0f);
    }

    public boolean addShip(Ship ship) {
        if (!ship.getFaction().equals(faction.get())) {
            return false;
        }

        ships.add(ship);

        // 更新统计数据
        ShipDesign design = ship.getDesign();
        shipCountByDesign.merge(design, 1, Integer::sum);

        updateFleetStats();
        return true;
    }

    /**
     * 根据舰船设计生成唯一的舰船名称
     * @param design 舰船设计
     * @return 唯一的舰船名称
     */
    public String generateUniqueShipName(ShipDesign design) {
        String baseName = design.getName();
        int count = shipCountByDesign.getOrDefault(design, 0) + 1;
        return baseName + " #" + count;
    }

    public boolean removeShip(Ship ship) {
        boolean removed = ships.remove(ship);

        if (removed) {
            ShipDesign design = ship.getDesign();
            Integer count = shipCountByDesign.get(design);
            if (count != null && count > 1) {
                shipCountByDesign.put(design, count - 1);
            } else {
                shipCountByDesign.remove(design);
            }

            updateFleetStats();
        }

        return removed;
    }

    public void transferShip(Ship ship, Fleet targetFleet) {
        if (removeShip(ship)) {
            targetFleet.addShip(ship);
        }
    }

    private void updateFleetStats() {
        // 计算总战斗力
        float combatPower = ships.stream()
                .map(Ship::calculateDamageOutput)
                .reduce(0f, Float::sum);
        totalCombatPower.set(combatPower);

        // 计算平均速度
        if (!ships.isEmpty()) {
            float avgSpeed = (float) ships.stream()
                    .mapToDouble(s -> s.getDesign().getWarpSpeed())
                    .average()
                    .orElse(0);
            averageSpeed.set(avgSpeed);
        }

        // 计算探测范围
        float maxDetection = ships.stream()
                .map(s -> {
                    // 这里应该计算每艘船的探测范围
                    return 100.0f; // 基础值
                })
                .max(Float::compare)
                .orElse(100.0f);
        detectionRange.set(maxDetection);

        // 计算燃料消耗
        float totalConsumption = ships.stream()
                .map(s -> {
                    float shipConsumption = s.getDesign().getEnginePower() * 0.01f;
                    if (s.isMoving()) {
                        shipConsumption *= 2.0f; // 移动时消耗加倍
                    }
                    return shipConsumption;
                })
                .reduce(0f, Float::sum);
        fuelConsumption.set(totalConsumption);
    }

    public void processTurn() {
        // 处理所有舰船
        for (Ship ship : ships) {
            ship.processTurn();
        }

        // 消耗舰队补给
        consumeSupplies();

        // 检查任务状态
        checkMission();

        // 自动补给
        autoResupply();

        // 更新舰队统计
        updateFleetStats();
    }

    private void consumeSupplies() {
        // 计算总消耗
        float foodConsumption = ships.stream()
                .mapToInt(Ship::getCurrentCrew)
                .sum() * 0.01f;

        float energyConsumption = ships.stream()
                .mapToInt(s -> s.getDesign().getModules().size())
                .sum() * 0.1f;

        float fuelConsumption = this.fuelConsumption.get();

        // 应用消耗
        consumeResource(ResourceType.FOOD, foodConsumption);
        consumeResource(ResourceType.ENERGY, energyConsumption);
        consumeResource(ResourceType.FUEL, fuelConsumption);

        // 计算补给效率
        calculateSupplyEfficiency();
    }

    private void consumeResource(ResourceType type, float amount) {
        float current = supplies.getOrDefault(type, 0f);
        if (current >= amount) {
            supplies.put(type, current - amount);
        } else {
            supplies.put(type, 0f);

            // 资源不足的影响
            if (type == ResourceType.FOOD) {
                // 食物不足降低士气
                for (Ship ship : ships) {
                    ship.moraleProperty().set(ship.getMorale() - 2.0f);
                }
            } else if (type == ResourceType.ENERGY) {
                // 能源不足降低战斗准备度
                for (Ship ship : ships) {
                    ship.combatReadinessProperty().set(ship.getCombatReadiness() * 0.9f);
                }
            } else if (type == ResourceType.FUEL) {
                // 燃料不足停止移动
                isMoving.set(false);
            }
        }
    }

    private void calculateSupplyEfficiency() {
        float efficiency = 1.0f;

        // 检查各种资源充足率
        for (ResourceType type : ResourceType.values()) {
            float current = supplies.getOrDefault(type, 0f);
            float required = calculateDailyRequirement(type);

            if (required > 0) {
                float ratio = current / (required * 7); // 7天储备
                efficiency *= Math.min(1.0f, ratio);
            }
        }

        supplyEfficiency.set(efficiency);
    }

    private float calculateDailyRequirement(ResourceType type) {
        switch (type) {
            case FOOD:
                return ships.stream().mapToInt(Ship::getCurrentCrew).sum() * 0.01f;
            case ENERGY:
                return ships.stream()
                        .mapToInt(s -> s.getDesign().getModules().size())
                        .sum() * 0.1f;
            case FUEL:
                return fuelConsumption.get();
            default:
                return 0;
        }
    }

    private void checkMission() {
        FleetMission mission = currentMission.get();
        if (mission == null) return;

        switch (mission) {
            case STANDBY:
                // 待命状态，不执行特殊操作
                break;

            case PATROL:
                executePatrolMission();
                break;

            case EXPLORE:
                executeExploreMission();
                break;

            case DEFEND:
                executeDefendMission();
                break;

            case ATTACK:
                executeAttackMission();
                break;

            case RETREAT:
                executeRetreatMission();
                break;
        }
    }

    private void executePatrolMission() {
        // 巡逻任务：在指定区域内移动
        if (destination.get() == null || currentHex.get().equals(destination.get())) {
            // 选择新的巡逻点
            Hex newDestination = findNearbyHex(5); // 5格范围内
            destination.set(newDestination);
            isMoving.set(true);
        }
    }

    private void executeExploreMission() {
        // 探索任务：移动到未知区域
        if (destination.get() == null || currentHex.get().equals(destination.get())) {
            // 寻找最近的未探索区域
            Hex unexplored = findUnexploredHex();
            if (unexplored != null) {
                destination.set(unexplored);
                isMoving.set(true);
            }
        }
    }

    private void executeDefendMission() {
        // 防御任务：保护特定区域
        if (destination.get() == null) {
            // 选择要防御的殖民地
            Hex colonyHex = findFriendlyColony();
            if (colonyHex != null) {
                destination.set(colonyHex);
                isMoving.set(true);
            }
        }

        // 如果到达目的地，停止移动
        if (currentHex.get().equals(destination.get())) {
            isMoving.set(false);
        }
    }

    private void executeAttackMission() {
        // 攻击任务：攻击敌方目标
        if (destination.get() == null) {
            // 寻找敌方目标
            Hex enemyTarget = findEnemyTarget();
            if (enemyTarget != null) {
                destination.set(enemyTarget);
                isMoving.set(true);
            }
        }

        // 检查是否到达目标并发动攻击
        if (currentHex.get().equals(destination.get())) {
            engageEnemy();
        }
    }

    private void executeRetreatMission() {
        // 撤退任务：返回安全区域
        if (destination.get() == null) {
            // 寻找最近的友好殖民地
            Hex safeHex = findFriendlyColony();
            if (safeHex != null) {
                destination.set(safeHex);
                isMoving.set(true);
            }
        }
    }

    private Hex findNearbyHex(int range) {
        // 在指定范围内随机选择一个六边形
        // 这里需要访问星系地图
        return currentHex.get(); // 简化实现
    }

    private Hex findUnexploredHex() {
        // 寻找最近的未探索六边形
        // 这里需要访问游戏地图
        return currentHex.get(); // 简化实现
    }

    private Hex findFriendlyColony() {
        // 寻找最近的友好殖民地
        // 这里需要访问派系的殖民地列表
        return currentHex.get(); // 简化实现
    }

    private Hex findEnemyTarget() {
        // 寻找敌方目标
        // 这里需要访问敌方情报
        return null; // 简化实现
    }

    private void engageEnemy() {
        // 与敌方交战
        // 这里会触发战斗系统
        System.out.println("舰队 " + name.get() + " 与敌人交战！");
    }

    private void autoResupply() {
        // 如果位于友好殖民地，自动补给
        if (isAtFriendlyColony()) {
            resupplyFromColony();
        }
    }

    private boolean isAtFriendlyColony() {
        // 检查当前位置是否有友好殖民地
        // 简化实现
        return false;
    }

    private void resupplyFromColony() {
        // 从殖民地获取补给
        // 这里需要访问殖民地的资源
    }

    public void setMission(FleetMission mission, Hex target) {
        this.currentMission.set(mission);
        this.destination.set(target);

        if (target != null && !target.equals(currentHex.get())) {
            isMoving.set(true);
        } else {
            isMoving.set(false);
        }
    }

    public void moveTo(Hex destination) {
        if (destination == null || destination.equals(currentHex.get())) return;

        this.destination.set(destination);
        isMoving.set(true);
        currentMission.set(FleetMission.MOVE);
    }

    public boolean canMove() {
        // 检查燃料和舰船状态
        float totalFuel = supplies.getOrDefault(ResourceType.FUEL, 0f);
        if (totalFuel <= 0) return false;

        boolean anyShipCanMove = ships.stream().anyMatch(Ship::canMove);
        return anyShipCanMove;
    }

    public void resupply(ResourceType type, float amount) {
        supplies.put(type, supplies.getOrDefault(type, 0f) + amount);
    }

    public void resupplyAll() {
        // 补充所有资源到最大值的50%
        for (ResourceType type : ResourceType.values()) {
            float maxCapacity = getMaxCapacity(type);
            float current = supplies.getOrDefault(type, 0f);
            float needed = maxCapacity * 0.5f - current;

            if (needed > 0) {
                supplies.put(type, current + needed);
            }
        }
    }

    private float getMaxCapacity(ResourceType type) {
        // 计算舰队最大容量
        switch (type) {
            case FOOD:
            case ENERGY:
            case METAL:
                return ships.size() * 1000;
            case FUEL:
                return ships.stream()
                        .mapToLong(s -> s.getDesign().getFuelCapacity())
                        .sum();
            default:
                return 1000;
        }
    }

    public void mergeFleet(Fleet otherFleet) {
        if (!otherFleet.getFaction().equals(faction.get())) {
            throw new IllegalArgumentException("只能合并同派系的舰队");
        }

        // 转移所有舰船
        for (Ship ship : new ArrayList<>(otherFleet.getShips())) {
            otherFleet.removeShip(ship);
            addShip(ship);
        }

        // 转移补给（部分）
        for (Map.Entry<ResourceType, Float> entry : otherFleet.getSupplies().entrySet()) {
            resupply(entry.getKey(), entry.getValue() * 0.5f);
        }
    }

    public Fleet splitFleet(String newFleetName, List<Ship> shipsToTransfer) {
        Fleet newFleet = new Fleet(newFleetName, faction.get(), currentHex.get());

        for (Ship ship : shipsToTransfer) {
            if (ships.contains(ship)) {
                removeShip(ship);
                newFleet.addShip(ship);
            }
        }

        // 分配部分补给
        for (ResourceType type : ResourceType.values()) {
            float amount = supplies.getOrDefault(type, 0f) * 0.3f; // 分配30%补给
            if (amount > 0) {
                consumeResource(type, amount);
                newFleet.resupply(type, amount);
            }
        }

        return newFleet;
    }

    public float calculateTotalHealth() {
        float totalHealth = 0;
        float maxHealth = 0;

        for (Ship ship : ships) {
            totalHealth += ship.getHitPoints();
            maxHealth += ship.getDesign().getHitPoints();
        }

        if (maxHealth <= 0) return 0;
        return totalHealth / maxHealth * 100;
    }

    public Map<ShipClass, Integer> getShipCountByClass() {
        Map<ShipClass, Integer> countByClass = new EnumMap<>(ShipClass.class);

        for (Ship ship : ships) {
            ShipClass shipClass = ship.getDesign().getShipClass();
            countByClass.merge(shipClass, 1, Integer::sum);
        }

        return countByClass;
    }

    public String getCompositionSummary() {
        Map<ShipClass, Integer> byClass = getShipCountByClass();
        StringBuilder summary = new StringBuilder();

        summary.append("舰队组成：\n");
        for (Map.Entry<ShipClass, Integer> entry : byClass.entrySet()) {
            summary.append(entry.getKey().getDisplayName())
                    .append(": ")
                    .append(entry.getValue())
                    .append("艘\n");
        }

        summary.append("\n总战斗力: ").append(String.format("%.0f", totalCombatPower.get()));
        summary.append("\n平均速度: ").append(String.format("%.1f", averageSpeed.get()));
        summary.append("\n燃料储备: ").append(String.format("%.0f", supplies.getOrDefault(ResourceType.FUEL, 0f)));
        summary.append("\n健康状况: ").append(String.format("%.1f%%", calculateTotalHealth()));

        return summary.toString();
    }

    // Getter 方法
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public Faction getFaction() { return faction.get(); }
    public ObjectProperty<Faction> factionProperty() { return faction; }

    public Hex getCurrentHex() { return currentHex.get(); }
    public ObjectProperty<Hex> currentHexProperty() { return currentHex; }

    public ObservableList<Ship> getShips() { return ships; }
    public Map<ShipDesign, Integer> getShipCountByDesign() { return new HashMap<>(shipCountByDesign); }

    public float getTotalCombatPower() { return totalCombatPower.get(); }
    public FloatProperty totalCombatPowerProperty() { return totalCombatPower; }

    public float getAverageSpeed() { return averageSpeed.get(); }
    public FloatProperty averageSpeedProperty() { return averageSpeed; }

    public float getDetectionRange() { return detectionRange.get(); }
    public FloatProperty detectionRangeProperty() { return detectionRange; }

    public float getFuelConsumption() { return fuelConsumption.get(); }
    public FloatProperty fuelConsumptionProperty() { return fuelConsumption; }

    public boolean isMoving() { return isMoving.get(); }
    public BooleanProperty movingProperty() { return isMoving; }

    public FleetMission getCurrentMission() { return currentMission.get(); }
    public ObjectProperty<FleetMission> currentMissionProperty() { return currentMission; }

    public Hex getDestination() { return destination.get(); }
    public ObjectProperty<Hex> destinationProperty() { return destination; }

    public FleetCommander getCommander() { return commander.get(); }
    public void setCommander(FleetCommander commander) { this.commander.set(commander); }
    public ObjectProperty<FleetCommander> commanderProperty() { return commander; }

    public Map<ResourceType, Float> getSupplies() { return new EnumMap<>(supplies); }
    public float getSupplyEfficiency() { return supplyEfficiency.get(); }
    public FloatProperty supplyEfficiencyProperty() { return supplyEfficiency; }

    public int getShipCount() { return ships.size(); }
    public int getTotalCrew() {
        return ships.stream().mapToInt(Ship::getCurrentCrew).sum();
    }
}