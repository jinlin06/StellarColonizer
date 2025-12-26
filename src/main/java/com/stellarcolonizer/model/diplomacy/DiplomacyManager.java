package com.stellarcolonizer.model.diplomacy;

import com.stellarcolonizer.model.faction.Faction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DiplomacyManager {
    private Map<String, DiplomaticRelationship> relationships; // 用String作为键，格式为"faction1-faction2"
    private Random random;

    public DiplomacyManager() {
        this.relationships = new ConcurrentHashMap<>();
        this.random = new Random();
    }

    public DiplomaticRelationship getRelationship(Faction faction1, Faction faction2) {
        String key = getRelationshipKey(faction1, faction2);
        return relationships.get(key);
    }

    public void setRelationship(Faction faction1, Faction faction2, DiplomaticRelationship.RelationshipStatus status) {
        String key = getRelationshipKey(faction1, faction2);
        DiplomaticRelationship relationship = new DiplomaticRelationship(faction1, faction2, status);
        relationships.put(key, relationship);
    }

    public void adjustRelationship(Faction faction1, Faction faction2, int delta) {
        String key = getRelationshipKey(faction1, faction2);
        DiplomaticRelationship relationship = relationships.get(key);
        
        if (relationship == null) {
            // 如果关系不存在，创建一个中立关系
            relationship = new DiplomaticRelationship(faction1, faction2, DiplomaticRelationship.RelationshipStatus.NEUTRAL);
            relationships.put(key, relationship);
        }
        
        relationship.adjustRelationship(delta);
        
        // 如果关系值变化很大，可能改变关系状态
        if (Math.abs(delta) > 10) {
            updateRelationshipStatus(faction1, faction2);
        }
    }

    private void updateRelationshipStatus(Faction faction1, Faction faction2) {
        DiplomaticRelationship relationship = getRelationship(faction1, faction2);
        if (relationship != null) {
            int value = relationship.getRelationshipValue();
            DiplomaticRelationship.RelationshipStatus newStatus;
            
            if (value >= 75) {
                newStatus = DiplomaticRelationship.RelationshipStatus.ALLIED;
            } else if (value >= 25) {
                newStatus = DiplomaticRelationship.RelationshipStatus.FRIENDLY;
            } else if (value >= -25) {
                newStatus = DiplomaticRelationship.RelationshipStatus.NEUTRAL;
            } else {
                newStatus = DiplomaticRelationship.RelationshipStatus.HOSTILE;
            }
            
            relationship.setStatus(newStatus);
        }
    }

    public void declareWar(Faction faction1, Faction faction2) {
        adjustRelationship(faction1, faction2, -50); // 设置为敌对关系
    }

    public void makePeace(Faction faction1, Faction faction2) {
        adjustRelationship(faction1, faction2, 20); // 改善关系
    }

    public void establishTradeAgreement(Faction faction1, Faction faction2) {
        adjustRelationship(faction1, faction2, 15); // 改善关系
    }

    public void terminateTradeAgreement(Faction faction1, Faction faction2) {
        adjustRelationship(faction1, faction2, -10); // 恶化关系
    }

    public List<Faction> getHostileFactions(Faction faction) {
        List<Faction> hostileFactions = new ArrayList<>();
        for (DiplomaticRelationship relationship : relationships.values()) {
            if (relationship.getSourceFaction().equals(faction) && 
                relationship.getStatus() == DiplomaticRelationship.RelationshipStatus.HOSTILE) {
                hostileFactions.add(relationship.getTargetFaction());
            } else if (relationship.getTargetFaction().equals(faction) && 
                       relationship.getStatus() == DiplomaticRelationship.RelationshipStatus.HOSTILE) {
                hostileFactions.add(relationship.getSourceFaction());
            }
        }
        return hostileFactions;
    }

    public List<Faction> getFriendlyFactions(Faction faction) {
        List<Faction> friendlyFactions = new ArrayList<>();
        for (DiplomaticRelationship relationship : relationships.values()) {
            if (relationship.getSourceFaction().equals(faction) && 
                (relationship.getStatus() == DiplomaticRelationship.RelationshipStatus.FRIENDLY ||
                 relationship.getStatus() == DiplomaticRelationship.RelationshipStatus.ALLIED)) {
                friendlyFactions.add(relationship.getTargetFaction());
            } else if (relationship.getTargetFaction().equals(faction) && 
                       (relationship.getStatus() == DiplomaticRelationship.RelationshipStatus.FRIENDLY ||
                        relationship.getStatus() == DiplomaticRelationship.RelationshipStatus.ALLIED)) {
                friendlyFactions.add(relationship.getSourceFaction());
            }
        }
        return friendlyFactions;
    }

    public List<Faction> getNeutralFactions(Faction faction) {
        List<Faction> neutralFactions = new ArrayList<>();
        for (DiplomaticRelationship relationship : relationships.values()) {
            if (relationship.getSourceFaction().equals(faction) && 
                relationship.getStatus() == DiplomaticRelationship.RelationshipStatus.NEUTRAL) {
                neutralFactions.add(relationship.getTargetFaction());
            } else if (relationship.getTargetFaction().equals(faction) && 
                       relationship.getStatus() == DiplomaticRelationship.RelationshipStatus.NEUTRAL) {
                neutralFactions.add(relationship.getSourceFaction());
            }
        }
        return neutralFactions;
    }

    private String getRelationshipKey(Faction faction1, Faction faction2) {
        // 确保键的顺序一致，这样faction1-faction2和faction2-faction1被视为同一个关系
        String name1 = faction1.getName();
        String name2 = faction2.getName();
        if (name1.compareTo(name2) < 0) {
            return name1 + "-" + name2;
        } else {
            return name2 + "-" + name1;
        }
    }

    public void nextTurn() {
        // 在每个回合结束时，关系可能会自然变化
        for (DiplomaticRelationship relationship : relationships.values()) {
            // 随机小幅调整关系值，模拟关系的自然波动
            if (random.nextDouble() < 0.1) { // 10%的概率发生小幅变化
                int change = random.nextInt(3) - 1; // -1, 0, 或 1
                relationship.adjustRelationship(change);
            }
        }
    }
}