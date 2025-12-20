package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.fleet.*;
import com.stellarcolonizer.model.fleet.enums.*;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import com.stellarcolonizer.model.technology.Technology;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;
import java.util.stream.Collectors;

public class ShipDesignerUI extends BorderPane {

    // 当前设计
    private ShipDesign currentDesign;
    private final ObservableList<ShipModule> availableModules;
    private final ObservableList<ShipModule> currentModules;

    // 舰船属性显示
    private Label shipNameLabel;
    private Label shipClassLabel;
    private Label hitPointsLabel;
    private Label shieldLabel;
    private Label armorLabel;
    private Label evasionLabel;
    private Label speedLabel;
    private Label warpSpeedLabel;
    private Label maneuverabilityLabel;
    private Label crewLabel;
    private Label cargoLabel;
    private Label fuelLabel;
    private Label combatPowerLabel;
    private Label strategicValueLabel;

    // 资源成本显示
    private VBox costPanel;
    private VBox maintenancePanel;

    // 模块列表
    private ListView<ShipModule> availableModulesList;
    private ListView<ShipModule> currentModulesList;

    // 设计验证
    private Label validationLabel;
    private ProgressBar powerBalanceBar;
    private ProgressBar hullSpaceBar;

    // 控制按钮
    private Button addModuleButton;
    private Button removeModuleButton;
    private Button saveDesignButton;
    private Button newDesignButton;
    private Button copyDesignButton;

    // 舰船等级选择
    private ComboBox<ShipClass> shipClassComboBox;

    // 设计列表
    private ComboBox<ShipDesign> existingDesigns;
    private ObservableList<ShipDesign> savedDesigns;

    // 模块分类
    private TabPane moduleTabs;

    public ShipDesignerUI() {
        this.availableModules = FXCollections.observableArrayList();
        this.currentModules = FXCollections.observableArrayList();
        this.savedDesigns = FXCollections.observableArrayList();

        initializeUI();
        setupEventHandlers();
        loadDefaultModules();
        createNewDesign(ShipClass.CORVETTE);
    }

    private void initializeUI() {
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #1e1e1e;");

        // 顶部：设计控制
        HBox topPanel = createTopPanel();
        setTop(topPanel);

        // 左侧：模块库
        VBox leftPanel = createModuleLibraryPanel();

        // 中心：当前设计
        VBox centerPanel = createDesignPanel();

        // 右侧：属性面板
        VBox rightPanel = createPropertyPanel();

        // 底部：验证和控制
        HBox bottomPanel = createBottomPanel();

        // 使用SplitPane布局
        SplitPane mainSplit = new SplitPane();
        mainSplit.getItems().addAll(leftPanel, centerPanel, rightPanel);
        mainSplit.setDividerPositions(0.25, 0.65);

        VBox mainLayout = new VBox(10);
        mainLayout.getChildren().addAll(mainSplit, bottomPanel);

        setCenter(mainLayout);
    }

