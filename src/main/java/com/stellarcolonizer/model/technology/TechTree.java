package com.stellarcolonizer.model.technology;

import com.stellarcolonizer.model.technology.enums.TechCategory;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.function.Consumer;

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

    // 科技研究完成监听器
    private final List<Consumer<String>> researchCompletedListeners;

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
        this.researchCompletedListeners = new ArrayList<>();

        initializeTechnologies();
    }

    private void initializeTechnologies() {
        // 首先创建所有科技对象（不设置科技值）
        // 物理学分支 - 从基础到高级
        Technology basicPhysics = new Technology("BASIC_PHYSICS", "基础物理学",
                "掌握基本物理原理", TechCategory.PHYSICS, 100);

        // 殖民科技分支 - 基础殖民技术
        Technology basicColonization = new Technology("BASIC_COLONIZATION", "基础殖民技术",
                "掌握基础的殖民技术，允许在宜居行星上建立殖民地", TechCategory.BIOLOGY, 100);

        Technology terraformingBasic = new Technology("TERRAFORMING_BASIC", "基础改造技术",
                "基础行星改造技术，允许殖民类地行星", TechCategory.BIOLOGY, 150);
        terraformingBasic.addPrerequisite("BASIC_COLONIZATION");

        Technology desertAdaptation = new Technology("DESERT_ADAPTATION", "沙漠适应技术",
                "适应沙漠环境的殖民技术，允许殖民沙漠行星", TechCategory.BIOLOGY, 175);
        desertAdaptation.addPrerequisite("BASIC_COLONIZATION");

        Technology aridAdaptation = new Technology("ARID_ADAPTATION", "干旱适应技术",
                "适应干旱环境的殖民技术，允许殖民干旱行星", TechCategory.BIOLOGY, 175);
        aridAdaptation.addPrerequisite("BASIC_COLONIZATION");

        Technology coldAdaptation = new Technology("COLD_ADAPTATION", "寒带适应技术",
                "适应寒冷环境的殖民技术，允许殖民冻土行星", TechCategory.BIOLOGY, 175);
        coldAdaptation.addPrerequisite("BASIC_COLONIZATION");

        Technology cryonicTech = new Technology("CRYONIC_TECH", "低温技术",
                "低温环境生存技术，允许殖民冰封行星", TechCategory.BIOLOGY, 200);
        cryonicTech.addPrerequisite("COLD_ADAPTATION");

        Technology aquaticHabitation = new Technology("AQUATIC_HABITATION", "水生栖息技术",
                "在海洋行星上建立栖息地的技术，允许殖民海洋行星", TechCategory.BIOLOGY, 200);
        aquaticHabitation.addPrerequisite("BASIC_COLONIZATION");

        Technology jungleAdaptation = new Technology("JUNGLE_ADAPTATION", "丛林适应技术",
                "适应丛林环境的殖民技术，允许殖民丛林行星", TechCategory.BIOLOGY, 200);
        jungleAdaptation.addPrerequisite("BASIC_COLONIZATION");

        Technology heatResistance = new Technology("HEAT_RESISTANCE", "高温防护技术",
                "高温环境生存技术，允许殖民熔岩行星", TechCategory.BIOLOGY, 200);
        heatResistance.addPrerequisite("BASIC_COLONIZATION");

        Technology gasGiantHarvesting = new Technology("GAS_GIANT_HARVESTING", "气态巨行星开采技术",
                "开采气态巨行星资源的技术，允许在气态巨行星建立设施", TechCategory.CHEMISTRY, 250);
        gasGiantHarvesting.addPrerequisite("BASIC_COLONIZATION");

        Technology asteroidMining = new Technology("ASTEROID_MINING", "小行星采矿技术",
                "在小行星上建立采矿设施的技术，允许在小行星建立基地", TechCategory.CHEMISTRY, 150);
        asteroidMining.addPrerequisite("BASIC_COLONIZATION");

        Technology terraformingAdvanced = new Technology("TERRAFORMING_ADVANCED", "高级改造技术",
                "高级行星改造技术，允许殖民贫瘠行星", TechCategory.BIOLOGY, 300);
        terraformingAdvanced.addPrerequisite("TERRAFORMING_BASIC");

        Technology mechanics = new Technology("MECHANICS", "力学",
                "理解和应用力的作用", TechCategory.PHYSICS, 150);
        mechanics.addPrerequisite("BASIC_PHYSICS");

        Technology thermodynamics = new Technology("THERMODYNAMICS", "热力学",
                "掌握热量与能量转换定律，解锁驱逐舰建造", TechCategory.PHYSICS, 200);
        thermodynamics.addPrerequisite("MECHANICS");
        thermodynamics.addUnlockedUnit("destroyer");

        // 添加中间层级的科技来填补可能的空隙
        Technology classicalPhysics = new Technology("CLASSICAL_PHYSICS", "经典物理学",
                "经典物理学理论，为现代物理学奠定基础", TechCategory.PHYSICS, 225);
        classicalPhysics.addPrerequisite("THERMODYNAMICS");

        // 再添加一个等级的物理学科技
        Technology modernPhysics = new Technology("MODERN_PHYSICS", "现代物理学",
                "现代物理学理论，结合量子力学与相对论的前沿理论", TechCategory.PHYSICS, 275);
        modernPhysics.addPrerequisite("CLASSICAL_PHYSICS");

        Technology quantumMechanics = new Technology("QUANTUM_MECHANICS", "量子力学",
                "理解微观世界的规律，解锁巡洋舰建造", TechCategory.PHYSICS, 250);
        quantumMechanics.addPrerequisite("CLASSICAL_PHYSICS"); // 依赖新增的中间科技
        quantumMechanics.addUnlockedUnit("cruiser");

        Technology nuclearPhysics = new Technology("NUCLEAR_PHYSICS", "核物理学",
                "掌握原子核反应原理，解锁战列舰建造", TechCategory.PHYSICS, 300);
        nuclearPhysics.addPrerequisite("MODERN_PHYSICS"); // 改为依赖新添加的现代物理
        nuclearPhysics.addUnlockedUnit("battleship");

        Technology electromagnetism = new Technology("ELECTROMAGNETISM", "电磁学",
                "电与磁的统一理论，解锁航母建造", TechCategory.PHYSICS, 350);
        electromagnetism.addPrerequisite("NUCLEAR_PHYSICS");
        electromagnetism.addUnlockedUnit("carrier");

        Technology particlePhysics = new Technology("PARTICLE_PHYSICS", "粒子物理学",
                "探索物质的基本构成，解锁无畏舰建造", TechCategory.PHYSICS, 400);
        particlePhysics.addPrerequisite("ELECTROMAGNETISM");
        particlePhysics.addUnlockedUnit("dreadnought");

        Technology relativisticPhysics = new Technology("RELATIVISTIC_PHYSICS", "相对论物理学",
                "高速与强引力场中的物理规律", TechCategory.PHYSICS, 450);
        relativisticPhysics.addPrerequisite("PARTICLE_PHYSICS");

        Technology quantumFieldTheory = new Technology("QUANTUM_FIELD_THEORY", "量子场论",
                "量子场与基本力的统一理论", TechCategory.PHYSICS, 500);
        quantumFieldTheory.addPrerequisite("RELATIVISTIC_PHYSICS");

        Technology unifiedFieldTheory = new Technology("UNIFIED_FIELD_THEORY", "统一场论",
                "统一四种基本相互作用力", TechCategory.PHYSICS, 600);
        unifiedFieldTheory.addPrerequisite("QUANTUM_FIELD_THEORY");

        // 化学分支 - 从基础到高级
        Technology basicChemistry = new Technology("BASIC_CHEMISTRY", "基础化学",
                "掌握化学基本原理", TechCategory.CHEMISTRY, 100);

        Technology inorganicChemistry = new Technology("INORGANIC_CHEMISTRY", "无机化学",
                "无机化合物的性质与反应", TechCategory.CHEMISTRY, 150);
        inorganicChemistry.addPrerequisite("BASIC_CHEMISTRY");

        Technology organicChemistry = new Technology("ORGANIC_CHEMISTRY", "有机化学",
                "碳基化合物的结构与反应", TechCategory.CHEMISTRY, 200);
        organicChemistry.addPrerequisite("INORGANIC_CHEMISTRY");

        Technology biochemistry = new Technology("BIOCHEMISTRY", "生物化学",
                "生命过程中的化学反应", TechCategory.CHEMISTRY, 250);
        biochemistry.addPrerequisite("ORGANIC_CHEMISTRY");

        Technology analyticalChemistry = new Technology("ANALYTICAL_CHEMISTRY", "分析化学",
                "物质成分与结构的分析方法", TechCategory.CHEMISTRY, 200);
        analyticalChemistry.addPrerequisite("INORGANIC_CHEMISTRY");

        Technology physicalChemistry = new Technology("PHYSICAL_CHEMISTRY", "物理化学",
                "化学系统的物理性质与过程", TechCategory.CHEMISTRY, 250);
        physicalChemistry.addPrerequisite("ANALYTICAL_CHEMISTRY");

        Technology materialsChemistry = new Technology("MATERIALS_CHEMISTRY", "材料化学",
                "先进材料的化学合成与应用", TechCategory.CHEMISTRY, 300);
        materialsChemistry.addPrerequisite("PHYSICAL_CHEMISTRY");

        Technology nanotechnology = new Technology("NANOCHEMISTRY", "纳米化学",
                "纳米尺度的化学操控", TechCategory.CHEMISTRY, 350);
        nanotechnology.addPrerequisite("MATERIALS_CHEMISTRY");

        // 添加更多化学分支科技
        Technology polymerChemistry = new Technology("POLYMER_CHEMISTRY", "高分子化学",
                "高分子材料的合成与应用", TechCategory.CHEMISTRY, 375);
        polymerChemistry.addPrerequisite("NANOCHEMISTRY");

        // 修复化学分支的依赖关系，避免依赖物理分支的科技
        Technology quantumChemistry = new Technology("QUANTUM_CHEMISTRY", "量子化学",
                "量子力学在化学中的应用", TechCategory.CHEMISTRY, 400);
        quantumChemistry.addPrerequisite("POLYMER_CHEMISTRY"); // 改为依赖新增的高分子化学

        Technology supramolecularChemistry = new Technology("SUPRAMOLECULAR_CHEMISTRY", "超分子化学",
                "分子间相互作用与自组装", TechCategory.CHEMISTRY, 450);
        supramolecularChemistry.addPrerequisite("QUANTUM_CHEMISTRY");

        Technology syntheticChemistry = new Technology("SYNTHETIC_CHEMISTRY", "合成化学",
                "复杂分子的人工合成技术", TechCategory.CHEMISTRY, 500);
        syntheticChemistry.addPrerequisite("SUPRAMOLECULAR_CHEMISTRY");

        // 生物学分支 - 从基础到高级
        Technology basicBiology = new Technology("BASIC_BIOLOGY", "基础生物学",
                "掌握生命科学基础", TechCategory.BIOLOGY, 100);

        Technology cellularBiology = new Technology("CELLULAR_BIOLOGY", "细胞生物学",
                "细胞结构与功能研究", TechCategory.BIOLOGY, 150);
        cellularBiology.addPrerequisite("BASIC_BIOLOGY");

        Technology genetics = new Technology("GENETICS", "遗传学",
                "生物遗传规律的研究", TechCategory.BIOLOGY, 200);
        genetics.addPrerequisite("CELLULAR_BIOLOGY");

        Technology molecularBiology = new Technology("MOLECULAR_BIOLOGY", "分子生物学",
                "生物大分子的结构与功能", TechCategory.BIOLOGY, 250);
        molecularBiology.addPrerequisite("GENETICS");

        Technology evolutionaryBiology = new Technology("EVOLUTIONARY_BIOLOGY", "进化生物学",
                "生物进化与适应机制", TechCategory.BIOLOGY, 250);
        evolutionaryBiology.addPrerequisite("CELLULAR_BIOLOGY");

        Technology microbiology = new Technology("MICROBIOLOGY", "微生物学",
                "微生物的特性与应用", TechCategory.BIOLOGY, 300);
        microbiology.addPrerequisite("CELLULAR_BIOLOGY");

        Technology bioengineering = new Technology("BIOENGINEERING", "生物工程学",
                "生物系统的设计与改造", TechCategory.BIOLOGY, 350);
        bioengineering.addPrerequisite("MOLECULAR_BIOLOGY");

        Technology geneticEngineering = new Technology("GENETIC_ENGINEERING", "基因工程",
                "改造和优化生命形式", TechCategory.BIOLOGY, 400);
        geneticEngineering.addPrerequisite("GENETICS");

        // 添加更多生物学分支科技
        Technology biotechnology = new Technology("BIOTECHNOLOGY", "生物技术",
                "生物技术的工程应用", TechCategory.BIOLOGY, 425);
        biotechnology.addPrerequisite("BIOENGINEERING");

        Technology syntheticBiology = new Technology("SYNTHETIC_BIOLOGY", "合成生物学",
                "人工生命的设计与创造", TechCategory.BIOLOGY, 450);
        syntheticBiology.addPrerequisite("BIOTECHNOLOGY"); // 改为依赖新增的生物技术

        Technology neuralBiology = new Technology("NEURAL_BIOLOGY", "神经生物学",
                "神经系统与大脑功能", TechCategory.BIOLOGY, 400);
        neuralBiology.addPrerequisite("MOLECULAR_BIOLOGY");

        Technology consciousnessStudies = new Technology("CONSCIOUSNESS_STUDIES", "意识研究",
                "探索意识的本质与机制", TechCategory.BIOLOGY, 550);
        consciousnessStudies.addPrerequisite("NEURAL_BIOLOGY");

        Technology xenobiology = new Technology("XENOBIOLOGY", "异种生物学",
                "外星生命的科学研究", TechCategory.BIOLOGY, 500);
        xenobiology.addPrerequisite("CONSCIOUSNESS_STUDIES");

        // 兵器科学分支 - 专门用于解锁武器、防御和功能模块
        Technology weaponsScience = new Technology("WEAPONS_SCIENCE", "武器与装备科学",
                "基础武器理论研究，用于解锁后续武器、防御和功能模块", TechCategory.WEAPONS_SCIENCE, 150);
        weaponsScience.addPrerequisite("MECHANICS");

        // 武器科技 - 按照指定顺序解锁
        // 轻型等离子炮 - 150科研值
        Technology plasmaWeapons = new Technology("PLASMA_WEAPONS", "等离子武器",
                "解锁轻型等离子炮", TechCategory.WEAPONS_SCIENCE, 150);
        plasmaWeapons.addPrerequisite("WEAPONS_SCIENCE");

        // 标准磁轨炮 - 200科研값
        Technology railgunWeapons = new Technology("RAILGUN_WEAPONS", "磁轨炮技术",
                "解锁标准磁轨炮", TechCategory.WEAPONS_SCIENCE, 200);
        railgunWeapons.addPrerequisite("PLASMA_WEAPONS"); // 依赖等离子武器科技

        // 先进激光炮 - 250科研값
        Technology advancedLaser = new Technology("ADVANCED_LASER", "高级激光技术",
                "解锁先进激光炮", TechCategory.WEAPONS_SCIENCE, 250);
        advancedLaser.addPrerequisite("RAILGUN_WEAPONS"); // 依赖磁轨炮科技

        // 重型轨道炮 - 300科研값
        Technology heavyCannons = new Technology("HEAVY_CANNONS", "重型火炮技术",
                "解锁重型轨道炮", TechCategory.WEAPONS_SCIENCE, 300);
        heavyCannons.addPrerequisite("ADVANCED_LASER"); // 依赖先进激光科技

        // 添加更多武器科技
        Technology energyWeapons = new Technology("ENERGY_WEAPONS", "能量武器",
                "高能束武器技术", TechCategory.WEAPONS_SCIENCE, 325);
        energyWeapons.addPrerequisite("HEAVY_CANNONS");

        // 防御科技
        // 复合装甲 - 150科研값
        Technology compositeArmor = new Technology("COMPOSITE_ARMOR", "复合装甲",
                "解锁复合装甲", TechCategory.WEAPONS_SCIENCE, 150);
        compositeArmor.addPrerequisite("MATERIALS_CHEMISTRY");

        // 点防御系统 - 200科研값
        Technology pointDefense = new Technology("POINT_DEFENSE", "点防御系统",
                "解锁点防御系统", TechCategory.WEAPONS_SCIENCE, 200);
        pointDefense.addPrerequisite("COMPOSITE_ARMOR"); // 依赖复合装甲科技

        // 高级护盾 - 250科研값
        Technology advancedShields = new Technology("ADVANCED_SHIELDS", "高级护盾技术",
                "解锁先进护盾", TechCategory.WEAPONS_SCIENCE, 250);
        advancedShields.addPrerequisite("POINT_DEFENSE"); // 依赖点防御系统

        // 添加更多防御科技
        Technology adaptiveShields = new Technology("ADAPTIVE_SHIELDS", "自适应护盾",
                "能够适应不同攻击类型的护盾系统", TechCategory.WEAPONS_SCIENCE, 325);
        adaptiveShields.addPrerequisite("ADVANCED_SHIELDS");

        // 功能模块科技
        // 高级功能模块科技
        Technology advancedUtilities = new Technology("ADVANCED_UTILITIES", "高级功能系统",
                "解锁先进传感器", TechCategory.WEAPONS_SCIENCE, 350);
        advancedUtilities.addPrerequisite("WEAPONS_SCIENCE");
        advancedUtilities.addUnlockedUnit("advanced_sensor");

        // 添加更多功能模块科技
        Technology advancedStorage = new Technology("ADVANCED_STORAGE", "高级存储系统",
                "提升货舱容量和效率", TechCategory.WEAPONS_SCIENCE, 375);
        advancedStorage.addPrerequisite("ADVANCED_UTILITIES");
        advancedStorage.addPrerequisite("ZERO_POINT_POWER"); // 让高级存储系统也依赖零点能源

        // 引擎科技
        // 标准引擎 - 150科研값
        Technology standardEngines = new Technology("STANDARD_ENGINES", "标准引擎技术",
                "解锁标准引擎", TechCategory.WEAPONS_SCIENCE, 150);
        standardEngines.addPrerequisite("WEAPONS_SCIENCE");

        // 高性能引擎 - 250科研값
        Technology highPerformanceEngines = new Technology("HIGH_PERFORMANCE_ENGINES", "高性能引擎技术",
                "解锁高性能引擎", TechCategory.WEAPONS_SCIENCE, 250);
        highPerformanceEngines.addPrerequisite("STANDARD_ENGINES"); // 依赖标准引擎

        // 先进引擎 - 400科研값
        Technology advancedEngines = new Technology("ADVANCED_ENGINES", "先进引擎系统",
                "解锁先进引擎", TechCategory.WEAPONS_SCIENCE, 400);
        advancedEngines.addPrerequisite("HIGH_PERFORMANCE_ENGINES"); // 依赖高性能引擎

        // 添加更多引擎科技
        Technology warpDrive = new Technology("WARP_DRIVE", "曲速引擎",
                "实现超光速航行", TechCategory.WEAPONS_SCIENCE, 425);
        warpDrive.addPrerequisite("ADVANCED_ENGINES");
        warpDrive.addPrerequisite("ZERO_POINT_POWER"); // 让曲速引擎也依赖零点能源

        // 电力科技
        // 标准发电机 - 150科研값
        Technology standardPower = new Technology("STANDARD_POWER", "标准发电机技术",
                "解锁标准发电机", TechCategory.WEAPONS_SCIENCE, 150);
        standardPower.addPrerequisite("WEAPONS_SCIENCE");

        // 性能发电机 - 250科研값
        Technology highEfficiencyPower = new Technology("HIGH_EFFICIENCY_POWER", "高性能发电机技术",
                "解锁性能发电机", TechCategory.WEAPONS_SCIENCE, 250);
        highEfficiencyPower.addPrerequisite("STANDARD_POWER"); // 依赖标准发电机

        // 先进发电机 - 450科研값
        Technology advancedPower = new Technology("ADVANCED_POWER", "先进发电机系统",
                "解锁先进发电机", TechCategory.WEAPONS_SCIENCE, 450);
        advancedPower.addPrerequisite("HIGH_EFFICIENCY_POWER"); // 依赖性能发电机

        // 添加更多电力科技
        Technology zeroPointPower = new Technology("ZERO_POINT_POWER", "零点能源",
                "从真空中提取能量的革命性技术", TechCategory.WEAPONS_SCIENCE, 475);
        zeroPointPower.addPrerequisite("ADVANCED_POWER");

        // 高级武器科技 - 300科研값（仅保留，但不用于解锁模块）
        Technology advancedWeapons = new Technology("ADVANCED_WEAPONS", "先进武器系统",
                "高级武器理论研究，用于解锁后续科技", TechCategory.WEAPONS_SCIENCE, 300);
        advancedWeapons.addPrerequisite("ENERGY_WEAPONS"); // 改为依赖新增的能量武器

        Technology advancedDefenses = new Technology("ADVANCED_DEFENSES", "高级防御系统",
                "高级防御理论研究，用于解锁后续科技", TechCategory.WEAPONS_SCIENCE, 350);
        advancedDefenses.addPrerequisite("ADAPTIVE_SHIELDS"); // 改为依赖新增的自适应护盾

        // 终极科技 - 终极武器
        Technology ultimateWeapon = new Technology("ULTIMATE_WEAPON", "终极武器",
                "一种能够摧毁整个星系的超级武器", TechCategory.PHYSICS, 1500);
        // 添加所有顶级科技作为前置条件
        ultimateWeapon.addPrerequisite("UNIFIED_FIELD_THEORY"); // 物理学最高级
        ultimateWeapon.addPrerequisite("SYNTHETIC_CHEMISTRY"); // 化学最高级
        ultimateWeapon.addPrerequisite("SYNTHETIC_BIOLOGY"); // 生物学分支最终科技
        ultimateWeapon.addPrerequisite("XENOBIOLOGY"); // 生物学分支最终科技
        ultimateWeapon.addPrerequisite("ADVANCED_DEFENSES"); // 武器与装备科学分支最终科技

        // 先将所有科技添加到映射中，但暂不添加到列表
        Map<String, Technology> tempTechMap = new HashMap<>();
        // 殖民科技
        tempTechMap.put("BASIC_COLONIZATION", basicColonization);
        tempTechMap.put("TERRAFORMING_BASIC", terraformingBasic);
        tempTechMap.put("DESERT_ADAPTATION", desertAdaptation);
        tempTechMap.put("ARID_ADAPTATION", aridAdaptation);
        tempTechMap.put("COLD_ADAPTATION", coldAdaptation);
        tempTechMap.put("CRYONIC_TECH", cryonicTech);
        tempTechMap.put("AQUATIC_HABITATION", aquaticHabitation);
        tempTechMap.put("JUNGLE_ADAPTATION", jungleAdaptation);
        tempTechMap.put("HEAT_RESISTANCE", heatResistance);
        tempTechMap.put("GAS_GIANT_HARVESTING", gasGiantHarvesting);
        tempTechMap.put("ASTEROID_MINING", asteroidMining);
        tempTechMap.put("TERRAFORMING_ADVANCED", terraformingAdvanced);
        tempTechMap.put("BASIC_PHYSICS", basicPhysics);
        tempTechMap.put("MECHANICS", mechanics);
        tempTechMap.put("THERMODYNAMICS", thermodynamics);
        tempTechMap.put("CLASSICAL_PHYSICS", classicalPhysics); // 添加新的中间科技
        tempTechMap.put("MODERN_PHYSICS", modernPhysics); // 再添加一个等级的物理科技
        tempTechMap.put("QUANTUM_MECHANICS", quantumMechanics);
        tempTechMap.put("NUCLEAR_PHYSICS", nuclearPhysics);
        tempTechMap.put("ELECTROMAGNETISM", electromagnetism);
        tempTechMap.put("PARTICLE_PHYSICS", particlePhysics);
        tempTechMap.put("RELATIVISTIC_PHYSICS", relativisticPhysics);
        tempTechMap.put("QUANTUM_FIELD_THEORY", quantumFieldTheory);
        tempTechMap.put("UNIFIED_FIELD_THEORY", unifiedFieldTheory);
        tempTechMap.put("BASIC_CHEMISTRY", basicChemistry);
        tempTechMap.put("INORGANIC_CHEMISTRY", inorganicChemistry);
        tempTechMap.put("ORGANIC_CHEMISTRY", organicChemistry);
        tempTechMap.put("BIOCHEMISTRY", biochemistry);
        tempTechMap.put("ANALYTICAL_CHEMISTRY", analyticalChemistry);
        tempTechMap.put("PHYSICAL_CHEMISTRY", physicalChemistry);
        tempTechMap.put("MATERIALS_CHEMISTRY", materialsChemistry);
        tempTechMap.put("NANOCHEMISTRY", nanotechnology);
        tempTechMap.put("POLYMER_CHEMISTRY", polymerChemistry); // 添加新的化学科技
        tempTechMap.put("QUANTUM_CHEMISTRY", quantumChemistry);
        tempTechMap.put("SUPRAMOLECULAR_CHEMISTRY", supramolecularChemistry);
        tempTechMap.put("SYNTHETIC_CHEMISTRY", syntheticChemistry);
        tempTechMap.put("BASIC_BIOLOGY", basicBiology);
        tempTechMap.put("CELLULAR_BIOLOGY", cellularBiology);
        tempTechMap.put("GENETICS", genetics);
        tempTechMap.put("MOLECULAR_BIOLOGY", molecularBiology);
        tempTechMap.put("EVOLUTIONARY_BIOLOGY", evolutionaryBiology);
        tempTechMap.put("MICROBIOLOGY", microbiology);
        tempTechMap.put("BIOENGINEERING", bioengineering);
        tempTechMap.put("GENETIC_ENGINEERING", geneticEngineering);
        tempTechMap.put("BIOTECHNOLOGY", biotechnology); // 添加新的生物科技
        tempTechMap.put("SYNTHETIC_BIOLOGY", syntheticBiology);
        tempTechMap.put("XENOBIOLOGY", xenobiology);
        tempTechMap.put("NEURAL_BIOLOGY", neuralBiology);
        tempTechMap.put("CONSCIOUSNESS_STUDIES", consciousnessStudies);
        tempTechMap.put("WEAPONS_SCIENCE", weaponsScience);
        tempTechMap.put("PLASMA_WEAPONS", plasmaWeapons);
        tempTechMap.put("RAILGUN_WEAPONS", railgunWeapons);
        tempTechMap.put("ADVANCED_LASER", advancedLaser);
        tempTechMap.put("HEAVY_CANNONS", heavyCannons);
        tempTechMap.put("ENERGY_WEAPONS", energyWeapons); // 添加新的武器科技
        tempTechMap.put("COMPOSITE_ARMOR", compositeArmor);
        tempTechMap.put("POINT_DEFENSE", pointDefense);
        tempTechMap.put("ADVANCED_SHIELDS", advancedShields);
        tempTechMap.put("ADAPTIVE_SHIELDS", adaptiveShields); // 添加新的防御科技
        tempTechMap.put("ADVANCED_UTILITIES", advancedUtilities);
        tempTechMap.put("ADVANCED_STORAGE", advancedStorage); // 添加新的功能科技
        tempTechMap.put("STANDARD_ENGINES", standardEngines);
        tempTechMap.put("HIGH_PERFORMANCE_ENGINES", highPerformanceEngines);
        tempTechMap.put("ADVANCED_ENGINES", advancedEngines);
        tempTechMap.put("WARP_DRIVE", warpDrive); // 添加新的引擎科技
        tempTechMap.put("STANDARD_POWER", standardPower);
        tempTechMap.put("HIGH_EFFICIENCY_POWER", highEfficiencyPower);
        tempTechMap.put("ADVANCED_POWER", advancedPower);
        tempTechMap.put("ZERO_POINT_POWER", zeroPointPower); // 添加新的电力科技
        tempTechMap.put("ADVANCED_WEAPONS", advancedWeapons);
        tempTechMap.put("ADVANCED_DEFENSES", advancedDefenses);
        tempTechMap.put("ULTIMATE_WEAPON", ultimateWeapon);

        // 重新计算每个科技的层级和科技值
        for (Technology tech : tempTechMap.values()) {
            int tier = calculateTechTier(tech, tempTechMap, new HashSet<>());
            int researchCost;
            
            // 终极武器保持固定值1500，不参与自动计算
            if ("ULTIMATE_WEAPON".equals(tech.getId())) {
                researchCost = 1500; // 保持终极武器的科技值为1500
            } else {
                researchCost = calculateResearchCostForTier(tier, tech.getCategory());
            }
            
            tech.setResearchCost(researchCost);
        }

        // 添加所有科技
        // 殖民科技分支
        addTechnology(basicColonization);
        addTechnology(terraformingBasic);
        addTechnology(desertAdaptation);
        addTechnology(aridAdaptation);
        addTechnology(coldAdaptation);
        addTechnology(cryonicTech);
        addTechnology(aquaticHabitation);
        addTechnology(jungleAdaptation);
        addTechnology(heatResistance);
        addTechnology(gasGiantHarvesting);
        addTechnology(asteroidMining);
        addTechnology(terraformingAdvanced);
        
        addTechnology(basicPhysics);
        addTechnology(mechanics);
        addTechnology(thermodynamics);
        addTechnology(classicalPhysics);
        addTechnology(modernPhysics);
        addTechnology(quantumMechanics);
        addTechnology(nuclearPhysics);
        addTechnology(electromagnetism);
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
        addTechnology(polymerChemistry); // 添加新的化学科技
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
        addTechnology(biotechnology); // 添加新的生物科技
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
        addTechnology(energyWeapons); // 添加新的武器科技
        addTechnology(advancedWeapons);

        // 添加防御科技
        addTechnology(compositeArmor);
        addTechnology(pointDefense);
        addTechnology(advancedShields);
        addTechnology(adaptiveShields); // 添加新的防御科技
        addTechnology(advancedDefenses);

        // 添加功能模块科技
        addTechnology(advancedUtilities);
        addTechnology(advancedStorage); // 添加新的功能科技

        // 添加引擎科技
        addTechnology(standardEngines);
        addTechnology(highPerformanceEngines);
        addTechnology(advancedEngines);
        addTechnology(warpDrive); // 添加新的引擎科技

        // 添加能源科技
        addTechnology(standardPower);
        addTechnology(highEfficiencyPower);
        addTechnology(advancedPower);
        addTechnology(zeroPointPower); // 添加新的电力科技

        addTechnology(ultimateWeapon);
    }

    /**
     * 计算科技的层级（基于前置科技的递归计算）
     * @param tech 要计算层级的科技
     * @param techMap 科技映射表
     * @param visited 已访问的科技集合（避免循环依赖）
     * @return 科技的层级
     */
    private int calculateTechTier(Technology tech, Map<String, Technology> techMap, Set<String> visited) {
        // 避免循环依赖
        if (visited.contains(tech.getId())) {
            return 1; // 如果出现循环依赖，返回基础层级
        }

        // 如果没有前置科技，层级为1
        if (tech.getPrerequisites().isEmpty()) {
            return 1;
        }

        // 递归计算前置科技中的最高层级
        int maxPrereqTier = 0;
        visited.add(tech.getId());
        
        for (String prereqId : tech.getPrerequisites()) {
            Technology prereq = techMap.get(prereqId);
            if (prereq != null) {
                int prereqTier = calculateTechTier(prereq, techMap, visited);
                maxPrereqTier = Math.max(maxPrereqTier, prereqTier);
            }
        }
        
        visited.remove(tech.getId());
        return maxPrereqTier + 1;
    }

    /**
     * 根据层级和科技类别计算研究成本
     * @param tier 科技层级
     * @param category 科技类别
     * @return 研究成本
     */
    private int calculateResearchCostForTier(int tier, TechCategory category) {
        // 基础成本：层级越高，成本越高
        int baseCost = tier * 50; // 每层基础50点
        
        // 根据类别调整成本
        double categoryMultiplier = category.getCostMultiplier();
        
        // 最终成本
        return (int) (baseCost * categoryMultiplier * 2); // 乘以2确保成本足够高
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
            Technology completedTech = currentProject.getTechnology();
            researchQueue.remove(0);

            // 通知所有监听器科技已完成
            notifyResearchCompleted(completedTech.getId());

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

    /**
     * 添加科技研究完成监听器
     * @param listener 监听器
     */
    public void addResearchCompletedListener(Consumer<String> listener) {
        this.researchCompletedListeners.add(listener);
    }

    /**
     * 移除科技研究完成监听器
     * @param listener 监听器
     */
    public void removeResearchCompletedListener(Consumer<String> listener) {
        this.researchCompletedListeners.remove(listener);
    }

    /**
     * 通知所有监听器科技已完成
     * @param techId 完成的科技ID
     */
    private void notifyResearchCompleted(String techId) {
        for (Consumer<String> listener : new ArrayList<>(researchCompletedListeners)) {
            listener.accept(techId);
        }
    }
}