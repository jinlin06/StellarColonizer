// ColonyManagerView.java - 殖民地管理器视图
package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.colony.*;
import com.stellarcolonizer.model.colony.enums.BuildingType;
import com.stellarcolonizer.model.colony.enums.GrowthFocus;
import com.stellarcolonizer.model.colony.enums.PopType;
import com.stellarcolonizer.model.economy.ResourceStockpile;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.view.models.ResourceStat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.chart.XYChart;

import java.util.Map;

public class ColonyManagerView extends VBox {

    private final ObservableList<Colony> colonies;
    private final Faction playerFaction;

    // UI组件
    private ListView<Colony> colonyListView;
    private TabPane colonyDetailPane;

    // 当前选择的殖民地
    private Colony selectedColony;

    // 详细视图组件
    private Label colonyNameLabel;
    private Label planetInfoLabel;
    private Label populationLabel;
    private Label happinessLabel;
    private Label stabilityLabel;
    private Label developmentLabel;
    
    // 人口增长进度条
    private ProgressBar populationGrowthProgressBar;
    private Label populationGrowthProgressLabel;

    // 资源显示
    private VBox resourcePanel;

    // 建筑列表
    private ListView<Building> buildingListView;

    // 人口分布图表
    private PieChart populationChart;
    
    // 资源趋势图相关
    private LineChart<String, Number> resourceChart;
    private int turnCounter = 0;  // 记录回合数
    private Map<String, XYChart.Series<String, Number>> resourceSeriesMap = new java.util.HashMap<>();

    public ColonyManagerView(Faction playerFaction) {
        this.playerFaction = playerFaction;
        this.colonies = FXCollections.observableArrayList();
        this.colonies.addAll(playerFaction.getColonies());

        initializeUI();
        setupEventHandlers();
        
        // 初始化后立即更新殖民地详细信息，如果存在殖民地
        if (!colonies.isEmpty()) {
            selectedColony = colonies.get(0);
            updateColonyDetails();
        }
    }

    private void initializeUI() {
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #2b2b2b;");

        // 创建主布局：左侧列表，右侧详情
        HBox mainLayout = new HBox(20);

        // 左侧：殖民地列表
        VBox leftPanel = createColonyListPanel();

        // 右侧：殖民地详情
        VBox rightPanel = createColonyDetailPanel();

        mainLayout.getChildren().addAll(leftPanel, rightPanel);
        getChildren().add(mainLayout);

        // 如果有关联的殖民地，选择第一个
        if (!colonies.isEmpty()) {
            colonyListView.getSelectionModel().select(0);
        }
    }

    private VBox createColonyListPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(300);

        Label title = new Label("殖民地列表");
        title.setStyle("-fx-font-size: 18; -fx-text-fill: white; -fx-font-weight: bold;");

        colonyListView = new ListView<>(colonies);
        colonyListView.setCellFactory(listView -> new ColonyListCell());
        colonyListView.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");

        // 添加新殖民地按钮
        Button addColonyButton = new Button("建立新殖民地");
        addColonyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addColonyButton.setOnAction(e -> showColonizeDialog());

