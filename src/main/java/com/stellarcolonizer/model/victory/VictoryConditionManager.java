package com.stellarcolonizer.model.victory;

import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.galaxy.Galaxy;
import com.stellarcolonizer.model.galaxy.Planet;
import com.stellarcolonizer.model.technology.TechTree;

import java.util.List;

public class VictoryConditionManager {

    private Galaxy galaxy;

    public VictoryConditionManager(Galaxy galaxy) {
        this.galaxy = galaxy;
    }

    
    public boolean checkCompleteVictory(Faction faction, TechTree techTree) {
        return checkControlRate(faction) && checkTechCompletion(techTree);
    }

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
}