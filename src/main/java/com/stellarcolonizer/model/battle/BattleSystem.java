package com.stellarcolonizer.model.battle;

import com.stellarcolonizer.model.fleet.Fleet;
import com.stellarcolonizer.model.fleet.Ship;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.colony.Colony;

import java.util.*;

/**
 * 战斗系统
 * 处理舰队间的战斗逻辑
 */
public class BattleSystem {
    
    /**
     * 检查指定六边形中是否存在敌对舰队
     * @param hex 六边形坐标
     * @return 是否存在敌对舰队
     */
    public static boolean hasEnemiesInHex(com.stellarcolonizer.model.galaxy.Hex hex) {
        List<Fleet> fleets = hex.getFleets();
        
        if (fleets.size() <= 1) {
            return false; // 少于2支舰队，无法战斗
        }
        
        // 检查是否存在不同派系的舰队
        Set<Faction> factions = new HashSet<>();
        for (Fleet fleet : fleets) {
            factions.add(fleet.getFaction());
        }
        
        return factions.size() > 1; // 不同派系的舰队在同一位置，可以战斗
    }
    
    /**
     * 开始战斗 - 已弃用，仅用于向后兼容
     * @param hex 战斗发生的六边形
     * @return 战斗结果
     */
    @Deprecated
    public static BattleResult startBattle(com.stellarcolonizer.model.galaxy.Hex hex) {
        // 如果需要处理六边形中的所有舰队，请使用新的方法
        return null;
    }
    
    /**
     * 模拟战斗
     * @param allShips 所有参与战斗的舰船
     * @param factionsFleets 各派系的舰队
     * @return 战斗结果
     */
    private static BattleResult simulateBattle(List<Ship> allShips, Map<Faction, List<Fleet>> factionsFleets) {
        List<Ship> remainingShips = new ArrayList<>(allShips);
        
        // 战斗回合循环
        int round = 0;
        while (hasMultipleFactions(remainingShips) && round < 50) { // 最多50回合
            // 每艘船进行一轮攻击
            for (Ship attacker : new ArrayList<>(remainingShips)) {
                if (!remainingShips.contains(attacker)) {
                    continue; // 舰船已被摧毁
                }
                
                // 选择目标
                Ship target = selectTarget(attacker, remainingShips);
                if (target != null) {
                    // 攻击目标
                    attacker.fireWeapons(target);
                    
                    // 检查目标是否被摧毁
                    if (target.getHitPoints() <= 0) {
                        remainingShips.remove(target);
                    }
                }
            }
            
            round++;
        }
        
        // 确定胜利者
        Faction winner = getWinningFaction(remainingShips);
        
        // 计算损失
        Map<Faction, Integer> losses = calculateLosses(allShips, remainingShips);
        
        return new BattleResult(winner, round, losses);
    }
    
