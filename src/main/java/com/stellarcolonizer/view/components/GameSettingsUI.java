package com.stellarcolonizer.view.components;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class GameSettingsUI extends VBox {
    
    private Spinner<Integer> aiCountSpinner;
    private List<TextField> aiNameFields;
    private Button startGameButton;
    private Button backButton;
    private Label titleLabel;
    private Label aiCountLabel;
    private Label aiNamesLabel;
    
    public GameSettingsUI() {
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // AI数量选择器
        titleLabel = new Label("游戏设置");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 8, 1); // 设置步长为1
        
        aiCountSpinner = new Spinner<>();
        aiCountSpinner.setValueFactory(valueFactory);
        aiCountSpinner.setEditable(true);
        
        aiCountLabel = new Label("AI数量:");
        
        // AI名称输入区域
        aiNamesLabel = new Label("AI名称设置:");
        aiNamesLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // 初始化AI名称输入框列表
        aiNameFields = new ArrayList<>();
        
        // 创建AI名称输入框
        updateAiNameFields();
        
        // 按钮
        startGameButton = new Button("开始游戏");
        backButton = new Button("返回");
        
        // 设置按钮样式
        startGameButton.getStyleClass().add("button-primary");
        backButton.getStyleClass().add("button-secondary");
    }
    
    private void layoutComponents() {
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        
        // 清除现有组件
        this.getChildren().clear();
        
        // 添加标题
        this.getChildren().add(titleLabel);
        
        // AI数量设置
        HBox aiCountBox = new HBox(10);
        aiCountBox.getChildren().addAll(aiCountLabel, aiCountSpinner);
        this.getChildren().add(aiCountBox);
        
        // AI名称设置标题
        this.getChildren().add(aiNamesLabel);
        
        // 创建滚动面板来容纳AI名称输入框
        VBox aiNamesContainer = new VBox(5);
        aiNamesContainer.setPadding(new Insets(10, 0, 0, 20));
        
        // 添加AI名称输入框
        for (TextField field : aiNameFields) {
            HBox nameBox = new HBox(5);
            nameBox.getChildren().add(new Label("AI玩家" + (aiNameFields.indexOf(field) + 1) + ":"));
            nameBox.getChildren().add(field);
            aiNamesContainer.getChildren().add(nameBox);
        }
        
        ScrollPane scrollPane = new ScrollPane(aiNamesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        this.getChildren().add(scrollPane);
        
        // 按钮区域
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(startGameButton, backButton);
        this.getChildren().add(buttonBox);
    }
    
    private void setupEventHandlers() {
        // 当AI数量变化时，更新输入框
        aiCountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateAiNameFields();
            layoutComponents(); // 重新布局
        });
        
        // 开始游戏按钮事件
        startGameButton.setOnAction(e -> onGameStart());
        
        // 返回按钮事件
        backButton.setOnAction(e -> onBack());
    }
    
    private void updateAiNameFields() {
        int aiCount = aiCountSpinner.getValue();
        
        // 保持现有的输入框
        List<String> existingNames = new ArrayList<>();
        for (int i = 0; i < Math.min(aiNameFields.size(), aiCount); i++) {
            existingNames.add(aiNameFields.get(i).getText());
        }
        
        // 清空列表并重新创建
        aiNameFields.clear();
        
        for (int i = 0; i < aiCount; i++) {
            TextField field = new TextField();
            field.setPromptText("AI玩家" + (i + 1) + "名称");
            
            // 如果有现有名称，使用它
            if (i < existingNames.size()) {
                field.setText(existingNames.get(i));
            }
            
            aiNameFields.add(field);
        }
    }
    
    private void onGameStart() {
        // 获取AI数量和名称
        int aiCount = aiCountSpinner.getValue();
        String[] aiNames = new String[aiCount];
        
        for (int i = 0; i < aiCount; i++) {
            String name = aiNameFields.get(i).getText().trim();
            if (name.isEmpty()) {
                // 如果没有输入名称，使用默认名称
                aiNames[i] = "AI玩家" + (i + 1);
            } else {
                aiNames[i] = name;
            }
        }
        
        // 触发开始游戏事件
        if (gameStartCallback != null) {
            gameStartCallback.onGameStart(aiCount, aiNames);
        }
    }
    
    private void onBack() {
        if (backCallback != null) {
            backCallback.run();
        }
    }
    
    // 回调接口
    public interface GameStartCallback {
        void onGameStart(int aiCount, String[] aiNames);
    }
    
    public interface BackCallback {
        void run();
    }
    
    private GameStartCallback gameStartCallback;
    private BackCallback backCallback;
    
    public void setGameStartCallback(GameStartCallback callback) {
        this.gameStartCallback = callback;
    }
    
    public void setBackCallback(BackCallback callback) {
        this.backCallback = callback;
    }
}