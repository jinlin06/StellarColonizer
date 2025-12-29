package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.economy.ResourceStockpile;
import com.stellarcolonizer.model.fleet.*;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.fleet.enums.FleetMission;
import com.stellarcolonizer.model.fleet.enums.ShipClass;
import com.stellarcolonizer.model.galaxy.CubeCoord;
import com.stellarcolonizer.model.galaxy.Hex;
import com.stellarcolonizer.model.galaxy.Planet;
import com.stellarcolonizer.model.galaxy.StarSystem;
import com.stellarcolonizer.model.colony.Colony;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.*;

public class FleetManagerUI extends BorderPane {

    private final Faction playerFaction;
    private final ObservableList<Fleet> fleets;
    private final ObservableList<Colony> shipyards;

    // 当前选择
    private Fleet selectedFleet;
    private Ship selectedShip;

    // UI组件
    private ListView<Fleet> fleetListView;
    private ListView<Ship> shipListView;
    private TreeView<String> organizationTree;

    // 舰队信息面板
    private Label fleetNameLabel;
    private Label fleetLocationLabel;
    private Label fleetMissionLabel;
    private Label fleetCombatPowerLabel;
    private Label fleetShipCountLabel;
    private Label fleetCrewLabel;
    private Label fleetSupplyLabel;

    // 舰船信息面板
    private Label shipNameLabel;
    private Label shipClassLabel;
    private Label shipStatusLabel;
    private ProgressBar shipHealthBar;
    private ProgressBar shipShieldBar;
    private ProgressBar shipArmorBar;
    private ProgressBar shipFuelBar;

    // 操作按钮
    private Button moveFleetButton;
    private Button splitFleetButton;
    private Button mergeFleetButton;
    private Button repairFleetButton;
    private Button resupplyFleetButton;
    private Button buildShipButton;
    private Button scrapShipButton;
    private Button transferShipButton;

    // 任务选择
    // private ComboBox<FleetMission> missionComboBox;
    
    // 任务控制按钮
    // private Button changeMissionButton;

    private VBox createRightPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(250);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        Label title = new Label("舰队操作");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        // 舰队操作按钮
        VBox fleetOperations = createFleetOperationsPanel();

        // 移除了舰船操作面板，因为用户不需要此功能
        // VBox shipOperations = createShipOperationsPanel();

        // 移除了任务控制面板，因为用户不需要此功能
        // VBox missionControl = createMissionControlPanel();

