package com.stellarcolonizer.model.victory;

import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.galaxy.Galaxy;
import com.stellarcolonizer.model.galaxy.Planet;
import com.stellarcolonizer.model.technology.TechTree;
import com.stellarcolonizer.model.technology.Technology;

import java.util.List;

public class VictoryConditionManager {

    private Galaxy galaxy;

    public VictoryConditionManager(Galaxy galaxy) {
        this.galaxy = galaxy;
    }

    
    public boolean checkCompleteVictory(Faction faction, TechTree techTree) {
        // 满足以下任一条件即可获胜：只剩下一个玩家 或 研究了终极武器
        return checkLastPlayerStanding(faction) || checkUltimateWeapon(faction);
    }

    private boolean checkLastPlayerStanding(Faction faction) {
        // 获取所有仍然活跃的派系（拥有至少一个殖民地的派系）
        List<Faction> activeFactions = galaxy.getFactions().stream()
                .filter(f -> f.getColonies().size() > 0) // 只有拥有殖民地的派系才算活跃
                .collect(java.util.stream.Collectors.toList());
        
        // 如果只剩一个活跃派系，且该派系就是传入的派系，则该派系获胜
        return activeFactions.size() == 1 && activeFactions.contains(faction);
    }

    private boolean checkTechCompletion(TechTree techTree) {
        // 科技完成度应该是所有科技都已研究
        // 即没有未研究的科技
        return techTree.getTechnologies().stream()
                .noneMatch(tech -> !tech.isResearched());
    }
    
    private boolean checkUltimateWeapon(Faction faction) {
        // 检查派系是否已经研究了终极武器科技
        TechTree techTree = faction.getTechTree();
        Technology ultimateWeapon = techTree.getTechnology("ULTIMATE_WEAPON");
        return ultimateWeapon != null && ultimateWeapon.isResearched();
    }
}