        panel.getChildren().addAll(title, colonyListView, addColonyButton);
        return panel;
    }

    private VBox createColonyDetailPanel() {
        VBox panel = new VBox(10);

        // 基本信息区域
        HBox infoPanel = createColonyInfoPanel();

        // 选项卡面板
        colonyDetailPane = new TabPane();
        colonyDetailPane.setStyle("-fx-background-color: #1e1e1e;");

        // 添加选项卡
        Tab overviewTab = new Tab("概览", createOverviewTab());
        Tab buildingsTab = new Tab("建筑", createBuildingsTab());
        Tab populationTab = new Tab("人口", createPopulationTab());
        Tab resourcesTab = new Tab("资源", createResourcesTab());
        
        // 设置选项卡不可关闭
        overviewTab.setClosable(false);
        buildingsTab.setClosable(false);
        populationTab.setClosable(false);
        resourcesTab.setClosable(false);

        colonyDetailPane.getTabs().addAll(overviewTab, buildingsTab, populationTab, resourcesTab);

        panel.getChildren().addAll(infoPanel, colonyDetailPane);
        return panel;
    }

    private HBox createColonyInfoPanel() {
        HBox panel = new HBox(20);
        panel.setStyle("-fx-background-color: #333333; -fx-padding: 15; -fx-background-radius: 5;");

        // 左侧：基础信息
        VBox basicInfo = new VBox(5);
        colonyNameLabel = createInfoLabel("殖民地名称", "N/A", Color.WHITE, 16, true);
        planetInfoLabel = createInfoLabel("行星", "N/A", Color.LIGHTGRAY, 14, false);

        basicInfo.getChildren().addAll(colonyNameLabel, planetInfoLabel);

        // 右侧：状态信息
        VBox statusInfo = new VBox(5);
        populationLabel = createInfoLabel("人口", "N/A", Color.LIGHTBLUE, 14, false);
        happinessLabel = createInfoLabel("幸福度", "N/A", Color.GREEN, 14, false);
        stabilityLabel = createInfoLabel("稳定度", "N/A", Color.ORANGE, 14, false);
        developmentLabel = createInfoLabel("发展度", "N/A", Color.YELLOW, 14, false);
        
        // 人口增长进度条
        populationGrowthProgressBar = new ProgressBar(0);
        populationGrowthProgressBar.setPrefWidth(200);
        populationGrowthProgressLabel = new Label("人口增长进度: 0/90");
        populationGrowthProgressLabel.setTextFill(Color.LIGHTGREEN);

        VBox growthProgressBox = new VBox(2);
        growthProgressBox.getChildren().addAll(populationGrowthProgressLabel, populationGrowthProgressBar);

        statusInfo.getChildren().addAll(populationLabel, happinessLabel, stabilityLabel, developmentLabel, growthProgressBox);

        panel.getChildren().addAll(basicInfo, statusInfo);
        return panel;
    }

    private Label createInfoLabel(String title, String value, Color color, int fontSize, boolean bold) {
        Label label = new Label(title + ": " + value);
        label.setTextFill(color);
        label.setStyle("-fx-font-size: " + fontSize + "px;" + (bold ? "-fx-font-weight: bold;" : ""));
        return label;
    }

    private VBox createOverviewTab() {
        VBox tabContent = new VBox(10);
        tabContent.setPadding(new Insets(10));

        // 生产统计
        VBox productionStats = createProductionStatsBox();

        // 快速操作按钮
        HBox quickActions = createQuickActionButtons();

        tabContent.getChildren().addAll(productionStats, quickActions);
        return tabContent;
    }

    private VBox createProductionStatsBox() {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: #333333; -fx-padding: 10; -fx-background-radius: 5;");

        Label title = new Label("生产统计");
        title.setStyle("-fx-font-size: 16; -fx-text-fill: white; -fx-font-weight: bold;");

        // 这里会动态更新
        VBox statsContent = new VBox(3);
        statsContent.setId("production-stats-content");

        box.getChildren().addAll(title, statsContent);
        return box;
    }

    private VBox createResourcePanel() {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: #333333; -fx-padding: 10; -fx-background-radius: 5;");

        Label title = new Label("资源库存");
        title.setStyle("-fx-font-size: 16; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox resourcesContent = new VBox(3);
        resourcesContent.setId("resources-content");

        box.getChildren().addAll(title, resourcesContent);
        return box;
    }

    private HBox createQuickActionButtons() {
        HBox box = new HBox(10);

        Button focusFoodButton = new Button("重点发展农业");
        focusFoodButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        focusFoodButton.setOnAction(e -> setGrowthFocus(GrowthFocus.RAPID_GROWTH));

        Button focusIndustryButton = new Button("重点发展工业");
        focusIndustryButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        focusIndustryButton.setOnAction(e -> setGrowthFocus(GrowthFocus.STABLE_GROWTH));

        Button focusQualityButton = new Button("平衡发展");
        focusQualityButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
        focusQualityButton.setOnAction(e -> setGrowthFocus(GrowthFocus.QUALITY_OF_LIFE));

        box.getChildren().addAll(focusFoodButton, focusIndustryButton, focusQualityButton);
        return box;
    }

    private VBox createBuildingsTab() {
        VBox tabContent = new VBox(10);
        tabContent.setPadding(new Insets(10));

        // 建筑列表
        buildingListView = new ListView<>();
        buildingListView.setPrefHeight(300);
        buildingListView.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");
        buildingListView.setCellFactory(listView -> new BuildingListCell());

        // 建筑操作按钮
        HBox buildingActions = new HBox(10);
        Button buildButton = new Button("建造新建筑");
        Button upgradeButton = new Button("升级建筑");
        Button demolishButton = new Button("拆除建筑");

        buildButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        upgradeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        demolishButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        buildButton.setOnAction(e -> showBuildDialog());
        upgradeButton.setOnAction(e -> upgradeSelectedBuilding());
        demolishButton.setOnAction(e -> demolishSelectedBuilding());

        buildingActions.getChildren().addAll(buildButton, upgradeButton, demolishButton);

        tabContent.getChildren().addAll(buildingListView, buildingActions);
        return tabContent;
    }

    private VBox createPopulationTab() {
        VBox tabContent = new VBox(10);
        tabContent.setPadding(new Insets(10));

        // 人口分布饼图
        populationChart = new PieChart();
        populationChart.setTitle("人口分布");
        populationChart.setLabelsVisible(true);
        populationChart.setLegendVisible(true);
        populationChart.setPrefHeight(300);

        // 人口管理界面
        VBox populationManagement = createPopulationManagementPanel();

        tabContent.getChildren().addAll(populationChart, populationManagement);
        return tabContent;
    }

    private VBox createPopulationManagementPanel() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: #333333; -fx-padding: 10; -fx-background-radius: 5;");

        Label title = new Label("人口管理");
        title.setStyle("-fx-font-size: 16; -fx-text-fill: white; -fx-font-weight: bold;");

        // 人口转移界面
        HBox transferPanel = new HBox(10);

        ComboBox<PopType> fromTypeCombo = new ComboBox<>();
        fromTypeCombo.getItems().addAll(PopType.values());
        fromTypeCombo.setPromptText("从职业");
        // 设置显示职业的中文名称
        fromTypeCombo.setCellFactory(lv -> new ListCell<PopType>() {
            @Override
            protected void updateItem(PopType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        fromTypeCombo.setButtonCell(new ListCell<PopType>() {
            @Override
            protected void updateItem(PopType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("从职业");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        ComboBox<PopType> toTypeCombo = new ComboBox<>();
        toTypeCombo.getItems().addAll(PopType.values());
        toTypeCombo.setPromptText("到职业");
        // 设置显示职业的中文名称
        toTypeCombo.setCellFactory(lv -> new ListCell<PopType>() {
            @Override
            protected void updateItem(PopType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        toTypeCombo.setButtonCell(new ListCell<PopType>() {
            @Override
            protected void updateItem(PopType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("到职业");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        Spinner<Integer> amountSpinner = new Spinner<>(0, 1000000, 1000, 100);
        amountSpinner.setEditable(true);

        Button transferButton = new Button("转移人口");
        transferButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        transferButton.setOnAction(e -> transferPopulation(
                fromTypeCombo.getValue(),
                toTypeCombo.getValue(),
                amountSpinner.getValue()
        ));

        transferPanel.getChildren().addAll(
                new Label("转移"), fromTypeCombo,
                new Label("到"), toTypeCombo,
                new Label("数量:"), amountSpinner, transferButton
        );

        panel.getChildren().addAll(title, transferPanel);
        return panel;
    }

    private VBox createResourcesTab() {
        VBox tabContent = new VBox(10);
        tabContent.setPadding(new Insets(10));

        // 创建资源生产/消耗表格
        TableView<ResourceStat> resourceTable = createResourceTable();

        // 资源趋势图表
        this.resourceChart = createResourceChart();

        tabContent.getChildren().addAll(resourceTable, resourceChart);
        return tabContent;
    }

    private TableView<ResourceStat> createResourceTable() {
        TableView<ResourceStat> table = new TableView<>();
        table.setId("resource-table");

        TableColumn<ResourceStat, String> nameColumn = new TableColumn<>("资源");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<ResourceStat, Number> productionColumn = new TableColumn<>("生产");
        productionColumn.setCellValueFactory(cellData -> cellData.getValue().productionProperty());

        TableColumn<ResourceStat, Number> consumptionColumn = new TableColumn<>("消耗");
        consumptionColumn.setCellValueFactory(cellData -> cellData.getValue().consumptionProperty());

        TableColumn<ResourceStat, Number> netColumn = new TableColumn<>("净产量");
        netColumn.setCellValueFactory(cellData -> cellData.getValue().netProperty());

        TableColumn<ResourceStat, Number> stockpileColumn = new TableColumn<>("库存");
        stockpileColumn.setCellValueFactory(cellData -> cellData.getValue().stockpileProperty());

        table.getColumns().addAll(nameColumn, productionColumn, consumptionColumn, netColumn, stockpileColumn);
        table.setPrefHeight(200);

        return table;
    }

    private LineChart<String, Number> createResourceChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("回合");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("数量");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("资源趋势");
        chart.setPrefHeight(300);

        return chart;
    }

    private void setupEventHandlers() {
        // 殖民地选择事件
        colonyListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedColony = newValue;
                    updateColonyDetails();
                }
        );
    }

    private void updateColonyDetails() {
        if (selectedColony == null) return;

        // 更新基本信息
        colonyNameLabel.setText("殖民地: " + selectedColony.getName());
        planetInfoLabel.setText("行星: " + selectedColony.getPlanet().getName() +
                " (" + selectedColony.getPlanet().getType().getDisplayName() + ")");

        populationLabel.setText("人口: " + String.format("%,d", selectedColony.getTotalPopulation()));
        happinessLabel.setText("幸福度: " + String.format("%.0f%%", selectedColony.getHappiness() * 100));
        stabilityLabel.setText("稳定度: " + selectedColony.getStability() + "%");
        developmentLabel.setText("发展度: " + String.format("%.1f%%", selectedColony.getDevelopment() * 100));
        
        // 更新人口增长进度条
        if (populationGrowthProgressBar != null && populationGrowthProgressLabel != null) {
            float currentPoints = selectedColony.getPopulationGrowthPoints();
            float requiredPoints = selectedColony.getPopulationGrowthPointsRequired();
            double progress = requiredPoints > 0 ? currentPoints / requiredPoints : 0;
            populationGrowthProgressBar.setProgress(Math.min(1.0, progress));
            populationGrowthProgressLabel.setText(String.format("人口增长进度: %.1f/%.1f", currentPoints, requiredPoints));
        }

        // 更新资源显示
        updateResourceDisplay();

        // 更新资源趋势图
        updateResourceChart();

        // 更新建筑列表
        updateBuildingList();

        // 更新人口图表
        updatePopulationChart();

        // 更新生产统计
        updateProductionStats();
    }

    private void updateResourceDisplay() {
        // 更新资源表格
        TableView<ResourceStat> resourceTable = (TableView<ResourceStat>) lookup("#resource-table");
        if (resourceTable == null) return;
        
        // 获取当前殖民地的资源数据
        ResourceStockpile stockpile = selectedColony.getResourceStockpile();
        Map<ResourceType, Float> production = selectedColony.getProductionStats();
        Map<ResourceType, Float> consumption = selectedColony.getConsumptionStats();
        Map<ResourceType, Float> net = selectedColony.getNetProduction();
        
        // 创建资源统计列表
        ObservableList<ResourceStat> resourceStats = FXCollections.observableArrayList();
        for (ResourceType type : ResourceType.values()) {
            float stockpileAmount = stockpile.getResource(type);
            float prod = production.getOrDefault(type, 0f);
            float cons = consumption.getOrDefault(type, 0f);
            float netValue = net.getOrDefault(type, 0f);
            
            // 只添加有数据的资源
            if (stockpileAmount != 0 || prod != 0 || cons != 0 || netValue != 0) {
                resourceStats.add(new ResourceStat(
                    type.getDisplayName(),
                    prod,
                    cons,
                    netValue,
                    stockpileAmount
                ));
            }
        }
        
        resourceTable.setItems(resourceStats);
    }

    private void updateResourcePanel() {
        VBox resourcesContent = (VBox) resourcePanel.lookup("#resources-content");
        if (resourcesContent == null || selectedColony == null) return;

        resourcesContent.getChildren().clear();

        ResourceStockpile stockpile = selectedColony.getResourceStockpile();
        Map<ResourceType, Float> netProduction = selectedColony.getNetProduction();

        for (ResourceType type : ResourceType.values()) {
            float amount = stockpile.getResource(type);
            float net = netProduction.getOrDefault(type, 0f);
            
            // 只显示有资源或有产量的资源类型
            if (amount > 0 || net != 0) {
                HBox resourceRow = new HBox(10);
                resourceRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label nameLabel = new Label(type.getDisplayName());
                nameLabel.setTextFill(Color.web(type.getColor()));
                nameLabel.setPrefWidth(100);

                ProgressBar stockpileBar = new ProgressBar();
                stockpileBar.setProgress(amount / stockpile.getCapacity(type)); 
                stockpileBar.setPrefWidth(200);

                // 显示资源数量和每回合变化
                Label amountLabel = new Label(String.format("%.0f (%+.0f/回合)", amount, net));
                amountLabel.setTextFill(Color.WHITE);

                resourceRow.getChildren().addAll(nameLabel, stockpileBar, amountLabel);
                resourcesContent.getChildren().add(resourceRow);
            }
        }
    }

    private void updateBuildingList() {
        if (buildingListView != null) {
            buildingListView.setItems(FXCollections.observableArrayList(selectedColony.getBuildings()));
        }
    }

    private void updatePopulationChart() {
        if (populationChart == null) return;

        populationChart.getData().clear();

        Map<PopType, Integer> population = selectedColony.getPopulationByType();
        for (Map.Entry<PopType, Integer> entry : population.entrySet()) {
            if (entry.getValue() > 0) {
                String displayName = entry.getKey().getDisplayName();
                
                PieChart.Data slice = new PieChart.Data(
                        displayName,
                        entry.getValue()
                );
                populationChart.getData().add(slice);
            }
        }
    }

    private void updateProductionStats() {
        VBox statsContent = (VBox) lookup("#production-stats-content");
        if (statsContent == null) return;

        statsContent.getChildren().clear();

        Map<ResourceType, Float> production = selectedColony.getProductionStats();
        Map<ResourceType, Float> consumption = selectedColony.getConsumptionStats();
        Map<ResourceType, Float> net = selectedColony.getNetProduction();

        for (ResourceType type : ResourceType.values()) {
            float prod = production.getOrDefault(type, 0f);
            float cons = consumption.getOrDefault(type, 0f);
            float netValue = net.getOrDefault(type, 0f);

            // 只显示生产和消耗不全为0的资源
            if (prod == 0 && cons == 0) {
                continue;
            }

            HBox statRow = new HBox(10);

            Label nameLabel = new Label(type.getDisplayName());
            nameLabel.setTextFill(Color.web(type.getColor()));
            nameLabel.setPrefWidth(100);

            Label productionLabel = new Label(String.format("生产: %.1f", prod));
            productionLabel.setTextFill(Color.GREEN);

            Label consumptionLabel = new Label(String.format("消耗: %.1f", cons));
            consumptionLabel.setTextFill(Color.RED);

            Label netLabel = new Label(String.format("净产量: %+.1f", netValue)); // 修改为显示正负号
            netLabel.setTextFill(netValue >= 0 ? Color.LIGHTGREEN : Color.SALMON);

            statRow.getChildren().addAll(nameLabel, productionLabel, consumptionLabel, netLabel);
            statsContent.getChildren().add(statRow);
        }
    }

    private void updateResourceChart() {
        if (resourceChart == null || selectedColony == null) return;
        
        // 获取当前回合的资源数据
        ResourceStockpile stockpile = selectedColony.getResourceStockpile();
        Map<ResourceType, Float> production = selectedColony.getProductionStats();
        Map<ResourceType, Float> consumption = selectedColony.getConsumptionStats();
        Map<ResourceType, Float> net = selectedColony.getNetProduction();
        
        // 为每种资源类型创建或更新系列
        for (ResourceType type : ResourceType.values()) {
            float stockpileAmount = stockpile.getResource(type);
            float prod = production.getOrDefault(type, 0f);
            float cons = consumption.getOrDefault(type, 0f);
            float netValue = net.getOrDefault(type, 0f);
            
            // 为库存创建系列
            String stockpileKey = type.getDisplayName() + " (库存)";
            XYChart.Series<String, Number> stockpileSeries = resourceSeriesMap.computeIfAbsent(stockpileKey, k -> {
                XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                newSeries.setName(k);
                resourceChart.getData().add(newSeries);
                return newSeries;
            });
            
            // 为生产创建系列
            String productionKey = type.getDisplayName() + " (生产)";
            XYChart.Series<String, Number> productionSeries = resourceSeriesMap.computeIfAbsent(productionKey, k -> {
                XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                newSeries.setName(k);
                resourceChart.getData().add(newSeries);
                return newSeries;
            });
            
            // 为消耗创建系列
            String consumptionKey = type.getDisplayName() + " (消耗)";
            XYChart.Series<String, Number> consumptionSeries = resourceSeriesMap.computeIfAbsent(consumptionKey, k -> {
                XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                newSeries.setName(k);
                resourceChart.getData().add(newSeries);
                return newSeries;
            });
            
            // 为净产量创建系列
            String netKey = type.getDisplayName() + " (净产量)";
            XYChart.Series<String, Number> netSeries = resourceSeriesMap.computeIfAbsent(netKey, k -> {
                XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                newSeries.setName(k);
                resourceChart.getData().add(newSeries);
                return newSeries;
            });
            
            // 添加新的数据点（仅当有数据时）
            if (stockpileAmount != 0) {
                stockpileSeries.getData().add(new XYChart.Data<>(String.valueOf(turnCounter), stockpileAmount));
                if (stockpileSeries.getData().size() > 10) {
                    stockpileSeries.getData().remove(0);
                }
            }
            
            if (prod != 0) {
                productionSeries.getData().add(new XYChart.Data<>(String.valueOf(turnCounter), prod));
                if (productionSeries.getData().size() > 10) {
                    productionSeries.getData().remove(0);
                }
            }
            
            if (cons != 0) {
                consumptionSeries.getData().add(new XYChart.Data<>(String.valueOf(turnCounter), cons));
                if (consumptionSeries.getData().size() > 10) {
                    consumptionSeries.getData().remove(0);
                }
            }
            
            if (netValue != 0) {
                netSeries.getData().add(new XYChart.Data<>(String.valueOf(turnCounter), netValue));
                if (netSeries.getData().size() > 10) {
                    netSeries.getData().remove(0);
                }
            }
        }
        
        // 增加回合计数
        turnCounter++;
    }
    // 操作方法
    private void showColonizeDialog() {
        // 显示殖民对话框
        System.out.println("显示殖民对话框");
        // 这里应该实现殖民逻辑
    }

    private void showBuildDialog() {
        if (selectedColony == null) return;

        // 创建建筑选择对话框
        Dialog<Building> dialog = new Dialog<>();
        dialog.setTitle("建造新建筑");
        dialog.setHeaderText("选择要建造的建筑");

        // 创建建筑列表
        ListView<BuildingType> buildingTypeList = new ListView<>();
        buildingTypeList.getItems().addAll(BuildingType.values());
        // 设置显示建筑类型的中文名称
        buildingTypeList.setCellFactory(lv -> new ListCell<BuildingType>() {
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

        dialog.getDialogPane().setContent(buildingTypeList);

        // 添加按钮
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                BuildingType selectedType = buildingTypeList.getSelectionModel().getSelectedItem();
                if (selectedType != null) {
                    // 创建对应建筑
                    Building newBuilding = new BasicBuilding(
                            selectedType.getDisplayName(),
                            selectedType,
                            3
                    );

                    // 使用新的详细建造方法
                    Colony.BuildResult result = selectedColony.buildDetailed(newBuilding);
                    
                    if (result.success) {
                        // 显示成功弹窗
                        showAlert("建造成功", result.message);
                        updateBuildingList();
                        updateColonyDetails();
                    } else {
                        // 显示失败弹窗
                        showAlert("建造失败", result.message);
                    }
                    
                    return newBuilding;
                }
            }
            return null;
        });

        dialog.showAndWait();
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
    
    private void upgradeSelectedBuilding() {
        if (selectedColony == null || buildingListView.getSelectionModel().isEmpty()) return;

        Building selectedBuilding = buildingListView.getSelectionModel().getSelectedItem();
        
        // 使用新的详细升级方法
        Colony.BuildResult result = selectedColony.upgradeBuildingDetailed(selectedBuilding);
        
        if (result.success) {
            // 显示成功弹窗
            showAlert("升级成功", result.message);
            updateBuildingList();
            updateColonyDetails();
        } else {
            // 显示失败弹窗
            showAlert("升级失败", result.message);
        }
    }

    private void demolishSelectedBuilding() {
        if (selectedColony == null || buildingListView.getSelectionModel().isEmpty()) return;

        Building selectedBuilding = buildingListView.getSelectionModel().getSelectedItem();
        // 显示确认对话框
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("确认拆解");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("确定要拆解 " + selectedBuilding.getName() + " 吗？");
        
        // 设置窗口图标
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/icon.png"));
            Stage stage = (Stage) confirmDialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("无法加载窗口图标: " + e.getMessage());
        }

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (selectedColony.demolishBuilding(selectedBuilding)) {
                    updateBuildingList();
                    updateColonyDetails();
                }
            }
        });
    }

    private void transferPopulation(PopType fromType, PopType toType, int amount) {
        if (selectedColony == null || fromType == null || toType == null) return;

        // 使用选择的人口类型进行转移
        selectedColony.reallocatePopulation(fromType, toType, amount);
        updatePopulationChart();
        updateColonyDetails();
    }

    private void setGrowthFocus(GrowthFocus focus) {
        if (selectedColony == null) return;

        selectedColony.setGrowthFocus(focus);
        updateColonyDetails();
    }

    // 自定义列表单元格
    private class ColonyListCell extends ListCell<Colony> {
        @Override
        protected void updateItem(Colony colony, boolean empty) {
            super.updateItem(colony, empty);

            if (empty || colony == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox container = new VBox(2);

                Label nameLabel = new Label(colony.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

                Label planetLabel = new Label(colony.getPlanet().getName());
                planetLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11;");

                Label populationLabel = new Label(String.format("人口: %,d", colony.getTotalPopulation()));
                populationLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11;");

                container.getChildren().addAll(nameLabel, planetLabel, populationLabel);
                setGraphic(container);
            }
        }
    }

    private class BuildingListCell extends ListCell<Building> {
        @Override
        protected void updateItem(Building building, boolean empty) {
            super.updateItem(building, empty);

            if (empty || building == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox container = new HBox(10);

                Label iconLabel = new Label(building.getType().getIcon());
                iconLabel.setStyle("-fx-font-size: 20;");

                VBox infoBox = new VBox(2);

                Label nameLabel = new Label(building.getName() + " (等级 " + building.getLevel() + ")");
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

                Label typeLabel = new Label(building.getType().getDescription());
                typeLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11;");

                Label statusLabel = new Label(building.isActive() ? "✓ 运行中" : "✗ 停止");
                statusLabel.setTextFill(building.isActive() ? Color.GREEN : Color.RED);
                statusLabel.setStyle("-fx-font-size: 11;");

                infoBox.getChildren().addAll(nameLabel, typeLabel, statusLabel);
                container.getChildren().addAll(iconLabel, infoBox);
                setGraphic(container);
            }
        }
    }

    private class GovernorListCell extends ListCell<ColonyGovernor> {
        @Override
        protected void updateItem(ColonyGovernor governor, boolean empty) {
            super.updateItem(governor, empty);

            if (empty || governor == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox container = new VBox(2);

                Label nameLabel = new Label(governor.getName() + " (等级 " + governor.getLevel() + ")");
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

                Label traitLabel = new Label(
                        governor.getPrimaryTrait().getDisplayName() +
                                " / " + governor.getSecondaryTrait().getDisplayName()
                );
                traitLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11;");

                container.getChildren().addAll(nameLabel, traitLabel);
                setGraphic(container);
            }
        }
    }
}