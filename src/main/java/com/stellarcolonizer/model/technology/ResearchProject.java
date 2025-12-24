package com.stellarcolonizer.model.technology;

import javafx.beans.property.*;

/**
 * 研究项目类
 * 表示正在进行的研究任务
 */
public class ResearchProject {
    private final ObjectProperty<Technology> technology;
    private final ObjectProperty<TechTree> techTree;
    private final IntegerProperty progress;              // 研发进度（已投入的科技值）
    private final IntegerProperty totalCost;            // 总研发成本（科技值）
    private final IntegerProperty researchPoints;       // 每回合投入的科技值

    /**
     * 构造函数
     *
     * @param technology 要研究的技术
     * @param techTree 所属科技树
     */
    public ResearchProject(Technology technology, TechTree techTree) {
        this.technology = new SimpleObjectProperty<>(technology);
        this.techTree = new SimpleObjectProperty<>(techTree);
        this.progress = new SimpleIntegerProperty(0);
        this.totalCost = new SimpleIntegerProperty(technology.getResearchCost()); // 使用科技的总研发成本
        this.researchPoints = new SimpleIntegerProperty(0); // 这将在processResearch中设置
    }

    /**
     * 推进研究进度
     *
     * @param points 每回合投入的研究点数
     * @return true-研究已完成，false-仍在进行中
     */
    public boolean progress(int points) {
        // 将当前回合的研究点数添加到累积进度中
        researchPoints.set(points); // 更新每回合投入的点数
        int newProgress = progress.get() + points;
        progress.set(newProgress);
        
        if (newProgress >= getTotalCost()) {
            // 研究完成
            technology.get().setResearched(true);
            return true;
        }
        
        return false;
    }

    /**
     * 获取研究进度百分比
     *
     * @return 进度百分比（0-100）
     */
    public float getProgressPercentage() {
        if (getTotalCost() <= 0) return 0;
        return (float) getProgress() / getTotalCost() * 100;
    }

    // ==================== Getter方法 ====================

    public Technology getTechnology() {
        return technology.get();
    }

    public ObjectProperty<Technology> technologyProperty() {
        return technology;
    }

    public TechTree getTechTree() {
        return techTree.get();
    }

    public ObjectProperty<TechTree> techTreeProperty() {
        return techTree;
    }

    public int getProgress() {
        return progress.get();
    }

    public IntegerProperty progressProperty() {
        return progress;
    }

    public int getTotalCost() {
        return totalCost.get();
    }

    public IntegerProperty totalCostProperty() {
        return totalCost;
    }

    public int getResearchPoints() {
        return researchPoints.get();
    }

    public IntegerProperty researchPointsProperty() {
        return researchPoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResearchProject that = (ResearchProject) o;
        return technology.get().getId().equals(that.technology.get().getId());
    }

    @Override
    public int hashCode() {
        return technology.get().getId().hashCode();
    }
}