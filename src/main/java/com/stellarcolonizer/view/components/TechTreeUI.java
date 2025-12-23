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
import java.util.stream.Collectors;

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
        Label researchTitle = new Label("研发点数/回合");
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
        panel.setStyle("-fx-background-color: #1a1a1a;");

        Label title = new Label("科技树");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);
        title.setEffect(new javafx.scene.effect.DropShadow(5, Color.BLACK));

        // 科技树画布
        techTreeCanvas = new Pane();
        techTreeCanvas.setStyle("-fx-background-color: #121212;");

        techTreePane = new ScrollPane(techTreeCanvas);
        techTreePane.setFitToWidth(false);
        techTreePane.setFitToHeight(false);
        techTreePane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        techTreePane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        techTreePane.setStyle("-fx-background-color: #1a1a1a;");
        techTreePane.setPrefHeight(400); // 调小高度
        techTreePane.setPrefWidth(800);  // 调小宽度
        
        // 美化滚动条
        techTreePane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        techTreePane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        techTreePane.getStyleClass().add("tech-tree-scroll-pane");

        // 创建科技树布局
        HBox treeLayout = new HBox(30); // 减小间距
        treeLayout.setPadding(new Insets(10)); // 减小内边距
        treeLayout.setStyle("-fx-background-color: #1e1e1e;");

        // 物理学分支
        VBox physicsBranch = createBranch("物理学", TechCategory.PHYSICS);
        // 化学分支
        VBox chemistryBranch = createBranch("化学", TechCategory.CHEMISTRY);
        // 生物学分支
        VBox biologyBranch = createBranch("生物学", TechCategory.BIOLOGY);

        treeLayout.getChildren().addAll(physicsBranch, chemistryBranch, biologyBranch);

        // 添加到面板
        panel.getChildren().addAll(title, techTreePane);
        VBox.setVgrow(techTreePane, Priority.ALWAYS);

        return panel;
    }

    private VBox createBranch(String branchName, TechCategory category) {
        VBox branch = new VBox(15); // 减小间距
        branch.setStyle("-fx-background-color: rgba(30, 30, 30, 0.7); -fx-background-radius: 8; -fx-padding: 10;"); // 减小内边距和圆角

        // 分支标题
        Label titleLabel = new Label(branchName);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16)); // 减小标题字体
        titleLabel.setTextFill(category.getColor());
        titleLabel.setEffect(new javafx.scene.effect.DropShadow(5, Color.BLACK));
        titleLabel.setAlignment(javafx.geometry.Pos.CENTER);

        // 科技列表
        VBox techList = new VBox(10); // 减小间距
        techList.setStyle("-fx-background-color: transparent;");

        // 按层级分组科技
        Map<Integer, List<Technology>> techsByTier = groupByTier(getTechnologiesByCategory(category));

        for (int tier = 1; tier <= 8; tier++) {
            List<Technology> tierTechs = techsByTier.getOrDefault(tier, new ArrayList<>());
            if (!tierTechs.isEmpty()) {
                // 创建层级标签
                Label tierLabel = new Label("等级 " + tier);
                tierLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10)); // 减小字体
                tierLabel.setTextFill(Color.LIGHTGRAY);
                tierLabel.setPadding(new Insets(0, 0, 3, 0)); // 减小内边距

                // 创建层级容器
                FlowPane tierContainer = new FlowPane(javafx.geometry.Orientation.HORIZONTAL, 10, 10); // 减小间距
                tierContainer.setPrefWidth(250); // 减小宽度
                tierContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                for (Technology tech : tierTechs) {
                    TechCard techCard = new TechCard(tech);
                    techCardMap.put(tech, techCard);
                    tierContainer.getChildren().add(techCard);
                }

                VBox tierBox = new VBox(3); // 减小间距
                tierBox.getChildren().addAll(tierLabel, tierContainer);
                techList.getChildren().add(tierBox);
            }
        }

        branch.getChildren().addAll(titleLabel, techList);
        return branch;
    }

    private List<Technology> getTechnologiesByCategory(TechCategory category) {
        return techTree.getTechnologies().stream()
                .filter(tech -> tech.getCategory() == category)
                .collect(Collectors.toList());
    }

    private HBox createFilterPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(8));
        panel.setStyle("-fx-background-color: #2d2d2d; -fx-background-radius: 8;");

        // 全选按钮
        Button selectAllButton = new Button("全选");
        selectAllButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; "
                               + "-fx-background-radius: 6; "
                               + "-fx-padding: 5 15 5 15; "
                               + "-fx-font-weight: bold;");
        selectAllButton.setOnAction(e -> showAllCategories());

        // 类别筛选按钮
        for (TechCategory category : TechCategory.values()) {
            ToggleButton categoryButton = new ToggleButton(category.getDisplayName());
            categoryButton.setUserData(category);
            categoryButton.setSelected(true);
            categoryButton.setStyle(
                "-fx-background-color: " + toHex(category.getColor()) + "; " +
                "-fx-text-fill: black; " +
                "-fx-background-radius: 6; " +
                "-fx-padding: 5 10 5 10; " +
                "-fx-font-weight: bold;" +
                "-fx-max-width: 80;" +
                "-fx-text-overrun: ellipsis;"
            );
             
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

        // 创建背景网格
        createGridBackground();

        // 创建类别列 - 按照物理学、化学、生物学的顺序
        double x = 50;
        double columnWidth = 260; // 减小列宽
        double maxCategoryHeight = 0;

        // 按顺序处理三个学科
        for (TechCategory category : new TechCategory[]{TechCategory.PHYSICS, TechCategory.CHEMISTRY, TechCategory.BIOLOGY}) {
            List<Technology> categoryTechs = techsByCategory.get(category);
            if (categoryTechs.isEmpty()) continue;

            // 创建类别标题
            Label categoryTitle = new Label(category.getDisplayName() + " " + category.getIcon());
            categoryTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16)); // 减小字体
            categoryTitle.setTextFill(category.getColor());
            categoryTitle.setLayoutX(x + 20);
            categoryTitle.setLayoutY(20);
            categoryTitle.setEffect(new javafx.scene.effect.DropShadow(5, Color.BLACK));

            // 创建类别列容器
            VBox column = new VBox(20); // 减小间距
            column.setLayoutX(x);
            column.setLayoutY(60);
            column.setPrefWidth(columnWidth);
            column.setStyle("-fx-background-color: rgba(30, 30, 30, 0.7); "
                          + "-fx-background-radius: 8; " // 减小圆角
                          + "-fx-padding: 10;"); // 减小内边距

            // 按层级排序科技
            Map<Integer, List<Technology>> techsByTier = groupByTier(categoryTechs);

            double y = 0;
            for (int tier = 1; tier <= 8; tier++) {
                List<Technology> tierTechs = techsByTier.getOrDefault(tier, new ArrayList<>());

                if (!tierTechs.isEmpty()) {
                    // 创建层级标签
                    Label tierLabel = new Label("等级 " + tier);
                    tierLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10)); // 减小字体
                    tierLabel.setTextFill(Color.LIGHTGRAY);
                    tierLabel.setPadding(new Insets(0, 0, 3, 0)); // 减小内边距

                    // 创建层级容器
                    FlowPane tierContainer = new FlowPane(javafx.geometry.Orientation.HORIZONTAL, 10, 10); // 减小间距
                    tierContainer.setPrefWidth(columnWidth - 20); // 调整宽度
                    tierContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    for (Technology tech : tierTechs) {
                        // 不添加终极武器到分类列中
                        if (!"ULTIMATE_WEAPON".equals(tech.getId())) {
                            TechCard techCard = new TechCard(tech);
                            techCardMap.put(tech, techCard);
                            tierContainer.getChildren().add(techCard);
                        }
                    }

                    VBox tierBox = new VBox(3); // 减小间距
                    tierBox.getChildren().addAll(tierLabel, tierContainer);
                    column.getChildren().add(tierBox);
                    y += 100; // 减小每层高度
                }
            }

            techTreeCanvas.getChildren().addAll(categoryTitle, column);
            categoryColumns.put(category, column);
            maxCategoryHeight = Math.max(maxCategoryHeight, y + 120); // 调整高度

            x += columnWidth + 30; // 减小间距
        }

        // 添加终极武器科技到中心位置
        Technology ultimateWeapon = techTree.getTechnology("ULTIMATE_WEAPON");
        if (ultimateWeapon != null) {
            // 计算中心位置，使其更居中并往下移动，再向右一些
            double centerX = (x - columnWidth) / 2 + 50; // 在三个学科的中心偏右下位置
            double centerY = maxCategoryHeight + 100; // 在所有科技下方更远的位置
            
            TechCard ultimateCard = new TechCard(ultimateWeapon);
            ultimateCard.setLayoutX(centerX);
            ultimateCard.setLayoutY(centerY);
            techCardMap.put(ultimateWeapon, ultimateCard);
            techTreeCanvas.getChildren().add(ultimateCard);
        }
        // 更新科技树画布大小
        updateCanvasSize(x, maxCategoryHeight + 200); // 为终极武器增加额外空间
    }

    private void createGridBackground() {
        // 创建科技树背景网格
        double canvasWidth = 1200; // 减小画布宽度
        double canvasHeight = 800; // 减小画布高度
        
        for (double x = 0; x < canvasWidth; x += 30) {
            Line line = new Line(x, 0, x, canvasHeight);
            line.setStroke(Color.rgb(50, 50, 50, 0.3));
            line.setStrokeWidth(0.5);
            techTreeCanvas.getChildren().add(line);
        }
        
        for (double y = 0; y < canvasHeight; y += 30) {
            Line line = new Line(0, y, canvasWidth, y);
            line.setStroke(Color.rgb(50, 50, 50, 0.3));
            line.setStrokeWidth(0.5);
            techTreeCanvas.getChildren().add(line);
        }
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



    private void addConnectionLine(Technology prereq, Technology tech) {
        TechCard prereqCard = techCardMap.get(prereq);
        TechCard techCard = techCardMap.get(tech);
        
        if (prereqCard != null && techCard != null) {
            // 绘制连接线
            Line connection = new Line();
            
            // 计算起始点和终点（卡片中心）
            javafx.geometry.Bounds prereqBounds = prereqCard.getBoundsInParent();
            javafx.geometry.Bounds techBounds = techCard.getBoundsInParent();
            
            double startX = prereqBounds.getMinX() + prereqBounds.getWidth() / 2;
            double startY = prereqBounds.getMinY() + prereqBounds.getHeight() / 2;
            double endX = techBounds.getMinX() + techBounds.getWidth() / 2;
            double endY = techBounds.getMinY() + techBounds.getHeight() / 2;
            
            connection.setStartX(startX);
            connection.setStartY(startY);
            connection.setEndX(endX);
            connection.setEndY(endY);

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
            
            // 添加箭头效果
            connection.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            
            // 添加到画布底部（在卡片下面）
            techTreeCanvas.getChildren().add(0, connection);
        }
    }

    private void updateCanvasSize(double width, double height) {
        techTreeCanvas.setPrefSize(Math.max(width, 800), Math.max(height + 80, 600)); // 调整最小尺寸
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

        Label costLabel = new Label("基础研发时间: " + selectedTechnology.getResearchTime() + " 回合");
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
        boolean hasUnlocks = false;
        VBox unlockPanel = new VBox(5);
        
        // 检查是否有解锁内容
        if (!selectedTechnology.getUnlocks().isEmpty()) {
            hasUnlocks = true;
            Label unlockTitle = new Label("解锁内容:");
            unlockTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            unlockTitle.setTextFill(Color.LIGHTGRAY);
            detailsPanel.getChildren().add(unlockTitle);

            for (Unlockable unlock : selectedTechnology.getUnlocks()) {
                Label unlockLabel = new Label("• " + unlock.getName());
                unlockLabel.setTextFill(Color.CYAN);
                unlockPanel.getChildren().add(unlockLabel);
            }
        }
        
        // 解锁的建筑
        if (!selectedTechnology.getUnlockedBuildings().isEmpty()) {
            hasUnlocks = true;
            Label buildingTitle = new Label("解锁建筑:");
            buildingTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            buildingTitle.setTextFill(Color.LIGHTGRAY);
            detailsPanel.getChildren().add(buildingTitle);
            
            for (String buildingId : selectedTechnology.getUnlockedBuildings()) {
                String buildingName = getBuildingDisplayName(buildingId); // 转换为中文名称
                Label buildingLabel = new Label("• " + buildingName);
                buildingLabel.setTextFill(Color.CYAN);
                detailsPanel.getChildren().add(buildingLabel);
            }
        }
        
        // 解锁的舰船等级
        Set<String> unlockedShipClasses = new HashSet<>();
        // 解锁的模块
        Set<String> unlockedModules = new HashSet<>();
        
        if (!selectedTechnology.getUnlockedUnits().isEmpty()) {
            for (String unitId : selectedTechnology.getUnlockedUnits()) {
                // 判断是否为舰船等级
                if (isShipClass(unitId)) {
                    unlockedShipClasses.add(getUnitDisplayName(unitId));
                } else {
                    // 否则为模块
                    unlockedModules.add(getUnitDisplayName(unitId));
                }
            }
        }
        
        // 显示解锁的舰船等级
        if (!unlockedShipClasses.isEmpty()) {
            hasUnlocks = true;
            Label shipTitle = new Label("解锁舰船等级:");
            shipTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            shipTitle.setTextFill(Color.LIGHTGRAY);
            detailsPanel.getChildren().add(shipTitle);
            
            for (String shipClass : unlockedShipClasses) {
                Label shipLabel = new Label("• " + shipClass);
                shipLabel.setTextFill(Color.CYAN);
                detailsPanel.getChildren().add(shipLabel);
            }
        }
        
        // 显示解锁的模块
        if (!unlockedModules.isEmpty()) {
            hasUnlocks = true;
            Label moduleTitle = new Label("解锁模块:");
            moduleTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            moduleTitle.setTextFill(Color.LIGHTGRAY);
            detailsPanel.getChildren().add(moduleTitle);
            
            for (String moduleName : unlockedModules) {
                Label moduleLabel = new Label("• " + moduleName);
                moduleLabel.setTextFill(Color.CYAN);
                detailsPanel.getChildren().add(moduleLabel);
            }
        }
        
        // 解锁的资源
        if (!selectedTechnology.getUnlockedResources().isEmpty()) {
            hasUnlocks = true;
            Label resourceTitle = new Label("解锁资源:");
            resourceTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            resourceTitle.setTextFill(Color.LIGHTGRAY);
            detailsPanel.getChildren().add(resourceTitle);
            
            for (String resourceId : selectedTechnology.getUnlockedResources()) {
                String resourceName = getResourceDisplayName(resourceId); // 转换为中文名称
                Label resourceLabel = new Label("• " + resourceName);
                resourceLabel.setTextFill(Color.CYAN);
                detailsPanel.getChildren().add(resourceLabel);
            }
        }
        

        

        
        if (hasUnlocks) {
            detailsPanel.getChildren().addAll(unlockPanel);
        } else {
            Label noUnlocksLabel = new Label("此科技无特殊解锁内容");
            noUnlocksLabel.setTextFill(Color.GRAY);
            detailsPanel.getChildren().add(noUnlocksLabel);
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

                Label costLabel = new Label("时间: " + tech.getResearchTime() + " 回合");
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

            setPrefSize(100, 70); // 调小卡片尺寸
            setStyle(createCardStyle());

            // 卡片内容
            VBox content = new VBox(3); // 减小间距
            content.setPadding(new Insets(5)); // 减小内边距
            content.setAlignment(javafx.geometry.Pos.CENTER);

            // 图标和名称
            Label iconLabel = new Label(technology.getIcon());
            iconLabel.setStyle("-fx-font-size: 18px;"); // 减小图标大小
            
            Label nameLabel = new Label(technology.getName());
            nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9)); // 减小字体
            nameLabel.setTextFill(Color.WHITE);
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(85); // 限制宽度
            nameLabel.setAlignment(javafx.geometry.Pos.CENTER);

            // 研究状态
            Label statusLabel = new Label();
            statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

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
                
                // 添加点击反馈效果
                setStyle(createCardStyle() + "-fx-effect: dropshadow(gaussian, white, 15, 0.7, 0, 0);");
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(200));
                pause.setOnFinished(event -> setStyle(createCardStyle()));
                pause.play();
            });

            // 悬停效果
            setOnMouseEntered(e -> {
                setStyle(createCardStyle() + "-fx-effect: dropshadow(gaussian, " +
                        TechTreeUI.this.toHex(technology.getColor().brighter()) + ", 15, 0.7, 0, 0);");
            });

            setOnMouseExited(e -> {
                setStyle(createCardStyle());
            });
        }

        private String createCardStyle() {
            StringBuilder style = new StringBuilder();
            style.append("-fx-background-color: linear-gradient(to bottom, ")
                 .append(TechTreeUI.this.toHex(technology.getColor().brighter().brighter()))
                 .append(", ")
                 .append(TechTreeUI.this.toHex(technology.getColor()))
                 .append("); ");
            style.append("-fx-background-radius: 8; "); // 减小圆角
            style.append("-fx-border-color: ").append(TechTreeUI.this.toHex(technology.getColor().desaturate())).append("; ");
            style.append("-fx-border-width: 1.5; "); // 减小边框
            style.append("-fx-border-radius: 8; ");

            if (technology.isResearched()) {
                style.append("-fx-effect: dropshadow(gaussian, ").append(TechTreeUI.this.toHex(technology.getColor().brighter()))
                        .append(", 10, 0.7, 0, 0);"); // 减小发光效果
            } else if (techTree.canResearch(technology)) {
                style.append("-fx-effect: dropshadow(gaussian, #FFD700, 6, 0.7, 0, 0);"); // 减小发光效果
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
    
    /**
     * 获取由特定科技解锁的模块
     * @param technology 科技
     * @return 解锁的模块名称集合
     */
    private Set<String> getModulesUnlockedByTechnology(Technology technology) {
        Set<String> unlockedModules = new HashSet<>();
        
        // 根据科技ID匹配解锁的模块
        switch (technology.getId()) {
            case "advanced_weapons":
                unlockedModules.add("先进激光炮");
                unlockedModules.add("重型磁轨炮");
                unlockedModules.add("先进导弹系统");
                break;
            case "advanced_defenses":
                unlockedModules.add("先进护盾");
                unlockedModules.add("高级装甲");
                unlockedModules.add("先进点防御系统");
                break;
            case "advanced_utilities":
                unlockedModules.add("先进传感器");
                unlockedModules.add("高级货舱");
                unlockedModules.add("高级机库");
                break;
            case "advanced_engines":
                unlockedModules.add("先进引擎");
                unlockedModules.add("超光速引擎");
                break;
            case "advanced_power":
                unlockedModules.add("先进发电机");
                unlockedModules.add("零点能源发电机");
                break;
            default:
                // 检查科技名称中是否包含特定关键词
                String techName = technology.getId().toLowerCase();
                if (techName.contains("weapon")) {
                    unlockedModules.add("高级武器系统");
                } else if (techName.contains("defense")) {
                    unlockedModules.add("高级防御系统");
                } else if (techName.contains("engine")) {
                    unlockedModules.add("高级引擎系统");
                } else if (techName.contains("power")) {
                    unlockedModules.add("高级能源系统");
                }
                break;
        }
        
        return unlockedModules;
    }
    
    /**
     * 获取由特定科技解锁的舰船等级
     * @param technology 科技
     * @return 解锁的舰船等级名称集合
     */
    private Set<String> getShipClassesUnlockedByTechnology(Technology technology) {
        Set<String> unlockedShipClasses = new HashSet<>();
        
        // 根据科技ID匹配解锁的舰船等级
        switch (technology.getId()) {
            case "advanced_ship_hulls":
                unlockedShipClasses.add("巡洋舰");
                unlockedShipClasses.add("战列舰");
                break;
            case "carrier_construction":
                unlockedShipClasses.add("航母");
                break;
            case "frigate_technology":
                unlockedShipClasses.add("护卫舰");
                break;
            case "destroyer_tech":
                unlockedShipClasses.add("驱逐舰");
                break;
            default:
                // 检查科技名称中是否包含特定关键词
                String techName = technology.getId().toLowerCase();
                if (techName.contains("cruiser") || techName.contains("battleship")) {
                    unlockedShipClasses.add("巡洋舰");
                    unlockedShipClasses.add("战列舰");
                }
                break;
        }
        
        return unlockedShipClasses;
    }
    
    /**
     * 将单位ID转换为显示名称
     * @param unitId 单位ID
     * @return 显示名称
     */
    private String getUnitDisplayName(String unitId) {
        switch (unitId) {
            case "advanced_laser":
                return "先进激光炮";
            case "heavy_railgun":
                return "重型磁轨炮";
            case "advanced_shield":
                return "先进护盾";
            case "advanced_armor":
                return "高级装甲";
            case "advanced_engine":
                return "先进引擎";
            case "advanced_generator":
                return "先进发电机";
            case "advanced_sensor":
                return "先进传感器";
            case "advanced_hangar":
                return "高级机库";
            case "corvette":
                return "护卫舰";
            case "frigate":
                return "护卫舰"; // 护卫舰始终可用，无需解锁
            case "destroyer":
                return "驱逐舰";
            case "cruiser":
                return "巡洋舰";
            case "battleship":
                return "战列舰";
            case "carrier":
                return "航母";
            case "dreadnought":
                return "无畏舰";
            default:
                // 如果没有找到对应的中文名称，返回原ID
                return unitId;
        }
    }
    
    /**
     * 将建筑ID转换为显示名称
     * @param buildingId 建筑ID
     * @return 显示名称
     */
    private String getBuildingDisplayName(String buildingId) {
        switch (buildingId) {
            case "basic_factory":
                return "基础工厂";
            case "advanced_factory":
                return "高级工厂";
            case "shipyard":
                return "造船厂";
            case "advanced_shipyard":
                return "高级造船厂";
            case "research_lab":
                return "研究实验室";
            case "advanced_research_lab":
                return "高级研究实验室";
            default:
                // 如果没有找到对应的中文名称，返回原ID
                return buildingId;
        }
    }
    
    /**
     * 将资源ID转换为显示名称
     * @param resourceId 资源ID
     * @return 显示名称
     */
    private String getResourceDisplayName(String resourceId) {
        switch (resourceId) {
            case "metal":
                return "金属";
            case "crystal":
                return "晶体";
            case "gas":
                return "气体";
            case "energy":
                return "能源";
            case "advanced_materials":
                return "高级材料";
            case "exotic_matter":
                return "奇异物质";
            default:
                // 如果没有找到对应的中文名称，返回原ID
                return resourceId;
        }
    }
    
    /**
     * 判断unitId是否为舰船等级
     * @param unitId 单位ID
     * @return 如果是舰船等级则返回true
     */
    private boolean isShipClass(String unitId) {
        switch (unitId) {
            case "corvette":
            case "frigate":
            case "destroyer":
            case "cruiser":
            case "battleship":
            case "carrier":
            case "dreadnought":
                return true;
            default:
                return false;
        }
    }
}