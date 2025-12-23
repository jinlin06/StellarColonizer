package com.stellarcolonizer.model.technology;

import javafx.beans.property.*;

/**
 * 研究项目类
 * 表示正在进行的研究任务
 */
public class ResearchProject {
    private final ObjectProperty<Technology> technology;
    private final ObjectProperty<TechTree> techTree;
    private final IntegerProperty progress;              // 研发进度（回合数）
    private final IntegerProperty totalCost;            // 总研发回合数
    private final IntegerProperty researchPoints;       // 累积的研发点数（用于减少回合数）

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
        this.totalCost = new SimpleIntegerProperty(technology.getResearchTime()); // 使用科技的研究时间作为总回合数
        this.researchPoints = new SimpleIntegerProperty(0);
    }

    /**
     * 推进研究进度
     *
     * @param points 要增加的研究点数（用于减少研发回合数）
     * @return true-研究已完成，false-仍在进行中
     */
    public boolean progress(int points) {
        // 累积研发点数
        researchPoints.set(researchPoints.get() + points);
        
        // 计算基于研发点数的回合数减少
        int baseTotalCost = technology.get().getResearchTime(); // 基础回合数
        int pointsToReduce = researchPoints.get(); // 已累积的研发点数
        int reductionPerPoint = 1; // 每点研发点数减少的回合数（可根据需要调整）
        int reducedTotalCost = Math.max(1, baseTotalCost - (pointsToReduce / reductionPerPoint)); // 确保至少需要1回合

        // 更新总成本
        totalCost.set(reducedTotalCost);
        
        // 增加一个回合的进度
        int newProgress = progress.get() + 1;
        progress.set(newProgress);
        
        if (newProgress >= totalCost.get()) {
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
        if (totalCost.get() <= 0) return 0;
        return (float) progress.get() / totalCost.get() * 100;
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