    /**
     * 检查是否还有多个派系的舰船存活
     * @param ships 舰船列表
     * @return 是否还有多个派系的舰船存活
     */
    private static boolean hasMultipleFactions(List<Ship> ships) {
        Set<Faction> factions = new HashSet<>();
        for (Ship ship : ships) {
            factions.add(ship.getFaction());
            if (factions.size() > 1) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 选择攻击目标
     * @param attacker 攻击者
     * @param allShips 所有舰船
     * @return 目标舰船
     */
    private static Ship selectTarget(Ship attacker, List<Ship> allShips) {
        // 选择敌对派系的舰船作为目标
        for (Ship ship : allShips) {
            if (ship != attacker && !ship.getFaction().equals(attacker.getFaction())) {
                return ship;
            }
        }
        return null;
    }
    
    /**
     * 获取获胜派系
     * @param remainingShips 剩余舰船
     * @return 获胜派系
     */
    private static Faction getWinningFaction(List<Ship> remainingShips) {
        if (remainingShips.isEmpty()) {
            return null; // 平局
        }
        
        Faction faction = remainingShips.get(0).getFaction();
        return faction;
    }
    
    /**
     * 计算各派系的损失
     * @param originalShips 原始舰船
     * @param remainingShips 剩余舰船
     * @return 损失统计
     */
    private static Map<Faction, Integer> calculateLosses(List<Ship> originalShips, List<Ship> remainingShips) {
        Map<Faction, Integer> originalCounts = new HashMap<>();
        Map<Faction, Integer> remainingCounts = new HashMap<>();
        
        for (Ship ship : originalShips) {
            originalCounts.merge(ship.getFaction(), 1, Integer::sum);
        }
        
        for (Ship ship : remainingShips) {
            remainingCounts.merge(ship.getFaction(), 1, Integer::sum);
        }
        
        Map<Faction, Integer> losses = new HashMap<>();
        for (Faction faction : originalCounts.keySet()) {
            int originalCount = originalCounts.get(faction);
            int remainingCount = remainingCounts.getOrDefault(faction, 0);
            losses.put(faction, originalCount - remainingCount);
        }
        
        return losses;
    }
    
    /**
     * 战斗结束后更新舰队状态
     * @param fleets 参战舰队
     * @param result 战斗结果
     */
    private static void updateFleetsAfterBattle(List<Fleet> fleets, BattleResult result) {
        for (Fleet fleet : fleets) {
            // 从舰队中移除被摧毁的舰船
            fleet.getShips().removeIf(ship -> ship.getHitPoints() <= 0);
            
            // 更新舰队统计 - 使用公共方法替代私有方法
            fleet.updateFleetStats();
        }
    }
    
    /**
     * 开始两个特定舰队之间的战斗
     * @param fleet1 第一个舰队（发起攻击的舰队）
     * @param fleet2 第二个舰队（被攻击的舰队）
     * @return 战斗结果
     */
    public static BattleResult startBattle(Fleet fleet1, Fleet fleet2) {
        if (fleet1 == null || fleet2 == null) {
            System.out.println("战斗失败: 舰队为空");
            return null; // 无法战斗
        }
        
        if (fleet1.getFaction().equals(fleet2.getFaction())) {
            System.out.println("战斗失败: 同一派系的舰队");
            return null; // 同一派系的舰队，无法战斗
        }
        
        System.out.println("开始战斗: " + fleet1.getName() + " vs " + fleet2.getName());
        
        // 执行单回合战斗：发起方攻击，如果目标未被摧毁则反击
        return executeSingleRoundBattle(fleet1, fleet2);
    }
    
    /**
     * 开始舰队攻击殖民地的战斗
     * @param attackingFleet 攻击舰队
     * @param defendingColony 防守殖民地
     * @return 战斗结果
     */
    public static BattleResult startBattle(Fleet attackingFleet, Colony defendingColony) {
        if (attackingFleet == null || defendingColony == null) {
            System.out.println("战斗失败: 舰队或殖民地为空");
            return null; // 无法战斗
        }
        
        if (attackingFleet.getFaction().equals(defendingColony.getFaction())) {
            System.out.println("战斗失败: 同一派系的舰队和殖民地");
            return null; // 同一派系，无法战斗
        }
        
        System.out.println("开始战斗: " + attackingFleet.getName() + " vs " + defendingColony.getName());
        
        // 执行舰队攻击殖民地的战斗逻辑
        return executeFleetVsColonyBattle(attackingFleet, defendingColony);
    }
    
    /**
     * 执行舰队攻击殖民地的战斗
     * @param attacker 攻击舰队
     * @param defender 防守殖民地
     * @return 战斗结果
     */
    private static BattleResult executeFleetVsColonyBattle(Fleet attacker, Colony defender) {
        // 保存原始状态用于计算伤害
        float originalAttackerHealth = calculateTotalHealth(attacker);
        int originalDefenderHealth = defender.getCurrentHealth();
        
        // 攻击方先攻击殖民地
        performAttackOnColony(attacker, defender);
        
        // 计算攻击方对殖民地造成的伤害
        float damageToDefender = originalDefenderHealth - defender.getCurrentHealth();
        
        // 检查殖民地是否被摧毁
        boolean defenderDestroyed = defender.getCurrentHealth() <= 0;
        float damageToAttacker = 0;
        
        if (!defenderDestroyed) {
            // 如果殖民地未被摧毁，进行反击（殖民地的防御力量反击）
            performColonyCounterAttack(defender, attacker);
            damageToAttacker = originalAttackerHealth - calculateTotalHealth(attacker);
        }
        
        // 计算损失
        Map<Faction, Integer> losses = new HashMap<>();
        losses.put(attacker.getFaction(), (int)(originalAttackerHealth - calculateTotalHealth(attacker))); // 舰船损失
        losses.put(defender.getFaction(), defender.getMaxHealth() - defender.getCurrentHealth()); // 殖民地生命值损失
        
        // 确定胜利者
        Faction winner = null;
        if (attacker.getShipCount() > 0 && defender.getCurrentHealth() <= 0) {
            // 攻击方胜利（殖民地被摧毁）
            winner = attacker.getFaction();
        } else if (defender.getCurrentHealth() > 0 && attacker.getShipCount() == 0) {
            // 防守方胜利（舰队被摧毁）
            winner = defender.getFaction();
        }
        // 如果双方都还有剩余单位，则为平局或继续战斗
        
        // 更新舰队状态
        updateFleetsAfterBattle(Arrays.asList(attacker), null);
        
        // 创建战斗结果，包含伤害信息
        return new BattleResult(winner, 1, losses, damageToDefender, damageToAttacker, defenderDestroyed);
    }
    
    /**
     * 舰队对殖民地执行攻击
     * @param attacker 攻击舰队
     * @param defender 防守殖民地
     */
    private static void performAttackOnColony(Fleet attacker, Colony defender) {
        if (attacker.getShipCount() == 0) {
            return; // 如果舰队没有舰船，则不进行攻击
        }
        
        // 计算舰队的总伤害
        float totalAttackDamage = attacker.getTotalCombatPower();
        
        // 计算殖民地的防御值（基于防御强度和人口/发展度）
        float totalDefense = defender.getDefenseStrength(); // 殖民地防御强度
        
        // 实际造成的伤害
        float actualDamage = Math.max(0, (int)(totalAttackDamage - totalDefense));
        
        // 对殖民地造成伤害
        if (actualDamage > 0) {
            defender.takeDamage((int)actualDamage);
        }
    }
    
    /**
     * 殖民地反击攻击舰队
     * @param defender 防守殖民地
     * @param attacker 攻击舰队
     */
    private static void performColonyCounterAttack(Colony defender, Fleet attacker) {
        if (defender.getCurrentHealth() <= 0) {
            return; // 如果殖民地已被摧毁，则不进行反击
        }
        
        // 殖民地的反击能力基于其防御力量和驻军
        int colonyCounterAttackPower = (int)(defender.getDefenseStrength() * 0.5); // 殖民地反击力量为防御力量的一半
        
        // 对舰队造成反击伤害
        if (colonyCounterAttackPower > 0) {
            performDamageToShips(attacker, colonyCounterAttackPower);
        }
    }
    
    /**
     * 对舰队中的舰船造成伤害
     * @param fleet 被攻击的舰队
     * @param damage 总伤害
     */
    private static void performDamageToShips(Fleet fleet, float damage) {
        List<Ship> ships = new ArrayList<>(fleet.getShips());
        
        // 按顺序对舰船造成伤害
        for (Ship ship : ships) {
            if (damage <= 0) break; // 如果伤害已经分配完，则停止
            
            float shipHitPoints = ship.getHitPoints();
            float damageToApply = Math.min(damage, shipHitPoints);
            
            // 减少舰船的生命值
            ship.hitPointsProperty().set(shipHitPoints - damageToApply);
            
            // 如果舰船生命值降至0或以下，则移除该舰船
            if (ship.getHitPoints() <= 0) {
                fleet.getShips().remove(ship);
            }
            
            damage -= damageToApply;
        }
    }
    
    /**
     * 执行单回合战斗：发起方攻击，如果目标未被摧毁则反击
     * @param attacker 发起攻击的舰队
     * @param defender 被攻击的舰队
     * @return 战斗结果
     */
    private static BattleResult executeSingleRoundBattle(Fleet attacker, Fleet defender) {
        // 保存原始总生命值用于计算伤害
        float originalAttackerHealth = calculateTotalHealth(attacker);
        float originalDefenderHealth = calculateTotalHealth(defender);
        
        // 攻击方先攻击
        performAttack(attacker, defender);
        
        // 计算攻击方对防守方造成的伤害
        float damageToDefender = originalDefenderHealth - calculateTotalHealth(defender);
        
        // 检查防守方是否被摧毁
        boolean defenderDestroyed = defender.getShipCount() == 0;
        float damageToAttacker = 0;
        
        if (!defenderDestroyed) {
            // 如果防守方未被摧毁，进行反击
            performAttack(defender, attacker);
            damageToAttacker = originalAttackerHealth - calculateTotalHealth(attacker);
        }
        
        // 计算损失
        Map<Faction, Integer> losses = new HashMap<>();
        losses.put(attacker.getFaction(), (int)(originalAttackerHealth - calculateTotalHealth(attacker))); // 使用生命值损失作为损失值
        losses.put(defender.getFaction(), (int)(originalDefenderHealth - calculateTotalHealth(defender))); // 使用生命值损失作为损失值
        
        // 确定胜利者
        Faction winner = null;
        if (attacker.getShipCount() > 0 && defender.getShipCount() == 0) {
            // 攻击方胜利
            winner = attacker.getFaction();
        } else if (defender.getShipCount() > 0 && attacker.getShipCount() == 0) {
            // 防守方胜利
            winner = defender.getFaction();
        }
        // 如果双方都有剩余舰船或都被摧毁，则为平局
        
        // 更新舰队状态
        updateFleetsAfterBattle(Arrays.asList(attacker, defender), null);
        
        // 创建战斗结果，包含伤害信息
        return new BattleResult(winner, 1, losses, damageToDefender, damageToAttacker, defenderDestroyed);
    }
    
    /**
     * 计算舰队的总生命值
     * @param fleet 舰队
     * @return 总生命值
     */
    private static float calculateTotalHealth(Fleet fleet) {
        float totalHealth = 0;
        for (Ship ship : fleet.getShips()) {
            totalHealth += ship.getHitPoints();
        }
        return totalHealth;
    }
    
    /**
     * 执行一次攻击
     * 造成的伤害=舰队的舰船的总面板伤害-被攻击舰队的总装甲值
     * @param attacker 攻击方
     * @param defender 防守方
     */
    private static void performAttack(Fleet attacker, Fleet defender) {
        if (attacker.getShipCount() == 0 || defender.getShipCount() == 0) {
            return; // 如果任一舰队没有舰船，则不进行攻击
        }
        
        // 计算攻击方的总伤害
        float totalAttackDamage = attacker.getTotalCombatPower();
        
        // 计算防守方的总装甲值
        float totalDefense = 0;
        for (Ship ship : defender.getShips()) {
            // 假设舰船的装甲值可以通过设计获取
            totalDefense += ship.getDesign().getArmor(); // 使用舰船设计的装甲值
        }
        
        // 实际造成的伤害
        float actualDamage = Math.max(0, totalAttackDamage - totalDefense);
        
        // 将伤害分配到防守方的舰船上
        if (actualDamage > 0) {
            distributeDamage(defender, actualDamage);
        }
    }
    
    /**
     * 将伤害分配到舰队中的舰船上
     * @param fleet 被攻击的舰队
     * @param damage 总伤害
     */
    private static void distributeDamage(Fleet fleet, float damage) {
        List<Ship> ships = new ArrayList<>(fleet.getShips());
        
        // 按顺序对舰船造成伤害
        for (Ship ship : ships) {
            if (damage <= 0) break; // 如果伤害已经分配完，则停止
            
            float shipHitPoints = ship.getHitPoints();
            float damageToApply = Math.min(damage, shipHitPoints);
            
            // 减少舰船的生命值
            ship.hitPointsProperty().set(shipHitPoints - damageToApply);
            
            // 如果舰船生命值降至0或以下，则移除该舰船
            if (ship.getHitPoints() <= 0) {
                fleet.getShips().remove(ship);
            }
            
            damage -= damageToApply;
        }
    }
    
    /**
     * 计算两个派系的损失
     * @param originalShips 原始舰船
     * @param remainingShips 剩余舰船
     * @param faction1 第一个派系
     * @param faction2 第二个派系
     * @return 损失统计
     */
    private static Map<Faction, Integer> calculateLosses(List<Ship> originalShips, List<Ship> remainingShips, Faction faction1, Faction faction2) {
        Map<Faction, Integer> originalCounts = new HashMap<>();
        Map<Faction, Integer> remainingCounts = new HashMap<>();
        
        for (Ship ship : originalShips) {
            if (ship.getFaction().equals(faction1) || ship.getFaction().equals(faction2)) {
                originalCounts.merge(ship.getFaction(), 1, Integer::sum);
            }
        }
        
        for (Ship ship : remainingShips) {
            if (ship.getFaction().equals(faction1) || ship.getFaction().equals(faction2)) {
                remainingCounts.merge(ship.getFaction(), 1, Integer::sum);
            }
        }
        
        Map<Faction, Integer> losses = new HashMap<>();
        for (Faction faction : originalCounts.keySet()) {
            int originalCount = originalCounts.get(faction);
            int remainingCount = remainingCounts.getOrDefault(faction, 0);
            losses.put(faction, originalCount - remainingCount);
        }
        
        return losses;
    }
}