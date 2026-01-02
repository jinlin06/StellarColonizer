package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.battle.BattleResult;
import com.stellarcolonizer.model.battle.BattleSystem;
import com.stellarcolonizer.model.fleet.Fleet;
import com.stellarcolonizer.model.galaxy.Hex;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * 战斗UI组件
 * 提供战斗选择和结果显示功能
 */
public class BattleUI extends VBox {
    
    private Hex hex;
    private List<Fleet> fleets;
    private ListView<Fleet> fleetListView;
    private Button startBattleButton;
    private Button cancelButton;
    private TextArea battleLog;
    
    public BattleUI(Hex hex) {
        this.hex = hex;
        this.fleets = hex.getFleets();
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // 标题
        Label titleLabel = new Label("舰队战斗");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // 舰队列表
        fleetListView = new ListView<>();
        fleetListView.getItems().addAll(fleets);
        fleetListView.setCellFactory(param -> new FleetListCell());
        
        // 按钮
        startBattleButton = new Button("开始战斗");
        cancelButton = new Button("取消");
        
        // 战斗日志
        battleLog = new TextArea();
        battleLog.setPrefHeight(150);
        battleLog.setEditable(false);
        battleLog.setWrapText(true);
        
        // 设置按钮样式
        startBattleButton.getStyleClass().add("button-primary");
        cancelButton.getStyleClass().add("button-secondary");
    }
    
    private void layoutComponents() {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        
        // 添加标题
        Label titleLabel = new Label("舰队战斗");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        this.getChildren().add(titleLabel);
        
        // 添加舰队列表
        Label fleetLabel = new Label("六边形中的舰队:");
        this.getChildren().addAll(fleetLabel, fleetListView);
        
        // 添加战斗日志
        Label logLabel = new Label("战斗日志:");
        this.getChildren().addAll(logLabel, battleLog);
        
        // 按钮区域
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(startBattleButton, cancelButton);
        this.getChildren().add(buttonBox);
    }
    
    private void setupEventHandlers() {
        startBattleButton.setOnAction(e -> startBattle());
        cancelButton.setOnAction(e -> cancelBattle());
    }
    
    private void startBattle() {
        if (BattleSystem.hasEnemiesInHex(hex)) {
            // 记录战斗开始
            battleLog.appendText("开始战斗...\n");
            
            // 执行战斗
            BattleResult result = BattleSystem.startBattle(hex);
            
            if (result != null) {
                // 显示战斗结果
                battleLog.appendText(result.getBattleSummary());
                
                // 通知父窗口更新
                if (getScene() != null && getScene().getWindow() instanceof Stage) {
                    ((Stage) getScene().getWindow()).close();
                }
            } else {
                battleLog.appendText("无法开始战斗：没有敌对舰队\n");
            }
        } else {
            battleLog.appendText("无法开始战斗：没有检测到敌对舰队\n");
        }
    }
    
    private void cancelBattle() {
        if (getScene() != null && getScene().getWindow() instanceof Stage) {
            ((Stage) getScene().getWindow()).close();
        }
    }
    
    /**
     * 自定义舰队列表单元格
     */
    private static class FleetListCell extends ListCell<Fleet> {
        @Override
        protected void updateItem(Fleet fleet, boolean empty) {
            super.updateItem(fleet, empty);
            
            if (empty || fleet == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(fleet.getName() + " (" + fleet.getFaction().getName() + 
                        ") - 舰船数量: " + fleet.getShipCount() + 
                        ", 战斗力: " + String.format("%.0f", fleet.getTotalCombatPower()));
                
                // 设置背景色基于派系
                if (fleet.getFaction().getColor() != null) {
                    setStyle("-fx-background-color: " + 
                            javafx.scene.paint.Color.web(fleet.getFaction().getColor().toString()).darker().toString() + ";");
                }
            }
        }
    }
}