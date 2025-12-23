package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.galaxy.Galaxy;
import com.stellarcolonizer.model.galaxy.StarSystem;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class DiplomacyView extends VBox {
    
    private Galaxy galaxy;
    private Faction playerFaction;
    
    public DiplomacyView(Galaxy galaxy, Faction playerFaction) {
        this.galaxy = galaxy;
        this.playerFaction = playerFaction;
        initializeUI();
    }
    
    private void initializeUI() {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: #2b2b2b;");
        
        // 标题
        Label titleLabel = new Label("外交概览");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // 星系控制情况列表
        VBox systemControlBox = new VBox(5);
        systemControlBox.setPadding(new Insets(10));
        systemControlBox.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
        
        Label systemControlTitle = new Label("星系控制情况:");
        systemControlTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        
        // 使用TreeView按派系分组显示
        TreeView<String> systemControlTree = new TreeView<>();
        systemControlTree.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");
        
        // 填充星系控制树
        populateSystemControlTree(systemControlTree);
        
        // 设置TreeCell工厂以显示派系颜色
        systemControlTree.setCellFactory(tree -> new TreeCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    // 检查是否是派系节点（包含"(我方)"或"(AI)"）
                    if (item.contains(" (我方)") || item.contains(" (AI)")) {
                        // 遍历所有派系找到匹配项并设置颜色
                        List<StarSystem> starSystems = galaxy.getStarSystems();
                        Map<Faction, List<StarSystem>> factionSystems = new HashMap<>();
                        
                        for (StarSystem system : starSystems) {
                            if (system.getControllingFaction() != null) {
                                factionSystems.computeIfAbsent(system.getControllingFaction(), k -> new ArrayList<>()).add(system);
                            }
                        }
                        
                        for (Map.Entry<Faction, List<StarSystem>> entry : factionSystems.entrySet()) {
                            String factionName = entry.getKey().getName() + (entry.getKey() == playerFaction ? " (我方)" : " (AI)");
                            if (factionName.equals(item)) {
                                Color factionColor = entry.getKey().getColor();
                                if (factionColor != null) {
                                    String hexColor = String.format("#%02X%02X%02X",
                                        (int)(factionColor.getRed() * 255),
                                        (int)(factionColor.getGreen() * 255),
                                        (int)(factionColor.getBlue() * 255));
                                    setStyle("-fx-text-fill: " + hexColor + ";");
                                }
                                break;
                            }
                        }
                    } else {
                        setStyle(""); // 普通星系名称使用默认样式
                    }
                }
            }
        });
        
        systemControlBox.getChildren().addAll(systemControlTitle, systemControlTree);
        
        this.getChildren().addAll(titleLabel, systemControlBox);
    }
    
    private void populateSystemControlTree(TreeView<String> treeView) {
        // 按派系分组星系
        Map<Faction, List<StarSystem>> factionSystems = new HashMap<>();
        
        for (StarSystem system : galaxy.getStarSystems()) {
            if (system.getControllingFaction() != null) {
                factionSystems.computeIfAbsent(system.getControllingFaction(), k -> new ArrayList<>()).add(system);
            }
        }
        
        // 创建根节点
        TreeItem<String> root = new TreeItem<>("派系控制星系");
        root.setExpanded(true);
        
        // 为每个派系创建节点
        for (Map.Entry<Faction, List<StarSystem>> entry : factionSystems.entrySet()) {
            Faction faction = entry.getKey();
            List<StarSystem> systems = entry.getValue();
            
            // 创建派系节点
            String factionName = faction.getName() + (faction == playerFaction ? " (我方)" : " (AI)");
            TreeItem<String> factionItem = new TreeItem<>(factionName);
            
            // 添加该派系控制的星系
            for (StarSystem system : systems) {
                TreeItem<String> systemItem = new TreeItem<>(system.getName());
                factionItem.getChildren().add(systemItem);
            }
            
            root.getChildren().add(factionItem);
        }
        
        treeView.setRoot(root);
        treeView.setShowRoot(false); // 隐藏根节点
    }
    
    private void populateSystemControlList(ListView<StarSystem> listView) {
        List<StarSystem> starSystems = galaxy.getStarSystems();
        
        // 只添加有派系控制的星系
        for (StarSystem system : starSystems) {
            if (system.getControllingFaction() != null) {
                listView.getItems().add(system);
            }
        }
    }
    
    public static void showDiplomacyView(Galaxy galaxy, Faction playerFaction) {
        Stage dialog = new Stage();
        dialog.setTitle("外交概览");
        dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
        dialog.setResizable(true);
        
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                DiplomacyView.class.getResourceAsStream("/images/icon.png"));
            dialog.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("无法加载窗口图标: " + e.getMessage());
        }
        
        DiplomacyView view = new DiplomacyView(galaxy, playerFaction);
        
        javafx.scene.Scene scene = new javafx.scene.Scene(view, 500, 600);
        scene.getStylesheets().add(DiplomacyView.class.getResource("/css/main.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }
}