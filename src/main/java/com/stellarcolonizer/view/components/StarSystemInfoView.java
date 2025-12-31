package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.colony.BasicBuilding;
import com.stellarcolonizer.model.colony.Building;
import com.stellarcolonizer.model.colony.Colony;
import com.stellarcolonizer.model.colony.ResourceRequirement;
import com.stellarcolonizer.model.colony.enums.BuildingType;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.fleet.Fleet;
import com.stellarcolonizer.model.galaxy.Planet;
import com.stellarcolonizer.model.galaxy.StarSystem;
import com.stellarcolonizer.model.galaxy.Hex;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StarSystemInfoView extends VBox {
    
    private StarSystem starSystem;
    private ListView<Planet> planetListView;
    private VBox planetDetailsContainer;
    private Faction playerFaction; // 添加玩家派系引用
    private List<Fleet> fleetList; // 添加舰队列表字段
    private FleetSelectionCallback fleetSelectionCallback; // 添加舰队选择回调
    private Hex hex; // 添加六边形引用，用于显示坐标信息
    
    public StarSystemInfoView(StarSystem starSystem, Faction playerFaction) {
        this.starSystem = starSystem;
        this.playerFaction = playerFaction;
        this.fleetList = null; // 初始化为null
        this.fleetSelectionCallback = null; // 初始化为null
        initializeUI();
        setupEventHandlers();
        populatePlanetList();
    }
    
    // 重载构造函数，接受舰队列表
    public StarSystemInfoView(StarSystem starSystem, Faction playerFaction, List<Fleet> fleetList) {
        this.starSystem = starSystem;
        this.playerFaction = playerFaction;
        this.fleetList = fleetList;
        this.fleetSelectionCallback = null; // 初始化为null
        initializeUI();
        setupEventHandlers();
        populatePlanetList();
    }
    
    // 重载构造函数，接受舰队列表和回调
    public StarSystemInfoView(StarSystem starSystem, Faction playerFaction, List<Fleet> fleetList, FleetSelectionCallback fleetSelectionCallback) {
        this.starSystem = starSystem;
        this.playerFaction = playerFaction;
        this.fleetList = fleetList;
        this.fleetSelectionCallback = fleetSelectionCallback;
        initializeUI();
        setupEventHandlers();
        populatePlanetList();
    }
    
    // 重载构造函数，接受六边形参数（用于显示空域坐标信息）
    public StarSystemInfoView(Hex hex, Faction playerFaction, List<Fleet> fleetList, FleetSelectionCallback fleetSelectionCallback) {
        this.starSystem = null; // 没有星系
        this.hex = hex; // 设置六边形
        this.playerFaction = playerFaction;
        this.fleetList = fleetList;
        this.fleetSelectionCallback = fleetSelectionCallback;
        initializeUI();
        setupEventHandlers();
        // 空域没有行星，所以不需要populatePlanetList()
    }
    
    // 重载构造函数，仅接受六边形参数（用于显示空域坐标信息，无舰队）
    public StarSystemInfoView(Hex hex, Faction playerFaction) {
        this.starSystem = null; // 没有星系
        this.hex = hex; // 设置六边形
        this.playerFaction = playerFaction;
        this.fleetList = null; // 无舰队
        this.fleetSelectionCallback = null; // 无回调
        initializeUI();
        setupEventHandlers();
        // 空域没有行星，所以不需要populatePlanetList()
    }
    
    private void initializeUI() {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: #2b2b2b;");
        
        // 标题
        Label titleLabel;
        if (starSystem != null) {
            titleLabel = new Label("星系信息: " + starSystem.getName());
        } else if (hex != null) {
            titleLabel = new Label("六边形详情: " + hex.getCoord());
        } else {
            titleLabel = new Label("舰队详情");
        }
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // 星系基本信息
        VBox systemInfoBox = new VBox(5);
        systemInfoBox.setPadding(new Insets(10));
        systemInfoBox.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
        
        if (starSystem != null) {
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
        } else if (hex != null) {
            // 显示六边形坐标信息
            Label coordLabel = new Label("坐标: " + hex.getCoord());
            coordLabel.setStyle("-fx-text-fill: white;");
            
            Label typeLabel = new Label("类型: " + hex.getType().getDisplayName());
            typeLabel.setStyle("-fx-text-fill: white;");
            
            Label visibilityLabel = new Label("可见度: " + String.format("%.1f%%", hex.getVisibility() * 100));
            visibilityLabel.setStyle("-fx-text-fill: white;");
            
            systemInfoBox.getChildren().addAll(coordLabel, typeLabel, visibilityLabel);
        } else {
            Label noSystemLabel = new Label("位置: 深空");
            noSystemLabel.setStyle("-fx-text-fill: white;");
            systemInfoBox.getChildren().add(noSystemLabel);
        }
        
        // 舰队信息区域 - 如果有舰队列表则显示
        VBox fleetInfoBox = null;
        if (fleetList != null && !fleetList.isEmpty()) {
            fleetInfoBox = new VBox(5);
            fleetInfoBox.setPadding(new Insets(10));
            fleetInfoBox.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
            
            Label fleetHeaderLabel = new Label("舰队信息:");
            fleetHeaderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
            
            ListView<Fleet> fleetListView = new ListView<>();
            fleetListView.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");
            fleetListView.getItems().addAll(fleetList);
            
            // 设置单元格工厂以显示舰队信息
            fleetListView.setCellFactory(param -> new ListCell<Fleet>() {
                @Override
                protected void updateItem(Fleet item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName() + " (舰船: " + item.getShipCount() + 
                               ", 战斗力: " + String.format("%.0f", item.getTotalCombatPower()) + 
                               ", 任务: " + item.getCurrentMission().getDisplayName() + ")");
                        setStyle("-fx-text-fill: white;");
                    }
                }
            });
            
            // 添加选择监听器，当选择舰队时高亮显示可移动范围
            fleetListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && fleetSelectionCallback != null) {
                    // 通过回调通知HexMapView选中舰队
                    // 但注意：现在只有在点击移动按钮时才应该高亮可移动范围
                    // 因此我们不在此处调用回调
                    // fleetSelectionCallback.onFleetSelected(newVal);
                }
            });
            
            // 添加移动按钮，允许玩家移动选中的舰队
            Button moveFleetButton = new Button("移动选中舰队");
            moveFleetButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            moveFleetButton.setOnAction(e -> {
                Fleet selectedFleet = fleetListView.getSelectionModel().getSelectedItem();
                if (selectedFleet != null) {
                    // 检查舰队是否属于AI，如果是AI舰队，则不允许玩家移动
                    if (selectedFleet.getFaction() != null && selectedFleet.getFaction().isAI()) {
                        showAlert("无法移动", "AI舰队由AI自动控制，不能手动移动");
                        return;
                    }
                    
                    // 检查舰队是否本回合已移动
                    if (selectedFleet.hasMovedThisTurn()) {
                        showAlert("移动限制", "该舰队本回合已移动过，无法再次移动");
                        return;
                    }
                    
                    // 通过回调通知HexMapView选中舰队以高亮可移动范围
                    if (fleetSelectionCallback != null) {
                        fleetSelectionCallback.onFleetSelected(selectedFleet);
                        
                        // 显示提示信息
                        showAlert("移动准备", "已高亮显示可移动范围，请点击目标六边形进行移动");
                    } else {
                        System.out.println("准备高亮显示舰队: " + selectedFleet.getName() + " 的可移动范围");
                    }
                } else {
                    showAlert("选择错误", "请先选择一个舰队");
                }
            });
            
            fleetInfoBox.getChildren().addAll(fleetHeaderLabel, fleetListView, moveFleetButton);
        }
        
        // 行星列表和详情区域
        HBox mainContent = new HBox(10);
        mainContent.setPrefHeight(400);
        
        if (starSystem != null) {
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
        } else {
            // 如果没有星系，只显示舰队信息或六边形信息
            if (hex != null) {
                // 显示六边形的额外信息
                VBox hexInfoBox = new VBox(5);
                hexInfoBox.setPadding(new Insets(10));
                hexInfoBox.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
                
                Label hexInfoLabel = new Label("六边形信息:");
                hexInfoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
                
                // 根据六边形类型显示不同信息
                if (hex.getType().toString().equals("EMPTY")) {
                    Label hexTypeLabel = new Label("这是一个空域六边形");
                    hexTypeLabel.setStyle("-fx-text-fill: white;");
                    
                    Label hexDescription = new Label("空域可以自由移动舰队，但不提供任何资源或战略优势");
                    hexDescription.setStyle("-fx-text-fill: #aaaaaa; -fx-wrap-text: true;");
                    
                    hexInfoBox.getChildren().addAll(hexInfoLabel, hexTypeLabel, hexDescription);
                } else {
                    Label hexTypeLabel = new Label("六边形类型: " + hex.getType().getDisplayName());
                    hexTypeLabel.setStyle("-fx-text-fill: white;");
                    hexInfoBox.getChildren().addAll(hexInfoLabel, hexTypeLabel);
                }
                
                mainContent.getChildren().add(hexInfoBox);
            } else {
                Label noSystemLabel = new Label("此位置没有星系，只有舰队");
                noSystemLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
                mainContent.getChildren().add(noSystemLabel);
            }
        }
        
        // 添加所有组件到主容器
        this.getChildren().addAll(titleLabel, systemInfoBox);
        
        // 如果有舰队信息，添加舰队信息框
        if (fleetInfoBox != null) {
            this.getChildren().add(fleetInfoBox);
        }
        
        if (starSystem != null) {
            this.getChildren().add(mainContent);
        }
    }
    
    private void setupEventHandlers() {
        if (starSystem != null) {
            planetListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    showPlanetDetails(newVal);
                }
            });
        }
    }
    
    private void populatePlanetList() {
        if (starSystem != null) {
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
            
            // 建筑按钮
            Button manageBuildingsButton = new Button("管理建筑");
            manageBuildingsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            manageBuildingsButton.setOnAction(e -> showBuildingManagement(planet));
            
            colonyBox.getChildren().addAll(colonyHeader, colonyStatus, factionLabel, populationLabel, manageBuildingsButton);
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
        
        // 计算殖民成本
        Map<ResourceType, Float> colonizationCost = planet.calculateColonizationCost();
        int requiredPopulation = planet.calculateRequiredPopulation();

        // 检查是否有足够的殖民地来迁移人口
        if (playerFaction.getColonies().isEmpty()) {
            showAlert("殖民失败", "您没有其他殖民地来迁移人口");
            return;
        }
        
        // 检查是否有足够的总人口来进行迁移
        int totalPopulationInColonies = playerFaction.getColonies().stream()
                .mapToInt(Colony::getTotalPopulation)
                .sum();
        if (totalPopulationInColonies < requiredPopulation) {
            showAlert("殖民失败", "您没有足够的总人口来迁移 (" + requiredPopulation + " 需要)");
            return;
        }
        
        // 弹出对话框让用户先选择源殖民地
        Dialog<Colony> sourceColonyDialog = new Dialog<>();
        sourceColonyDialog.setTitle("选择源殖民地");
        sourceColonyDialog.setHeaderText("选择要迁移人口的源殖民地");
        
        VBox dialogContent = new VBox(10);
        
        Label descriptionLabel = new Label("选择一个殖民地作为人口迁移的来源");
        descriptionLabel.setWrapText(true);
        
        ComboBox<Colony> sourceColonyCombo = new ComboBox<>();
        sourceColonyCombo.getItems().addAll(playerFaction.getColonies());
        sourceColonyCombo.setPromptText("选择源殖民地");
        
        // 设置下拉框样式
        sourceColonyCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<Colony>() {
            @Override
            protected void updateItem(Colony item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (人口: " + String.format("%,d", item.getTotalPopulation()) + ")");
                }
            }
        });
        sourceColonyCombo.setButtonCell(new javafx.scene.control.ListCell<Colony>() {
            @Override
            protected void updateItem(Colony item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("选择源殖民地");
                } else {
                    setText(item.getName() + " (人口: " + String.format("%,d", item.getTotalPopulation()) + ")");
                }
            }
        });
        
        dialogContent.getChildren().addAll(descriptionLabel, sourceColonyCombo);
        sourceColonyDialog.getDialogPane().setContent(dialogContent);

        ButtonType confirmButtonType = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        sourceColonyDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // 设置结果转换器
        sourceColonyDialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return sourceColonyCombo.getValue(); // 返回选中的殖民地
            }
            return null; // 取消按钮或其他情况返回null
        });

        Optional<Colony> sourceResult = sourceColonyDialog.showAndWait();
        if (!sourceResult.isPresent()) {
            return; // 用户取消了操作
        }

        Colony sourceColony = sourceResult.get();
        if (sourceColony == null) {
            showAlert("错误", "请选择源殖民地");
            return;
        }

        // 获取源殖民地和目标行星所在的星系
        StarSystem sourceSystem = sourceColony.getPlanet().getStarSystem();
        StarSystem targetSystem = planet.getStarSystem();

        // 获取星系所在的六边形
        Hex sourceHex = null;
        Hex targetHex = null;

        // 从玩家派系获取星系
        for (StarSystem system : playerFaction.getGalaxy().getStarSystems()) {
            if (system.equals(sourceSystem)) {
                sourceHex = playerFaction.getGalaxy().getHexForStarSystem(system);
            }
            if (system.equals(targetSystem)) {
                targetHex = playerFaction.getGalaxy().getHexForStarSystem(system);
            }
        }

        double distance = 0.0;
        if (sourceHex != null && targetHex != null) {
            // 计算六边形之间的距离
            distance = (Math.abs(sourceHex.getCoord().q - targetHex.getCoord().q) +
                       Math.abs(sourceHex.getCoord().r - targetHex.getCoord().r) +
                       Math.abs(sourceHex.getCoord().s - targetHex.getCoord().s)) / 2.0;
        }

        // 根据距离计算成本乘数
        double distanceCostMultiplier = 1.0 + (distance * 0.1); // 每格距离增加10%成本

        // 应用距离成本乘数
        Map<ResourceType, Float> adjustedColonizationCost = new EnumMap<>(ResourceType.class);
        for (Map.Entry<ResourceType, Float> costEntry : colonizationCost.entrySet()) {
            adjustedColonizationCost.put(costEntry.getKey(), costEntry.getValue() * (float)distanceCostMultiplier);
        }

        // 显示成本信息
        StringBuilder costInfo = new StringBuilder("殖民成本:\n");
        for (Map.Entry<ResourceType, Float> costEntry : adjustedColonizationCost.entrySet()) {
            costInfo.append(costEntry.getKey().getDisplayName())
                    .append(": ").append(String.format("%.1f", costEntry.getValue()))
                    .append("\n");
        }
        costInfo.append("\n距离: ").append(String.format("%.1f格", distance))
                .append(" (成本增加").append(String.format("%.0f%%", (distanceCostMultiplier - 1.0) * 100)).append(")");

        // 检查资源是否足够（使用调整后的成本）
        boolean hasEnoughResources = true;
        StringBuilder insufficientResources = new StringBuilder("资源不足:\n");
        for (Map.Entry<ResourceType, Float> costEntry : adjustedColonizationCost.entrySet()) {
            float available = playerFaction.getResourceStockpile().getResource(costEntry.getKey());
            if (available < costEntry.getValue()) {
                hasEnoughResources = false;
                insufficientResources.append(costEntry.getKey().getDisplayName())
                        .append(": 需要 ").append(String.format("%.1f", costEntry.getValue()))
                        .append(", 拥有 ").append(String.format("%.1f", available))
                        .append("\n");
            }
        }

        if (!hasEnoughResources) {
            // 显示距离成本信息
            showAlert("殖民失败", insufficientResources.toString() +
                     "\n距离成本: " + String.format("%.1f格", distance) + " (成本增加" + String.format("%.0f%%", (distanceCostMultiplier - 1.0) * 100) + ")");
            return;
        }

        // 弹出对话框显示成本信息并选择迁移人口数量
        Dialog<Integer> transferDialog = new Dialog<>();
        transferDialog.setTitle("确认殖民");
        transferDialog.setHeaderText("确认殖民操作");

        VBox dialogContent2 = new VBox(10);

        Label costLabel = new Label(costInfo.toString());
        costLabel.setWrapText(true);
        costLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");

        Label sourceColonyLabel = new Label("源殖民地: " + sourceColony.getName());
        sourceColonyLabel.setWrapText(true);
        sourceColonyLabel.setStyle("-fx-text-fill: white;");

        Label descriptionLabel2 = new Label("需要迁移 " + requiredPopulation + " 人口到新殖民地");
        descriptionLabel2.setWrapText(true);

        Spinner<Integer> populationSpinner = new Spinner<>(1000, requiredPopulation, Math.min(requiredPopulation, 10000), 100);
        populationSpinner.getValueFactory().setValue(Math.min(requiredPopulation, Math.min(requiredPopulation, 10000)));
        populationSpinner.setPrefWidth(150);
        
        Label populationLabel = new Label("迁移人口数量:");
        
        dialogContent2.getChildren().addAll(costLabel, sourceColonyLabel, descriptionLabel2, populationLabel, populationSpinner);
        transferDialog.getDialogPane().setContent(dialogContent2);
        
        ButtonType confirmButtonType2 = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        transferDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType2, ButtonType.CANCEL);
        
        // 设置结果转换器
        transferDialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType2) {
                return populationSpinner.getValue(); // 返回迁移的人口数量
            }
            return null; // 取消按钮或其他情况返回null
        });
        
        Optional<Integer> result = transferDialog.showAndWait();
        if (!result.isPresent()) {
            return; // 用户取消了操作
        }
        
        int populationToTransfer = result.get();
        if (populationToTransfer == 0) {
            showAlert("错误", "请确认迁移人口数量");
            return;
        }
        
        if (populationToTransfer > sourceColony.getTotalPopulation() - 100) { // 确保源殖民地至少保留100人口
            showAlert("错误", "源殖民地必须保留至少100人口");
            return;
        }
        
        if (populationToTransfer > requiredPopulation) {
            showAlert("错误", "迁移人口不能超过所需人口 (" + requiredPopulation + ")");
            return;
        }
        
        // 更新成本为调整后的成本
        colonizationCost = adjustedColonizationCost;

        // 扣除资源成本
        for (Map.Entry<ResourceType, Float> costEntry : colonizationCost.entrySet()) {
            playerFaction.getResourceStockpile().consumeResource(costEntry.getKey(), costEntry.getValue());
        }
        
        // 从源殖民地迁移人口
        int farmersToTransfer = (int) (populationToTransfer * 0.4); // 40% 农民
        int workersToTransfer = (int) (populationToTransfer * 0.3); // 30% 工人
        int minersToTransfer = (int) (populationToTransfer * 0.15); // 15% 矿工
        int artisansToTransfer = (int) (populationToTransfer * 0.15); // 15% 工匠
        
        // 获取源殖民地的原始总人口
        int originalTotalPopulation = sourceColony.getTotalPopulation();
        
        // 更新源殖民地的人口
        Map<com.stellarcolonizer.model.colony.enums.PopType, Integer> sourcePopulation = sourceColony.getPopulationByType();
        int currentFarmers = sourcePopulation.getOrDefault(com.stellarcolonizer.model.colony.enums.PopType.FARMERS, 0);
        int currentWorkers = sourcePopulation.getOrDefault(com.stellarcolonizer.model.colony.enums.PopType.WORKERS, 0);
        int currentMiners = sourcePopulation.getOrDefault(com.stellarcolonizer.model.colony.enums.PopType.MINERS, 0);
        int currentArtisans = sourcePopulation.getOrDefault(com.stellarcolonizer.model.colony.enums.PopType.ARTISANS, 0);
        
        Map<com.stellarcolonizer.model.colony.enums.PopType, Integer> newSourcePopulation = new EnumMap<>(com.stellarcolonizer.model.colony.enums.PopType.class);
        newSourcePopulation.put(com.stellarcolonizer.model.colony.enums.PopType.FARMERS, Math.max(0, currentFarmers - farmersToTransfer));
        newSourcePopulation.put(com.stellarcolonizer.model.colony.enums.PopType.WORKERS, Math.max(0, currentWorkers - workersToTransfer));
        newSourcePopulation.put(com.stellarcolonizer.model.colony.enums.PopType.MINERS, Math.max(0, currentMiners - minersToTransfer));
        newSourcePopulation.put(com.stellarcolonizer.model.colony.enums.PopType.ARTISANS, Math.max(0, currentArtisans - artisansToTransfer));
        
        sourceColony.setPopulationByType(newSourcePopulation);
        
        // 更新源殖民地总人口
        sourceColony.totalPopulationProperty().set(originalTotalPopulation - populationToTransfer);
        
        // 创建新殖民地
        Colony colony = new Colony(planet, playerFaction);
        
        // 设置新殖民地的人口（从源殖民地迁移来的人口）
        Map<com.stellarcolonizer.model.colony.enums.PopType, Integer> newColonyPopulation = new EnumMap<>(com.stellarcolonizer.model.colony.enums.PopType.class);
        newColonyPopulation.put(com.stellarcolonizer.model.colony.enums.PopType.FARMERS, farmersToTransfer);
        newColonyPopulation.put(com.stellarcolonizer.model.colony.enums.PopType.WORKERS, workersToTransfer);
        newColonyPopulation.put(com.stellarcolonizer.model.colony.enums.PopType.MINERS, minersToTransfer);
        newColonyPopulation.put(com.stellarcolonizer.model.colony.enums.PopType.ARTISANS, artisansToTransfer);
        
        colony.setPopulationByType(newColonyPopulation);
        
        // 设置行星的殖民地
        planet.setColony(colony);
        playerFaction.addColony(colony);
        
        showAlert("殖民成功", "您已成功殖民 " + planet.getName() + 
                  "，从 " + sourceColony.getName() + " 迁移了 " + populationToTransfer + " 人口");
        
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
    
    public static void showSystemInfoWithFleets(StarSystem system, Faction playerFaction, List<Fleet> fleetList) {
        showSystemInfoWithFleets(system, playerFaction, fleetList, null);
    }
    
    public static void showSystemInfoWithFleets(StarSystem system, Faction playerFaction, List<Fleet> fleetList, FleetSelectionCallback fleetSelectionCallback) {
        Stage dialog = new Stage();
        String title = system != null ? "星系详情 - " + system.getName() + " (包含舰队信息)" : "舰队详情";
        dialog.setTitle(title);
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
        
        StarSystemInfoView view = new StarSystemInfoView(system, playerFaction, fleetList, fleetSelectionCallback);
        
        Scene scene = new Scene(view, 700, 600); // 增加窗口大小以适应舰队信息
        scene.getStylesheets().add(StarSystemInfoView.class.getResource("/css/main.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }
    
    public static void showHexInfoWithFleets(Hex hex, Faction playerFaction, List<Fleet> fleetList, FleetSelectionCallback fleetSelectionCallback) {
        Stage dialog = new Stage();
        String title = "六边形详情 - " + hex.getCoord() + " (包含舰队信息)";
        dialog.setTitle(title);
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
        
        StarSystemInfoView view = new StarSystemInfoView(hex, playerFaction, fleetList, fleetSelectionCallback);
        
        Scene scene = new Scene(view, 700, 600); // 增加窗口大小以适应舰队信息
        scene.getStylesheets().add(StarSystemInfoView.class.getResource("/css/main.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }
    
    public static void showHexInfo(Hex hex, Faction playerFaction) {
        Stage dialog = new Stage();
        String title = "六边形详情 - " + hex.getCoord();
        dialog.setTitle(title);
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
        
        StarSystemInfoView view = new StarSystemInfoView(hex, playerFaction);
        
        Scene scene = new Scene(view, 700, 600); // 增加窗口大小以适应舰队信息
        scene.getStylesheets().add(StarSystemInfoView.class.getResource("/css/main.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }
}