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
import javafx.stage.Stage;

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
    private static final ObservableList<ShipDesign> savedDesigns = FXCollections.observableArrayList();

    // 模块分类
    private TabPane moduleTabs;
    
    // 科技加成显示
    private Label hullSizeMultiplierLabel;
    
    // 已研发的科技（示例）
    private java.util.Set<String> researchedTechnologies;

    public ShipDesignerUI() {
        this.availableModules = FXCollections.observableArrayList();
        this.currentModules = FXCollections.observableArrayList();
        this.researchedTechnologies = new java.util.HashSet<>();

        // 添加一些示例已研发的科技
        researchedTechnologies.add("basic_modules");

        initializeUI();
        setupEventHandlers();
        loadDefaultModules();
        
        // 创建一个空的初始设计，而不是自动保存
        createNewDesign(ShipClass.CORVETTE);
        
        // 确保现有的保存设计在组合框中可见
        existingDesigns.setItems(savedDesigns);
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
        
        // 设置显示舰船等级的中文名称
        shipClassComboBox.setCellFactory(lv -> new ListCell<ShipClass>() {
            @Override
            protected void updateItem(ShipClass item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        shipClassComboBox.setButtonCell(new ListCell<ShipClass>() {
            @Override
            protected void updateItem(ShipClass item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("请选择舰船等级");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        // 设计列表
        existingDesigns = new ComboBox<>(savedDesigns);
        existingDesigns.setPromptText("选择现有设计");
        existingDesigns.setPrefWidth(200);
        
        // 设置显示设计方案的名称
        existingDesigns.setCellFactory(lv -> new ListCell<ShipDesign>() {
            @Override
            protected void updateItem(ShipDesign item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullName());
                }
            }
        });
        
        existingDesigns.setButtonCell(new ListCell<ShipDesign>() {
            @Override
            protected void updateItem(ShipDesign item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("选择现有设计");
                } else {
                    setText(item.getFullName());
                }
            }
        });

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

        // 操作按钮
        HBox buttonPanel = new HBox(10);
        addModuleButton = new Button("添加模块");
        removeModuleButton = new Button("移除模块");

        String buttonStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white;";
        addModuleButton.setStyle(buttonStyle);
        removeModuleButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        buttonPanel.getChildren().addAll(addModuleButton, removeModuleButton);

        panel.getChildren().addAll(title, shipInfo, modulesTitle, currentModulesList, buttonPanel);
        return panel;
    }

    private HBox createShipInfoPanel() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        VBox leftColumn = new VBox(5);
        VBox middleColumn = new VBox(5);
        VBox rightColumn = new VBox(5);

        // 左列信息
        shipNameLabel = new Label("未命名设计");
        shipNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        shipNameLabel.setTextFill(Color.WHITE);

        shipClassLabel = new Label("护卫舰");
        shipClassLabel.setTextFill(Color.LIGHTGRAY);

        Label nameLabel = new Label("名称:");
        nameLabel.setTextFill(Color.LIGHTGRAY);

        // 中列信息
        hitPointsLabel = createStatLabel("生命值:", "1000");
        shieldLabel = createStatLabel("护盾:", "100");
        armorLabel = createStatLabel("装甲:", "50");
        evasionLabel = createStatLabel("回避:", "30%");

        // 右列信息
        speedLabel = createStatLabel("速度:", "150");
        warpSpeedLabel = createStatLabel("跃迁:", "1.0");
        maneuverabilityLabel = createStatLabel("机动:", "80");
        crewLabel = createStatLabel("船员:", "50");

        leftColumn.getChildren().addAll(nameLabel, shipNameLabel, shipClassLabel);
        middleColumn.getChildren().addAll(hitPointsLabel, shieldLabel, armorLabel, evasionLabel);
        rightColumn.getChildren().addAll(speedLabel, warpSpeedLabel, maneuverabilityLabel, crewLabel);

        panel.getChildren().addAll(leftColumn, middleColumn, rightColumn);
        return panel;
    }

    private Label createStatLabel(String name, String value) {
        Label label = new Label(name + " " + value);
        label.setTextFill(Color.LIGHTGRAY);
        return label;
    }

    private VBox createPropertyPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(300);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        Label title = new Label("舰船属性");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        // 基础属性
        VBox basicStats = createBasicStatsPanel();

        // 资源成本
        VBox costPanel = createCostPanel();

        // 维护成本
        VBox maintenancePanel = createMaintenancePanel();

        // 战斗力评估
        VBox evaluationPanel = createEvaluationPanel();

        panel.getChildren().addAll(title, basicStats, costPanel, maintenancePanel, evaluationPanel);
        return panel;
    }

    private VBox createBasicStatsPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        Label title = new Label("基础属性");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        cargoLabel = createStatLabel("货舱:", "100");
        fuelLabel = createStatLabel("燃料:", "200");
        combatPowerLabel = createStatLabel("战斗力:", "500");
        strategicValueLabel = createStatLabel("战略价值:", "1000");
        hullSizeMultiplierLabel = createStatLabel("船体加成:", "100%");

        panel.getChildren().addAll(title, cargoLabel, fuelLabel, combatPowerLabel, strategicValueLabel, hullSizeMultiplierLabel);
        return panel;
    }

    private VBox createCostPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        Label title = new Label("建造成本");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        costPanel = new VBox(3);

        panel.getChildren().addAll(title, costPanel);
        return panel;
    }

    private VBox createMaintenancePanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        Label title = new Label("维护成本");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        maintenancePanel = new VBox(3);

        panel.getChildren().addAll(title, maintenancePanel);
        return panel;
    }

    private VBox createEvaluationPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        Label title = new Label("评估");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        // 战斗力和战略价值只是示例值，实际应该基于设计计算

        panel.getChildren().addAll(title, combatPowerLabel, strategicValueLabel);
        return panel;
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

    private HBox createBottomPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(5));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        // 验证标签
        validationLabel = new Label("设计有效 ✓");
        validationLabel.setTextFill(Color.GREEN);

        // 能源平衡
        VBox powerBalanceBox = new VBox(2);
        Label powerLabel = new Label("能源平衡:");
        powerLabel.setTextFill(Color.LIGHTGRAY);
        powerBalanceBar = new ProgressBar(1.0);
        powerBalanceBar.setPrefWidth(150);
        powerBalanceBar.setStyle("-fx-accent: #4CAF50;");
        powerBalanceBox.getChildren().addAll(powerLabel, powerBalanceBar);

        // 船体空间
        VBox hullSpaceBox = new VBox(2);
        Label spaceLabel = new Label("船体空间:");
        spaceLabel.setTextFill(Color.LIGHTGRAY);
        hullSpaceBar = new ProgressBar(0.3);
        hullSpaceBar.setPrefWidth(150);
        hullSpaceBar.setStyle("-fx-accent: #2196F3;");
        hullSpaceBox.getChildren().addAll(spaceLabel, hullSpaceBar);

        panel.getChildren().addAll(validationLabel, powerBalanceBox, hullSpaceBox);
        return panel;
    }

    private void setupEventHandlers() {
        // 舰船等级选择
        shipClassComboBox.setOnAction(e -> {
            ShipClass selectedClass = shipClassComboBox.getValue();
            if (selectedClass != null && currentDesign != null 
                && currentDesign.getShipClass() != selectedClass) {
                // 只有当选择的舰船等级与当前设计不同时才创建新设计
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

        // 新建设计按钮
        newDesignButton.setOnAction(e -> {
            ShipClass selectedClass = shipClassComboBox.getValue();
            createNewDesign(selectedClass);
        });

        // 复制设计按钮
        copyDesignButton.setOnAction(e -> {
            if (currentDesign != null) {
                // 显示输入对话框让用户输入新设计名称
                TextInputDialog dialog = new TextInputDialog(currentDesign.getName() + " 副本");
                dialog.setTitle("复制设计");
                dialog.setHeaderText("请输入新设计的名称");
                dialog.setContentText("设计名称:");
                
                // 设置窗口图标
                try {
                    javafx.scene.image.Image icon = new javafx.scene.image.Image(
                        getClass().getResourceAsStream("/images/icon.png"));
                    Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(icon);
                } catch (Exception ex) {
                    System.err.println("无法加载窗口图标: " + ex.getMessage());
                }
                
                // 设置弹窗样式，与主界面风格保持一致
                DialogPane dialogPane = dialog.getDialogPane();
                dialogPane.setStyle("-fx-font-family: 'Arial'; " +
                                   "-fx-background-color: #2b2b2b;");
                dialogPane.setPrefSize(450, 200);
                
                // 设置文本框和标签样式
                TextField textField = dialog.getEditor();
                textField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");
                
                Label contentLabel = (Label) dialogPane.lookup(".content.label");
                if (contentLabel != null) {
                    contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                }
                
                Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
                if (headerLabel != null) {
                    headerLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                }

                dialog.showAndWait().ifPresent(name -> {
                    ShipDesign copiedDesign = currentDesign.createCopy(name);
                    savedDesigns.add(copiedDesign);
                    existingDesigns.setValue(copiedDesign);
                    // 确保列表更新
                    existingDesigns.setItems(savedDesigns);
                    
                    showAlert("复制成功", "设计 \"" + name + "\" 已创建。");
                });
            } else {
                showAlert("无法复制设计", "请先创建或选择一个舰船设计。");
            }
        });

        // 保存设计按钮
        saveDesignButton.setOnAction(e -> {
            if (currentDesign != null) {
                // 显示输入对话框让用户输入设计名称
                TextInputDialog dialog = new TextInputDialog(currentDesign.getName());
                dialog.setTitle("保存设计");
                dialog.setHeaderText("请输入设计的名称");
                dialog.setContentText("设计名称:");
                
                // 设置窗口图标
                try {
                    javafx.scene.image.Image icon = new javafx.scene.image.Image(
                        getClass().getResourceAsStream("/images/icon.png"));
                    Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(icon);
                } catch (Exception ex) {
                    System.err.println("无法加载窗口图标: " + ex.getMessage());
                }
                
                // 设置弹窗样式，与主界面风格保持一致
                DialogPane dialogPane = dialog.getDialogPane();
                dialogPane.setStyle("-fx-font-family: 'Arial'; " +
                                   "-fx-background-color: #2b2b2b;");
                dialogPane.setPrefSize(450, 200);
                
                // 设置文本框和标签样式
                TextField textField = dialog.getEditor();
                textField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");
                
                Label contentLabel = (Label) dialogPane.lookup(".content.label");
                if (contentLabel != null) {
                    contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
                }
                
                Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
                if (headerLabel != null) {
                    headerLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                }

                dialog.showAndWait().ifPresent(name -> {
                    // 更新当前设计的名称
                    currentDesign.setName(name);
                    
                    // 如果设计尚未保存，则添加到保存列表中
                    if (!savedDesigns.contains(currentDesign)) {
                        savedDesigns.add(currentDesign);
                    }
                    
                    // 更新UI显示
                    shipNameLabel.setText(currentDesign.getFullName());
                    existingDesigns.setValue(currentDesign);
                    // 确保列表更新
                    existingDesigns.setItems(savedDesigns);
                    
                    showAlert("保存成功", "设计 \"" + name + "\" 已保存。");
                });
            } else {
                showAlert("无法保存设计", "请先创建一个舰船设计。");
            }
        });

        // 添加模块按钮
        addModuleButton.setOnAction(e -> {
            // 获取当前选中的模块
            ShipModule selectedModule = getSelectedModuleFromLibrary();
            if (selectedModule != null && currentDesign != null) {
                // 检查模块是否已解锁
                if (!selectedModule.isUnlocked()) {
                    showAlert("无法添加模块", "该模块尚未解锁，请先研究相关科技。");
                    return;
                }
                
                // 先检查能否添加模块
                if (!currentDesign.canAddModule(selectedModule)) {
                    // 获取详细的失败原因
                    String errorMessage = getDetailedFailureReason(selectedModule);
                    showAlert("无法添加模块", errorMessage);
                    return;
                }
                
                // 尝试添加模块
                if (currentDesign.addModule(selectedModule.createCopy())) {
                    updateCurrentModules();
                    updateShipProperties();
                    updateValidation();
                } else {
                    String errorMessage = currentDesign.getValidationMessage();
                    if (errorMessage == null || errorMessage.isEmpty()) {
                        errorMessage = "无法添加模块。可能的原因：\n" +
                                      "1. 超过模块数量限制\n" +
                                      "2. 能源不足\n" +
                                      "3. 船体空间不足\n" +
                                      "4. 达到武器/功能模块上限";
                    }
                    showAlert("无法添加模块", errorMessage);
                }
            } else if (currentDesign == null) {
                showAlert("无法添加模块", "请先创建或选择一个舰船设计。");
            } else {
                showAlert("无法添加模块", "请从模块库中选择一个模块。");
            }
        });

        // 移除模块按钮
        removeModuleButton.setOnAction(e -> {
            ShipModule selectedModule = currentModulesList.getSelectionModel().getSelectedItem();
            if (selectedModule != null && currentDesign != null) {
                // 检查是否是核心模块（不能移除）
                if (selectedModule instanceof HullModule || 
                    selectedModule instanceof EngineModule || 
                    selectedModule instanceof PowerModule) {
                    showAlert("无法移除模块", "核心模块（船体、引擎、电力）不能被移除。");
                    return;
                }
                
                if (currentDesign.removeModule(selectedModule)) {
                    updateCurrentModules();
                    updateShipProperties();
                    updateValidation();
                } else {
                    showAlert("无法移除模块", "移除模块时发生错误。");
                }
            } else if (currentDesign == null) {
                showAlert("无法移除模块", "请先创建或选择一个舰船设计。");
            } else {
                showAlert("无法移除模块", "请从当前设计中选择一个模块。");
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

        // 武器模块 - 基础级
        availableModules.add(createWeaponModule("小型激光炮", WeaponType.LASER, 50, 2, 100, 1));
        availableModules.add(createWeaponModule("轻型等离子炮", WeaponType.PLASMA, 100, 1, 150, 2));
        availableModules.add(createWeaponModule("标准磁轨炮", WeaponType.RAILGUN, 200, 0.5f, 200, 3));
        availableModules.add(createWeaponModule("基础导弹", WeaponType.MISSILE, 150, 0.3f, 180, 2));

        // 武器模块 - 高级（需要科技解锁）
        WeaponModule advancedLaser = createWeaponModule("先进激光炮", WeaponType.LASER, 120, 3, 250, 4);
        advancedLaser.setUnlocked(false); // 需要科技解锁
        availableModules.add(advancedLaser);
        
        WeaponModule advancedRailgun = createWeaponModule("重型磁轨炮", WeaponType.RAILGUN, 350, 0.4f, 350, 5);
        advancedRailgun.setUnlocked(false); // 需要科技解锁
        availableModules.add(advancedRailgun);

        // 防御模块 - 基础级
        availableModules.add(createDefenseModule("基础能量护盾", DefenseType.SHIELD, 200, 120, 1));
        availableModules.add(createDefenseModule("复合装甲", DefenseType.ARMOR, 100, 100, 1));
        availableModules.add(createDefenseModule("点防御系统", DefenseType.POINT_DEFENSE, 50, 100, 2));
        availableModules.add(createDefenseModule("电子对抗系统", DefenseType.ECM, 30, 80, 3));

        // 防御模块 - 高级（需要科技解锁）
        DefenseModule advancedShield = createDefenseModule("先进护盾", DefenseType.SHIELD, 400, 250, 3);
        advancedShield.setUnlocked(false); // 需要科技解锁
        availableModules.add(advancedShield);

        // 功能模块 - 基础级
        availableModules.add(createUtilityModule("基础传感器", UtilityType.SENSOR, 50, 80, 1));
        availableModules.add(createUtilityModule("简易货舱", UtilityType.CARGO_BAY, 100, 100, 1));
        availableModules.add(createUtilityModule("基础机库", UtilityType.HANGAR, 80, 200, 3));

        // 功能模块 - 高级（需要科技解锁）
        UtilityModule advancedSensor = createUtilityModule("先进传感器", UtilityType.SENSOR, 120, 180, 3);
        advancedSensor.setUnlocked(false); // 需要科技解锁
        availableModules.add(advancedSensor);

        // 引擎模块 - 基础级
        availableModules.add(createEngineModule("基础引擎", 150, 300, 1));
        availableModules.add(createEngineModule("标准引擎", 200, 320, 2));
        availableModules.add(createEngineModule("高性能引擎", 300, 350, 3));

        // 引擎模块 - 高级（需要科技解锁）
        EngineModule advancedEngine = createEngineModule("先进引擎", 500, 450, 5);
        advancedEngine.setUnlocked(false); // 需要科技解锁
        availableModules.add(advancedEngine);

        // 电力模块 - 基础级
        availableModules.add(createPowerModule("基础发电机", 500, 250, 1));
        availableModules.add(createPowerModule("标准发电机", 1000, 280, 2));
        availableModules.add(createPowerModule("高效发电机", 2000, 300, 3));

        // 电力模块 - 高级（需要科技解锁）
        PowerModule advancedPower = createPowerModule("先进发电机", 5000, 400, 5);
        advancedPower.setUnlocked(false); // 需要科技解锁
        availableModules.add(advancedPower);
    }

    // 创建防御模块的辅助方法
    private DefenseModule createDefenseModule(String name, DefenseType type, float defenseValue, int size, int techLevel) {
        DefenseModule module = new DefenseModule(name, type, defenseValue);
        // 通过反射设置受保护字段
        try {
            java.lang.reflect.Field sizeField = ShipModule.class.getDeclaredField("size");
            sizeField.setAccessible(true);
            ((javafx.beans.property.IntegerProperty) sizeField.get(module)).set(size);
            
            java.lang.reflect.Field techLevelField = ShipModule.class.getDeclaredField("techLevel");
            techLevelField.setAccessible(true);
            ((javafx.beans.property.IntegerProperty) techLevelField.get(module)).set(techLevel);
            
            // 设置科技需求
            if (techLevel > 1) {
                java.lang.reflect.Field reqTechField = ShipModule.class.getDeclaredField("requiredTechnology");
                reqTechField.setAccessible(true);
                ((java.lang.reflect.Field) reqTechField).set(module, "advanced_defenses");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    // 创建功能模块的辅助方法
    private UtilityModule createUtilityModule(String name, UtilityType type, float utilityValue, int size, int techLevel) {
        UtilityModule module = new UtilityModule(name, type, utilityValue);
        // 通过反射设置受保护字段
        try {
            java.lang.reflect.Field sizeField = ShipModule.class.getDeclaredField("size");
            sizeField.setAccessible(true);
            ((javafx.beans.property.IntegerProperty) sizeField.get(module)).set(size);
            
            java.lang.reflect.Field techLevelField = ShipModule.class.getDeclaredField("techLevel");
            techLevelField.setAccessible(true);
            ((javafx.beans.property.IntegerProperty) techLevelField.get(module)).set(techLevel);
            
            // 设置科技需求
            if (techLevel > 1) {
                java.lang.reflect.Field reqTechField = ShipModule.class.getDeclaredField("requiredTechnology");
                reqTechField.setAccessible(true);
                ((java.lang.reflect.Field) reqTechField).set(module, "advanced_utilities");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    // 创建武器模块的辅助方法
    private WeaponModule createWeaponModule(String name, WeaponType type, float damage, float fireRate, int size, int techLevel) {
        WeaponModule module = new WeaponModule(name, type, damage, fireRate);
        // 通过反射设置受保护字段
        try {
            java.lang.reflect.Field sizeField = ShipModule.class.getDeclaredField("size");
            sizeField.setAccessible(true);
            ((javafx.beans.property.IntegerProperty) sizeField.get(module)).set(size);
            
            java.lang.reflect.Field techLevelField = ShipModule.class.getDeclaredField("techLevel");
            techLevelField.setAccessible(true);
            ((javafx.beans.property.IntegerProperty) techLevelField.get(module)).set(techLevel);
            
            // 设置科技需求
            if (techLevel > 1) {
                java.lang.reflect.Field reqTechField = ShipModule.class.getDeclaredField("requiredTechnology");
                reqTechField.setAccessible(true);
                ((java.lang.reflect.Field) reqTechField).set(module, "advanced_weapons");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    // 创建引擎模块的辅助方法
    private EngineModule createEngineModule(String name, float thrust, int size, int techLevel) {
        EngineModule module = new EngineModule(thrust);
        // 通过反射设置受保护字段
        try {
            java.lang.reflect.Field nameField = ShipModule.class.getDeclaredField("name");
            nameField.setAccessible(true);
            ((StringProperty) nameField.get(module)).set(name);
            
            java.lang.reflect.Field sizeField = ShipModule.class.getDeclaredField("size");
            sizeField.setAccessible(true);
            ((javafx.beans.property.IntegerProperty) sizeField.get(module)).set(size);
            
            java.lang.reflect.Field techLevelField = ShipModule.class.getDeclaredField("techLevel");
            techLevelField.setAccessible(true);
            ((javafx.beans.property.IntegerProperty) techLevelField.get(module)).set(techLevel);
            
            // 设置科技需求
            if (techLevel > 1) {
                java.lang.reflect.Field reqTechField = ShipModule.class.getDeclaredField("requiredTechnology");
                reqTechField.setAccessible(true);
                ((java.lang.reflect.Field) reqTechField).set(module, "advanced_engines");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    // 创建电力模块的辅助方法
    private PowerModule createPowerModule(String name, int powerOutput, int size, int techLevel) {
        PowerModule module = new PowerModule(powerOutput);
        // 通过反射设置受保护字段
        try {
            java.lang.reflect.Field nameField = ShipModule.class.getDeclaredField("name");
            nameField.setAccessible(true);
            ((StringProperty) nameField.get(module)).set(name);
            
            java.lang.reflect.Field sizeField = ShipModule.class.getDeclaredField("size");
            sizeField.setAccessible(true);
            ((javafx.beans.property.IntegerProperty) sizeField.get(module)).set(size);
            
            java.lang.reflect.Field techLevelField = ShipModule.class.getDeclaredField("techLevel");
            techLevelField.setAccessible(true);
            ((javafx.beans.property.IntegerProperty) techLevelField.get(module)).set(techLevel);
            
            // 设置科技需求
            if (techLevel > 1) {
                java.lang.reflect.Field reqTechField = ShipModule.class.getDeclaredField("requiredTechnology");
                reqTechField.setAccessible(true);
                ((java.lang.reflect.Field) reqTechField).set(module, "advanced_power");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    private void createNewDesign(ShipClass shipClass) {
        String designName = "新" + shipClass.getDisplayName() + "设计";
        currentDesign = new ShipDesign(designName, shipClass);
        
        // 应用科技加成
        float hullMultiplier = calculateHullSizeMultiplier(researchedTechnologies);
        currentDesign.setHullSizeMultiplier(hullMultiplier);
        
        // 确保初始设计是有效的
        // 移除所有模块并重新添加默认模块，确保符合设计规则
        currentDesign.getModules().clear();
        addDefaultModulesForDesign(currentDesign, shipClass);
        
        updateUIFromDesign();
        
        // 不再自动添加到保存列表中，只在用户明确点击保存时才保存
    }
    
    private void addDefaultModulesForDesign(ShipDesign design, ShipClass shipClass) {
        // 根据舰船等级添加合适的默认模块
        switch (shipClass) {
            case CORVETTE:
                design.addModule(new HullModule(900));
                design.addModule(new EngineModule(150));
                design.addModule(new PowerModule(500));
                break;
            case FRIGATE:
                design.addModule(new HullModule(1800));
                design.addModule(new EngineModule(120));
                design.addModule(new PowerModule(1200));
                break;
            case DESTROYER:
                design.addModule(new HullModule(3500));
                design.addModule(new EngineModule(100));
                design.addModule(new PowerModule(2500));
                break;
            case CRUISER:
                design.addModule(new HullModule(8000));
                design.addModule(new EngineModule(80));
                design.addModule(new PowerModule(5000));
                break;
            case BATTLESHIP:
                design.addModule(new HullModule(18000));
                design.addModule(new EngineModule(60));
                design.addModule(new PowerModule(10000));
                break;
            case CARRIER:
                design.addModule(new HullModule(28000));
                design.addModule(new EngineModule(50));
                design.addModule(new PowerModule(15000));
                break;
        }
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
        
        // 更新模块解锁状态
        updateModuleUnlockStatus();
    }
    
    private void updateModuleUnlockStatus() {
        updateModuleUnlockStatus(
            new ArrayList<>(availableModules), researchedTechnologies);
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
        hitPointsLabel.setText(String.format("生命值: %.0f", currentDesign.getHitPoints()));
        shieldLabel.setText(String.format("护盾: %.0f", currentDesign.getShieldStrength()));
        armorLabel.setText(String.format("装甲: %.0f", currentDesign.getArmor()));
        evasionLabel.setText(String.format("回避: %.1f%%", currentDesign.getEvasion()));

        speedLabel.setText(String.format("速度: %.0f", currentDesign.getEnginePower()));
        warpSpeedLabel.setText(String.format("跃迁: %.1f", currentDesign.getWarpSpeed()));
        maneuverabilityLabel.setText(String.format("机动: %.1f", currentDesign.getManeuverability()));

        crewLabel.setText(String.format("船员: %d", currentDesign.getCrewCapacity()));
        cargoLabel.setText(String.format("货舱: %d", currentDesign.getCargoCapacity()));
        fuelLabel.setText(String.format("燃料: %d", currentDesign.getFuelCapacity()));
        
        // 更新船体空间加成
        hullSizeMultiplierLabel.setText(String.format("船体加成: %.1f%%", currentDesign.getHullSizeMultiplier() * 100));

        // 更新评分
        combatPowerLabel.setText(String.format("战斗力: %.0f", currentDesign.calculateCombatPower()));
        strategicValueLabel.setText(String.format("战略价值: %.0f", currentDesign.calculateStrategicValue()));
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

        nameLabel.setTextFill(Color.LIGHTGRAY);
        nameLabel.setPrefWidth(80);

        Label valueLabel = new Label(String.format("%.1f", amount));
        valueLabel.setTextFill(color);

        row.getChildren().addAll(nameLabel, valueLabel);
        return row;
    }

    private void updateValidation() {
        if (currentDesign == null) return;

        // 更新验证标签
        if (currentDesign.isValidDesign()) {
            validationLabel.setTextFill(Color.GREEN);
            validationLabel.setText("设计有效 ✓");
        } else {
            validationLabel.setTextFill(Color.RED);
            if (currentDesign.getValidationMessage() == null || currentDesign.getValidationMessage().isEmpty()) {
                validationLabel.setText("设计无效！请检查以下问题：\n" +
                                     "1. 能源平衡（确保电力模块提供足够能源）\n" +
                                     "2. 船体空间（确保模块总大小不超过船体容量）\n" +
                                     "3. 船员数量（确保至少有10名船员）\n" +
                                     "4. 模块限制（检查武器和功能模块数量限制）");
            } else {
                validationLabel.setText("设计无效！\n" + currentDesign.getValidationMessage());
            }
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
                powerBalanceBar.setStyle("-fx-accent: #f44336;"); // 红色 - 能源不足
            } else {
                powerBalanceBar.setStyle("-fx-accent: #4CAF50;"); // 绿色 - 能源充足
            }
        } else {
            powerBalanceBar.setProgress(0);
            powerBalanceBar.setStyle("-fx-accent: #f44336;"); // 红色 - 无能源
        }

        // 更新船体空间进度条
        int totalSpace = currentDesign.getHullSize();
        int usedSpace = currentDesign.getUsedHullSpace();
        
        if (totalSpace > 0) {
            float spaceRatio = (float) usedSpace / totalSpace;
            hullSpaceBar.setProgress(spaceRatio);

            if (spaceRatio > 0.9) {
                hullSpaceBar.setStyle("-fx-accent: #f44336;"); // 红色 - 空间不足
            } else if (spaceRatio > 0.75) {
                hullSpaceBar.setStyle("-fx-accent: #FF9800;"); // 橙色 - 空间紧张
            } else {
                hullSpaceBar.setStyle("-fx-accent: #2196F3;"); // 蓝色 - 空间充足
            }
            
            // 更新提示文字，显示具体的空间使用情况
            Label spaceLabel = (Label) hullSpaceBar.getParent().getChildrenUnmodifiable().get(0);
            spaceLabel.setText("船体空间 (" + usedSpace + "/" + totalSpace + "):");
        } else {
            hullSpaceBar.setProgress(0);
            hullSpaceBar.setStyle("-fx-accent: #f44336;"); // 红色 - 无空间
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

        // 科技等级
        Label techLevelLabel = new Label("科技等级: " + module.getTechLevel());
        techLevelLabel.setTextFill(Color.LIGHTGRAY);
        detailsPanel.getChildren().add(techLevelLabel);
        
        // 解锁状态
        Label unlockLabel = new Label(module.isUnlocked() ? "状态: 已解锁" : "状态: 未解锁");
        unlockLabel.setTextFill(module.isUnlocked() ? Color.GREEN : Color.RED);
        detailsPanel.getChildren().add(unlockLabel);

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
        
        // 科技需求
        if (!"BASIC_MODULE".equals(module.getRequiredTechnology())) {
            Label techReqLabel = new Label("需要科技: " + module.getRequiredTechnology());
            techReqLabel.setTextFill(Color.YELLOW);
            detailsPanel.getChildren().add(techReqLabel);
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
    
    /**
     * 获取添加模块失败的详细原因
     */
    private String getDetailedFailureReason(ShipModule module) {
        StringBuilder reason = new StringBuilder();
        reason.append("无法添加模块 \"").append(module.getName()).append("\"：\n\n");
        
        // 检查模块数量限制
        /*
        if (currentDesign.getModules().size() >= currentDesign.getMaxModules()) {
            reason.append("• 已达到最大模块数量限制 (").append(currentDesign.getMaxModules()).append(")\n");
        }
        */
        
        // 检查特定类型模块限制
        /*
        if (module instanceof WeaponModule) {
            long weaponCount = currentDesign.getModules().stream()
                    .filter(m -> m instanceof WeaponModule)
                    .count();
            if (weaponCount >= currentDesign.getMaxWeapons()) {
                reason.append("• 已达到最大武器模块数量限制 (").append(currentDesign.getMaxWeapons()).append(")\n");
            }
        } else if (module instanceof UtilityModule) {
            long utilityCount = currentDesign.getModules().stream()
                    .filter(m -> m instanceof UtilityModule)
                    .count();
            if (utilityCount >= currentDesign.getMaxUtility()) {
                reason.append("• 已达到最大功能模块数量限制 (").append(currentDesign.getMaxUtility()).append(")\n");
            }
        }
        */
        
        // 检查能源是否足够
        int totalPowerRequirement = currentDesign.getModules().stream()
                .mapToInt(ShipModule::getPowerRequirement)
                .sum();
        totalPowerRequirement += module.getPowerRequirement();
        
        int availablePower = currentDesign.getAvailablePower();
        int powerDeficit = totalPowerRequirement - (availablePower + module.getPowerRequirement());
        if (powerDeficit > 0) {
            reason.append("• 能源不足，还需要 ").append(powerDeficit).append(" 单位能源\n");
        }
        
        // 检查船体空间是否足够
        // 计算除船体模块外的所有模块占用的空间
        int totalSize = currentDesign.getModules().stream()
                .filter(m -> !(m instanceof HullModule))  // 船体模块不计入占用空间
                .mapToInt(ShipModule::getSize)
                .sum();
        totalSize += module.getSize();
        
        int freeSpace = currentDesign.getHullSize() - (totalSize - module.getSize());
        int spaceNeeded = module.getSize() - freeSpace;
        if (spaceNeeded > 0) {
            reason.append("• 船体空间不足，还需要 ").append(spaceNeeded).append(" 单位空间\n");
            reason.append("  (当前已用空间: ").append(totalSize - module.getSize())
                  .append("/").append(currentDesign.getHullSize()).append(")\n");
        }
        
        // 如果没有任何限制被触发，则给出通用原因
        if (reason.toString().equals("无法添加模块 \"" + module.getName() + "\"：\n\n")) {
            reason.append("• 未知原因，请检查模块兼容性和设计规则\n");
        }
        
        return reason.toString();
    }

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
        
        // 设置弹窗样式，与主界面风格保持一致
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-family: 'Arial'; " +
                           "-fx-background-color: #2b2b2b;");
        dialogPane.setPrefSize(450, 350);
        dialogPane.setMinSize(450, 350);
        dialogPane.setMaxSize(450, 350);
        
        // 设置内容标签样式
        Label contentLabel = (Label) dialogPane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        }
        
        // 设置标题样式
        dialogPane.lookup(".alert-title").setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
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
            specs.append(" | 科技等级: ").append(module.getTechLevel());
            
            // 显示是否已解锁
            if (!module.isUnlocked()) {
                specs.append(" | [未解锁]");
            }

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
    
    /**
     * 根据已研发的科技更新模块解锁状态
     * @param modules 模块列表
     * @param researchedTechs 已研发的科技ID集合
     */
    private static void updateModuleUnlockStatus(List<com.stellarcolonizer.model.fleet.ShipModule> modules, 
                                               java.util.Set<String> researchedTechs) {
        for (com.stellarcolonizer.model.fleet.ShipModule module : modules) {
            // 基础模块始终解锁
            if ("BASIC_MODULE".equals(module.getRequiredTechnology())) {
                module.setUnlocked(true);
                continue;
            }
            
            // 根据科技解锁状态设置模块解锁状态
            switch (module.getName()) {
                case "先进激光炮":
                case "重型磁轨炮":
                    module.setUnlocked(researchedTechs.contains("advanced_weapons"));
                    break;
                case "先进护盾":
                    module.setUnlocked(researchedTechs.contains("advanced_defenses"));
                    break;
                case "先进引擎":
                    module.setUnlocked(researchedTechs.contains("advanced_engines"));
                    break;
                case "先进发电机":
                    module.setUnlocked(researchedTechs.contains("advanced_power"));
                    break;
                default:
                    // 其他模块默认解锁（基础模块）
                    module.setUnlocked(true);
                    break;
            }
        }
    }
    
    /**
     * 根据已研发的科技计算船体空间乘数
     * @param researchedTechs 已研发的科技ID集合
     * @return 船体空间乘数
     */
    private static float calculateHullSizeMultiplier(java.util.Set<String> researchedTechs) {
        float multiplier = 1.0f;
        
        // 船体强化科技提供1.2倍加成
        if (researchedTechs.contains("hull_reinforcement")) {
            multiplier += 0.2f;
        }
        
        // 高级船体强化科技额外提供0.3倍加成
        if (researchedTechs.contains("advanced_hull_reinforcement")) {
            multiplier += 0.3f;
        }
        
        return multiplier;
    }
}