    private HBox createTopPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(5));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        // 舰船等级选择
        shipClassComboBox = new ComboBox<>();
        shipClassComboBox.getItems().addAll(ShipClass.values());
        shipClassComboBox.setValue(ShipClass.CORVETTE);
        shipClassComboBox.setPrefWidth(150);

        // 设计列表
        existingDesigns = new ComboBox<>(savedDesigns);
        existingDesigns.setPromptText("选择现有设计");
        existingDesigns.setPrefWidth(200);

        // 新设计按钮
        newDesignButton = new Button("新建设计");
        newDesignButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        // 复制设计按钮
        copyDesignButton = new Button("复制设计");
        copyDesignButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        // 保存设计按钮
        saveDesignButton = new Button("保存设计");
        saveDesignButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");

        panel.getChildren().addAll(
                new Label("舰船等级:"), shipClassComboBox,
                new Label("现有设计:"), existingDesigns,
                newDesignButton, copyDesignButton, saveDesignButton
        );

        return panel;
    }

    private VBox createModuleLibraryPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(300);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        Label title = new Label("模块库");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        // 模块分类选项卡
        moduleTabs = new TabPane();
        moduleTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // 武器模块
        Tab weaponsTab = new Tab("武器", createModuleList(ModuleType.WEAPON));

        // 防御模块
        Tab defenseTab = new Tab("防御", createModuleList(ModuleType.DEFENSE));

        // 功能模块
        Tab utilityTab = new Tab("功能", createModuleList(ModuleType.UTILITY));

        // 引擎模块
        Tab engineTab = new Tab("引擎", createModuleList(ModuleType.ENGINE));

        // 电力模块
        Tab powerTab = new Tab("电力", createModuleList(ModuleType.POWER));

        moduleTabs.getTabs().addAll(weaponsTab, defenseTab, utilityTab, engineTab, powerTab);

        // 模块详细信息
        VBox moduleDetailPanel = createModuleDetailPanel();

        panel.getChildren().addAll(title, moduleTabs, moduleDetailPanel);
        return panel;
    }

    private ListView<ShipModule> createModuleList(ModuleType moduleType) {
        ListView<ShipModule> listView = new ListView<>();
        listView.setPrefHeight(200);
        listView.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");

        // 根据类型筛选模块
        ObservableList<ShipModule> filteredModules = availableModules.filtered(
                module -> module.getType() == moduleType
        );
        listView.setItems(filteredModules);
        listView.setCellFactory(lv -> new ModuleListCell());

        return listView;
    }

    private VBox createModuleDetailPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        Label title = new Label("模块详情");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        // 模块属性显示
        VBox details = new VBox(3);
        details.setId("module-details");

        panel.getChildren().addAll(title, details);
        return panel;
    }

    private VBox createDesignPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        Label title = new Label("当前设计");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        // 舰船信息
        HBox shipInfo = createShipInfoPanel();

        // 当前模块列表
        Label modulesTitle = new Label("已安装模块");
        modulesTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        modulesTitle.setTextFill(Color.WHITE);

        currentModulesList = new ListView<>(currentModules);
        currentModulesList.setPrefHeight(300);
        currentModulesList.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");
        currentModulesList.setCellFactory(lv -> new ModuleListCell());

        // 模块操作按钮
        HBox moduleButtons = new HBox(10);
        addModuleButton = new Button("添加模块");
        removeModuleButton = new Button("移除模块");

        addModuleButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        removeModuleButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        moduleButtons.getChildren().addAll(addModuleButton, removeModuleButton);

        panel.getChildren().addAll(title, shipInfo, modulesTitle, currentModulesList, moduleButtons);
        return panel;
    }

    private HBox createShipInfoPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        // 舰船名称
        VBox nameBox = new VBox(2);
        Label nameLabel = new Label("名称:");
        nameLabel.setTextFill(Color.LIGHTGRAY);
        shipNameLabel = new Label();
        shipNameLabel.setTextFill(Color.WHITE);
        shipNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameBox.getChildren().addAll(nameLabel, shipNameLabel);

        // 舰船等级
        VBox classBox = new VBox(2);
        Label classLabel = new Label("等级:");
        classLabel.setTextFill(Color.LIGHTGRAY);
        shipClassLabel = new Label();
        shipClassLabel.setTextFill(Color.WHITE);
        classBox.getChildren().addAll(classLabel, shipClassLabel);

        panel.getChildren().addAll(nameBox, classBox);
        return panel;
    }

    private VBox createPropertyPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(350);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        Label title = new Label("舰船属性");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        // 属性网格
        GridPane attributeGrid = new GridPane();
        attributeGrid.setHgap(10);
        attributeGrid.setVgap(5);
        attributeGrid.setPadding(new Insets(10));
        attributeGrid.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        // 战斗属性
        addAttributeRow(attributeGrid, 0, "生命值:", hitPointsLabel = new Label());
        addAttributeRow(attributeGrid, 1, "护盾:", shieldLabel = new Label());
        addAttributeRow(attributeGrid, 2, "装甲:", armorLabel = new Label());
        addAttributeRow(attributeGrid, 3, "回避率:", evasionLabel = new Label());

        // 移动属性
        addAttributeRow(attributeGrid, 4, "引擎功率:", speedLabel = new Label());
        addAttributeRow(attributeGrid, 5, "曲速等级:", warpSpeedLabel = new Label());
        addAttributeRow(attributeGrid, 6, "机动性:", maneuverabilityLabel = new Label());

        // 容量属性
        addAttributeRow(attributeGrid, 7, "船员:", crewLabel = new Label());
        addAttributeRow(attributeGrid, 8, "货舱:", cargoLabel = new Label());
        addAttributeRow(attributeGrid, 9, "燃料:", fuelLabel = new Label());

        // 综合评分
        VBox ratingBox = new VBox(5);
        ratingBox.setPadding(new Insets(10));
        ratingBox.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        Label ratingTitle = new Label("综合评分");
        ratingTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        ratingTitle.setTextFill(Color.WHITE);

        addRatingRow(ratingBox, "战斗力:", combatPowerLabel = new Label());
        addRatingRow(ratingBox, "战略价值:", strategicValueLabel = new Label());

        // 资源成本面板
        costPanel = new VBox(5);
        costPanel.setPadding(new Insets(10));
        costPanel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        Label costTitle = new Label("建造成本");
        costTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        costTitle.setTextFill(Color.WHITE);

        // 维护成本面板
        maintenancePanel = new VBox(5);
        maintenancePanel.setPadding(new Insets(10));
        maintenancePanel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        Label maintenanceTitle = new Label("维护成本");
        maintenanceTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        maintenanceTitle.setTextFill(Color.WHITE);

        panel.getChildren().addAll(
                title, attributeGrid, ratingBox,
                costTitle, costPanel,
                maintenanceTitle, maintenancePanel
        );

        return panel;
    }

    private void addAttributeRow(GridPane grid, int row, String label, Label value) {
        Label nameLabel = new Label(label);
        nameLabel.setTextFill(Color.LIGHTGRAY);

        value.setTextFill(Color.WHITE);
        value.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        grid.add(nameLabel, 0, row);
        grid.add(value, 1, row);
    }

    private void addRatingRow(VBox box, String label, Label value) {
        HBox row = new HBox(10);

        Label nameLabel = new Label(label);
        nameLabel.setTextFill(Color.LIGHTGRAY);
        nameLabel.setPrefWidth(80);

        value.setTextFill(Color.YELLOW);
        value.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        row.getChildren().addAll(nameLabel, value);
        box.getChildren().add(row);
    }

    private HBox createBottomPanel() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        // 验证信息
        VBox validationBox = new VBox(5);
        validationLabel = new Label();
        validationLabel.setTextFill(Color.RED);
        validationLabel.setWrapText(true);

        // 能源平衡指示器
        HBox powerBox = new HBox(5);
        Label powerLabel = new Label("能源平衡:");
        powerLabel.setTextFill(Color.LIGHTGRAY);
        powerBalanceBar = new ProgressBar();
        powerBalanceBar.setPrefWidth(200);
        powerBalanceBar.setStyle("-fx-accent: #4CAF50;");
        powerBox.getChildren().addAll(powerLabel, powerBalanceBar);

        // 船体空间指示器
        HBox spaceBox = new HBox(5);
        Label spaceLabel = new Label("船体空间:");
        spaceLabel.setTextFill(Color.LIGHTGRAY);
        hullSpaceBar = new ProgressBar();
        hullSpaceBar.setPrefWidth(200);
        hullSpaceBar.setStyle("-fx-accent: #2196F3;");
        spaceBox.getChildren().addAll(spaceLabel, hullSpaceBar);

        validationBox.getChildren().addAll(validationLabel, powerBox, spaceBox);

        panel.getChildren().add(validationBox);
        return panel;
    }

    private void setupEventHandlers() {
        // 舰船等级选择
        shipClassComboBox.setOnAction(e -> {
            ShipClass selectedClass = shipClassComboBox.getValue();
            if (selectedClass != null) {
                createNewDesign(selectedClass);
            }
        });

        // 现有设计选择
        existingDesigns.setOnAction(e -> {
            ShipDesign selectedDesign = existingDesigns.getValue();
            if (selectedDesign != null) {
                loadDesign(selectedDesign);
            }
        });

        // 新建设计
        newDesignButton.setOnAction(e -> {
            ShipClass selectedClass = shipClassComboBox.getValue();
            if (selectedClass != null) {
                createNewDesign(selectedClass);
            }
        });

        // 复制设计
        copyDesignButton.setOnAction(e -> {
            if (currentDesign != null) {
                String newName = currentDesign.getName() + " 复制版";
                ShipDesign copy = currentDesign.createCopy(newName);
                savedDesigns.add(copy);
                loadDesign(copy);
            }
        });

        // 保存设计
        saveDesignButton.setOnAction(e -> {
            if (currentDesign != null && currentDesign.isValidDesign()) {
                // 如果设计是新创建的，添加到列表
                if (!savedDesigns.contains(currentDesign)) {
                    savedDesigns.add(currentDesign);
                    existingDesigns.setItems(savedDesigns);
                }
                showAlert("设计已保存", currentDesign.getFullName() + " 已保存到设计库。");
            } else {
                showAlert("设计无效", "请修正设计中的问题后再保存。");
            }
        });

        // 添加模块按钮
        addModuleButton.setOnAction(e -> {
            // 获取当前选中的模块
            ShipModule selectedModule = getSelectedModuleFromLibrary();
            if (selectedModule != null && currentDesign != null) {
                if (currentDesign.addModule(selectedModule.createCopy())) {
                    updateCurrentModules();
                    updateShipProperties();
                    updateValidation();
                } else {
                    showAlert("无法添加模块", "舰船设计限制或资源不足。");
                }
            }
        });

        // 移除模块按钮
        removeModuleButton.setOnAction(e -> {
            ShipModule selectedModule = currentModulesList.getSelectionModel().getSelectedItem();
            if (selectedModule != null && currentDesign != null) {
                if (currentDesign.removeModule(selectedModule)) {
                    updateCurrentModules();
                    updateShipProperties();
                    updateValidation();
                }
            }
        });

        // 模块选择监听
        currentModulesList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> updateModuleDetails(newVal)
        );
    }

    private void loadDefaultModules() {
        // 加载所有可用的模块
        availableModules.clear();

        // 武器模块
        availableModules.add(new WeaponModule("小型激光炮", WeaponType.LASER, 50, 2));
        availableModules.add(new WeaponModule("中型等离子炮", WeaponType.PLASMA, 100, 1));
        availableModules.add(new WeaponModule("磁轨炮", WeaponType.RAILGUN, 200, 0.5f));
        availableModules.add(new WeaponModule("制导导弹", WeaponType.MISSILE, 150, 0.3f));

        // 防御模块
        availableModules.add(new DefenseModule("能量护盾", DefenseType.SHIELD, 200));
        availableModules.add(new DefenseModule("复合装甲", DefenseType.ARMOR, 100));
        availableModules.add(new DefenseModule("点防御系统", DefenseType.POINT_DEFENSE, 50));
        availableModules.add(new DefenseModule("电子对抗系统", DefenseType.ECM, 30));

        // 功能模块
        availableModules.add(new UtilityModule("高级传感器", UtilityType.SENSOR, 100));
        availableModules.add(new UtilityModule("隐形装置", UtilityType.CLOAKING, 80));
        availableModules.add(new UtilityModule("扩展货舱", UtilityType.CARGO_BAY, 200));
        availableModules.add(new UtilityModule("机库甲板", UtilityType.HANGAR, 150));

        // 引擎模块
        availableModules.add(new EngineModule(150));
        availableModules.add(new EngineModule(200));
        availableModules.add(new EngineModule(300));

        // 电力模块
        availableModules.add(new PowerModule(500));
        availableModules.add(new PowerModule(1000));
        availableModules.add(new PowerModule(2000));
    }

    private void createNewDesign(ShipClass shipClass) {
        String designName = "新" + shipClass.getDisplayName() + "设计";
        currentDesign = new ShipDesign(designName, shipClass);
        updateUIFromDesign();
    }

    private void loadDesign(ShipDesign design) {
        currentDesign = design;
        updateUIFromDesign();
    }

    private void updateUIFromDesign() {
        if (currentDesign == null) return;

        // 更新基本信息
        shipNameLabel.setText(currentDesign.getFullName());
        shipClassLabel.setText(currentDesign.getShipClass().getDisplayName());

        // 更新模块列表
        updateCurrentModules();

        // 更新属性显示
        updateShipProperties();

        // 更新成本和维护费
        updateCostPanels();

        // 更新验证状态
        updateValidation();
    }

    private void updateCurrentModules() {
        currentModules.clear();
        if (currentDesign != null) {
            currentModules.addAll(currentDesign.getModules());
        }
    }

    private void updateShipProperties() {
        if (currentDesign == null) return;

        // 更新基础属性
        hitPointsLabel.setText(String.format("%.0f", currentDesign.getHitPoints()));
        shieldLabel.setText(String.format("%.0f", currentDesign.getShieldStrength()));
        armorLabel.setText(String.format("%.0f", currentDesign.getArmor()));
        evasionLabel.setText(String.format("%.1f%%", currentDesign.getEvasion()));

        speedLabel.setText(String.format("%.0f", currentDesign.getEnginePower()));
        warpSpeedLabel.setText(String.format("%.1f", currentDesign.getWarpSpeed()));
        maneuverabilityLabel.setText(String.format("%.1f", currentDesign.getManeuverability()));

        crewLabel.setText(String.format("%d", currentDesign.getCrewCapacity()));
        cargoLabel.setText(String.format("%d", currentDesign.getCargoCapacity()));
        fuelLabel.setText(String.format("%d", currentDesign.getFuelCapacity()));

        // 更新评分
        combatPowerLabel.setText(String.format("%.0f", currentDesign.calculateCombatPower()));
        strategicValueLabel.setText(String.format("%.0f", currentDesign.calculateStrategicValue()));
    }

    private void updateCostPanels() {
        if (currentDesign == null) return;

        // 更新建造成本
        costPanel.getChildren().clear();
        Map<ResourceType, Float> costs = currentDesign.getConstructionCost();
        for (Map.Entry<ResourceType, Float> entry : costs.entrySet()) {
            HBox costRow = createCostRow(entry.getKey(), entry.getValue(), Color.YELLOW);
            costPanel.getChildren().add(costRow);
        }

        // 更新维护成本
        maintenancePanel.getChildren().clear();
        Map<ResourceType, Float> maintenance = currentDesign.getMaintenanceCost();
        for (Map.Entry<ResourceType, Float> entry : maintenance.entrySet()) {
            HBox costRow = createCostRow(entry.getKey(), entry.getValue(), Color.ORANGE);
            maintenancePanel.getChildren().add(costRow);
        }
    }

    private HBox createCostRow(ResourceType type, float amount, Color color) {
        HBox row = new HBox(10);

        Label nameLabel = new Label(type.getDisplayName());
        nameLabel.setTextFill(color);
        nameLabel.setPrefWidth(100);

        Label amountLabel = new Label(String.format("%.1f", amount));
        amountLabel.setTextFill(Color.WHITE);

        // 资源图标
        Label iconLabel = new Label(getResourceIcon(type));

        row.getChildren().addAll(iconLabel, nameLabel, amountLabel);
        return row;
    }

    private String getResourceIcon(ResourceType type) {
        switch (type) {
            case METAL: return "⛏️";
            case ENERGY: return "⚡";
            case FOOD: return "🌾";
            case SCIENCE: return "🔬";
            case EXOTIC_MATTER: return "✨";
            case NEUTRONIUM: return "⭐";
            case CRYSTAL: return "💎";
            case DARK_MATTER: return "🌑";
            case ANTI_MATTER: return "💥";
            case LIVING_METAL: return "🔩";
            default: return "📦";
        }
    }

    private void updateValidation() {
        if (currentDesign == null) return;

        // 更新验证信息
        validationLabel.setText(currentDesign.getValidationMessage());

        if (currentDesign.isValidDesign()) {
            validationLabel.setTextFill(Color.GREEN);
            validationLabel.setText("设计有效 ✓");
        } else {
            validationLabel.setTextFill(Color.RED);
        }

        // 更新能源平衡进度条
        int availablePower = currentDesign.getAvailablePower();
        int totalPowerOutput = currentDesign.getModules().stream()
                .filter(m -> m instanceof PowerModule)
                .mapToInt(ShipModule::getPowerOutput)
                .sum();

        if (totalPowerOutput > 0) {
            float powerRatio = (float) (totalPowerOutput + availablePower) / totalPowerOutput;
            powerBalanceBar.setProgress(powerRatio);

            if (availablePower < 0) {
                powerBalanceBar.setStyle("-fx-accent: #f44336;");
            } else if (availablePower < totalPowerOutput * 0.1) {
                powerBalanceBar.setStyle("-fx-accent: #FF9800;");
            } else {
                powerBalanceBar.setStyle("-fx-accent: #4CAF50;");
            }
        }

        // 更新船体空间进度条
        int usedSpace = currentDesign.getUsedHullSpace();
        int totalSpace = currentDesign.getHullSize();
        float spaceRatio = (float) usedSpace / totalSpace;
        hullSpaceBar.setProgress(spaceRatio);

        if (spaceRatio > 0.9) {
            hullSpaceBar.setStyle("-fx-accent: #f44336;");
        } else if (spaceRatio > 0.7) {
            hullSpaceBar.setStyle("-fx-accent: #FF9800;");
        } else {
            hullSpaceBar.setStyle("-fx-accent: #2196F3;");
        }
    }

    private void updateModuleDetails(ShipModule module) {
        VBox detailsPanel = (VBox) lookup("#module-details");
        if (detailsPanel == null || module == null) return;

        detailsPanel.getChildren().clear();

        // 模块名称
        Label nameLabel = new Label(module.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.WHITE);
        detailsPanel.getChildren().add(nameLabel);

        // 模块类型
        Label typeLabel = new Label("类型: " + module.getType().getDisplayName());
        typeLabel.setTextFill(Color.LIGHTGRAY);
        detailsPanel.getChildren().add(typeLabel);

        // 模块属性
        addDetailRow(detailsPanel, "占用空间:", module.getSize() + " 单位");
        addDetailRow(detailsPanel, "能源需求:", module.getPowerRequirement() + " 单位");

        if (module instanceof WeaponModule) {
            WeaponModule weapon = (WeaponModule) module;
            addDetailRow(detailsPanel, "伤害:", String.format("%.0f", weapon.getDamage()));
            addDetailRow(detailsPanel, "射速:", String.format("%.1f/秒", weapon.getFireRate()));
            addDetailRow(detailsPanel, "射程:", String.format("%.0f", weapon.getRange()));
            addDetailRow(detailsPanel, "精度:", String.format("%.1f%%", weapon.getAccuracy()));
            addDetailRow(detailsPanel, "穿甲:", String.format("%.1f%%", weapon.getArmorPenetration()));

            if (weapon.usesAmmo()) {
                addDetailRow(detailsPanel, "弹药类型:", weapon.getAmmoType().getDisplayName());
                addDetailRow(detailsPanel, "弹药容量:", weapon.getAmmoCapacity() + " 发");
            }
        } else if (module instanceof DefenseModule) {
            DefenseModule defense = (DefenseModule) module;
            addDetailRow(detailsPanel, "防御值:", String.format("%.0f", defense.getDefenseValue()));
            addDetailRow(detailsPanel, "恢复速度:", String.format("%.1f/秒", defense.getRechargeRate()));
            addDetailRow(detailsPanel, "覆盖范围:", String.format("%.1f%%", defense.getCoverage()));
        } else if (module instanceof UtilityModule) {
            UtilityModule utility = (UtilityModule) module;
            addDetailRow(detailsPanel, "功能值:", String.format("%.0f", utility.getUtilityValue()));

            Map<String, Float> effects = utility.getSpecialAbilities();
            for (Map.Entry<String, Float> effect : effects.entrySet()) {
                addDetailRow(detailsPanel, effect.getKey() + ":", String.format("%.1f", effect.getValue()));
            }
        } else if (module instanceof EngineModule) {
            EngineModule engine = (EngineModule) module;
            addDetailRow(detailsPanel, "推力:", String.format("%.0f", engine.getThrust()));
        } else if (module instanceof PowerModule) {
            PowerModule power = (PowerModule) module;
            addDetailRow(detailsPanel, "能源输出:", power.getPowerOutput() + " 单位");
        }

        // 建造成本
        Label costTitle = new Label("建造成本:");
        costTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        costTitle.setTextFill(Color.LIGHTGRAY);
        detailsPanel.getChildren().add(costTitle);

        Map<ResourceType, Float> costs = module.getConstructionCost();
        for (Map.Entry<ResourceType, Float> cost : costs.entrySet()) {
            addDetailRow(detailsPanel, "  " + cost.getKey().getDisplayName() + ":",
                    String.format("%.1f", cost.getValue()));
        }
    }

    private void addDetailRow(VBox container, String label, String value) {
        HBox row = new HBox(5);

        Label nameLabel = new Label(label);
        nameLabel.setTextFill(Color.LIGHTGRAY);
        nameLabel.setPrefWidth(100);

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.WHITE);

        row.getChildren().addAll(nameLabel, valueLabel);
        container.getChildren().add(row);
    }

    private ShipModule getSelectedModuleFromLibrary() {
        Tab selectedTab = moduleTabs.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof ListView) {
            @SuppressWarnings("unchecked")
            ListView<ShipModule> listView = (ListView<ShipModule>) selectedTab.getContent();
            return listView.getSelectionModel().getSelectedItem();
        }
        return null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 自定义模块列表单元格
    private class ModuleListCell extends ListCell<ShipModule> {
        @Override
        protected void updateItem(ShipModule module, boolean empty) {
            super.updateItem(module, empty);

            if (empty || module == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox container = new HBox(10);

                // 模块图标
                Label iconLabel = new Label(getModuleIcon(module.getType()));
                iconLabel.setStyle("-fx-font-size: 20;");

                VBox infoBox = new VBox(2);

                // 模块名称
                Label nameLabel = new Label(module.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

                // 模块类型
                Label typeLabel = new Label(module.getType().getDisplayName());
                typeLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11;");

                // 模块规格
                Label specsLabel = new Label(getModuleSpecs(module));
                specsLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 10;");

                infoBox.getChildren().addAll(nameLabel, typeLabel, specsLabel);
                container.getChildren().addAll(iconLabel, infoBox);
                setGraphic(container);
            }
        }

        private String getModuleIcon(ModuleType type) {
            return type.getIcon();
        }

        private String getModuleSpecs(ShipModule module) {
            StringBuilder specs = new StringBuilder();
            specs.append("大小: ").append(module.getSize());
            specs.append(" | 能耗: ").append(module.getPowerRequirement());

            if (module instanceof WeaponModule) {
                WeaponModule weapon = (WeaponModule) module;
                specs.append(" | 伤害: ").append(String.format("%.0f", weapon.getDamage()));
            } else if (module instanceof DefenseModule) {
                DefenseModule defense = (DefenseModule) module;
                specs.append(" | 防御: ").append(String.format("%.0f", defense.getDefenseValue()));
            }

            return specs.toString();
        }
    }

    // Getter方法
    public ObservableList<ShipDesign> getSavedDesigns() { return savedDesigns; }
    public ShipDesign getCurrentDesign() { return currentDesign; }
}