package com.stellarcolonizer.model.technology;

import com.stellarcolonizer.model.technology.enums.TechCategory;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.*;

public class TechTree {

    private final StringProperty name;
    private final ObservableList<Technology> technologies;
    private final Map<String, Technology> technologyMap;

    private final ObservableList<ResearchProject> researchQueue;
    private final IntegerProperty currentResearchPoints;
    private int baseResearchPointsPerRound;  // 每回合基础科研产出
    private final IntegerProperty baseResearchPointsPerRoundProperty;  // JavaFX属性，用于UI绑定

    private final FloatProperty researchSpeedBonus;
    private final FloatProperty researchCostReduction;

    public TechTree(String name) {
        this.name = new SimpleStringProperty(name);
        this.technologies = FXCollections.observableArrayList();
        this.technologyMap = new HashMap<>();

        this.researchQueue = FXCollections.observableArrayList();
        this.currentResearchPoints = new SimpleIntegerProperty(0);
        this.baseResearchPointsPerRound = 0; // 默认值
        this.baseResearchPointsPerRoundProperty = new SimpleIntegerProperty(0);

        this.researchSpeedBonus = new SimpleFloatProperty(1.0f);
        this.researchCostReduction = new SimpleFloatProperty(0.0f);

        initializeTechnologies();
    }

