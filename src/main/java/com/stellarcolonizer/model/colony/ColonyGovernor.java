// ColonyGovernor.java - 殖民地管理者
package com.stellarcolonizer.model.colony;

import com.stellarcolonizer.model.colony.enums.GovernorTrait;
import javafx.beans.property.*;

import java.util.*;

public class ColonyGovernor {

    private final StringProperty name;
    private final IntegerProperty level;
    private final IntegerProperty experience;
    private final ObjectProperty<GovernorTrait> primaryTrait;
    private final ObjectProperty<GovernorTrait> secondaryTrait;

    // 能力值（1-100）
    private final IntegerProperty administration;
    private final IntegerProperty logistics;
    private final IntegerProperty diplomacy;
    private final IntegerProperty military;
    private final IntegerProperty research;

    // 状态
    private final BooleanProperty isAssigned;
    private Colony assignedColony;

    public ColonyGovernor(String name) {
        this.name = new SimpleStringProperty(name);
        this.level = new SimpleIntegerProperty(1);
        this.experience = new SimpleIntegerProperty(0);

        // 随机生成特质
        List<GovernorTrait> traits = Arrays.asList(GovernorTrait.values());
        Collections.shuffle(traits);
        this.primaryTrait = new SimpleObjectProperty<>(traits.get(0));
        this.secondaryTrait = new SimpleObjectProperty<>(traits.get(1));

        // 初始化能力值（受特质影响）
        this.administration = new SimpleIntegerProperty(50);
        this.logistics = new SimpleIntegerProperty(50);
        this.diplomacy = new SimpleIntegerProperty(50);
        this.military = new SimpleIntegerProperty(50);
        this.research = new SimpleIntegerProperty(50);

        applyTraitEffects();

        this.isAssigned = new SimpleBooleanProperty(false);
    }

    private void applyTraitEffects() {
        // 主特质效果
        administration.set(administration.get() + primaryTrait.get().getAdministrationBonus());
        logistics.set(logistics.get() + primaryTrait.get().getLogisticsBonus());
        diplomacy.set(diplomacy.get() + primaryTrait.get().getDiplomacyBonus());
        military.set(military.get() + primaryTrait.get().getMilitaryBonus());
        research.set(research.get() + primaryTrait.get().getResearchBonus());

        // 副特质效果（50%）
        administration.set(administration.get() + secondaryTrait.get().getAdministrationBonus() / 2);
        logistics.set(logistics.get() + secondaryTrait.get().getLogisticsBonus() / 2);
        diplomacy.set(diplomacy.get() + secondaryTrait.get().getDiplomacyBonus() / 2);
        military.set(military.get() + secondaryTrait.get().getMilitaryBonus() / 2);
        research.set(research.get() + secondaryTrait.get().getResearchBonus() / 2);

        // 确保在1-100范围内
        clampAbilities();
    }

    private void clampAbilities() {
        administration.set(clamp(administration.get()));
        logistics.set(clamp(logistics.get()));
        diplomacy.set(clamp(diplomacy.get()));
        military.set(clamp(military.get()));
        research.set(clamp(research.get()));
    }

    private int clamp(int value) {
        return Math.max(1, Math.min(100, value));
    }

    public void assignToColony(Colony colony) {
        if (isAssigned.get() && assignedColony != null) {
            assignedColony.setGovernor(null);
        }

        this.assignedColony = colony;
        colony.setGovernor(this);
        isAssigned.set(true);
    }

    public void unassign() {
        if (assignedColony != null) {
            assignedColony.setGovernor(null);
            assignedColony = null;
        }
        isAssigned.set(false);
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

        // 升级时能力提升
        Random random = new Random();
        administration.set(administration.get() + random.nextInt(6));
        logistics.set(logistics.get() + random.nextInt(6));
        diplomacy.set(diplomacy.get() + random.nextInt(6));
        military.set(military.get() + random.nextInt(6));
        research.set(research.get() + random.nextInt(6));

        clampAbilities();
    }

    private int getExperienceForNextLevel() {
        return level.get() * 100;
    }

    // 应用管理效果到殖民地
    public void applyGovernorEffects(Colony colony) {
        if (!isAssigned.get() || assignedColony != colony) return;

        // 行政能力影响
        float adminBonus = administration.get() / 100.0f;
        colony.setHappiness(colony.getHappiness() * (1 + adminBonus * 0.1f));

        // 后勤能力影响
        float logisticsBonus = logistics.get() / 100.0f;
        // 提高生产效率
        // 这里可以添加具体效果

        // 研究能力影响
        float researchBonus = research.get() / 100.0f;
        // 提高科研产出

        // 特质效果
        primaryTrait.get().applyEffect(colony);
        secondaryTrait.get().applyEffect(colony, 0.5f); // 副特质效果减半
    }

    // Getter 方法
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public int getLevel() { return level.get(); }
    public IntegerProperty levelProperty() { return level; }

    public int getExperience() { return experience.get(); }
    public IntegerProperty experienceProperty() { return experience; }

    public GovernorTrait getPrimaryTrait() { return primaryTrait.get(); }
    public ObjectProperty<GovernorTrait> primaryTraitProperty() { return primaryTrait; }

    public GovernorTrait getSecondaryTrait() { return secondaryTrait.get(); }
    public ObjectProperty<GovernorTrait> secondaryTraitProperty() { return secondaryTrait; }

    public int getAdministration() { return administration.get(); }
    public IntegerProperty administrationProperty() { return administration; }

    public int getLogistics() { return logistics.get(); }
    public IntegerProperty logisticsProperty() { return logistics; }

    public int getDiplomacy() { return diplomacy.get(); }
    public IntegerProperty diplomacyProperty() { return diplomacy; }

    public int getMilitary() { return military.get(); }
    public IntegerProperty militaryProperty() { return military; }

    public int getResearch() { return research.get(); }
    public IntegerProperty researchProperty() { return research; }

    public boolean isAssigned() { return isAssigned.get(); }
    public BooleanProperty assignedProperty() { return isAssigned; }

    public Colony getAssignedColony() { return assignedColony; }
}

