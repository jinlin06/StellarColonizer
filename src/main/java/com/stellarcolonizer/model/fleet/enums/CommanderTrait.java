package com.stellarcolonizer.model.fleet.enums;


import com.stellarcolonizer.model.fleet.Fleet;
import com.stellarcolonizer.model.fleet.Ship;

// 指挥官特质枚举
public enum CommanderTrait {
    AGGRESSIVE("侵略性", "+20战术，-10后勤", "提高舰队攻击力，降低补给效率") {
        @Override
        public void applyEffect(Fleet fleet) {
            // 提高舰队战斗力
            fleet.totalCombatPowerProperty().set(fleet.getTotalCombatPower() * 1.2f);
        }
    },

    DEFENSIVE("防御性", "+20后勤，-10战术", "提高舰队防御力，降低攻击力") {
        @Override
        public void applyEffect(Fleet fleet) {
            // 提高舰队生存能力
            for (Ship ship : fleet.getShips()) {
                ship.currentShieldProperty().set(ship.getCurrentShield() * 1.1f);
                ship.currentArmorProperty().set(ship.getCurrentArmor() * 1.1f);
            }
        }
    },

    STRATEGIST("战略家", "+15战术，+15领导", "平衡的战术和领导能力") {
        @Override
        public void applyEffect(Fleet fleet) {
            // 全面提高舰队效能
            fleet.totalCombatPowerProperty().set(fleet.getTotalCombatPower() * 1.1f);
            fleet.supplyEfficiencyProperty().set(fleet.getSupplyEfficiency() * 1.1f);
        }
    },

    LOGISTICIAN("后勤专家", "+30后勤", "大幅提高补给效率") {
        @Override
        public void applyEffect(Fleet fleet) {
            fleet.supplyEfficiencyProperty().set(fleet.getSupplyEfficiency() * 1.3f);
        }
    },

    CHARISMATIC("魅力型", "+30领导", "大幅提高士气") {
        @Override
        public void applyEffect(Fleet fleet) {
            for (Ship ship : fleet.getShips()) {
                ship.moraleProperty().set(ship.getMorale() * 1.3f);
            }
        }
    };

    private final String displayName;
    private final String statBonus;
    private final String description;

    CommanderTrait(String displayName, String statBonus, String description) {
        this.displayName = displayName;
        this.statBonus = statBonus;
        this.description = description;
    }

    // 能力加成
    public int getTacticsBonus() {
        if (this == AGGRESSIVE) return 20;
        if (this == DEFENSIVE) return -10;
        if (this == STRATEGIST) return 15;
        return 0;
    }

    public int getLeadershipBonus() {
        if (this == STRATEGIST) return 15;
        if (this == CHARISMATIC) return 30;
        return 0;
    }

    public int getLogisticsBonus() {
        if (this == AGGRESSIVE) return -10;
        if (this == DEFENSIVE) return 20;
        if (this == LOGISTICIAN) return 30;
        return 0;
    }

    // 应用特质效果到舰队
    public abstract void applyEffect(Fleet fleet);

    public String getDisplayName() { return displayName; }
    public String getStatBonus() { return statBonus; }
    public String getDescription() { return description; }
}