package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.technology.TechTree;
import com.stellarcolonizer.model.technology.Technology;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.List;

public class TechTreeView extends BorderPane {
    private Faction faction;
    private TechTree techTree;
    private ListView<String> techListView;
    
    public TechTreeView() {
        initializeUI();
    }
    
    private void initializeUI() {
        setPrefSize(600, 400);
        
        // 创建标题
        Label titleLabel = new Label("科技树查看");
        titleLabel.setFont(new Font(18));
        
        // 创建科技列表
        techListView = new ListView<>();
        techListView.setPrefHeight(300);
        
        // 创建关闭按钮
        Button closeButton = new Button("关闭");
        closeButton.setOnAction(e -> closeTechTree());
        
        // 设置布局
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(10));
        topBox.getChildren().addAll(titleLabel);
        
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(10));
        centerBox.getChildren().addAll(new Label("已研究科技:"), techListView);
        
        HBox bottomBox = new HBox(10);
        bottomBox.setPadding(new Insets(10));
        bottomBox.getChildren().add(closeButton);
        bottomBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        setTop(topBox);
        setCenter(centerBox);
        setBottom(bottomBox);
    }
    
    public void displayFactionTechTree(Faction faction) {
        this.faction = faction;
        this.techTree = faction.getTechTree();
        
        updateTechList();
    }
    
    private void updateTechList() {
        if (techTree == null) return;
        
        List<Technology> researchedTechs = techTree.getResearchedTechnologies();
        techListView.getItems().clear();
        
        for (Technology tech : researchedTechs) {
            String techInfo = tech.getName() + " (" + tech.getCategory().getDisplayName() + ")";
            techListView.getItems().add(techInfo);
        }
    }
    
    private void closeTechTree() {
        // 隐藏界面，但不销毁
        setVisible(false);
        setManaged(false);
    }
    
    public void showTechTree() {
        setVisible(true);
        setManaged(true);
        updateTechList();
    }
}