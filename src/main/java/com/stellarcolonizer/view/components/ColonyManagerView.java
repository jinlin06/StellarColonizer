// ColonyManagerView.java - 殖民地管理器视图
package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.colony.*;
import com.stellarcolonizer.model.colony.enums.BuildingType;
import com.stellarcolonizer.model.colony.enums.GrowthFocus;
import com.stellarcolonizer.model.colony.enums.PopType;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.view.models.ResourceStat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

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

    // 资源显示
    private VBox resourcePanel;

    // 建筑列表
    private ListView<Building> buildingListView;

    // 人口分布图表
    private PieChart populationChart;

    public ColonyManagerView(Faction playerFaction) {
        this.playerFaction = playerFaction;
        this.colonies = FXCollections.observableArrayList();
        this.colonies.addAll(playerFaction.getColonies());

        initializeUI();
        setupEventHandlers();
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
        Tab governorTab = new Tab("管理者", createGovernorTab());

        // 设置选项卡不可关闭
        overviewTab.setClosable(false);
        buildingsTab.setClosable(false);
        populationTab.setClosable(false);
        resourcesTab.setClosable(false);
        governorTab.setClosable(false);

        colonyDetailPane.getTabs().addAll(overviewTab, buildingsTab, populationTab, resourcesTab, governorTab);

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

        statusInfo.getChildren().addAll(populationLabel, happinessLabel, stabilityLabel, developmentLabel);

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

        // 资源库存
        resourcePanel = createResourcePanel();

        // 快速操作按钮
        HBox quickActions = createQuickActionButtons();

        tabContent.getChildren().addAll(productionStats, resourcePanel, quickActions);
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

        Button focusQualityButton = new Button("提高生活质量");
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

        ComboBox<PopType> toTypeCombo = new ComboBox<>();
        toTypeCombo.getItems().addAll(PopType.values());
        toTypeCombo.setPromptText("到职业");

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
        LineChart<String, Number> resourceChart = createResourceChart();

        tabContent.getChildren().addAll(resourceTable, resourceChart);
        return tabContent;
    }

    private TableView<ResourceStat> createResourceTable() {
        TableView<ResourceStat> table = new TableView<>();

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

    private VBox createGovernorTab() {
        VBox tabContent = new VBox(10);
        tabContent.setPadding(new Insets(10));

        // 管理者信息面板
        VBox governorInfo = createGovernorInfoPanel();

        // 管理者列表（可选）
        ListView<ColonyGovernor> governorListView = createGovernorListView();

        // 分配按钮
        Button assignButton = new Button("分配管理者");
        assignButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        assignButton.setOnAction(e -> assignGovernor());

        tabContent.getChildren().addAll(governorInfo, governorListView, assignButton);
        return tabContent;
    }

    private VBox createGovernorInfoPanel() {
        VBox panel = new VBox(5);
        panel.setStyle("-fx-background-color: #333333; -fx-padding: 10; -fx-background-radius: 5;");

        // 这里会显示当前管理者的信息
        Label title = new Label("当前管理者");
        title.setStyle("-fx-font-size: 16; -fx-text-fill: white; -fx-font-weight: bold;");

        panel.getChildren().add(title);
        return panel;
    }

    private ListView<ColonyGovernor> createGovernorListView() {
        // 这里应该从游戏引擎获取管理者列表
        ObservableList<ColonyGovernor> governors = FXCollections.observableArrayList();

        ListView<ColonyGovernor> listView = new ListView<>(governors);
        listView.setPrefHeight(200);
        listView.setCellFactory(lv -> new GovernorListCell());

        return listView;
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

        // 更新资源显示
        updateResourceDisplay();

        // 更新建筑列表
        updateBuildingList();

        // 更新人口图表
        updatePopulationChart();

        // 更新生产统计
        updateProductionStats();
    }

    private void updateResourceDisplay() {
        VBox resourcesContent = (VBox) resourcePanel.lookup("#resources-content");
        if (resourcesContent == null) return;

        resourcesContent.getChildren().clear();

        // 显示每种资源
        for (ResourceType type : ResourceType.values()) {
            float amount = selectedColony.getResourceStockpile().getResource(type);

            HBox resourceRow = new HBox(10);

            Label nameLabel = new Label(type.getDisplayName());
            nameLabel.setTextFill(Color.web(type.getColor()));
            nameLabel.setPrefWidth(100);

            ProgressBar stockpileBar = new ProgressBar();
            stockpileBar.setProgress(amount / 1000.0); // 假设最大1000
            stockpileBar.setPrefWidth(200);

            Label amountLabel = new Label(String.format("%.1f", amount));
            amountLabel.setTextFill(Color.WHITE);

            resourceRow.getChildren().addAll(nameLabel, stockpileBar, amountLabel);
            resourcesContent.getChildren().add(resourceRow);
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
                PieChart.Data slice = new PieChart.Data(
                        entry.getKey().getDisplayName(),
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

            HBox statRow = new HBox(10);

            Label nameLabel = new Label(type.getDisplayName());
            nameLabel.setTextFill(Color.web(type.getColor()));
            nameLabel.setPrefWidth(100);

            Label productionLabel = new Label(String.format("生产: %.1f", prod));
            productionLabel.setTextFill(Color.GREEN);

            Label consumptionLabel = new Label(String.format("消耗: %.1f", cons));
            consumptionLabel.setTextFill(Color.RED);

            Label netLabel = new Label(String.format("净产量: %.1f", netValue));
            netLabel.setTextFill(netValue >= 0 ? Color.LIGHTGREEN : Color.SALMON);

            statRow.getChildren().addAll(nameLabel, productionLabel, consumptionLabel, netLabel);
            statsContent.getChildren().add(statRow);
        }
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

                    if (selectedColony.build(newBuilding)) {
                        updateBuildingList();
                        updateColonyDetails();
                    }
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void upgradeSelectedBuilding() {
        if (selectedColony == null || buildingListView.getSelectionModel().isEmpty()) return;

        Building selectedBuilding = buildingListView.getSelectionModel().getSelectedItem();
        if (selectedColony.upgradeBuilding(selectedBuilding)) {
            updateBuildingList();
            updateColonyDetails();
        }
    }

    private void demolishSelectedBuilding() {
        if (selectedColony == null || buildingListView.getSelectionModel().isEmpty()) return;

        Building selectedBuilding = buildingListView.getSelectionModel().getSelectedItem();
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("确认拆除");
        confirmDialog.setHeaderText("确定要拆除 " + selectedBuilding.getName() + " 吗？");
        confirmDialog.setContentText("将返还50%的建筑资源。");

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

        selectedColony.reallocatePopulation(fromType, toType, amount);
        updatePopulationChart();
        updateColonyDetails();
    }

    private void setGrowthFocus(GrowthFocus focus) {
        if (selectedColony == null) return;

        selectedColony.setGrowthFocus(focus);
        updateColonyDetails();
    }

    private void assignGovernor() {
        // 实现分配管理者的逻辑
        System.out.println("分配管理者");
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