    private void initializeTechnologies() {
        // 物理学分支 - 从基础到高级
        Technology basicPhysics = new Technology("BASIC_PHYSICS", "基础物理学",
                "掌握基本物理原理", TechCategory.PHYSICS, 100, 3);

        Technology mechanics = new Technology("MECHANICS", "力学",
                "理解和应用力的作用", TechCategory.PHYSICS, 150, 4);
        mechanics.addPrerequisite("BASIC_PHYSICS");

        Technology thermodynamics = new Technology("THERMODYNAMICS", "热力学",
                "掌握热量与能量转换定律，解锁驱逐舰建造", TechCategory.PHYSICS, 200, 5);
        thermodynamics.addPrerequisite("MECHANICS");
        thermodynamics.addUnlockedUnit("destroyer");

        Technology quantumMechanics = new Technology("QUANTUM_MECHANICS", "量子力学",
                "理解微观世界的规律，解锁巡洋舰建造", TechCategory.PHYSICS, 250, 6);
        quantumMechanics.addPrerequisite("THERMODYNAMICS");
        quantumMechanics.addUnlockedUnit("cruiser");

        Technology nuclearPhysics = new Technology("NUCLEAR_PHYSICS", "核物理学",
                "掌握原子核反应原理，解锁战列舰建造", TechCategory.PHYSICS, 300, 7);
        nuclearPhysics.addPrerequisite("QUANTUM_MECHANICS");
        nuclearPhysics.addUnlockedUnit("battleship");

        Technology electromagnetism = new Technology("ELECTROMAGNETISM", "电磁学",
                "电与磁的统一理论，解锁航母建造", TechCategory.PHYSICS, 350, 8);
        electromagnetism.addPrerequisite("NUCLEAR_PHYSICS");
        electromagnetism.addUnlockedUnit("carrier");

        Technology particlePhysics = new Technology("PARTICLE_PHYSICS", "粒子物理学",
                "探索物质的基本构成，解锁无畏舰建造", TechCategory.PHYSICS, 400, 9);
        particlePhysics.addPrerequisite("ELECTROMAGNETISM");
        particlePhysics.addUnlockedUnit("dreadnought");

        Technology relativisticPhysics = new Technology("RELATIVISTIC_PHYSICS", "相对论物理学",
                "高速与强引力场中的物理规律", TechCategory.PHYSICS, 450, 10);
        relativisticPhysics.addPrerequisite("PARTICLE_PHYSICS");

        Technology quantumFieldTheory = new Technology("QUANTUM_FIELD_THEORY", "量子场论",
                "量子场与基本力的统一理论", TechCategory.PHYSICS, 500, 11);
        quantumFieldTheory.addPrerequisite("PARTICLE_PHYSICS");

        Technology unifiedFieldTheory = new Technology("UNIFIED_FIELD_THEORY", "统一场论",
                "统一四种基本相互作用力", TechCategory.PHYSICS, 600, 12);
        unifiedFieldTheory.addPrerequisite("RELATIVISTIC_PHYSICS");
        unifiedFieldTheory.addPrerequisite("QUANTUM_FIELD_THEORY");

        // 化学分支 - 从基础到高级
        Technology basicChemistry = new Technology("BASIC_CHEMISTRY", "基础化学",
                "掌握化学基本原理", TechCategory.CHEMISTRY, 100, 3);

        Technology inorganicChemistry = new Technology("INORGANIC_CHEMISTRY", "无机化学",
                "无机化合物的性质与反应", TechCategory.CHEMISTRY, 150, 4);
        inorganicChemistry.addPrerequisite("BASIC_CHEMISTRY");

        Technology organicChemistry = new Technology("ORGANIC_CHEMISTRY", "有机化学",
                "碳基化合物的结构与反应", TechCategory.CHEMISTRY, 200, 5);
        organicChemistry.addPrerequisite("INORGANIC_CHEMISTRY");

        Technology biochemistry = new Technology("BIOCHEMISTRY", "生物化学",
                "生命过程中的化学反应", TechCategory.CHEMISTRY, 250, 6);
        biochemistry.addPrerequisite("ORGANIC_CHEMISTRY");

        Technology analyticalChemistry = new Technology("ANALYTICAL_CHEMISTRY", "分析化学",
                "物质成分与结构的分析方法", TechCategory.CHEMISTRY, 200, 5);
        analyticalChemistry.addPrerequisite("INORGANIC_CHEMISTRY");

        Technology physicalChemistry = new Technology("PHYSICAL_CHEMISTRY", "物理化学",
                "化学系统的物理性质与过程", TechCategory.CHEMISTRY, 250, 6);
        physicalChemistry.addPrerequisite("ANALYTICAL_CHEMISTRY");

        Technology materialsChemistry = new Technology("MATERIALS_CHEMISTRY", "材料化学",
                "先进材料的化学合成与应用", TechCategory.CHEMISTRY, 300, 7);
        materialsChemistry.addPrerequisite("PHYSICAL_CHEMISTRY");

        Technology nanotechnology = new Technology("NANOCHEMISTRY", "纳米化学",
                "纳米尺度的化学操控", TechCategory.CHEMISTRY, 350, 8);
        nanotechnology.addPrerequisite("MATERIALS_CHEMISTRY");

        Technology quantumChemistry = new Technology("QUANTUM_CHEMISTRY", "量子化学",
                "量子力学在化学中的应用", TechCategory.CHEMISTRY, 400, 9);
        quantumChemistry.addPrerequisite("QUANTUM_MECHANICS");

        Technology supramolecularChemistry = new Technology("SUPRAMOLECULAR_CHEMISTRY", "超分子化学",
                "分子间相互作用与自组装", TechCategory.CHEMISTRY, 450, 10);
        supramolecularChemistry.addPrerequisite("NANOCHEMISTRY");

        Technology syntheticChemistry = new Technology("SYNTHETIC_CHEMISTRY", "合成化学",
                "复杂分子的人工合成技术", TechCategory.CHEMISTRY, 500, 11);
        syntheticChemistry.addPrerequisite("SUPRAMOLECULAR_CHEMISTRY");

        // 生物学分支 - 从基础到高级
        Technology basicBiology = new Technology("BASIC_BIOLOGY", "基础生物学",
                "掌握生命科学基础", TechCategory.BIOLOGY, 100, 3);

        Technology cellularBiology = new Technology("CELLULAR_BIOLOGY", "细胞生物学",
                "细胞结构与功能研究", TechCategory.BIOLOGY, 150, 4);
        cellularBiology.addPrerequisite("BASIC_BIOLOGY");

        Technology genetics = new Technology("GENETICS", "遗传学",
                "生物遗传规律的研究", TechCategory.BIOLOGY, 200, 5);
        genetics.addPrerequisite("CELLULAR_BIOLOGY");

        Technology molecularBiology = new Technology("MOLECULAR_BIOLOGY", "分子生物学",
                "生物大分子的结构与功能", TechCategory.BIOLOGY, 250, 6);
        molecularBiology.addPrerequisite("GENETICS");

        Technology evolutionaryBiology = new Technology("EVOLUTIONARY_BIOLOGY", "进化生物学",
                "生物进化与适应机制", TechCategory.BIOLOGY, 250, 6);
        evolutionaryBiology.addPrerequisite("CELLULAR_BIOLOGY");

        Technology microbiology = new Technology("MICROBIOLOGY", "微生物学",
                "微生物的特性与应用", TechCategory.BIOLOGY, 300, 7);
        microbiology.addPrerequisite("CELLULAR_BIOLOGY");

        Technology bioengineering = new Technology("BIOENGINEERING", "生物工程学",
                "生物系统的设计与改造", TechCategory.BIOLOGY, 350, 8);
        bioengineering.addPrerequisite("MOLECULAR_BIOLOGY");

        Technology geneticEngineering = new Technology("GENETIC_ENGINEERING", "基因工程",
                "改造和优化生命形式", TechCategory.BIOLOGY, 400, 9);
        geneticEngineering.addPrerequisite("GENETICS");

        Technology syntheticBiology = new Technology("SYNTHETIC_BIOLOGY", "合成生物学",
                "人工生命的设计与创造", TechCategory.BIOLOGY, 450, 10);
        syntheticBiology.addPrerequisite("GENETIC_ENGINEERING");

        Technology xenobiology = new Technology("XENOBIOLOGY", "异种生物学",
                "外星生命的科学研究", TechCategory.BIOLOGY, 500, 11);
        xenobiology.addPrerequisite("SYNTHETIC_BIOLOGY");

        Technology neuralBiology = new Technology("NEURAL_BIOLOGY", "神经生物学",
                "神经系统与大脑功能", TechCategory.BIOLOGY, 400, 10);
        neuralBiology.addPrerequisite("MOLECULAR_BIOLOGY");

        Technology consciousnessStudies = new Technology("CONSCIOUSNESS_STUDIES", "意识研究",
                "探索意识的本质与机制", TechCategory.BIOLOGY, 550, 12);
        consciousnessStudies.addPrerequisite("NEURAL_BIOLOGY");

        // 兵器科学分支 - 专门用于解锁武器、防御和功能模块
        Technology weaponsScience = new Technology("WEAPONS_SCIENCE", "兵器科学",
                "基础武器理论研究，用于解锁后续武器、防御和功能模块", TechCategory.WEAPONS_SCIENCE, 150, 4);
        weaponsScience.addPrerequisite("MECHANICS");

        // 武器科技 - 按照指定顺序解锁
        // 轻型等离子炮 - 150科研值
        Technology plasmaWeapons = new Technology("PLASMA_WEAPONS", "等离子武器",
                "解锁轻型等离子炮", TechCategory.WEAPONS_SCIENCE, 150, 4);
        plasmaWeapons.addPrerequisite("WEAPONS_SCIENCE");

        // 标准磁轨炮 - 200科研값
        Technology railgunWeapons = new Technology("RAILGUN_WEAPONS", "磁轨炮技术",
                "解锁标准磁轨炮", TechCategory.WEAPONS_SCIENCE, 200, 5);
        railgunWeapons.addPrerequisite("PLASMA_WEAPONS"); // 依赖等离子武器科技

        // 先进激光炮 - 250科研값
        Technology advancedLaser = new Technology("ADVANCED_LASER", "高级激光技术",
                "解锁先进激光炮", TechCategory.WEAPONS_SCIENCE, 250, 6);
        advancedLaser.addPrerequisite("RAILGUN_WEAPONS"); // 依赖磁轨炮科技

        // 重型轨道炮 - 300科研값
        Technology heavyCannons = new Technology("HEAVY_CANNONS", "重型火炮技术",
                "解锁重型轨道炮", TechCategory.WEAPONS_SCIENCE, 300, 7);
        heavyCannons.addPrerequisite("ADVANCED_LASER"); // 依赖先进激光科技

        // 防御科技
        // 复合装甲 - 150科研값
        Technology compositeArmor = new Technology("COMPOSITE_ARMOR", "复合装甲",
                "解锁复合装甲", TechCategory.WEAPONS_SCIENCE, 150, 4);
        compositeArmor.addPrerequisite("MATERIALS_CHEMISTRY");

        // 点防御系统 - 200科研값
        Technology pointDefense = new Technology("POINT_DEFENSE", "点防御系统",
                "解锁点防御系统", TechCategory.WEAPONS_SCIENCE, 200, 5);
        pointDefense.addPrerequisite("COMPOSITE_ARMOR"); // 依赖复合装甲科技

        // 高级护盾 - 250科研값
        Technology advancedShields = new Technology("ADVANCED_SHIELDS", "高级护盾技术",
                "解锁先进护盾", TechCategory.WEAPONS_SCIENCE, 250, 6);
        advancedShields.addPrerequisite("POINT_DEFENSE"); // 依赖点防御系统

        // 功能模块科技
        // 高级功能模块科技
        Technology advancedUtilities = new Technology("ADVANCED_UTILITIES", "高级功能系统",
                "解锁先进传感器", TechCategory.WEAPONS_SCIENCE, 350, 7);
        advancedUtilities.addPrerequisite("WEAPONS_SCIENCE");
        advancedUtilities.addUnlockedUnit("advanced_sensor");

        // 引擎科技
        // 标准引擎 - 150科研값
        Technology standardEngines = new Technology("STANDARD_ENGINES", "标准引擎技术",
                "解锁标准引擎", TechCategory.PHYSICS, 150, 4);
        standardEngines.addPrerequisite("MECHANICS");

        // 高性能引擎 - 250科研값
        Technology highPerformanceEngines = new Technology("HIGH_PERFORMANCE_ENGINES", "高性能引擎技术",
                "解锁高性能引擎", TechCategory.PHYSICS, 250, 6);
        highPerformanceEngines.addPrerequisite("STANDARD_ENGINES"); // 依赖标准引擎

        // 先进引擎 - 400科研값
        Technology advancedEngines = new Technology("ADVANCED_ENGINES", "先进引擎系统",
                "解锁先进引擎", TechCategory.PHYSICS, 400, 8);
        advancedEngines.addPrerequisite("HIGH_PERFORMANCE_ENGINES"); // 依赖高性能引擎

        // 电力科技
        // 标准发电机 - 150科研값
        Technology standardPower = new Technology("STANDARD_POWER", "标准发电机技术",
                "解锁标准发电机", TechCategory.PHYSICS, 150, 4);
        standardPower.addPrerequisite("MECHANICS");

        // 性能发电机 - 250科研값
        Technology highEfficiencyPower = new Technology("HIGH_EFFICIENCY_POWER", "高性能发电机技术",
                "解锁性能发电机", TechCategory.PHYSICS, 250, 6);
        highEfficiencyPower.addPrerequisite("STANDARD_POWER"); // 依赖标准发电机

        // 先进发电机 - 450科研값
        Technology advancedPower = new Technology("ADVANCED_POWER", "先进发电机系统",
                "解锁先进发电机", TechCategory.PHYSICS, 450, 9);
        advancedPower.addPrerequisite("HIGH_EFFICIENCY_POWER"); // 依赖性能发电机

        // 高级武器科技 - 300科研값（仅保留，但不用于解锁模块）
        Technology advancedWeapons = new Technology("ADVANCED_WEAPONS", "先进武器系统",
                "高级武器理论研究，用于解锁后续科技", TechCategory.WEAPONS_SCIENCE, 300, 6);
        advancedWeapons.addPrerequisite("HEAVY_CANNONS"); // 依赖重型火炮科技

        // 高级防御科技 - 350科研값（仅保留，但不用于解锁模块）
        Technology advancedDefenses = new Technology("ADVANCED_DEFENSES", "高级防御系统",
                "高级防御理论研究，用于解锁后续科技", TechCategory.WEAPONS_SCIENCE, 350, 7);
        advancedDefenses.addPrerequisite("ADVANCED_SHIELDS"); // 依赖高级护盾科技

        // 终极科技 - 终极武器
        Technology ultimateWeapon = new Technology("ULTIMATE_WEAPON", "终极武器",
                "一种能够摧毁整个星系的超级武器", TechCategory.PHYSICS, 1000, 15);
        // 添加所有顶级科技作为前置条件
        ultimateWeapon.addPrerequisite("UNIFIED_FIELD_THEORY"); // 物理学最高级
        ultimateWeapon.addPrerequisite("SYNTHETIC_CHEMISTRY"); // 化学最高级
        ultimateWeapon.addPrerequisite("CONSCIOUSNESS_STUDIES"); // 生物学最高级
        ultimateWeapon.addPrerequisite("ADVANCED_UTILITIES"); // 兵器科学最高级

        // 添加所有科技
        addTechnology(basicPhysics);
        addTechnology(mechanics);
        addTechnology(thermodynamics);
        addTechnology(electromagnetism);
        addTechnology(quantumMechanics);
        addTechnology(nuclearPhysics);
        addTechnology(particlePhysics);
        addTechnology(relativisticPhysics);
        addTechnology(quantumFieldTheory);
        addTechnology(unifiedFieldTheory);
        addTechnology(basicChemistry);
        addTechnology(inorganicChemistry);
        addTechnology(organicChemistry);
        addTechnology(biochemistry);
        addTechnology(analyticalChemistry);
        addTechnology(physicalChemistry);
        addTechnology(materialsChemistry);
        addTechnology(nanotechnology);
        addTechnology(quantumChemistry);
        addTechnology(supramolecularChemistry);
        addTechnology(syntheticChemistry);

        // 添加生物学分支
        addTechnology(basicBiology);
        addTechnology(cellularBiology);
        addTechnology(genetics);
        addTechnology(molecularBiology);
        addTechnology(evolutionaryBiology);
        addTechnology(microbiology);
        addTechnology(bioengineering);
        addTechnology(geneticEngineering);
        addTechnology(syntheticBiology);
        addTechnology(xenobiology);
        addTechnology(neuralBiology);
        addTechnology(consciousnessStudies);

        // 添加兵器科学分支
        addTechnology(weaponsScience);

        // 添加武器科技
        addTechnology(plasmaWeapons);
        addTechnology(railgunWeapons);
        addTechnology(advancedLaser);
        addTechnology(heavyCannons);
        addTechnology(advancedWeapons);

        // 添加防御科技
        addTechnology(compositeArmor);
        addTechnology(pointDefense);
        addTechnology(advancedShields);
        addTechnology(advancedDefenses);

        // 添加功能模块科技
        addTechnology(advancedUtilities);

        // 添加引擎科技
        addTechnology(standardEngines);
        addTechnology(highPerformanceEngines);
        addTechnology(advancedEngines);

        // 添加能源科技
        addTechnology(standardPower);
        addTechnology(highEfficiencyPower);
        addTechnology(advancedPower);

        addTechnology(ultimateWeapon);
    }

