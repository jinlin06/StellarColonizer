package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.diplomacy.DiplomaticRelationship;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.galaxy.Galaxy;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class DiplomacyView {
    public static void showDiplomacyView(Galaxy galaxy, Faction playerFaction) {
        Stage diplomacyStage = new Stage();
        diplomacyStage.initModality(Modality.APPLICATION_MODAL);
        diplomacyStage.setTitle("外交界面");
        diplomacyStage.setWidth(800);
        diplomacyStage.setHeight(600);
        
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));
        
        // 标题
        Label titleLabel = new Label("外交界面");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // 显示玩家派系名称
        Label playerFactionLabel = new Label("您的派系: " + playerFaction.getName());
        playerFactionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // 创建外交关系表格
        TableView<DiplomaticRelationship> relationshipTable = createRelationshipTable(galaxy, playerFaction);
        
        // 按钮面板
        HBox buttonPanel = new HBox(10);
        Button establishDiplomaticRelationButton = new Button("建交");
        Button declareWarButton = new Button("宣战");
        Button makePeaceButton = new Button("议和");
        Button cancelButton = new Button("关闭");
        
        // 按钮样式
        establishDiplomaticRelationButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        declareWarButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        makePeaceButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        cancelButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");
        
        // 初始隐藏议和按钮，只有在战争状态下才显示
        makePeaceButton.setVisible(false);
        buttonPanel.getChildren().addAll(establishDiplomaticRelationButton, declareWarButton, makePeaceButton, cancelButton);
        
        // 按钮事件
        establishDiplomaticRelationButton.setOnAction(e -> {
            DiplomaticRelationship selected = relationshipTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Faction targetFaction = selected.getTargetFaction().equals(playerFaction) ? 
                                       selected.getSourceFaction() : selected.getTargetFaction();
                // 建交操作：使用外交管理器建立外交关系
                playerFaction.getDiplomacyManager().establishDiplomaticRelation(playerFaction, targetFaction);
                refreshRelationshipTable(relationshipTable, galaxy, playerFaction);
                updateButtonVisibility(relationshipTable, makePeaceButton, declareWarButton, establishDiplomaticRelationButton);
                showAlert("建交", "已与 " + targetFaction.getName() + " 建立外交关系！");
            } else {
                showAlert("错误", "请选择一个派系进行操作。");
            }
        });
        
        declareWarButton.setOnAction(e -> {
            DiplomaticRelationship selected = relationshipTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Faction targetFaction = selected.getTargetFaction().equals(playerFaction) ? 
                                       selected.getSourceFaction() : selected.getTargetFaction();
                playerFaction.declareWarOn(targetFaction);
                refreshRelationshipTable(relationshipTable, galaxy, playerFaction);
                updateButtonVisibility(relationshipTable, makePeaceButton, declareWarButton, establishDiplomaticRelationButton);
                showAlert("宣战", "已向 " + targetFaction.getName() + " 宣战！");
            } else {
                showAlert("错误", "请选择一个派系进行操作。");
            }
        });
        
        makePeaceButton.setOnAction(e -> {
            DiplomaticRelationship selected = relationshipTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Faction targetFaction = selected.getTargetFaction().equals(playerFaction) ? 
                                       selected.getSourceFaction() : selected.getTargetFaction();
                playerFaction.makePeaceWith(targetFaction);
                refreshRelationshipTable(relationshipTable, galaxy, playerFaction);
                updateButtonVisibility(relationshipTable, makePeaceButton, declareWarButton, establishDiplomaticRelationButton);
                showAlert("议和", "已与 " + targetFaction.getName() + " 议和！");
            } else {
                showAlert("错误", "请选择一个派系进行操作。");
            }
        });
        
        // 监听表格选择变化，更新按钮可见性
        relationshipTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateButtonVisibility(relationshipTable, makePeaceButton, declareWarButton, establishDiplomaticRelationButton);
        });
        
        // 初始更新按钮可见性
        updateButtonVisibility(relationshipTable, makePeaceButton, declareWarButton, establishDiplomaticRelationButton);
        
        cancelButton.setOnAction(e -> diplomacyStage.close());
        
        mainLayout.getChildren().addAll(titleLabel, playerFactionLabel, relationshipTable, buttonPanel);
        
        Scene scene = new Scene(mainLayout);
        diplomacyStage.setScene(scene);
        diplomacyStage.show();
    }
    
    private static TableView<DiplomaticRelationship> createRelationshipTable(Galaxy galaxy, Faction playerFaction) {
        // 创建列
        TableColumn<DiplomaticRelationship, String> factionColumn = new TableColumn<>("派系");
        factionColumn.setCellValueFactory(cellData -> {
            DiplomaticRelationship rel = cellData.getValue();
            Faction otherFaction = rel.getTargetFaction().equals(playerFaction) ? 
                                  rel.getSourceFaction() : rel.getTargetFaction();
            return new javafx.beans.property.SimpleStringProperty(otherFaction.getName());
        });
        
        TableColumn<DiplomaticRelationship, String> statusColumn = new TableColumn<>("关系状态");
        statusColumn.setCellValueFactory(cellData -> {
            DiplomaticRelationship rel = cellData.getValue();
            String status = rel.getStatus().getDisplayName();
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        
        // 创建表格
        TableView<DiplomaticRelationship> table = new TableView<>();
        table.getColumns().addAll(factionColumn, statusColumn);
        
        // 填充数据
        List<Faction> allFactions = galaxy.getFactions();
        for (Faction faction : allFactions) {
            if (!faction.equals(playerFaction)) {
                DiplomaticRelationship relationship = playerFaction.getRelationshipWith(faction);
                if (relationship == null) {
                    // 如果没有关系，创建一个中立关系
                    relationship = new DiplomaticRelationship(playerFaction, faction, 
                        DiplomaticRelationship.RelationshipStatus.NEUTRAL);
                }
                table.getItems().add(relationship);
            }
        }
        
        table.setPrefHeight(400);
        return table;
    }
    
    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private static void refreshRelationshipTable(TableView<DiplomaticRelationship> table, Galaxy galaxy, Faction playerFaction) {
        // 重新填充表格数据以确保显示更新
        table.getItems().clear();
        List<Faction> allFactions = galaxy.getFactions();
        for (Faction faction : allFactions) {
            if (!faction.equals(playerFaction)) {
                DiplomaticRelationship relationship = playerFaction.getRelationshipWith(faction);
                if (relationship == null) {
                    // 如果没有关系，创建一个中立关系
                    relationship = new DiplomaticRelationship(playerFaction, faction, 
                        DiplomaticRelationship.RelationshipStatus.NEUTRAL);
                }
                table.getItems().add(relationship);
            }
        }
    }
    
    private static void updateButtonVisibility(TableView<DiplomaticRelationship> table, Button makePeaceButton, Button declareWarButton, Button establishDiplomaticRelationButton) {
        DiplomaticRelationship selected = table.getSelectionModel().getSelectedItem();
        
        if (selected != null) {
            // 根据当前关系状态决定按钮可见性
            switch (selected.getStatus()) {
                case HOSTILE: // 交恶
                    makePeaceButton.setVisible(true); // 只能议和
                    declareWarButton.setVisible(false); // 不能再宣战
                    establishDiplomaticRelationButton.setVisible(false); // 不能直接建交
                    break;
                case NEUTRAL: // 中立
                    makePeaceButton.setVisible(false); // 中立状态不需要议和
                    declareWarButton.setVisible(true); // 可以宣战
                    establishDiplomaticRelationButton.setVisible(true); // 可以建交
                    break;
                case PEACEFUL: // 和平
                    makePeaceButton.setVisible(false); // 和平状态不需要议和
                    declareWarButton.setVisible(true); // 可以宣战
                    establishDiplomaticRelationButton.setVisible(false); // 已经和平，不需要建交
                    break;
            }
        } else {
            // 没有选择任何关系时，隐藏操作按钮
            makePeaceButton.setVisible(false);
            declareWarButton.setVisible(false);
            establishDiplomaticRelationButton.setVisible(false);
        }
    }
}