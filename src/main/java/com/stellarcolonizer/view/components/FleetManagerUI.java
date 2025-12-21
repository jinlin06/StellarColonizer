package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.fleet.*;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.fleet.enums.FleetMission;
import com.stellarcolonizer.model.fleet.enums.ShipClass;
import com.stellarcolonizer.model.galaxy.CubeCoord;
import com.stellarcolonizer.model.galaxy.Hex;
import com.stellarcolonizer.model.colony.Colony;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
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
    private Button changeMissionButton;
    private Button buildShipButton;
    private Button scrapShipButton;
    private Button transferShipButton;

    // 任务选择
    private ComboBox<FleetMission> missionComboBox;

    // 地图视图集成
    private HexMapView hexMapView;

    public FleetManagerUI(Faction playerFaction) {
        this.playerFaction = playerFaction;
        this.fleets = FXCollections.observableArrayList();
        this.shipyards = FXCollections.observableArrayList();

        // 加载数据
        loadFleets();
        loadShipyards();

        initializeUI();
        setupEventHandlers();
    }

    private void loadFleets() {
        // 这里应该从游戏引擎加载舰队数据
        // 暂时创建一些示例舰队
        fleets.clear();

        // 示例舰队
        Hex startHex = new Hex(new CubeCoord(0, 0, 0));
        Fleet fleet1 = new Fleet("第一舰队", playerFaction, startHex);
        fleet1.setName("探索舰队");

        Fleet fleet2 = new Fleet("第二舰队", playerFaction, startHex);
        fleet2.setName("防御舰队");

        fleets.addAll(fleet1, fleet2);
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
        addFleetInfoRow(infoGrid, 2, "任务:", fleetMissionLabel = new Label());
        addFleetInfoRow(infoGrid, 3, "战斗力:", fleetCombatPowerLabel = new Label());
        addFleetInfoRow(infoGrid, 4, "舰船数量:", fleetShipCountLabel = new Label());
        addFleetInfoRow(infoGrid, 5, "船员总数:", fleetCrewLabel = new Label());
        addFleetInfoRow(infoGrid, 6, "补给效率:", fleetSupplyLabel = new Label());

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

        // 舰船操作按钮
        VBox shipOperations = createShipOperationsPanel();

        // 任务控制
        VBox missionControl = createMissionControlPanel();

        panel.getChildren().addAll(title, fleetOperations, shipOperations, missionControl);
        return panel;
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
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        Label title = new Label("舰船操作");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        scrapShipButton = new Button("拆解舰船");
        transferShipButton = new Button("转移舰船");

        String buttonStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-min-width: 120;";
        scrapShipButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-min-width: 120;");
        transferShipButton.setStyle(buttonStyle);

        panel.getChildren().addAll(title, scrapShipButton, transferShipButton);
        return panel;
    }

    private VBox createMissionControlPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        Label title = new Label("任务控制");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        // 任务选择
        HBox missionBox = new HBox(5);
        Label missionLabel = new Label("任务:");
        missionLabel.setTextFill(Color.LIGHTGRAY);

        missionComboBox = new ComboBox<>();
        missionComboBox.getItems().addAll(FleetMission.values());
        missionComboBox.setPrefWidth(150);
        
        // 设置任务下拉框显示中文名称
        missionComboBox.setCellFactory(lv -> new ListCell<FleetMission>() {
            @Override
            protected void updateItem(FleetMission item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        missionComboBox.setButtonCell(new ListCell<FleetMission>() {
            @Override
            protected void updateItem(FleetMission item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("选择任务");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        missionBox.getChildren().addAll(missionLabel, missionComboBox);

        // 任务按钮
        changeMissionButton = new Button("更改任务");
        changeMissionButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-min-width: 120;");

        panel.getChildren().addAll(title, missionBox, changeMissionButton);
        return panel;
    }

    private void setupEventHandlers() {
        // 舰队选择
        fleetListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldFleet, newFleet) -> {
                    selectedFleet = newFleet;
                    updateFleetDetails();
                    updateShipList();
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

        // 更改任务
        changeMissionButton.setOnAction(e -> changeFleetMission());

        // 建造舰船
        buildShipButton.setOnAction(e -> showBuildShipDialog());

        // 拆解舰船
        scrapShipButton.setOnAction(e -> scrapSelectedShip());

        // 转移舰船
        transferShipButton.setOnAction(e -> showTransferShipDialog());
    }

    private void updateFleetDetails() {
        if (selectedFleet == null) {
            clearFleetDetails();
            return;
        }

        fleetNameLabel.setText(selectedFleet.getName());
        fleetLocationLabel.setText(selectedFleet.getCurrentHex() != null ?
                selectedFleet.getCurrentHex().getCoord().toString() : "未知");
        fleetMissionLabel.setText(selectedFleet.getCurrentMission().getDisplayName());
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
        fleetMissionLabel.setText("");
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
        loadFleets();
        loadShipyards();
        fleetListView.setItems(fleets);

        if (!fleets.isEmpty() && selectedFleet != null) {
            // 重新选择当前舰队
            fleetListView.getSelectionModel().select(selectedFleet);
        }
    }

    // 对话框方法
    private void showMoveFleetDialog() {
        if (selectedFleet == null) return;

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
        if (selectedFleet == null) return;

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

        // 造船厂选择
        ComboBox<Colony> shipyardCombo = new ComboBox<>(shipyards);
        shipyardCombo.setPromptText("选择造船厂");
        
        // 设置下拉框样式，与主界面风格保持一致
        shipyardCombo.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");

        VBox content = new VBox(10,
                new Label("选择舰船设计:"),
                designList,
                new Label("选择造船厂:"),
                shipyardCombo
        );
        
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
        dialogPane.setPrefSize(500, 500);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                ShipDesign selectedDesign = designList.getSelectionModel().getSelectedItem();
                Colony selectedShipyard = shipyardCombo.getValue();

                if (selectedDesign != null && selectedShipyard != null) {
                    return selectedDesign;
                }
            }
            return null;
        });

        // 修复：正确处理对话框结果
        Optional<ShipDesign> result = dialog.showAndWait();
        if (result.isPresent()) {
            ShipDesign design = result.get();
            Colony selectedShipyard = shipyardCombo.getValue();
            
            if (design != null && selectedShipyard != null) {
                // 执行建造逻辑
                // 创建新舰船实例
                String shipName = selectedFleet.generateUniqueShipName(design);
                Ship newShip = new Ship(shipName, design, selectedFleet.getFaction());
                
                // 将新舰船添加到选定的舰队中
                selectedFleet.addShip(newShip);
                
                // 更新UI
                updateFleetDetails();
                updateShipList();
                
                showAlert("建造完成", design.getFullName() + " 已建造并加入 " + selectedFleet.getName() + "。");
            } else {
                showAlert("建造失败", "请选择一个舰船设计和造船厂。");
            }
        }
    }

    private void scrapSelectedShip() {
        if (selectedShip == null) return;

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("拆解舰船");
        confirmDialog.setHeaderText("确定要拆解 " + selectedShip.getName() + " 吗？");
        confirmDialog.setContentText("将返还部分建造资源。");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (selectedFleet != null) {
                    selectedFleet.removeShip(selectedShip);
                    updateFleetDetails();
                    updateShipList();
                    showAlert("拆解完成", selectedShip.getName() + " 已拆解。");
                }
            }
        });
    }

    private void showTransferShipDialog() {
        if (selectedShip == null || fleets.size() < 2) return;

        Dialog<Fleet> dialog = new Dialog<>();
        dialog.setTitle("转移舰船");
        dialog.setHeaderText("选择目标舰队");

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
            if (targetFleet != null && selectedFleet != null) {
                selectedFleet.transferShip(selectedShip, targetFleet);
                updateFleetDetails();
                updateShipList();
                showAlert("转移完成", selectedShip.getName() + " 已转移到 " + targetFleet.getName());
            }
        });
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