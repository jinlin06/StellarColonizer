package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.colony.BasicBuilding;
import com.stellarcolonizer.model.colony.Building;
import com.stellarcolonizer.model.colony.Colony;
import com.stellarcolonizer.model.colony.ResourceRequirement;
import com.stellarcolonizer.model.colony.enums.BuildingType;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.galaxy.Planet;
import com.stellarcolonizer.model.galaxy.StarSystem;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class StarSystemInfoView extends VBox {
    
    private StarSystem starSystem;
    private ListView<Planet> planetListView;
    private VBox planetDetailsContainer;
    private Faction playerFaction; // 添加玩家派系引用
    
    public StarSystemInfoView(StarSystem starSystem, Faction playerFaction) {
        this.starSystem = starSystem;
        this.playerFaction = playerFaction;
        initializeUI();
        setupEventHandlers();
        populatePlanetList();
    }
    
    private void initializeUI() {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: #2b2b2b;");
        
        // 标题
        Label titleLabel = new Label("星系信息: " + starSystem.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // 星系基本信息
        VBox systemInfoBox = new VBox(5);
        systemInfoBox.setPadding(new Insets(10));
        systemInfoBox.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
        
        Label starTypeLabel = new Label("恒星类型: " + starSystem.getStarType().getDisplayName());
        starTypeLabel.setStyle("-fx-text-fill: white;");
        
        Label planetCountLabel = new Label("行星数量: " + starSystem.getPlanets().size());
        planetCountLabel.setStyle("-fx-text-fill: white;");
        
        // 显示控制该星系的派系
        Label controllingFactionLabel;
        if (starSystem.getControllingFaction() != null) {
            String controllingFactionText = "控制派系: " + starSystem.getControllingFaction().getName();
            
            // 根据派系颜色设置标签颜色
            javafx.scene.paint.Color factionColor = starSystem.getControllingFaction().getColor();
            if (factionColor != null) {
                // 将JavaFX颜色转换为CSS颜色字符串
                String hexColor = String.format("#%02X%02X%02X",
                    (int)(factionColor.getRed() * 255),
                    (int)(factionColor.getGreen() * 255),
                    (int)(factionColor.getBlue() * 255));
                controllingFactionLabel = new Label(controllingFactionText);
                controllingFactionLabel.setStyle("-fx-text-fill: " + hexColor + ";");
            } else {
                controllingFactionLabel = new Label(controllingFactionText);
                controllingFactionLabel.setStyle("-fx-text-fill: white;");
            }
        } else {
            controllingFactionLabel = new Label("控制派系: 无");
            controllingFactionLabel.setStyle("-fx-text-fill: white;");
        }
        
        systemInfoBox.getChildren().addAll(starTypeLabel, planetCountLabel, controllingFactionLabel);
        
        // 行星列表和详情区域
        HBox mainContent = new HBox(10);
        mainContent.setPrefHeight(400);
        
        // 左侧：行星列表
        VBox planetListBox = new VBox(5);
        planetListBox.setPrefWidth(200);
        
        Label planetListLabel = new Label("行星列表:");
        planetListLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        
        planetListView = new ListView<>();
        planetListView.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");
        
        planetListBox.getChildren().addAll(planetListLabel, planetListView);
        
        // 右侧：行星详情
        VBox planetDetailsBox = new VBox(10);
        planetDetailsBox.setPrefWidth(400);
        
        Label detailsLabel = new Label("行星详情:");
        detailsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        
        planetDetailsContainer = new VBox(5);
        planetDetailsContainer.setPadding(new Insets(10));
        planetDetailsContainer.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
        planetDetailsContainer.setAlignment(Pos.TOP_LEFT);
        
        ScrollPane detailsScrollPane = new ScrollPane(planetDetailsContainer);
        detailsScrollPane.setFitToWidth(true);
        detailsScrollPane.setPrefHeight(350);
        detailsScrollPane.setStyle("-fx-background: #2d2d2d; -fx-border-color: #555555;");
        
        planetDetailsBox.getChildren().addAll(detailsLabel, detailsScrollPane);
        
        mainContent.getChildren().addAll(planetListBox, planetDetailsBox);
        
        this.getChildren().addAll(titleLabel, systemInfoBox, mainContent);
    }
    
    private void setupEventHandlers() {
        planetListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showPlanetDetails(newVal);
            }
        });
    }
    
    private void populatePlanetList() {
        planetListView.getItems().addAll(starSystem.getPlanets());
        planetListView.setCellFactory(param -> new ListCell<Planet>() {
            @Override
            protected void updateItem(Planet item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText((getIndex() + 1) + ". " + item.getName() + " (" + item.getType().getDisplayName() + ")");
                    setStyle("-fx-text-fill: white;");
                }
            }
        });
    }
    
    private void showPlanetDetails(Planet planet) {
        planetDetailsContainer.getChildren().clear();
        
        // 行星名称
        Label nameLabel = new Label(planet.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // 行星类型
        Label typeLabel = new Label("类型: " + planet.getType().getDisplayName());
        typeLabel.setStyle("-fx-text-fill: white;");
        
        // 行星大小
        Label sizeLabel = new Label("大小: " + planet.getSize());
        sizeLabel.setStyle("-fx-text-fill: white;");
        
        // 轨道距离
        Label orbitLabel = new Label("轨道距离: " + String.format("%.2f AU", planet.getOrbitDistance()));
        orbitLabel.setStyle("-fx-text-fill: white;");
        
        // 宜居度
        Label habitabilityLabel = new Label("宜居度: " + String.format("%.1f%%", planet.getHabitability() * 100));
        habitabilityLabel.setStyle("-fx-text-fill: white;");
        
        // 可产出资源
        VBox producibleResourcesBox = new VBox(3);
        producibleResourcesBox.setPadding(new Insets(5));
        producibleResourcesBox.setStyle("-fx-background-color: #4d4d4d; -fx-background-radius: 3;");
        
        Label producibleResourcesHeader = new Label("可产出资源:");
        producibleResourcesHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        
        VBox producibleResourcesList = new VBox(2);
        // 基础资源产出
        if (planet.getType().getBaseMetal() > 0) {
            HBox metalItem = new HBox(5);
            Label metalLabel = new Label("• 金属:");
            metalLabel.setStyle("-fx-text-fill: white;");
            
            Label metalValue = new Label(String.format("%.1f/回合", planet.getType().getBaseMetal() * planet.getSize()));
            metalValue.setStyle("-fx-text-fill: white;");
            
            metalItem.getChildren().addAll(metalLabel, metalValue);
            producibleResourcesList.getChildren().add(metalItem);
        }
        
        if (planet.getType().getBaseEnergy() > 0) {
            HBox energyItem = new HBox(5);
            Label energyLabel = new Label("• 能量:");
            energyLabel.setStyle("-fx-text-fill: white;");
            
            Label energyValue = new Label(String.format("%.1f/回合", planet.getType().getBaseEnergy() * planet.getSize()));
            energyValue.setStyle("-fx-text-fill: white;");
            
            energyItem.getChildren().addAll(energyLabel, energyValue);
            producibleResourcesList.getChildren().add(energyItem);
        }
        
        // 稀有资源
        for (ResourceType rareResource : ResourceType.getRareResources()) {
            float resourceAmount = planet.getResource(rareResource);
            if (resourceAmount > 0) {
                HBox rareItem = new HBox(5);
                Label rareLabel = new Label("• " + rareResource.getDisplayName() + ":");
                rareLabel.setStyle("-fx-text-fill: white;");
                
                Label rareValue = new Label(String.format("%.1f", resourceAmount));
                rareValue.setStyle("-fx-text-fill: white;");
                
                rareItem.getChildren().addAll(rareLabel, rareValue);
                producibleResourcesList.getChildren().add(rareItem);
            }
        }
        
        if (producibleResourcesList.getChildren().isEmpty()) {
            Label noResourcesLabel = new Label("无可产出资源");
            noResourcesLabel.setStyle("-fx-text-fill: #888888;");
            producibleResourcesList.getChildren().add(noResourcesLabel);
        }
        
        producibleResourcesBox.getChildren().addAll(producibleResourcesHeader, producibleResourcesList);
        
        // 殖民地信息和操作
        VBox colonyBox = new VBox(3);
        colonyBox.setPadding(new Insets(5));
        colonyBox.setStyle("-fx-background-color: #4d4d4d; -fx-background-radius: 3;");
        
        Label colonyHeader = new Label("殖民地信息:");
        colonyHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        
        if (planet.getColony() != null) {
            // 已殖民行星信息
            Label colonyStatus = new Label("状态: 已殖民");
            colonyStatus.setStyle("-fx-text-fill: #4CAF50;");
            
            Label factionLabel = new Label("派系: " + planet.getColony().getFaction().getName());
            factionLabel.setStyle("-fx-text-fill: white;");
            
            Label populationLabel = new Label("人口: " + String.format("%,d", planet.getColony().getTotalPopulation()));
            populationLabel.setStyle("-fx-text-fill: white;");
            
            // 显示人口增长速度信息
            float growthRate = planet.getColony().getGrowthRate();
            String growthDescription = "稳定";
            if (planet.getHabitability() < 0.3) {
                growthDescription = "快速下降";
            } else if (planet.getHabitability() < 0.5) {
                growthDescription = "缓慢下降";
            } else if (planet.getHabitability() > 0.8) {
                growthDescription = "快速增长";
            }
            
            Label growthLabel = new Label("人口增长: " + growthDescription);
            growthLabel.setStyle("-fx-text-fill: " + (growthDescription.contains("下降") ? "#f44336" : "#4CAF50") + ";");
            
            // 建筑按钮
            Button manageBuildingsButton = new Button("管理建筑");
            manageBuildingsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            manageBuildingsButton.setOnAction(e -> showBuildingManagement(planet));
            
            colonyBox.getChildren().addAll(colonyHeader, colonyStatus, factionLabel, populationLabel, growthLabel, manageBuildingsButton);
        } else if (planet.getType().isColonizable() && playerFaction != null) {
            // 未殖民但可殖民的行星
            Label colonyStatus = new Label("状态: 未殖民");
            colonyStatus.setStyle("-fx-text-fill: #f44336;");
            
            Label colonizableLabel = new Label("可殖民: 是");
            colonizableLabel.setStyle("-fx-text-fill: #4CAF50;");
            
            // 宜居度影响描述
            String habitabilityEffect = "";
            if (planet.getHabitability() < 0.3) {
                habitabilityEffect = " (人口增长将快速下降)";
            } else if (planet.getHabitability() < 0.5) {
                habitabilityEffect = " (人口增长将缓慢下降)";
            } else if (planet.getHabitability() > 0.8) {
                habitabilityEffect = " (人口将快速增长)";
            }
            
            Label habitabilityEffectLabel = new Label("预计效果:" + habitabilityEffect);
            habitabilityEffectLabel.setStyle("-fx-text-fill: " + 
                (habitabilityEffect.contains("下降") ? "#f44336" : 
                 habitabilityEffect.contains("增长") ? "#4CAF50" : "#FFFFFF") + ";");
            
            // 殖民按钮
            Button colonizeButton = new Button("殖民此行星");
            colonizeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            colonizeButton.setOnAction(e -> colonizePlanet(planet));
            
            colonyBox.getChildren().addAll(colonyHeader, colonyStatus, colonizableLabel, habitabilityEffectLabel, colonizeButton);
        } else {
            // 不可殖民的行星
            Label colonyStatus = new Label("状态: 未殖民");
            colonyStatus.setStyle("-fx-text-fill: #f44336;");
            
            Label colonizableLabel = new Label("可殖民: 否");
            colonizableLabel.setStyle("-fx-text-fill: #f44336;");
            
            colonyBox.getChildren().addAll(colonyHeader, colonyStatus, colonizableLabel);
        }
        
        // 特性信息
        VBox traitsBox = new VBox(3);
        traitsBox.setPadding(new Insets(5));
        traitsBox.setStyle("-fx-background-color: #4d4d4d; -fx-background-radius: 3;");
        
        Label traitsHeader = new Label("行星特性:");
        traitsHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        
        VBox traitsList = new VBox(2);
        for (var trait : planet.getTraits()) {
            Label traitLabel = new Label("• " + trait.getDisplayName());
            traitLabel.setStyle("-fx-text-fill: white;");
            traitsList.getChildren().add(traitLabel);
        }
        
        traitsBox.getChildren().addAll(traitsHeader, traitsList);
        
        planetDetailsContainer.getChildren().addAll(
                nameLabel, typeLabel, sizeLabel, orbitLabel, habitabilityLabel,
                new Separator(),
                producibleResourcesBox,
                new Separator(),
                colonyBox,
                new Separator(),
                traitsBox
        );
    }
    
    // 殖民行星
    private void colonizePlanet(Planet planet) {
        if (playerFaction == null) {
            showAlert("错误", "无法获取玩家派系信息");
            return;
        }
        
        if (!planet.canColonize(playerFaction)) {
            showAlert("殖民失败", "您没有足够的科技来殖民这颗行星");
            return;
        }
        
        // 创建殖民地
        Colony colony = new Colony(planet, playerFaction);
        planet.setColony(colony);
        
        showAlert("殖民成功", "您已成功殖民 " + planet.getName());
        
        // 刷新显示
        Planet selectedPlanet = planetListView.getSelectionModel().getSelectedItem();
        if (selectedPlanet != null) {
            showPlanetDetails(selectedPlanet);
        }
    }
    
    // 显示建筑管理界面
    private void showBuildingManagement(Planet planet) {
        if (planet.getColony() == null) {
            showAlert("错误", "该行星上没有殖民地");
            return;
        }
        
        Colony colony = planet.getColony();
        
        Stage buildingStage = new Stage();
        buildingStage.setTitle("建筑管理 - " + planet.getName());
        buildingStage.initModality(Modality.WINDOW_MODAL);
        
        // 设置窗口图标
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/icon.png"));
            buildingStage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("无法加载窗口图标: " + e.getMessage());
        }
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        // 当前建筑列表
        Label buildingsLabel = new Label("已建造建筑:");
        buildingsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        
        ListView<Building> buildingsList = new ListView<>();
        buildingsList.getItems().addAll(colony.getBuildings());
        buildingsList.setPrefHeight(150);
        buildingsList.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");
        buildingsList.setCellFactory(param -> new ListCell<Building>() {
            @Override
            protected void updateItem(Building item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (等级 " + item.getLevel() + ")");
                    setStyle("-fx-text-fill: white;");
                }
            }
        });
        
        // 建造新建筑按钮
        Button buildNewButton = new Button("建造新建筑");
        buildNewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        buildNewButton.setOnAction(e -> showBuildNewBuildingDialog(colony, buildingStage));
        
        root.getChildren().addAll(buildingsLabel, buildingsList, buildNewButton);
        
        Scene scene = new Scene(root, 400, 300);
        buildingStage.setScene(scene);
        buildingStage.show();
    }
    
    // 显示建造新建筑对话框
    private void showBuildNewBuildingDialog(Colony colony, Stage parentStage) {
        Dialog<BuildingType> dialog = new Dialog<>();
        dialog.setTitle("选择建筑类型");
        dialog.setHeaderText("选择要建造的建筑类型");
        
        // 设置对话框按钮
        ButtonType buildButtonType = new ButtonType("建造", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buildButtonType, ButtonType.CANCEL);
        
        // 创建建筑类型选择框
        ComboBox<BuildingType> buildingTypeCombo = new ComboBox<>();
        buildingTypeCombo.getItems().addAll(BuildingType.values());
        buildingTypeCombo.setPromptText("选择建筑类型");
        // 设置显示建筑类型的中文名称
        buildingTypeCombo.setCellFactory(lv -> new ListCell<BuildingType>() {
            @Override
            protected void updateItem(BuildingType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        buildingTypeCombo.setButtonCell(new ListCell<BuildingType>() {
            @Override
            protected void updateItem(BuildingType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("选择建筑类型");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        VBox content = new VBox(10);
        content.getChildren().add(buildingTypeCombo);
        
        dialog.getDialogPane().setContent(content);
        
        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buildButtonType) {
                return buildingTypeCombo.getValue();
            }
            return null;
        });
        
        // 显示对话框并处理结果
        dialog.showAndWait().ifPresent(buildingType -> {
            // 创建建筑
            String buildingName = getBuildingTypeName(buildingType);
            BasicBuilding building = new BasicBuilding(buildingName, buildingType, 3); // 最大等级3
            
            // 检查是否可以建造
            if (colony.canBuild(building)) {
                // 建造建筑
                if (colony.build(building)) {
                    showAlert("建造成功", "成功建造 " + building.getName());
                    
                    // 关闭建筑管理窗口并重新打开以刷新列表
                    parentStage.close();
                    showBuildingManagement(colony.getPlanet());
                } else {
                    showAlert("建造失败", "无法建造选定的建筑");
                }
            } else {
                // 显示缺少的资源
                StringBuilder missingResources = new StringBuilder("资源不足:\n");
                for (ResourceRequirement req : building.getConstructionRequirements()) {
                    missingResources.append(req.getResourceType().getDisplayName())
                                   .append(": ").append(req.getAmount()).append("\n");
                }
                showAlert("资源不足", missingResources.toString());
            }
        });
    }
    
    // 获取建筑类型名称
    private String getBuildingTypeName(BuildingType type) {
        switch (type) {
            case FOOD_PRODUCTION: return "农场";
            case ENERGY_PRODUCTION: return "发电厂";
            case MINERAL_PRODUCTION: return "矿场";
            case RESEARCH: return "科研所";
            case HOUSING: return "住宅区";
            case ADMINISTRATION: return "行政中心";
            default: return type.getDisplayName();
        }
    }
    
    // 显示警告对话框
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // 设置窗口图标
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/icon.png"));
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("无法加载窗口图标: " + e.getMessage());
        }
        
        alert.showAndWait();
    }
    
    public static void showSystemInfo(StarSystem system, Faction playerFaction) {
        Stage dialog = new Stage();
        dialog.setTitle("星系详情 - " + system.getName());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(true);
        
        // 设置窗口图标
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                StarSystemInfoView.class.getResourceAsStream("/images/icon.png"));
            dialog.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("无法加载窗口图标: " + e.getMessage());
        }
        
        StarSystemInfoView view = new StarSystemInfoView(system, playerFaction);
        
        Scene scene = new Scene(view, 650, 500);
        scene.getStylesheets().add(StarSystemInfoView.class.getResource("/css/main.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }
}