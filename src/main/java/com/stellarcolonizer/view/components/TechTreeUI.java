
package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.technology.*;
import com.stellarcolonizer.model.technology.enums.TechCategory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;

public class TechTreeUI extends BorderPane {

    private final TechTree techTree;

    // UI组件
    private ScrollPane techTreePane;
    private Pane techTreeCanvas;

    // 控制面板
    private Label researchPointsLabel;
    private Label currentResearchLabel;
    private ProgressBar researchProgressBar;
    private ListView<ResearchProject> researchQueueList;

    // 科技卡片映射
    private final Map<Technology, TechCard> techCardMap;
    private final Map<TechCategory, VBox> categoryColumns;

    // 当前选择
    private Technology selectedTechnology;

    public TechTreeUI(TechTree techTree) {
        this.techTree = techTree;
        this.techCardMap = new HashMap<>();
        this.categoryColumns = new EnumMap<>(TechCategory.class);

        initializeUI();
        setupEventHandlers();
        buildTechTree();
    }

    private void initializeUI() {
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #1e1e1e;");

        // 顶部：研究状态
        HBox topPanel = createTopPanel();
        setTop(topPanel);

        // 左侧：研究队列
        VBox leftPanel = createLeftPanel();

        // 中心：科技树
        VBox centerPanel = createCenterPanel();

        // 右侧：科技详情
        VBox rightPanel = createRightPanel();

        // 使用SplitPane布局
        SplitPane mainSplit = new SplitPane();
        mainSplit.getItems().addAll(leftPanel, centerPanel, rightPanel);
        mainSplit.setDividerPositions(0.2, 0.8);

        setCenter(mainSplit);
    }

    private HBox createTopPanel() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        // 研究点数
        VBox researchBox = new VBox(2);
        Label researchTitle = new Label("研究点数/回合");
        researchTitle.setTextFill(Color.LIGHTGRAY);
        researchTitle.setFont(Font.font(12));

        researchPointsLabel = new Label("0");
        researchPointsLabel.setTextFill(Color.CYAN);
        researchPointsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        researchBox.getChildren().addAll(researchTitle, researchPointsLabel);

        // 当前研究
        VBox currentResearchBox = new VBox(2);
        Label currentResearchTitle = new Label("当前研究");
        currentResearchTitle.setTextFill(Color.LIGHTGRAY);
        currentResearchTitle.setFont(Font.font(12));

        currentResearchLabel = new Label("无研究项目");
        currentResearchLabel.setTextFill(Color.WHITE);
        currentResearchLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        currentResearchBox.getChildren().addAll(currentResearchTitle, currentResearchLabel);

        // 研究进度
        VBox progressBox = new VBox(2);
        Label progressTitle = new Label("研究进度");
        progressTitle.setTextFill(Color.LIGHTGRAY);
        progressTitle.setFont(Font.font(12));

        researchProgressBar = new ProgressBar();
        researchProgressBar.setPrefWidth(300);
        researchProgressBar.setStyle("-fx-accent: #4CAF50;");

        progressBox.getChildren().addAll(progressTitle, researchProgressBar);

        // 研究速度加成
        VBox bonusBox = new VBox(2);
        Label bonusTitle = new Label("研究速度");
        bonusTitle.setTextFill(Color.LIGHTGRAY);
        bonusTitle.setFont(Font.font(12));

        Label bonusLabel = new Label(String.format("+%.0f%%",
                (techTree.getResearchSpeedBonus() - 1) * 100));
        bonusLabel.setTextFill(Color.GREEN);
        bonusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        bonusBox.getChildren().addAll(bonusTitle, bonusLabel);

