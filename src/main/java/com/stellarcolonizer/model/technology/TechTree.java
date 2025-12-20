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

    private final FloatProperty researchSpeedBonus;
    private final FloatProperty researchCostReduction;

    public TechTree(String name) {
        this.name = new SimpleStringProperty(name);
        this.technologies = FXCollections.observableArrayList();
        this.technologyMap = new HashMap<>();

        this.researchQueue = FXCollections.observableArrayList();
        this.currentResearchPoints = new SimpleIntegerProperty(0);

        this.researchSpeedBonus = new SimpleFloatProperty(1.0f);
        this.researchCostReduction = new SimpleFloatProperty(0.0f);

        initializeTechnologies();
    }

    private void initializeTechnologies() {
        // 物理学分支
        Technology physics = new Technology("BASIC_PHYSICS", "基础物理学",
                "掌握基本物理原理", TechCategory.PHYSICS, 100, 30);

        Technology quantumMechanics = new Technology("QUANTUM_MECHANICS", "量子力学",
                "理解微观世界的规律", TechCategory.PHYSICS, 250, 30);
        quantumMechanics.addPrerequisite("BASIC_PHYSICS");

        Technology ftlTheory = new Technology("FTL_THEORY", "超光速理论",
                "超越光速的旅行成为可能", TechCategory.PHYSICS, 500, 30);
        ftlTheory.addPrerequisite("QUANTUM_MECHANICS");

        // 工程学分支
        Technology basicEngineering = new Technology("BASIC_ENGINEERING", "基础工程学",
                "掌握基本工程原理", TechCategory.MATERIALS_ENGINEERING, 100, 30);

        Technology nanotechnology = new Technology("NANOTECHNOLOGY", "纳米技术",
                "在分子尺度上操纵物质", TechCategory.NANOTECHNOLOGY, 300, 30);
        nanotechnology.addPrerequisite("BASIC_ENGINEERING");

        Technology molecularAssembly = new Technology("MOLECULAR_ASSEMBLY", "分子组装",
                "原子级的精确制造", TechCategory.MATERIALS_ENGINEERING, 600, 30);
        molecularAssembly.addPrerequisite("NANOTECHNOLOGY");

        // 军事学分支
        Technology basicWeapons = new Technology("BASIC_WEAPONS", "基础武器学",
                "掌握基本武器原理", TechCategory.MILITARY_TECH, 100, 30);

        Technology laserWeapons = new Technology("LASER_WEAPONS", "激光武器",
                "高能定向能量武器", TechCategory.MILITARY_TECH, 200, 30);
        laserWeapons.addPrerequisite("BASIC_WEAPONS");

        Technology plasmaWeapons = new Technology("PLASMA_WEAPONS", "等离子武器",
                "高温等离子体武器", TechCategory.MILITARY_TECH, 350, 30);
        plasmaWeapons.addPrerequisite("LASER_WEAPONS");

        // 社会学分支
        Technology basicSociology = new Technology("BASIC_SOCIOLOGY", "基础社会学",
                "理解社会运作原理", TechCategory.SOCIOLOGY, 100, 30);

        Technology collectiveConsciousness = new Technology("COLLECTIVE_CONSCIOUSNESS", "集体意识",
                "社会思维的协调统一", TechCategory.SOCIOLOGY, 300, 30);
        collectiveConsciousness.addPrerequisite("BASIC_SOCIOLOGY");

        // 生物学分支
        Technology basicBiology = new Technology("BASIC_BIOLOGY", "基础生物学",
                "掌握生命科学基础", TechCategory.BIOLOGY, 100, 30);

        Technology geneticEngineering = new Technology("GENETIC_ENGINEERING", "基因工程",
                "改造和优化生命形式", TechCategory.BIOENGINEERING, 400, 30);
        geneticEngineering.addPrerequisite("BASIC_BIOLOGY");

        // 添加所有科技
        addTechnology(physics);
        addTechnology(quantumMechanics);
        addTechnology(ftlTheory);
        addTechnology(basicEngineering);
        addTechnology(nanotechnology);
        addTechnology(molecularAssembly);
        addTechnology(basicWeapons);
        addTechnology(laserWeapons);
        addTechnology(plasmaWeapons);
        addTechnology(basicSociology);
        addTechnology(collectiveConsciousness);
        addTechnology(basicBiology);
        addTechnology(geneticEngineering);
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

        ResearchProject project = new ResearchProject(technology, this);
        researchQueue.add(project);
        return project;
    }

    public void processResearch(int researchPoints) {
        currentResearchPoints.set(researchPoints);

        if (researchQueue.isEmpty()) {
            return;
        }

        ResearchProject currentProject = researchQueue.get(0);
        float effectivePoints = researchPoints * researchSpeedBonus.get();
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
        if (canResearch(technology) && !researchQueue.contains(new ResearchProject(technology, this))) {
            researchQueue.add(new ResearchProject(technology, this));
        }
    }

    public void removeFromQueue(ResearchProject project) {
        researchQueue.remove(project);
    }

    public void moveUpInQueue(ResearchProject project) {
        int index = researchQueue.indexOf(project);
        if (index > 0) {
            researchQueue.remove(index);
            researchQueue.add(index - 1, project);
        }
    }

    public void moveDownInQueue(ResearchProject project) {
        int index = researchQueue.indexOf(project);
        if (index >= 0 && index < researchQueue.size() - 1) {
            researchQueue.remove(index);
            researchQueue.add(index + 1, project);
        }
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
}