        // 移除了shipOperations和missionControl，因为它们已被移除
        panel.getChildren().addAll(title, fleetOperations);
        return panel;
    }

    private void setupEventHandlers() {
        // 舰队选择
        fleetListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldFleet, newFleet) -> {
                    selectedFleet = newFleet;
                    updateFleetDetails();
                    updateShipList();
                    // 不自动触发移动功能，等待用户点击“移动所选舰队”按钮
                    
                    // 如果hexMapView存在，取消其选中舰队以避免自动高亮
                    if (hexMapView != null) {
                        hexMapView.setSelectedFleet(null);
                    }
                }
        );

        // 舰船选择
        shipListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldShip, newShip) -> {
                    selectedShip = newShip;
                    updateShipDetails();
                }
        );

        // 操作按钮事件
        setupOperationButtons();
    }

    private void setupOperationButtons() {
        // 移动舰队
        moveFleetButton.setOnAction(e -> showMoveFleetDialog());

        // 拆分舰队
        splitFleetButton.setOnAction(e -> showSplitFleetDialog());

        // 合并舰队
        mergeFleetButton.setOnAction(e -> showMergeFleetDialog());

        // 修理舰队
        repairFleetButton.setOnAction(e -> repairSelectedFleet());

        // 补给舰队
        resupplyFleetButton.setOnAction(e -> resupplySelectedFleet());

        // 移除更改任务功能
        // changeMissionButton.setOnAction(e -> changeFleetMission());

        // 建造舰船
        buildShipButton.setOnAction(e -> showBuildShipDialog());

        // 移除拆解舰船和转移舰船功能
        // scrapShipButton.setOnAction(e -> scrapSelectedShip());
        // transferShipButton.setOnAction(e -> showTransferShipDialog());
    }

    // 移除更改任务功能
    // private void changeFleetMission() {
    //     if (selectedFleet == null || missionComboBox.getValue() == null) return;
    // 
    //     FleetMission newMission = missionComboBox.getValue();
    //     selectedFleet.setMission(newMission, null);
    // 
    //     showAlert("任务更改", selectedFleet.getName() + " 的任务已更改为 " + newMission.getDisplayName());
    //     updateFleetDetails();
    // }

    // 任务选择
    private ComboBox<FleetMission> missionComboBox;

    // 任务控制
    private VBox createMissionControlPanel() {
        // 移除了任务控制面板，因为用户不需要此功能
        return new VBox();
    }

    // 地图视图集成
    private HexMapView hexMapView;
    
    public void setHexMapView(HexMapView hexMapView) {
        this.hexMapView = hexMapView;
    }

    public FleetManagerUI(Faction playerFaction) {
        this.playerFaction = playerFaction;
        this.fleets = FXCollections.observableArrayList();
        this.shipyards = FXCollections.observableArrayList();

        // 加载数据
        loadFleets();
        loadShipyards();

        initializeUI();
        setupEventHandlers();
        
        // 添加对FleetSelectedEvent事件的监听
        this.addEventHandler(FleetSelectedEvent.FLEET_SELECTED, event -> {
            System.out.println("FleetSelectedEvent triggered for fleet: " + event.getSelectedFleet().getName());
            Fleet selectedFleet = event.getSelectedFleet();
            if (selectedFleet != null) {
                // 设置选中的舰队
                this.selectedFleet = selectedFleet;
                
                // 更新舰队详情显示
                updateFleetDetails();
                
                // 更新舰船列表
                updateShipList();
                
                // 在舰队列表中选择该舰队
                int index = fleets.indexOf(selectedFleet);
                if (index >= 0) {
                    fleetListView.getSelectionModel().select(index);
                }
                
                // 确保舰队管理界面可见（如果它被隐藏或覆盖）
                // 这里需要根据实际的UI架构来实现
                // 例如，如果有一个主窗口管理器，可能需要调用类似 showFleetManager() 的方法
                // 或者将舰队管理界面设置为前景
            }
        });
    }

    private void loadFleets() {
        // 从游戏引擎加载舰队数据
        fleets.clear();
        
        // 获取玩家派系的舰队列表
        if (playerFaction != null) {
            List<Fleet> playerFleets = playerFaction.getFleets();
            fleets.addAll(playerFleets);
        }
    }

    private void loadShipyards() {
        // 加载有造船厂的殖民地
        shipyards.addAll(playerFaction.getColonies().stream()
                .filter(colony -> colony.getBuildings().stream()
                        .anyMatch(building -> building.getType().toString().contains("SHIPYARD")))
                .toList());
    }

    private void initializeUI() {
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #1e1e1e;");

        // 顶部：标题和控制
        HBox topPanel = createTopPanel();
        setTop(topPanel);

        // 左侧：舰队列表和组织结构
        VBox leftPanel = createLeftPanel();

        // 中心：详细信息
        VBox centerPanel = createCenterPanel();

        // 右侧：操作面板
        VBox rightPanel = createRightPanel();

        // 使用SplitPane布局
        SplitPane mainSplit = new SplitPane();
        mainSplit.getItems().addAll(leftPanel, centerPanel, rightPanel);
        mainSplit.setDividerPositions(0.25, 0.7);

        setCenter(mainSplit);

        // 如果有关联的舰队，选择第一个
        if (!fleets.isEmpty()) {
            fleetListView.getSelectionModel().select(0);
        }
    }

    private HBox createTopPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(5));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        Label title = new Label("舰队管理器");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);

        // 统计信息
        Label statsLabel = new Label();
        statsLabel.setTextFill(Color.LIGHTGRAY);
        statsLabel.textProperty().bind(
                new SimpleStringProperty("总舰队: ").concat(createFleetCountBinding())
                        .concat(" | 总舰船: ").concat(createTotalShipsBinding())
        );

        // 刷新按钮
        Button refreshButton = new Button("刷新");
        refreshButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> refreshData());

        panel.getChildren().addAll(title, statsLabel, new Region(), refreshButton);
        HBox.setHgrow(panel.getChildren().get(2), Priority.ALWAYS); // 推挤按钮到右边

        return panel;
    }

    private StringBinding createFleetCountBinding() {
        return Bindings.createStringBinding(
            () -> String.valueOf(fleets.size()),
            fleets
        );
    }

    private IntegerBinding createTotalShipsBinding() {
        return new IntegerBinding() {
            {
                bind(fleets);
            }

            @Override
            protected int computeValue() {
                return fleets.stream().mapToInt(Fleet::getShipCount).sum();
            }
        };
    }

    private VBox createLeftPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(300);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        // 舰队列表
        Label fleetListTitle = new Label("舰队列表");
        fleetListTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        fleetListTitle.setTextFill(Color.WHITE);

        fleetListView = new ListView<>(fleets);
        fleetListView.setPrefHeight(200);
        fleetListView.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");
        fleetListView.setCellFactory(lv -> new FleetListCell());

        // 组织结构树
        Label orgTitle = new Label("组织结构");
        orgTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        orgTitle.setTextFill(Color.WHITE);

        organizationTree = createOrganizationTree();

        panel.getChildren().addAll(fleetListTitle, fleetListView, orgTitle, organizationTree);
        VBox.setVgrow(fleetListView, Priority.ALWAYS);
        VBox.setVgrow(organizationTree, Priority.ALWAYS);

        return panel;
    }

    private TreeView<String> createOrganizationTree() {
        TreeItem<String> root = new TreeItem<>("舰队组织");
        root.setExpanded(true);

        // 按任务分类
        for (FleetMission mission : FleetMission.values()) {
            TreeItem<String> missionItem = new TreeItem<>(mission.getDisplayName() + " " + mission.getIcon());

            // 添加该任务下的舰队
            for (Fleet fleet : fleets) {
                if (fleet.getCurrentMission() == mission) {
                    TreeItem<String> fleetItem = new TreeItem<>(fleet.getName());
                    missionItem.getChildren().add(fleetItem);
                }
            }

            if (!missionItem.getChildren().isEmpty()) {
                root.getChildren().add(missionItem);
            }
        }

        TreeView<String> treeView = new TreeView<>(root);
        treeView.setStyle("-fx-background-color: #1e1e1e;");
        treeView.setShowRoot(true);

        return treeView;
    }

    private VBox createCenterPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));

        // 舰队详细信息
        VBox fleetDetailPanel = createFleetDetailPanel();

        // 舰船列表
        Label shipListTitle = new Label("舰船列表");
        shipListTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        shipListTitle.setTextFill(Color.WHITE);

        shipListView = new ListView<>();
        shipListView.setPrefHeight(250);
        shipListView.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");
        shipListView.setCellFactory(lv -> new ShipListCell());

        // 舰船详细信息
        VBox shipDetailPanel = createShipDetailPanel();

        panel.getChildren().addAll(fleetDetailPanel, shipListTitle, shipListView, shipDetailPanel);
        return panel;
    }

    private VBox createFleetDetailPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        Label title = new Label("舰队信息");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        // 基本信息网格
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(8);
        infoGrid.setPadding(new Insets(10));

        addFleetInfoRow(infoGrid, 0, "名称:", fleetNameLabel = new Label());
        addFleetInfoRow(infoGrid, 1, "位置:", fleetLocationLabel = new Label());
        addFleetInfoRow(infoGrid, 2, "战斗力:", fleetCombatPowerLabel = new Label());
        addFleetInfoRow(infoGrid, 3, "舰船数量:", fleetShipCountLabel = new Label());
        addFleetInfoRow(infoGrid, 4, "船员总数:", fleetCrewLabel = new Label());
        addFleetInfoRow(infoGrid, 5, "补给效率:", fleetSupplyLabel = new Label());

        // 舰船组成图表
        VBox compositionBox = createCompositionChart();

        panel.getChildren().addAll(title, infoGrid, compositionBox);
        return panel;
    }

    private void addFleetInfoRow(GridPane grid, int row, String label, Label value) {
        Label nameLabel = new Label(label);
        nameLabel.setTextFill(Color.LIGHTGRAY);

        value.setTextFill(Color.WHITE);
        value.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        grid.add(nameLabel, 0, row);
        grid.add(value, 1, row);
    }

    private VBox createCompositionChart() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        Label title = new Label("舰船组成");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        // 这里可以添加一个条形图或饼图
        // 简化版本：使用文本显示
        VBox compositionList = new VBox(3);
        compositionList.setId("composition-list");

        panel.getChildren().addAll(title, compositionList);
        return panel;
    }

    private VBox createShipDetailPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        Label title = new Label("舰船信息");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        // 基本信息
        HBox basicInfo = new HBox(20);

        VBox nameBox = new VBox(2);
        Label nameLabel = new Label("名称:");
        nameLabel.setTextFill(Color.LIGHTGRAY);
        shipNameLabel = new Label();
        shipNameLabel.setTextFill(Color.WHITE);
        shipNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameBox.getChildren().addAll(nameLabel, shipNameLabel);

        VBox classBox = new VBox(2);
        Label classLabel = new Label("等级:");
        classLabel.setTextFill(Color.LIGHTGRAY);
        shipClassLabel = new Label();
        shipClassLabel.setTextFill(Color.WHITE);
        classBox.getChildren().addAll(classLabel, shipClassLabel);

        VBox statusBox = new VBox(2);
        Label statusLabel = new Label("状态:");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        shipStatusLabel = new Label();
        shipStatusLabel.setTextFill(Color.GREEN);
        statusBox.getChildren().addAll(statusLabel, shipStatusLabel);

        basicInfo.getChildren().addAll(nameBox, classBox, statusBox);

        // 状态条
        VBox statusBars = new VBox(8);
        statusBars.setPadding(new Insets(10));
        statusBars.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        addStatusBar(statusBars, "船体:", shipHealthBar = new ProgressBar());
        addStatusBar(statusBars, "护盾:", shipShieldBar = new ProgressBar());
        addStatusBar(statusBars, "装甲:", shipArmorBar = new ProgressBar());
        addStatusBar(statusBars, "燃料:", shipFuelBar = new ProgressBar());

        panel.getChildren().addAll(title, basicInfo, statusBars);
        return panel;
    }

    private void addStatusBar(VBox container, String label, ProgressBar progressBar) {
        HBox row = new HBox(10);

        Label nameLabel = new Label(label);
        nameLabel.setTextFill(Color.LIGHTGRAY);
        nameLabel.setPrefWidth(50);

        progressBar.setPrefWidth(200);
        progressBar.setStyle("-fx-accent: #4CAF50;");

        row.getChildren().addAll(nameLabel, progressBar);
        container.getChildren().add(row);
    }

    private VBox createFleetOperationsPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        Label title = new Label("舰队操作");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        moveFleetButton = new Button("移动舰队");
        splitFleetButton = new Button("拆分舰队");
        mergeFleetButton = new Button("合并舰队");
        repairFleetButton = new Button("修理舰队");
        resupplyFleetButton = new Button("补给舰队");
        buildShipButton = new Button("建造舰船"); // 添加建造舰船按钮

        // 设置按钮样式
        String buttonStyle = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-min-width: 120;";
        moveFleetButton.setStyle(buttonStyle);
        splitFleetButton.setStyle(buttonStyle);
        mergeFleetButton.setStyle(buttonStyle);
        repairFleetButton.setStyle(buttonStyle);
        resupplyFleetButton.setStyle(buttonStyle);
        buildShipButton.setStyle(buttonStyle); // 设置建造舰船按钮样式

        panel.getChildren().addAll(title, moveFleetButton, splitFleetButton,
                mergeFleetButton, repairFleetButton, resupplyFleetButton, buildShipButton);
        return panel;
    }

    private VBox createShipOperationsPanel() {
        // 舰船操作功能已移除
        return new VBox();
    }

    private void updateFleetDetails() {
        if (selectedFleet == null) {
            clearFleetDetails();
            return;
        }

        fleetNameLabel.setText(selectedFleet.getName());
        
        // 改进位置显示，显示更详细的位置信息
        if (selectedFleet.getCurrentHex() != null) {
            Hex currentHex = selectedFleet.getCurrentHex();
            CubeCoord coord = currentHex.getCoord();
            
            // 尝试获取六边形中的星系信息
            String locationInfo = coord.toString();
            
            // 如果六边形包含星系，显示星系名称
            if (currentHex.hasStarSystem()) {
                locationInfo += " (" + currentHex.getStarSystem().getName() + ")";
            }
            
            fleetLocationLabel.setText(locationInfo);
        } else {
            fleetLocationLabel.setText("未知");
        }
        
        // fleetMissionLabel.setText(selectedFleet.getCurrentMission().getDisplayName());  // 移除任务显示
        fleetCombatPowerLabel.setText(String.format("%.0f", selectedFleet.getTotalCombatPower()));
        fleetShipCountLabel.setText(String.valueOf(selectedFleet.getShipCount()));
        fleetCrewLabel.setText(String.valueOf(selectedFleet.getTotalCrew()));
        fleetSupplyLabel.setText(String.format("%.1f%%", selectedFleet.getSupplyEfficiency() * 100));

        // 更新舰船组成
        updateCompositionChart();
    }

    private void clearFleetDetails() {
        fleetNameLabel.setText("");
        fleetLocationLabel.setText("");
        // fleetMissionLabel.setText("");  // 移除任务显示
        fleetCombatPowerLabel.setText("");
        fleetShipCountLabel.setText("");
        fleetCrewLabel.setText("");
        fleetSupplyLabel.setText("");
    }

    private void updateCompositionChart() {
        VBox compositionList = (VBox) lookup("#composition-list");
        if (compositionList == null) return;

        compositionList.getChildren().clear();

        if (selectedFleet != null) {
            Map<ShipClass, Integer> composition = selectedFleet.getShipCountByClass();
            for (Map.Entry<ShipClass, Integer> entry : composition.entrySet()) {
                HBox row = createCompositionRow(entry.getKey(), entry.getValue());
                compositionList.getChildren().add(row);
            }
        }
    }

    private HBox createCompositionRow(ShipClass shipClass, int count) {
        HBox row = new HBox(10);

        Label iconLabel = new Label(shipClass.getIcon());

        Label nameLabel = new Label(shipClass.getDisplayName());
        nameLabel.setTextFill(Color.LIGHTGRAY);
        nameLabel.setPrefWidth(80);

        ProgressBar bar = new ProgressBar((double) count / Math.max(1, selectedFleet.getShipCount()));
        bar.setPrefWidth(100);
        bar.setStyle("-fx-accent: " + getClassColor(shipClass));

        Label countLabel = new Label(String.valueOf(count));
        countLabel.setTextFill(Color.WHITE);

        row.getChildren().addAll(iconLabel, nameLabel, bar, countLabel);
        return row;
    }

    private String getClassColor(ShipClass shipClass) {
        switch (shipClass) {
            case CORVETTE: return "#2196F3";
            case FRIGATE: return "#4CAF50";
            case DESTROYER: return "#FF9800";
            case CRUISER: return "#9C27B0";
            case BATTLESHIP: return "#f44336";
            case CARRIER: return "#00BCD4";
            default: return "#607D8B";
        }
    }

    private void updateShipList() {
        if (selectedFleet != null) {
            shipListView.setItems(FXCollections.observableArrayList(selectedFleet.getShips()));
        } else {
            shipListView.setItems(FXCollections.observableArrayList());
        }
    }

    private void updateShipDetails() {
        if (selectedShip == null) {
            clearShipDetails();
            return;
        }

        shipNameLabel.setText(selectedShip.getName());
        shipClassLabel.setText(selectedShip.getDesign().getShipClass().getDisplayName());
        shipStatusLabel.setText(selectedShip.getStatus());

        // 更新状态条
        shipHealthBar.setProgress(selectedShip.getHitPointPercentage() / 100.0);
        shipShieldBar.setProgress(selectedShip.getShieldPercentage() / 100.0);
        shipArmorBar.setProgress(selectedShip.getArmorPercentage() / 100.0);
        shipFuelBar.setProgress(selectedShip.getFuelPercentage() / 100.0);

        // 根据状态设置颜色
        updateStatusBarColors();
    }

    private void updateStatusBarColors() {
        if (selectedShip == null) return;

        // 船体健康度
        if (selectedShip.getHitPointPercentage() < 30) {
            shipHealthBar.setStyle("-fx-accent: #f44336;");
        } else if (selectedShip.getHitPointPercentage() < 70) {
            shipHealthBar.setStyle("-fx-accent: #FF9800;");
        } else {
            shipHealthBar.setStyle("-fx-accent: #4CAF50;");
        }

        // 护盾
        shipShieldBar.setStyle("-fx-accent: #2196F3;");

        // 装甲
        shipArmorBar.setStyle("-fx-accent: #795548;");

        // 燃料
        if (selectedShip.getFuelPercentage() < 20) {
            shipFuelBar.setStyle("-fx-accent: #f44336;");
        } else if (selectedShip.getFuelPercentage() < 50) {
            shipFuelBar.setStyle("-fx-accent: #FF9800;");
        } else {
            shipFuelBar.setStyle("-fx-accent: #FFC107;");
        }
    }

    private void clearShipDetails() {
        shipNameLabel.setText("");
        shipClassLabel.setText("");
        shipStatusLabel.setText("");

        shipHealthBar.setProgress(0);
        shipShieldBar.setProgress(0);
        shipArmorBar.setProgress(0);
        shipFuelBar.setProgress(0);
    }

    private void refreshData() {
        // 保存当前选择的舰队信息
        String selectedFleetName = selectedFleet != null ? selectedFleet.getName() : null;
        
        // 重新加载数据
        loadFleets();
        loadShipyards();
        
        // 保持UI组件的引用
        fleetListView.setItems(fleets);

        // 尝试重新选择之前的舰队
        if (selectedFleetName != null) {
            Fleet fleetToSelect = fleets.stream()
                .filter(f -> f.getName().equals(selectedFleetName))
                .findFirst()
                .orElse(null);
                
            if (fleetToSelect != null) {
                fleetListView.getSelectionModel().select(fleetToSelect);
                selectedFleet = fleetToSelect; // 更新引用
                updateFleetDetails();
                updateShipList();
            }
        }
    }

    // 对话框方法
    private void showMoveFleetDialog() {
        if (selectedFleet == null) return;

        // 检查舰队是否本回合已移动
        if (selectedFleet.hasMovedThisTurn()) {
            showAlert("移动限制", "该舰队本回合已移动过，无法再次移动");
            return;
        }
        
        // 高亮可移动范围，等待用户在地图上点击目标六边形
        if (hexMapView != null) {
            hexMapView.setSelectedFleet(selectedFleet);
            
            showAlert("移动准备", "已高亮显示可移动范围，请点击目标六边形进行移动\n" +
                      "(点击当前六边形可取消移动模式)");
        } else {
            Dialog<Hex> dialog = new Dialog<>();
            dialog.setTitle("移动舰队");
            dialog.setHeaderText("选择舰队目的地");

            // 创建地图选择界面
            // 这里可以集成HexMapView
            Label mapLabel = new Label("地图选择界面");
            mapLabel.setPrefSize(400, 300);

            dialog.getDialogPane().setContent(mapLabel);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    // 返回选择的Hex
                    return selectedFleet.getCurrentHex(); // 简化实现
                }
                return null;
            });

            dialog.showAndWait().ifPresent(hex -> {
                selectedFleet.moveTo(hex);
                updateFleetDetails();
            });
        }
    }

    private void showSplitFleetDialog() {
        if (selectedFleet == null || selectedFleet.getShipCount() < 2) return;

        Dialog<Fleet> dialog = new Dialog<>();
        dialog.setTitle("拆分舰队");
        dialog.setHeaderText("选择要转移的舰船");

        // 创建舰船选择列表
        ListView<Ship> shipSelectionList = new ListView<>(
                FXCollections.observableArrayList(selectedFleet.getShips())
        );
        shipSelectionList.setPrefHeight(300);
        shipSelectionList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        shipSelectionList.setCellFactory(lv -> new ShipListCell());

        // 新舰队名称输入
        TextField newFleetName = new TextField();
        newFleetName.setPromptText("新舰队名称");

        VBox content = new VBox(10,
                new Label("选择要转移的舰船:"),
                shipSelectionList,
                new Label("新舰队名称:"),
                newFleetName
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                List<Ship> selectedShips = shipSelectionList.getSelectionModel().getSelectedItems();
                if (!selectedShips.isEmpty() && !newFleetName.getText().trim().isEmpty()) {
                    Fleet newFleet = selectedFleet.splitFleet(newFleetName.getText(), selectedShips);
                    fleets.add(newFleet);
                    return newFleet;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newFleet -> {
            // 确保新舰队在六边形中
            if (newFleet.getCurrentHex() != null && !newFleet.getCurrentHex().getFleets().contains(newFleet)) {
                newFleet.getCurrentHex().addEntity(newFleet);
            }
            
            updateFleetDetails();
            updateShipList();
            fleetListView.getSelectionModel().select(newFleet);
        });
    }

    private void showMergeFleetDialog() {
        if (selectedFleet == null || fleets.size() < 2) return;

        Dialog<Fleet> dialog = new Dialog<>();
        dialog.setTitle("合并舰队");
        dialog.setHeaderText("选择要合并的舰队");

        // 创建舰队选择列表（排除当前舰队）
        ListView<Fleet> fleetSelectionList = new ListView<>(
                fleets.filtered(fleet -> !fleet.equals(selectedFleet))
        );
        fleetSelectionList.setPrefHeight(200);
        fleetSelectionList.setCellFactory(lv -> new FleetListCell());

        dialog.getDialogPane().setContent(fleetSelectionList);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return fleetSelectionList.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(targetFleet -> {
            if (targetFleet != null) {
                selectedFleet.mergeFleet(targetFleet);
                fleets.remove(targetFleet);
                
                // 确保合并后的舰队在六边形中
                if (selectedFleet.getCurrentHex() != null && !selectedFleet.getCurrentHex().getFleets().contains(selectedFleet)) {
                    selectedFleet.getCurrentHex().addEntity(selectedFleet);
                }
                
                updateFleetDetails();
                updateShipList();
            }
        });
    }

    private void repairSelectedFleet() {
        if (selectedFleet == null) return;

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("修理舰队");
        confirmDialog.setHeaderText("确定要修理舰队吗？");
        confirmDialog.setContentText("这将消耗资源来修复舰队损伤。");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 执行修理逻辑
                showAlert("修理完成", selectedFleet.getName() + " 已开始修理。");
                updateFleetDetails();
            }
        });
    }

    private void resupplySelectedFleet() {
        if (selectedFleet == null) return;

        selectedFleet.resupplyAll();
        showAlert("补给完成", selectedFleet.getName() + " 已获得补给。");
        updateFleetDetails();
    }

    private void changeFleetMission() {
        if (selectedFleet == null || missionComboBox.getValue() == null) return;

        FleetMission newMission = missionComboBox.getValue();
        selectedFleet.setMission(newMission, null);

        showAlert("任务更改", selectedFleet.getName() + " 的任务已更改为 " + newMission.getDisplayName());
        updateFleetDetails();
    }

    private void showBuildShipDialog() {
        // 显示舰船设计器
        ShipDesignerUI designer = new ShipDesignerUI();

        Dialog<ShipDesign> dialog = new Dialog<>();
        dialog.setTitle("建造舰船");
        dialog.setHeaderText("选择舰船设计");

        // 获取可用的设计
        ObservableList<ShipDesign> availableDesigns = designer.getSavedDesigns();
        if (availableDesigns.isEmpty()) {
            showAlert("没有设计", "请先设计舰船。");
            return;
        }

        ListView<ShipDesign> designList = new ListView<>(availableDesigns);
        designList.setPrefHeight(300);
        designList.setCellFactory(lv -> new DesignListCell());
        
        // 设置列表样式，与主界面风格保持一致
        designList.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");

        // 殖民地选择（代替造船厂）
        ComboBox<Colony> colonyCombo = new ComboBox<>(FXCollections.observableArrayList(playerFaction.getColonies()));
        colonyCombo.setPromptText("选择殖民地");
        
        // 设置下拉框样式，与主界面风格保持一致
        colonyCombo.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");

        // 数量选择
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 100, 1);
        quantitySpinner.getValueFactory().setValue(1);
        quantitySpinner.setPrefWidth(100);

        TextArea resourceLabel = new TextArea();
        resourceLabel.setEditable(false);
        resourceLabel.setWrapText(true);
        resourceLabel.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: black; -fx-font-family: 'Arial'; -fx-font-size: 14;");
        resourceLabel.setPrefHeight(100); // 设置默认高度
        resourceLabel.setMaxHeight(Double.MAX_VALUE);
        resourceLabel.setText("请先选择舰船设计和殖民地。");

        // 当选择设计时更新资源需求
        designList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && colonyCombo.getValue() != null) {
                updateResourceRequirements(newVal, quantitySpinner.getValue(), resourceLabel, colonyCombo.getValue());
            } else {
                resourceLabel.setText("请先选择殖民地。");
            }
        });
        
        // 当数量变化时更新资源需求
        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            ShipDesign selectedDesign = designList.getSelectionModel().getSelectedItem();
            if (selectedDesign != null && colonyCombo.getValue() != null) {
                updateResourceRequirements(selectedDesign, newVal, resourceLabel, colonyCombo.getValue());
            }
        });
        
        // 当殖民地变化时更新资源需求
        colonyCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            ShipDesign selectedDesign = designList.getSelectionModel().getSelectedItem();
            if (selectedDesign != null && newVal != null) {
                updateResourceRequirements(selectedDesign, quantitySpinner.getValue(), resourceLabel, newVal);
            } else if (selectedDesign != null) {
                resourceLabel.setText("请先选择殖民地。");
            }
        });

        // 移除了舰队名称输入功能

        VBox content = new VBox(10,
                new Label("选择舰船设计:"),
                designList,
                new Label("选择殖民地:"),
                colonyCombo,
                new Label("建造数量:"),
                quantitySpinner,
                // 移除了舰队名称输入功能
                new Label("资源需求:"),
                resourceLabel
        );
        
        // 设置VBox的优先级，允许资源标签扩展
        VBox.setVgrow(resourceLabel, Priority.ALWAYS);

        // 设置内容面板样式，与主界面风格保持一致
        content.setPadding(new Insets(10));
        content.setStyle("-fx-font-family: 'Arial'; -fx-background-color: #2b2b2b;");

        // 设置标签样式
        for (javafx.scene.Node node : content.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setTextFill(Color.WHITE);
                ((Label) node).setFont(Font.font("Arial", 14));
            }
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 设置窗口图标
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/icon.png"));
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("无法加载窗口图标: " + e.getMessage());
        }

        // 设置弹窗样式，与主界面风格保持一致
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-font-family: 'Arial'; " +
                           "-fx-background-color: #2b2b2b;");
        dialogPane.setPrefSize(600, 800); // 进一步增大对话框高度以容纳更多资源需求信息

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                ShipDesign selectedDesign = designList.getSelectionModel().getSelectedItem();
                Colony selectedColony = colonyCombo.getValue();

                if (selectedDesign != null && selectedColony != null) {
                    return selectedDesign;
                }
            }
            return null;
        });

        // 修复：正确处理对话框结果
        Optional<ShipDesign> result = dialog.showAndWait();
        if (result.isPresent()) {
            ShipDesign design = result.get();
            Colony selectedColony = colonyCombo.getValue();
            int quantity = quantitySpinner.getValue();
            // 移除了舰队命名功能，使用默认名称
            String fleetName = selectedColony.getName() + " 舰队";

            if (design != null && selectedColony != null) {
                // 检查资源是否足够
                String insufficientResources = getInsufficientResources(selectedColony.getFaction(), design, quantity);
                if (insufficientResources.isEmpty()) {
                    // 创建新舰队，舰队位置在所选殖民地所在的六边形
                    Planet colonyPlanet = selectedColony.getPlanet();
                    StarSystem starSystem = colonyPlanet.getStarSystem();

                    // 检查星系和星系的六边形是否存在
                    Hex colonyHex = null;
                    if (playerFaction.getGalaxy() != null && starSystem != null) {
                        colonyHex = playerFaction.getGalaxy().getHexForStarSystem(starSystem);
                    }

                    // 如果无法获取六边形，使用默认六边形或抛出错误
                    if (colonyHex == null) {
                        showAlert("建造失败", "无法确定殖民地所在六边形，无法建造舰队。");
                        return;
                    }

                    Fleet newFleet = new Fleet(fleetName, selectedColony.getFaction(), colonyHex);

                    // 执行建造逻辑 - 为新舰队添加舰船
                    for (int i = 0; i < quantity; i++) {
                        // 创建新舰船实例
                        String shipName = newFleet.generateUniqueShipName(design);
                        Ship newShip = new Ship(shipName, design, selectedColony.getFaction());

                        // 将新舰船添加到新创建的舰队中
                        newFleet.addShip(newShip);

                        // 消耗资源
                        consumeResources(selectedColony.getFaction(), design);
                    }

                    // 将新舰队添加到UI列表中
                    fleets.add(newFleet);

                    // 更新UI
                    updateFleetDetails();
                    updateShipList();

                    // 选择新创建的舰队
                    fleetListView.getSelectionModel().select(newFleet);
                    selectedFleet = newFleet;

                    showAlert("建造完成", quantity + "艘 " + design.getFullName() + " 已建造并加入新舰队 " + newFleet.getName() + "。");
                } else {
                    showAlert("资源不足", "派系资源不足以建造指定数量的舰船。\n\n缺少的资源:\n" + insufficientResources);
                }
            } else {
                showAlert("建造失败", "请选择一个舰船设计和殖民地。");
            }
        }
    }

    private void updateResourceRequirements(ShipDesign design, int quantity, TextArea resourceLabel, Colony colony) {
        if (design == null || quantity <= 0) {
            resourceLabel.setText("请选择舰船设计并输入大于0的建造数量。");
            return;
        }

        StringBuilder resourceText = new StringBuilder("\n");
        Map<ResourceType, Float> costs = design.getConstructionCost();

        boolean hasRequirements = false;
        for (Map.Entry<ResourceType, Float> entry : costs.entrySet()) {
            float totalCost = entry.getValue() * quantity;
            if (totalCost > 0) {
                float available = playerFaction != null
                        ? playerFaction.getResourceStockpile().getResource(entry.getKey())
                        : 0f;

                boolean enough = available >= totalCost;

                resourceText.append(entry.getKey().getDisplayName())
                           .append(": ")
                           .append(String.format("%.2f", totalCost))
                           .append(" (库存: ")
                           .append(String.format("%.2f", available))
                           .append(")");

                if (!enough) {
                    resourceText.append("  << 不足");
                }
                resourceText.append("\n");
                hasRequirements = true;
            }
        }

        if (!hasRequirements) {
            resourceText.append("无特殊资源需求\n");
        }

        resourceLabel.setText(resourceText.toString());
    }

    private String getInsufficientResources(Faction faction, ShipDesign design, int quantity) {
        Map<ResourceType, Float> costs = design.getConstructionCost();
        ResourceStockpile stockpile = playerFaction.getResourceStockpile();

        StringBuilder missingResources = new StringBuilder();

        for (Map.Entry<ResourceType, Float> entry : costs.entrySet()) {
            float required = entry.getValue() * quantity;
            float available = stockpile.getResource(entry.getKey());

            if (available < required) {
                if (missingResources.length() > 0) {
                    missingResources.append("\n");
                }
                missingResources.append(entry.getKey().getDisplayName())
                    .append(": 需要 ").append(String.format("%.2f", required))
                    .append(", 拥有 ").append(String.format("%.2f", available));
            }
        }

        return missingResources.toString();
    }

    private void consumeResources(Faction faction, ShipDesign design) {
        Map<ResourceType, Float> costs = design.getConstructionCost();
        ResourceStockpile stockpile = playerFaction.getResourceStockpile();

        for (Map.Entry<ResourceType, Float> entry : costs.entrySet()) {
            stockpile.consumeResource(entry.getKey(), entry.getValue());
        }
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
        Node titleNode = dialogPane.lookup(".alert-title");
        if (titleNode != null) {
            titleNode.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        }

        alert.showAndWait();
    }

    // 自定义列表单元格
    private class FleetListCell extends ListCell<Fleet> {
        @Override
        protected void updateItem(Fleet fleet, boolean empty) {
            super.updateItem(fleet, empty);

            if (empty || fleet == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox container = new HBox(10);

                // 任务图标
                Label missionIcon = new Label(fleet.getCurrentMission().getIcon());
                missionIcon.setStyle("-fx-font-size: 16;");

                VBox infoBox = new VBox(2);

                // 舰队名称
                Label nameLabel = new Label(fleet.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

                // 舰队信息
                Label infoLabel = new Label(
                        fleet.getShipCount() + "艘舰船 | " +
                                String.format("%.0f", fleet.getTotalCombatPower()) + "战斗力"
                );
                infoLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11;");

                infoBox.getChildren().addAll(nameLabel, infoLabel);
                container.getChildren().addAll(missionIcon, infoBox);
                setGraphic(container);
            }
        }
    }

    private class ShipListCell extends ListCell<Ship> {
        @Override
        protected void updateItem(Ship ship, boolean empty) {
            super.updateItem(ship, empty);

            if (empty || ship == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox container = new HBox(10);

                // 舰船等级图标
                Label classIcon = new Label(ship.getDesign().getShipClass().getIcon());
                classIcon.setStyle("-fx-font-size: 16;");

                VBox infoBox = new VBox(2);

                // 舰船名称
                Label nameLabel = new Label(ship.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

                // 舰船状态
                Label statusLabel = new Label(ship.getStatus() + " | " +
                        String.format("%.0f%%", ship.getHitPointPercentage()));
                statusLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11;");

                infoBox.getChildren().addAll(nameLabel, statusLabel);
                container.getChildren().addAll(classIcon, infoBox);
                setGraphic(container);
            }
        }
    }

    private class DesignListCell extends ListCell<ShipDesign> {
        @Override
        protected void updateItem(ShipDesign design, boolean empty) {
            super.updateItem(design, empty);

            if (empty || design == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox container = new HBox(10);

                // 舰船等级图标
                Label classIcon = new Label(design.getShipClass().getIcon());
                classIcon.setStyle("-fx-font-size: 16;");

                VBox infoBox = new VBox(2);

                // 设计名称
                Label nameLabel = new Label(design.getFullName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

                // 设计规格
                Label specsLabel = new Label(
                        String.format("%.0f战斗力 | %d模块",
                                design.calculateCombatPower(),
                                design.getModules().size())
                );
                specsLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11;");

                infoBox.getChildren().addAll(nameLabel, specsLabel);
                container.getChildren().addAll(classIcon, infoBox);
                setGraphic(container);
            }
        }
    }
}

