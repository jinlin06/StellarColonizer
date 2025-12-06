package com.stellarcolonizer.model.fleet;

import com.stellarcolonizer.model.fleet.enums.CommanderTrait;
import javafx.beans.property.*;

class FleetCommander {
    private final StringProperty name;
    private final IntegerProperty level;
    private final IntegerProperty experience;
    private final ObjectProperty<CommanderTrait> trait;

    private final IntegerProperty tactics;     // 战术能力（1-100）
    private final IntegerProperty leadership;  // 领导能力（1-100）
    private final IntegerProperty logistics;   // 后勤能力（1-100）

    public FleetCommander(String name) {
        this.name = new SimpleStringProperty(name);
        this.level = new SimpleIntegerProperty(1);
        this.experience = new SimpleIntegerProperty(0);

        // 随机生成特质
        CommanderTrait[] traits = CommanderTrait.values();
        this.trait = new SimpleObjectProperty<>(traits[(int) (Math.random() * traits.length)]);

        // 初始化能力值
        this.tactics = new SimpleIntegerProperty(50);
        this.leadership = new SimpleIntegerProperty(50);
        this.logistics = new SimpleIntegerProperty(50);

        applyTraitEffects();
    }

    private void applyTraitEffects() {
        tactics.set(tactics.get() + trait.get().getTacticsBonus());
        leadership.set(leadership.get() + trait.get().getLeadershipBonus());
        logistics.set(logistics.get() + trait.get().getLogisticsBonus());
    }

    public void gainExperience(int amount) {
        experience.set(experience.get() + amount);

        // 检查升级
        while (experience.get() >= getExperienceForNextLevel()) {
            levelUp();
        }
    }

    private void levelUp() {
        level.set(level.get() + 1);
        experience.set(experience.get() - getExperienceForNextLevel());

        // 升级时随机提升能力
        tactics.set(tactics.get() + (int) (Math.random() * 6));
        leadership.set(leadership.get() + (int) (Math.random() * 6));
        logistics.set(logistics.get() + (int) (Math.random() * 6));

        // 限制在1-100范围内
        clampAbilities();
    }

    private int getExperienceForNextLevel() {
        return level.get() * 1000;
    }

    private void clampAbilities() {
        tactics.set(Math.max(1, Math.min(100, tactics.get())));
        leadership.set(Math.max(1, Math.min(100, leadership.get())));
        logistics.set(Math.max(1, Math.min(100, logistics.get())));
    }

    public void applyCommanderEffects(Fleet fleet) {
        // 战术能力影响战斗效果
        float tacticsBonus = tactics.get() / 100.0f;
        // 应用到舰队战斗力

        // 领导能力影响士气
        float leadershipBonus = leadership.get() / 100.0f;
        for (Ship ship : fleet.getShips()) {
            ship.moraleProperty().set(ship.getMorale() * (1 + leadershipBonus * 0.1f));
        }

        // 后勤能力影响补给效率
        float logisticsBonus = logistics.get() / 100.0f;
        fleet.supplyEfficiencyProperty().set(fleet.getSupplyEfficiency() * (1 + logisticsBonus * 0.2f));

        // 特质效果
        trait.get().applyEffect(fleet);
    }

    // Getter 方法
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public int getLevel() { return level.get(); }
    public IntegerProperty levelProperty() { return level; }

    public int getExperience() { return experience.get(); }
    public IntegerProperty experienceProperty() { return experience; }

    public CommanderTrait getTrait() { return trait.get(); }
    public ObjectProperty<CommanderTrait> traitProperty() { return trait; }

    public int getTactics() { return tactics.get(); }
    public IntegerProperty tacticsProperty() { return tactics; }

    public int getLeadership() { return leadership.get(); }
    public IntegerProperty leadershipProperty() { return leadership; }

    public int getLogistics() { return logistics.get(); }
    public IntegerProperty logisticsProperty() { return logistics; }
}