    public void addTechnology(Technology technology) {
        technologies.add(technology);
        technologyMap.put(technology.getId(), technology);
    }

    public Technology getTechnology(String id) {
        return technologyMap.get(id);
    }

    public boolean isTechnologyResearched(String id) {
        Technology tech = getTechnology(id);
        return tech != null && tech.isResearched();
    }

    public boolean canResearch(Technology technology) {
        if (technology.isResearched()) {
            return false;
        }

        for (String prereqId : technology.getPrerequisites()) {
            Technology prereq = getTechnology(prereqId);
            if (prereq == null || !prereq.isResearched()) {
                return false;
            }
        }

        return true;
    }

    public ResearchProject startResearch(Technology technology) {
        if (!canResearch(technology)) {
            return null;
        }

        // 如果已经有正在研究的科技，则不允许开始新的研究
        if (!researchQueue.isEmpty()) {
            return null;
        }

        ResearchProject project = new ResearchProject(technology, this);
        researchQueue.add(project);
        return project;
    }

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
            researchQueue.remove(0);

            if (!researchQueue.isEmpty()) {
                // 可以发送通知
            }
        }
    }

    public void addToQueue(Technology technology) {
        if (canResearch(technology)) {
            // 如果队列为空（没有正在研究的科技），则开始研究
            if (researchQueue.isEmpty()) {
                startResearch(technology);
            }
            // 如果队列中已经有相同的科技，则不添加
            else if (!researchQueue.get(0).getTechnology().equals(technology)) {
                // 替换当前研究项目
                researchQueue.clear();
                startResearch(technology);
            }
        }
    }

    public void removeFromQueue(ResearchProject project) {
        // 由于只允许一个研究项目，直接清空队列
        researchQueue.clear();
    }

    public void moveUpInQueue(ResearchProject project) {
        // 在单项目研究系统中，此方法无意义
    }

    public void moveDownInQueue(ResearchProject project) {
        // 在单项目研究系统中，此方法无意义
    }

    public List<Technology> getAvailableTechnologies() {
        return technologies.stream()
                .filter(this::canResearch)
                .filter(tech -> !tech.isResearched())
                .toList();
    }

    public List<Technology> getResearchedTechnologies() {
        return technologies.stream()
                .filter(Technology::isResearched)
                .toList();
    }

    public List<Technology> getTechnologiesByCategory(TechCategory category) {
        return technologies.stream()
                .filter(tech -> tech.getCategory() == category)
                .toList();
    }

    public float getResearchProgressPercentage() {
        if (researchQueue.isEmpty()) {
            return 0;
        }

        ResearchProject current = researchQueue.get(0);
        return current.getProgressPercentage();
    }

    public Technology getCurrentResearch() {
        if (researchQueue.isEmpty()) {
            return null;
        }
        return researchQueue.get(0).getTechnology();
    }

    public String getResearchStatus() {
        if (researchQueue.isEmpty()) {
            return "无研究项目";
        }

        ResearchProject current = researchQueue.get(0);
        return String.format("%s: %.1f%%",
                current.getTechnology().getName(),
                current.getProgressPercentage());
    }

    /**
     * 计算完成当前研究项目还需要多少回合
     * @return 完成当前研究项目还需要的回合数
     */
    public int getRemainingRoundsForCurrentResearch() {
        if (researchQueue.isEmpty()) {
            return 0;
        }

        ResearchProject currentProject = researchQueue.get(0);
        Technology tech = currentProject.getTechnology();

        // 计算有效每回合产出
        float effectivePointsPerRound = baseResearchPointsPerRound * researchSpeedBonus.get();
        float effectivePoints = effectivePointsPerRound;

        // 计算剩余需要的科技值
        int remainingCost = currentProject.getTotalCost() - currentProject.getProgress();

        // 计算还需要多少回合
        if (effectivePoints <= 0) {
            return Integer.MAX_VALUE; // 如果没有科研产出，则无法完成
        }

        return (int) Math.ceil(remainingCost / effectivePoints);
    }

    // Getter方法
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public ObservableList<Technology> getTechnologies() { return technologies; }
    public ObservableList<ResearchProject> getResearchQueue() { return researchQueue; }
    public ObjectProperty<ObservableList<ResearchProject>> researchQueueProperty() { 
        return new SimpleObjectProperty<>(researchQueue); 
    }

    public int getCurrentResearchPoints() { return currentResearchPoints.get(); }
    public IntegerProperty currentResearchPointsProperty() { return currentResearchPoints; }

    public float getResearchSpeedBonus() { return researchSpeedBonus.get(); }
    public void setResearchSpeedBonus(float bonus) { this.researchSpeedBonus.set(bonus); }
    public FloatProperty researchSpeedBonusProperty() { return researchSpeedBonus; }

    public float getResearchCostReduction() { return researchCostReduction.get(); }
    public void setResearchCostReduction(float reduction) { this.researchCostReduction.set(reduction); }
    public FloatProperty researchCostReductionProperty() { return researchCostReduction; }

    public int getBaseResearchPointsPerRound() { return baseResearchPointsPerRound; }
    public void setBaseResearchPointsPerRound(int baseResearchPointsPerRound) { 
        this.baseResearchPointsPerRound = baseResearchPointsPerRound;
        this.baseResearchPointsPerRoundProperty.set(baseResearchPointsPerRound);
    }

    public IntegerProperty baseResearchPointsPerRoundProperty() { return baseResearchPointsPerRoundProperty; }

    public void initializeBaseResearchPoints(int initialPoints) {
        this.baseResearchPointsPerRound = initialPoints;
        this.baseResearchPointsPerRoundProperty.set(initialPoints);
    }

    public int getCurrentBaseResearchPoints() {
        return this.baseResearchPointsPerRound;
    }

    /**
     * 计算完成指定科技研究需要的回合数
     * @param technology 要研究的科技
     * @return 完成该科技研究需要的回合数
     */
    public int getRemainingRoundsForTechnology(Technology technology) {
        if (technology == null) {
            return 0;
        }

        // 计算有效每回合产出
        float effectivePointsPerRound = baseResearchPointsPerRound * researchSpeedBonus.get();

        // 计算还需要多少回合
        if (effectivePointsPerRound <= 0) {
            return Integer.MAX_VALUE; // 如果没有科研产出，则无法完成
        }

        // 使用科技的总成本
        int remainingCost = technology.getResearchCost();

        return (int) Math.ceil(remainingCost / effectivePointsPerRound);
    }
}