        panel.getChildren().addAll(researchBox, currentResearchBox, progressBox, bonusBox);
        return panel;
    }

    private VBox createLeftPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(250);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        Label title = new Label("研究队列");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        // 研究队列列表
        researchQueueList = new ListView<>(techTree.getResearchQueue());
        researchQueueList.setPrefHeight(400);
        researchQueueList.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");
        researchQueueList.setCellFactory(lv -> new ResearchProjectCell());

        // 队列操作按钮
        HBox queueButtons = new HBox(5);
        Button addToQueueButton = new Button("添加");
        Button removeFromQueueButton = new Button("移除");
        Button moveUpButton = new Button("↑");
        Button moveDownButton = new Button("↓");

        addToQueueButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        removeFromQueueButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        moveUpButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        moveDownButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        addToQueueButton.setOnAction(e -> addSelectedToQueue());
        removeFromQueueButton.setOnAction(e -> removeSelectedFromQueue());
        moveUpButton.setOnAction(e -> moveSelectedUp());
        moveDownButton.setOnAction(e -> moveSelectedDown());

        queueButtons.getChildren().addAll(addToQueueButton, removeFromQueueButton,
                moveUpButton, moveDownButton);

        // 可用科技列表
        Label availableTitle = new Label("可研究科技");
        availableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        availableTitle.setTextFill(Color.WHITE);

        ListView<Technology> availableList = new ListView<>(
                FXCollections.observableArrayList(techTree.getAvailableTechnologies())
        );
        availableList.setPrefHeight(200);
        availableList.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e;");
        availableList.setCellFactory(lv -> new TechnologyCell());

        availableList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTech, newTech) -> {
                    selectedTechnology = newTech;
                    updateTechnologyDetails();
                }
        );

        panel.getChildren().addAll(title, researchQueueList, queueButtons,
                availableTitle, availableList);
        return panel;
    }

    private VBox createCenterPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));

        Label title = new Label("科技树");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);

        // 科技树画布
        techTreeCanvas = new Pane();
        techTreeCanvas.setStyle("-fx-background-color: #1a1a1a;");

        techTreePane = new ScrollPane(techTreeCanvas);
        techTreePane.setFitToWidth(true);
        techTreePane.setFitToHeight(true);
        techTreePane.setStyle("-fx-background-color: #1a1a1a;");
        techTreePane.setPrefHeight(600);

        // 类别筛选
        HBox filterPanel = createFilterPanel();

        panel.getChildren().addAll(title, filterPanel, techTreePane);
        VBox.setVgrow(techTreePane, Priority.ALWAYS);

        return panel;
    }

    private HBox createFilterPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(5));
        panel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        // 全选按钮
        Button selectAllButton = new Button("全选");
        selectAllButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        selectAllButton.setOnAction(e -> showAllCategories());

        // 类别筛选按钮
        for (TechCategory category : TechCategory.values()) {
            ToggleButton categoryButton = new ToggleButton(category.getDisplayName());
            categoryButton.setUserData(category);
            categoryButton.setSelected(true);
            categoryButton.setStyle("-fx-background-color: " + toHex(category.getColor()) +
                    "; -fx-text-fill: white;");
            categoryButton.setOnAction(e -> toggleCategory(category));

            panel.getChildren().add(categoryButton);
        }

        panel.getChildren().add(0, selectAllButton);
        return panel;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(350);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 5;");

        Label title = new Label("科技详情");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        // 科技详情面板
        VBox detailsPanel = createTechnologyDetailsPanel();

        panel.getChildren().addAll(title, detailsPanel);
        return panel;
    }

    private VBox createTechnologyDetailsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #333333; -fx-background-radius: 5;");

        // 这里的内容会在选择科技时动态更新
        panel.setId("tech-details");

        return panel;
    }

    private void buildTechTree() {
        techTreeCanvas.getChildren().clear();
        techCardMap.clear();
        categoryColumns.clear();

        // 按类别分组科技
        Map<TechCategory, List<Technology>> techsByCategory = new EnumMap<>(TechCategory.class);
        for (TechCategory category : TechCategory.values()) {
            techsByCategory.put(category, new ArrayList<>());
        }

        for (Technology tech : techTree.getTechnologies()) {
            techsByCategory.get(tech.getCategory()).add(tech);
        }

        // 创建类别列
        double x = 50;
        double columnWidth = 300;

        for (TechCategory category : TechCategory.values()) {
            List<Technology> categoryTechs = techsByCategory.get(category);
            if (categoryTechs.isEmpty()) continue;

            // 创建类别标题
            Label categoryTitle = new Label(category.getDisplayName() + " " + category.getIcon());
            categoryTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            categoryTitle.setTextFill(category.getColor());
            categoryTitle.setLayoutX(x + 20);
            categoryTitle.setLayoutY(20);

            // 创建类别列容器
            VBox column = new VBox(30);
            column.setLayoutX(x);
            column.setLayoutY(60);
            column.setPrefWidth(columnWidth);

            // 按层级排序科技
            Map<Integer, List<Technology>> techsByTier = groupByTier(categoryTechs);

            double y = 0;
            for (int tier = 1; tier <= 5; tier++) {
                List<Technology> tierTechs = techsByTier.getOrDefault(tier, new ArrayList<>());

                // 创建层级容器
                HBox tierContainer = new HBox(20);
                tierContainer.setPrefWidth(columnWidth);

                for (Technology tech : tierTechs) {
                    TechCard techCard = createTechCard(tech, x, y);
                    techCardMap.put(tech, techCard);
                    tierContainer.getChildren().add(techCard);
                }

                column.getChildren().add(tierContainer);
                y += 120; // 每个层级的高度
            }

            techTreeCanvas.getChildren().addAll(categoryTitle, column);
            categoryColumns.put(category, column);

            x += columnWidth + 50;
        }

        // 绘制连接线
        drawConnections();

        // 更新科技树画布大小
        updateCanvasSize(x);
    }

    private Map<Integer, List<Technology>> groupByTier(List<Technology> techs) {
        Map<Integer, List<Technology>> tiers = new HashMap<>();

        for (Technology tech : techs) {
            int tier = calculateTier(tech);
            tiers.computeIfAbsent(tier, k -> new ArrayList<>()).add(tech);
        }

        return tiers;
    }

    private int calculateTier(Technology tech) {
        // 根据前置科技数量确定层级
        if (tech.getPrerequisites().isEmpty()) return 1;

        int maxPrereqTier = 0;
        for (String prereqId : tech.getPrerequisites()) {
            Technology prereq = techTree.getTechnology(prereqId);
            if (prereq != null) {
                int prereqTier = calculateTier(prereq);
                maxPrereqTier = Math.max(maxPrereqTier, prereqTier);
            }
        }

        return maxPrereqTier + 1;
    }

    private TechCard createTechCard(Technology tech, double x, double y) {
        return new TechCard(tech);
    }

    private void drawConnections() {
        for (Technology tech : techTree.getTechnologies()) {
            TechCard techCard = techCardMap.get(tech);
            if (techCard == null) continue;

            for (String prereqId : tech.getPrerequisites()) {
                Technology prereq = techTree.getTechnology(prereqId);
                if (prereq == null) continue;

                TechCard prereqCard = techCardMap.get(prereq);
                if (prereqCard == null) continue;

                // 绘制连接线
                Line connection = new Line();
                connection.setStartX(prereqCard.getLayoutX() + prereqCard.getWidth() / 2);
                connection.setStartY(prereqCard.getLayoutY() + prereqCard.getHeight() / 2);
                connection.setEndX(techCard.getLayoutX() + techCard.getWidth() / 2);
                connection.setEndY(techCard.getLayoutY() + techCard.getHeight() / 2);

                // 设置线条样式
                if (tech.isResearched() && prereq.isResearched()) {
                    connection.setStroke(Color.LIME);
                    connection.setStrokeWidth(3);
                } else if (techTree.canResearch(tech) && prereq.isResearched()) {
                    connection.setStroke(Color.YELLOW);
                    connection.setStrokeWidth(2);
                } else {
                    connection.setStroke(Color.GRAY);
                    connection.setStrokeWidth(1);
                }

                // 添加到画布底部（在卡片下面）
                techTreeCanvas.getChildren().add(0, connection);
            }
        }
    }

    private void updateCanvasSize(double width) {
        double height = 0;

        for (VBox column : categoryColumns.values()) {
            height = Math.max(height, column.getLayoutY() + column.getHeight());
        }

        techTreeCanvas.setPrefSize(width, height + 100);
    }

    private void setupEventHandlers() {
        // 研究队列选择
        researchQueueList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldProject, newProject) -> {
                    if (newProject != null) {
                        selectedTechnology = newProject.getTechnology();
                        updateTechnologyDetails();
                    }
                }
        );

        // 绑定研究点数
        researchPointsLabel.textProperty().bind(
                techTree.currentResearchPointsProperty().asString()
        );

        // 绑定当前研究
        currentResearchLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> techTree.getResearchStatus(),
                        techTree.researchQueueProperty()
                )
        );

        // 绑定研究进度
        researchProgressBar.progressProperty().bind(
                Bindings.createDoubleBinding(
                        () -> techTree.getResearchProgressPercentage() / 100.0,
                        techTree.researchQueueProperty()
                )
        );
    }

    private void updateTechnologyDetails() {
        VBox detailsPanel = (VBox) lookup("#tech-details");
        if (detailsPanel == null || selectedTechnology == null) return;

        detailsPanel.getChildren().clear();

        // 科技名称
        Label nameLabel = new Label(selectedTechnology.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameLabel.setTextFill(selectedTechnology.getColor());
        detailsPanel.getChildren().add(nameLabel);

        // 科技描述
        Label descLabel = new Label(selectedTechnology.getDescription());
        descLabel.setWrapText(true);
        descLabel.setTextFill(Color.LIGHTGRAY);
        descLabel.setPrefWidth(300);
        detailsPanel.getChildren().add(descLabel);

        // 类别和成本
        HBox infoRow = new HBox(20);
        Label categoryLabel = new Label("类别: " + selectedTechnology.getCategory().getDisplayName());
        categoryLabel.setTextFill(selectedTechnology.getColor());

        Label costLabel = new Label("研究成本: " + selectedTechnology.getBaseCost());
        costLabel.setTextFill(Color.YELLOW);

        infoRow.getChildren().addAll(categoryLabel, costLabel);
        detailsPanel.getChildren().add(infoRow);

        // 研究状态
        Label statusLabel = new Label();
        if (selectedTechnology.isResearched()) {
            statusLabel.setText("状态: 已研究 ✓");
            statusLabel.setTextFill(Color.GREEN);
        } else if (techTree.canResearch(selectedTechnology)) {
            statusLabel.setText("状态: 可研究");
            statusLabel.setTextFill(Color.YELLOW);
        } else {
            statusLabel.setText("状态: 不可研究");
            statusLabel.setTextFill(Color.RED);
        }
        detailsPanel.getChildren().add(statusLabel);

        // 前置科技
        if (!selectedTechnology.getPrerequisites().isEmpty()) {
            Label prereqTitle = new Label("前置科技:");
            prereqTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            prereqTitle.setTextFill(Color.LIGHTGRAY);
            detailsPanel.getChildren().add(prereqTitle);

            for (String prereqId : selectedTechnology.getPrerequisites()) {
                Technology prereq = techTree.getTechnology(prereqId);
                if (prereq != null) {
                    Label prereqLabel = new Label("• " + prereq.getName());
                    prereqLabel.setTextFill(prereq.isResearched() ? Color.LIGHTGREEN : Color.SALMON);
                    detailsPanel.getChildren().add(prereqLabel);
                }
            }
        }

        // 解锁内容
        if (!selectedTechnology.getUnlocks().isEmpty()) {
            Label unlockTitle = new Label("解锁内容:");
            unlockTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            unlockTitle.setTextFill(Color.LIGHTGRAY);
            detailsPanel.getChildren().add(unlockTitle);

            for (Unlockable unlock : selectedTechnology.getUnlocks()) {
                Label unlockLabel = new Label("• " + unlock.getName());
                unlockLabel.setTextFill(Color.CYAN);
                detailsPanel.getChildren().add(unlockLabel);
            }
        }

        // 研究按钮
        if (!selectedTechnology.isResearched() && techTree.canResearch(selectedTechnology)) {
            Button researchButton = new Button("开始研究");
            researchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            researchButton.setOnAction(e -> startResearch(selectedTechnology));
            detailsPanel.getChildren().add(researchButton);
        }
    }

    private void startResearch(Technology technology) {
        ResearchProject project = techTree.startResearch(technology);
        if (project != null) {
            researchQueueList.setItems(techTree.getResearchQueue());
            updateTechnologyDetails();
        }
    }

    private void addSelectedToQueue() {
        if (selectedTechnology != null && !selectedTechnology.isResearched()) {
            techTree.addToQueue(selectedTechnology);
            researchQueueList.setItems(techTree.getResearchQueue());
        }
    }

    private void removeSelectedFromQueue() {
        ResearchProject selected = researchQueueList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            techTree.removeFromQueue(selected);
            researchQueueList.setItems(techTree.getResearchQueue());
        }
    }

    private void moveSelectedUp() {
        ResearchProject selected = researchQueueList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            techTree.moveUpInQueue(selected);
            researchQueueList.setItems(techTree.getResearchQueue());
            researchQueueList.getSelectionModel().select(selected);
        }
    }

    private void moveSelectedDown() {
        ResearchProject selected = researchQueueList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            techTree.moveDownInQueue(selected);
            researchQueueList.setItems(techTree.getResearchQueue());
            researchQueueList.getSelectionModel().select(selected);
        }
    }

    private void showAllCategories() {
        for (VBox column : categoryColumns.values()) {
            column.setVisible(true);
            column.setManaged(true);
        }

        // 更新画布
        buildTechTree();
    }

    private void toggleCategory(TechCategory category) {
        VBox column = categoryColumns.get(category);
        if (column != null) {
            column.setVisible(!column.isVisible());
            column.setManaged(!column.isManaged());
        }
    }

    // 自定义单元格
    private class TechnologyCell extends ListCell<Technology> {
        @Override
        protected void updateItem(Technology tech, boolean empty) {
            super.updateItem(tech, empty);

            if (empty || tech == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox container = new HBox(10);

                Label iconLabel = new Label(tech.getIcon());
                iconLabel.setStyle("-fx-font-size: 16;");

                VBox infoBox = new VBox(2);

                Label nameLabel = new Label(tech.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

                Label costLabel = new Label("成本: " + tech.getBaseCost());
                costLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11;");

                infoBox.getChildren().addAll(nameLabel, costLabel);
                container.getChildren().addAll(iconLabel, infoBox);
                setGraphic(container);
            }
        }
    }

    private class ResearchProjectCell extends ListCell<ResearchProject> {
        @Override
        protected void updateItem(ResearchProject project, boolean empty) {
            super.updateItem(project, empty);

            if (empty || project == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox container = new HBox(10);

                Label iconLabel = new Label(project.getTechnology().getIcon());
                iconLabel.setStyle("-fx-font-size: 16;");

                VBox infoBox = new VBox(2);

                Label nameLabel = new Label(project.getTechnology().getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

                ProgressBar progressBar = new ProgressBar(
                        project.getProgressPercentage() / 100.0
                );
                progressBar.setPrefWidth(100);
                progressBar.setStyle("-fx-accent: #4CAF50;");

                Label progressLabel = new Label(
                        String.format("%.0f/%.0f", project.getProgress(), project.getTotalCost())
                );
                progressLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11;");

                HBox progressBox = new HBox(10, progressBar, progressLabel);

                infoBox.getChildren().addAll(nameLabel, progressBox);
                container.getChildren().addAll(iconLabel, infoBox);
                setGraphic(container);
            }
        }
    }

    // 科技卡片类
    private class TechCard extends StackPane {
        private final Technology technology;

        public TechCard(Technology technology) {
            this.technology = technology;

            setPrefSize(120, 80);
            setStyle(createCardStyle());

            // 卡片内容
            VBox content = new VBox(5);
            content.setPadding(new Insets(10));
            content.setAlignment(javafx.geometry.Pos.CENTER);

            // 图标和名称
            Label iconLabel = new Label(technology.getIcon());
            iconLabel.setStyle("-fx-font-size: 20;");

            Label nameLabel = new Label(technology.getName());
            nameLabel.setFont(Font.font(10));
            nameLabel.setTextFill(Color.WHITE);
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(100);

            // 研究状态
            Label statusLabel = new Label();
            statusLabel.setFont(Font.font(8));

            if (technology.isResearched()) {
                statusLabel.setText("✓");
                statusLabel.setTextFill(Color.LIME);
            } else if (techTree.canResearch(technology)) {
                statusLabel.setText("●");
                statusLabel.setTextFill(Color.YELLOW);
            } else {
                statusLabel.setText("✗");
                statusLabel.setTextFill(Color.RED);
            }

            content.getChildren().addAll(iconLabel, nameLabel, statusLabel);
            getChildren().add(content);

            // 点击事件
            setOnMouseClicked(e -> {
                selectedTechnology = technology;
                updateTechnologyDetails();
            });

            // 悬停效果
            setOnMouseEntered(e -> {
                setStyle(createCardStyle() + "-fx-effect: dropshadow(gaussian, " +
                        TechTreeUI.this.toHex(technology.getColor().brighter()) + ", 10, 0.5);");
            });

            setOnMouseExited(e -> {
                setStyle(createCardStyle());
            });
        }

        private String createCardStyle() {
            StringBuilder style = new StringBuilder();
            style.append("-fx-background-color: ").append(TechTreeUI.this.toHex(technology.getColor().darker())).append("; ");
            style.append("-fx-background-radius: 10; ");
            style.append("-fx-border-color: ").append(TechTreeUI.this.toHex(technology.getColor())).append("; ");
            style.append("-fx-border-width: 2; ");
            style.append("-fx-border-radius: 10; ");

            if (technology.isResearched()) {
                style.append("-fx-effect: dropshadow(gaussian, ").append(TechTreeUI.this.toHex(technology.getColor()))
                        .append(", 10, 0.7);");
            } else if (techTree.canResearch(technology)) {
                style.append("-fx-effect: dropshadow(gaussian, #FFD700, 5, 0.5);");
            }

            return style.toString();
        }
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}