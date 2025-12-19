package com.stellarcolonizer.model.colony.enums;

import com.stellarcolonizer.model.colony.Colony;
import javafx.scene.paint.Color;

// 管理者特质枚举
public enum GovernorTrait {
    ORGANIZER("组织者", "+20行政，+10后勤",
            "提高殖民地管理效率", Color.BLUE) {
        @Override
        public void applyEffect(Colony colony, float strength) {
            colony.stabilityProperty().set(colony.getStability() + (int)(10 * strength));
        }
    },

    LOGISTICIAN("后勤专家", "+30后勤，+10行政",
            "提高资源运输效率", Color.GREEN) {
        @Override
        public void applyEffect(Colony colony, float strength) {
            // 提高所有资源产出
            // 具体效果在Colony中实现
        }
    },

    DIPLOMAT("外交家", "+20外交，+10行政",
            "提高与其他派系的关系", Color.YELLOW) {
        @Override
        public void applyEffect(Colony colony, float strength) {
            colony.setHappiness(colony.getHappiness() * (1 + 0.1f * strength));
        }
    },

    MILITARIST("军事家", "+30军事，+10行政",
            "提高防御能力", Color.RED) {
        @Override
        public void applyEffect(Colony colony, float strength) {
            colony.defenseStrengthProperty().set(colony.getDefenseStrength() + (int)(50 * strength));
        }
    },

    SCHOLAR("学者", "+30研究，+10行政",
            "提高科研效率", Color.PURPLE) {
        @Override
        public void applyEffect(Colony colony, float strength) {
            // 提高科研产出
            // 具体效果在Colony中实现
        }
    },

    ENTREPRENEUR("企业家", "+20后勤，+20外交",
            "提高贸易收入", Color.GOLD) {
        @Override
        public void applyEffect(Colony colony, float strength) {
            // 提高贸易收入
            // 具体效果在Colony中实现
        }
    };

    private final String displayName;
    private final String statBonus;
    private final String description;
    private final Color color;

    GovernorTrait(String displayName, String statBonus, String description, Color color) {
        this.displayName = displayName;
        this.statBonus = statBonus;
        this.description = description;
        this.color = color;
    }

    // 能力加成
    public int getAdministrationBonus() {
        if (this == ORGANIZER) return 20;
        if (this == LOGISTICIAN) return 10;
        if (this == DIPLOMAT) return 10;
        if (this == MILITARIST) return 10;
        if (this == SCHOLAR) return 10;
        if (this == ENTREPRENEUR) return 0;
        return 0;
    }

    public int getLogisticsBonus() {
        if (this == ORGANIZER) return 10;
        if (this == LOGISTICIAN) return 30;
        if (this == DIPLOMAT) return 0;
        if (this == MILITARIST) return 0;
        if (this == SCHOLAR) return 0;
        if (this == ENTREPRENEUR) return 20;
        return 0;
    }

    public int getDiplomacyBonus() {
        if (this == ORGANIZER) return 0;
        if (this == LOGISTICIAN) return 0;
        if (this == DIPLOMAT) return 20;
        if (this == MILITARIST) return 0;
        if (this == SCHOLAR) return 0;
        if (this == ENTREPRENEUR) return 20;
        return 0;
    }

    public int getMilitaryBonus() {
        if (this == ORGANIZER) return 0;
        if (this == LOGISTICIAN) return 0;
        if (this == DIPLOMAT) return 0;
        if (this == MILITARIST) return 30;
        if (this == SCHOLAR) return 0;
        if (this == ENTREPRENEUR) return 0;
        return 0;
    }

    public int getResearchBonus() {
        if (this == ORGANIZER) return 0;
        if (this == LOGISTICIAN) return 0;
        if (this == DIPLOMAT) return 0;
        if (this == MILITARIST) return 0;
        if (this == SCHOLAR) return 30;
        if (this == ENTREPRENEUR) return 0;
        return 0;
    }

    // 应用效果到殖民地
    public abstract void applyEffect(Colony colony, float strength);

    public void applyEffect(Colony colony) {
        applyEffect(colony, 1.0f);
    }

    public String getDisplayName() { return displayName; }
    public String getStatBonus() { return statBonus; }
    public String getDescription() { return description; }
    public Color getColor() { return color; }
}