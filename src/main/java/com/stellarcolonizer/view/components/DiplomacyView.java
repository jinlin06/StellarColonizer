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
        Button declareWarButton = new Button("宣战");
        Button makePeaceButton = new Button("议和");
        Button tradeAgreementButton = new Button("贸易协定");
        Button cancelButton = new Button("关闭");
        
        // 按钮样式
        declareWarButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        makePeaceButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        tradeAgreementButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        cancelButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");
        
        buttonPanel.getChildren().addAll(declareWarButton, makePeaceButton, tradeAgreementButton, cancelButton);
        
        // 按钮事件
        declareWarButton.setOnAction(e -> {
            DiplomaticRelationship selected = relationshipTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Faction targetFaction = selected.getTargetFaction().equals(playerFaction) ? 
                                       selected.getSourceFaction() : selected.getTargetFaction();
                playerFaction.declareWarOn(targetFaction);
                relationshipTable.refresh();
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
                relationshipTable.refresh();
                showAlert("议和", "已与 " + targetFaction.getName() + " 议和！");
            } else {
                showAlert("错误", "请选择一个派系进行操作。");
            }
        });
        
        tradeAgreementButton.setOnAction(e -> {
            DiplomaticRelationship selected = relationshipTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Faction targetFaction = selected.getTargetFaction().equals(playerFaction) ? 
                                       selected.getSourceFaction() : selected.getTargetFaction();
                playerFaction.establishTradeAgreementWith(targetFaction);
                relationshipTable.refresh();
                showAlert("贸易协定", "已与 " + targetFaction.getName() + " 签署贸易协定！");
            } else {
                showAlert("错误", "请选择一个派系进行操作。");
            }
        });
        
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
        
        TableColumn<DiplomaticRelationship, Integer> valueColumn = new TableColumn<>("关系值");
        valueColumn.setCellValueFactory(cellData -> {
            DiplomaticRelationship rel = cellData.getValue();
            return new javafx.beans.property.SimpleIntegerProperty(rel.getRelationshipValue()).asObject();
        });
        
        // 创建表格
        TableView<DiplomaticRelationship> table = new TableView<>();
        table.getColumns().addAll(factionColumn, statusColumn, valueColumn);
        
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
}