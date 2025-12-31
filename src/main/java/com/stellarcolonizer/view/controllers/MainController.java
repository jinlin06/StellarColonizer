package com.stellarcolonizer.view.controllers;

import com.stellarcolonizer.core.GameEngine;
import com.stellarcolonizer.model.colony.Building;
import com.stellarcolonizer.model.colony.Colony;
import com.stellarcolonizer.model.colony.ResourceRequirement;
import com.stellarcolonizer.model.economy.ResourceStockpile;
import com.stellarcolonizer.model.fleet.Fleet;
import com.stellarcolonizer.model.galaxy.Hex;
import com.stellarcolonizer.model.galaxy.StarSystem;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import com.stellarcolonizer.model.service.event.GameEvent;
import com.stellarcolonizer.model.service.event.GameEventListener;
import com.stellarcolonizer.model.technology.TechTree;
import com.stellarcolonizer.view.components.*;
import com.stellarcolonizer.view.components.StarSystemInfoView; // 添加导入
import com.stellarcolonizer.view.components.DiplomacyView; // 添加外交界面导入
import com.stellarcolonizer.view.controllers.UniversalResourceMarketController; // 添加市场控制器导入
import com.stellarcolonizer.view.components.FleetListSelectedEvent; // 添加舰队列表选择事件导入
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.util.Optional;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.HBox;

import java.util.EnumMap;
import java.util.List; // 添加List导入
import java.util.Map;

public class MainController {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private Button nextTurnButton;
    
    @FXML
    private Label turnLabel;
    

    
    @FXML
    private VBox resourcesContainer; // 新添加的资源容器

    @FXML
    private TextArea eventLog;

    private HexMapView hexMapView;

    private GameEngine gameEngine;
    
    // 添加回调接口
    private MainMenuCallback mainMenuCallback;

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;

        // 设置地图数据
        hexMapView.setHexGrid(gameEngine.getGalaxy().getHexGrid());
        hexMapView.setGalaxy(gameEngine.getGalaxy());
        hexMapView.setPlayerFaction(gameEngine.getPlayerFaction());
        hexMapView.setPlayerStartHex(gameEngine.getPlayerStartHex()); // 设置玩家起始位置

        // 初始化资源显示
        updateResourceDisplay();
        updateTurnDisplay();
        
        // 请求焦点以便接收键盘事件
        hexMapView.requestFocus();
        
        // 添加一个延时请求焦点，确保在所有初始化完成后获得焦点
        javafx.application.Platform.runLater(() -> {
            // 再次延时确保UI完全加载
            javafx.application.Platform.runLater(() -> {
                hexMapView.requestFocus();
            });
        });
        
        // 设置游戏事件监听器
        setupEventListeners();
    }

    // 添加设置回调的方法
    public void setMainMenuCallback(MainMenuCallback callback) {
        this.mainMenuCallback = callback;
    }

    @FXML
    public void initialize() {
        // 初始化地图视图
        hexMapView = new HexMapView();
        mainContainer.setCenter(hexMapView);
        // 确保地图视图能够接收键盘事件
        hexMapView.requestFocus();
        
        // 设置六边形选择监听（提前设置，避免事件丢失）
        hexMapView.addEventHandler(HexSelectedEvent.HEX_SELECTED, event -> {
            Hex selectedHex = event.getSelectedHex();
            
            // 如果点击的是其他六边形且当前有选中的舰队，清除选中状态
            if (hexMapView.getSelectedFleet() != null && 
                !event.getSelectedHex().equals(hexMapView.getSelectedFleet().getCurrentHex())) {
                hexMapView.setSelectedFleet(null);
            }
            
            onHexSelected(selectedHex);
        });
        
        // 设置舰队选择监听
        hexMapView.addEventHandler(FleetSelectedEvent.FLEET_SELECTED, event -> {
            Fleet selectedFleet = event.getSelectedFleet();
            onFleetSelected(selectedFleet);
        });
        
        // 设置舰队列表选择监听
        hexMapView.addEventHandler(FleetListSelectedEvent.FLEET_LIST_SELECTED, event -> {
            onFleetListSelected(event.getFleetList());
        });
    }

    private void setupEventListeners() {
        // 游戏事件监听
        if (gameEngine != null) {
            gameEngine.addEventListener(new GameEventListener() {
                @Override
                public void onEvent(GameEvent event) {
                    javafx.application.Platform.runLater(() -> {
                        if ("AI_LOG".equals(event.getType())) {
                            // 处理AI日志事件
                            addEventToLog("[AI] " + event.getData().toString());
                        } else if ("VICTORY".equals(event.getType())) {
                            // 处理胜利事件
                            String victoryMessage = event.getData().toString();
                            addEventToLog("[胜利] " + victoryMessage);
                            showVictoryDialog(victoryMessage);
                        } else {
                            // 处理其他游戏事件
                            addEventToLog(event.getMessage());
                        }
                    });
                }
            });
        }

        // 下一回合按钮
        nextTurnButton.setOnAction(event -> {
            gameEngine.nextTurn();
            updateTurnDisplay();
            updateResourceDisplay();
        });
    }

    private void onHexSelected(Hex hex) {
        // 显示选中单元格信息的弹窗
        if (hex.hasStarSystem()) {
            StarSystem system = hex.getStarSystem();
            // 如果六边形中有舰队，将舰队信息传递给星系详情窗口
            if (!hex.getEntities().isEmpty()) {
                showStarSystemInfoWithFleets(system, hex.getEntities());
            } else {
                showStarSystemInfo(system);
            }
        } else {
            // 如果六边形中没有星系但有舰队，显示六边形详情和舰队信息
            if (!hex.getEntities().isEmpty()) {
                showHexInfoWithFleets(hex, hex.getEntities());
            } else {
                showHexInfo(hex);
            }
        }
    }
    
    private void onFleetSelected(Fleet fleet) {
        // 设置地图视图中的选中舰队，以高亮显示可移动范围
        // 这个方法现在只在用户明确点击移动按钮后调用
        if (hexMapView != null) {
            hexMapView.setSelectedFleet(fleet);
        }
    }
    
    private void onFleetListSelected(List<Fleet> fleetList) {
        // 当六边形中有多个舰队时，将舰队列表传递给星系详情窗口
        if (fleetList != null && !fleetList.isEmpty() && hexMapView != null && hexMapView.getSelectedHex() != null) {
            Hex selectedHex = hexMapView.getSelectedHex();
            if (selectedHex.hasStarSystem()) {
                showStarSystemInfoWithFleets(selectedHex.getStarSystem(), fleetList);
            } else {
                // 如果没有星系，创建一个只显示舰队的界面
                showFleetListInfo(fleetList);
            }
        }
    }
    
    private void showStarSystemInfoWithFleets(StarSystem system, List<Fleet> fleetList) {
        // 使用新的星系信息展示窗口，包含舰队信息
        StarSystemInfoView.showSystemInfoWithFleets(system, gameEngine.getPlayerFaction(), fleetList, 
            fleet -> {
                // 回调函数：当在星系详情窗口中选择舰队时，通知HexMapView选中该舰队以高亮可移动范围
                if (hexMapView != null) {
                    hexMapView.setSelectedFleet(fleet);
                }
            });
    }
    
    private void showFleetListInfo(List<Fleet> fleetList) {
        // 显示六边形中舰队列表的信息
        String info = "六边形中的舰队:\n\n";
        for (int i = 0; i < fleetList.size(); i++) {
            Fleet fleet = fleetList.get(i);
            info += (i + 1) + ". " + fleet.getName() + "\n" +
                   "   舰船数量: " + fleet.getShipCount() + "\n" +
                   "   战斗力: " + String.format("%.0f", fleet.getTotalCombatPower()) + "\n" +
                   "   任务: " + fleet.getCurrentMission().getDisplayName() + "\n\n";
        }
        
        showInfoDialog("舰队列表", info);
    }
    
    private void showHexInfoWithFleets(Hex hex, List<Fleet> fleetList) {
        // 使用新的六边形信息展示窗口，包含舰队信息
        StarSystemInfoView.showHexInfoWithFleets(hex, gameEngine.getPlayerFaction(), fleetList, 
            fleet -> {
                // 回调函数：当在六边形详情窗口中选择舰队时，通知HexMapView选中该舰队以高亮可移动范围
                if (hexMapView != null) {
                    hexMapView.setSelectedFleet(fleet);
                }
            });
    }

    private void showStarSystemInfo(StarSystem system) {
        // 使用新的星系信息展示窗口
        StarSystemInfoView.showSystemInfo(system, gameEngine.getPlayerFaction());
    }

    private void showHexInfo(Hex hex) {
        String info = "坐标: " + hex.getCoord() + "\n" +
                "类型: " + hex.getType().getDisplayName() + "\n" +
                "可见度: " + String.format("%.1f%%", hex.getVisibility() * 100);

        showInfoDialog(String.format("%.1f%%", hex.getVisibility() * 100), info);
    }
    
    private void showFleetInfo(Fleet fleet) {
        // 显示选中的舰队信息
        if (fleet != null) {
            // 不再显示独立的舰队信息弹窗
            // 直接设置地图视图中的选中舰队，以高亮显示可移动范围
            if (hexMapView != null) {
                hexMapView.setSelectedFleet(fleet);
            }
        }
    }
    
    private void showFleetOperations(Fleet fleet) {
        // 显示舰队信息和操作选项
        if (fleet != null) {
            // 不再显示独立弹窗，而是通过星系详情窗口显示舰队信息
            // 设置地图视图中的选中舰队，以高亮显示可移动范围
            if (hexMapView != null) {
                hexMapView.setSelectedFleet(fleet);
            }
        }
    }
    


    private void showInfoDialog(String title, String content) {
        // 创建弹窗显示信息
        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(true);
        
        // 设置窗口图标
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/icon.png"));
            dialog.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("无法加载窗口图标: " + e.getMessage());
        }
        
        // 创建标题标签
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title-label");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        
        // 创建内容标签
        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("content-label");
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 14px; -fx-text-fill: #ffffff;");
        
        // 创建滚动面板
        ScrollPane scrollPane = new ScrollPane(contentLabel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(450, 300);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setStyle("-fx-background: #2d2d2d; -fx-border-color: #555555; -fx-padding: 15px;");
        
        // 创建容器
        VBox vbox = new VBox(15);
        vbox.getChildren().addAll(titleLabel, scrollPane);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #3c3c3c;");
        
        Scene scene = new Scene(vbox, 450, 350);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    private void updateResourceDisplay() {
        if (gameEngine == null || gameEngine.getPlayerFaction() == null) return;

        // 获取玩家阵营的统一资源数据（现在所有资源都存储在派系层面）
        ResourceStockpile factionStockpile = gameEngine.getPlayerFaction().getResourceStockpile();
        
        // 计算所有殖民地的净产量总和
        Map<ResourceType, Float> totalNetProduction = new EnumMap<>(ResourceType.class);
        
        // 初始化所有资源类型的净产量
        for (ResourceType type : ResourceType.values()) {
            totalNetProduction.put(type, 0f);
        }
        
        System.out.println("更新资源显示，殖民地数量: " + gameEngine.getPlayerFaction().getColonies().size());
        
        // 遍历所有殖民地计算总净产量
        for (Colony colony : gameEngine.getPlayerFaction().getColonies()) {
            Map<ResourceType, Float> netProduction = colony.getNetProduction();
            
            System.out.println("殖民地: " + colony.getName());
            System.out.println("净产量: " + netProduction.size() + " 种");
            
            // 累加净产量
            for (ResourceType type : ResourceType.values()) {
                float netAmount = netProduction.getOrDefault(type, 0f);
                totalNetProduction.put(type, totalNetProduction.get(type) + netAmount);
                
                if (netAmount != 0) {
                    System.out.println("  " + type.getDisplayName() + 
                        " 净产量: " + String.format("%.2f", netAmount));
                }
            }
            
            // 添加建筑维护成本的考虑
            for (Building building : colony.getBuildings()) {
                // 获取所有资源类型的维护成本
                for (ResourceType type : ResourceType.values()) {
                    float maintenanceCost = building.getMaintenanceCost(type);
                    if (maintenanceCost > 0) {
                        totalNetProduction.put(type, totalNetProduction.get(type) - maintenanceCost);
                        
                        System.out.println("  " + type.getDisplayName() + 
                            " 维护成本: " + String.format("%.2f", maintenanceCost));
                    }
                }
            }
        }

        // 清空资源容器
        resourcesContainer.getChildren().clear();
        
        // 为每种资源创建标签并添加到容器中（排除科研资源，因为科研只在科技树界面显示）
        for (ResourceType type : ResourceType.values()) {
            if (type == ResourceType.SCIENCE) {
                continue; // 跳过科研资源，不在主资源界面显示
            }
            
            float amount = factionStockpile.getResource(type);  // 从派系库存获取资源数量
            float net = totalNetProduction.get(type); // 使用所有殖民地的净产量总和
            
            Label resourceLabel = new Label(formatResourceText(type, amount, net));
            resourceLabel.setTextFill(Color.web(type.getColor()));
            resourcesContainer.getChildren().add(resourceLabel);
        }
    }
    
    // 格式化资源显示文本
    private String formatResourceText(ResourceType type, float amount, float net) {
        return String.format("%s: %s (%s/回合)", 
            type.getDisplayName(), 
            formatNumber(amount), 
            formatNumberWithSign(net));
    }
    
    // 格式化简短资源显示文本
    private String formatResourceTextShort(ResourceType type, float amount, float net) {
        return String.format("%s: %s (%s/回合)", 
            type.getDisplayName(), 
            formatNumber(amount), 
            formatNumberWithSign(net));
    }
    
    // 格式化数字，添加k、w等单位
    private String formatNumber(float number) {
        // 处理负数显示
        if (number < 0) {
            return "-" + formatNumberPositive(-number);
        }
        return formatNumberPositive(number);
    }
    
    // 格式化正数
    private String formatNumberPositive(float number) {
        if (number >= 1000000) {
            return String.format("%.1fW", number / 1000000);
        } else if (number >= 1000) {
            return String.format("%.1fk", number / 1000);
        } else {
            // 对于小于1000的数字，保留1位小数
            return String.format("%.1f", number);
        }
    }

    // 格式化带符号的数字
    private String formatNumberWithSign(float number) {
        if (number >= 1000000) {
            return String.format("%+.1fW", number / 1000000);
        } else if (number >= 1000) {
            return String.format("%+.1fk", number / 1000);
        } else {
            // 对于小于1000的数字，保留1位小数
            return String.format("%+.1f", number);
        }
    }

    private void updateTurnDisplay() {
        if (gameEngine == null || gameEngine.getGameState() == null) return;

        turnLabel.setText("回合: " + gameEngine.getGameState().getCurrentTurn());
    }

    private void addEventToLog(String event) {
        eventLog.appendText(event + "\n");
        eventLog.setScrollTop(Double.MAX_VALUE);
    }

    @FXML
    private void onNextTurnClicked() {
        if (gameEngine != null) {
            gameEngine.nextTurn();
            updateTurnDisplay();
            updateResourceDisplay();
        }
    }
    
    // 添加测试方法
    @FXML
    private void onCenterViewClicked() {
        if (hexMapView != null && gameEngine != null) {
            hexMapView.centerOnHex(gameEngine.getPlayerStartHex());
        }
    }
    
    // 添加缺失的事件处理器方法
    @FXML
    private void showFleetManager() {
        System.out.println("显示舰队管理器");
        // TODO: 实现舰队管理器界面
        // showInfoDialog("舰队管理器", "舰队管理器界面正在开发中...\n\n在这里您可以:\n- 查看您的舰队\n- 管理舰船配置\n- 发送舰队执行任务\n- 查看舰队状态");
        
        // 创建并显示舰队管理器界面
        try {
            // 先检查gameEngine和playerFaction是否存在
            if (gameEngine == null || gameEngine.getPlayerFaction() == null) {
                showInfoDialog("错误", "游戏尚未初始化完成");
                return;
            }
            
            FleetManagerUI fleetManagerUI = new FleetManagerUI(gameEngine.getPlayerFaction());
            fleetManagerUI.setHexMapView(hexMapView); // 连接HexMapView以支持移动功能
            showComponentInWindow(fleetManagerUI, "舰队管理器");
        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("错误", "无法打开舰队管理器: " + e.getMessage());
        }
    }
    
    @FXML
    private void showTechTree() {
        System.out.println("显示科技树");
        // TODO: 实现科技树界面
        // showInfoDialog("科技树", "科技树界面正在开发中...\n\n在这里您可以:\n- 查看可研发的技术\n- 选择研究项目\n- 查看技术效果\n- 解锁新的能力");
        
        // 创建并显示科技树界面
        try {
            // 先检查gameEngine和playerFaction是否存在
            if (gameEngine == null || gameEngine.getPlayerFaction() == null) {
                showInfoDialog("错误", "游戏尚未初始化完成");
                return;
            }
            
            // 使用玩家派系的实际科技树，而不是创建新的
            TechTree techTree = gameEngine.getPlayerFaction().getTechTree();
            TechTreeUI techTreeUI = new TechTreeUI(techTree);
            showComponentInWindow(techTreeUI, "科技树");
        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("错误", "无法打开科技树: " + e.getMessage());
        }
    }
    
    @FXML
    private void showDiplomacy() {
        System.out.println("显示外交界面");
        
        // 创建并显示外交界面
        try {
            // 检查gameEngine和playerFaction是否存在
            if (gameEngine == null || gameEngine.getPlayerFaction() == null) {
                showInfoDialog("错误", "游戏尚未初始化完成");
                return;
            }
            
            DiplomacyView.showDiplomacyView(gameEngine.getGalaxy(), gameEngine.getPlayerFaction());
        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("错误", "无法打开外交界面: " + e.getMessage());
        }
    }
    
    @FXML
    private void showShipDesigner() {
        System.out.println("显示舰船设计");
        try {
            // 先检查gameEngine是否存在
            if (gameEngine == null || gameEngine.getPlayerFaction() == null) {
                showInfoDialog("错误", "游戏尚未初始化完成");
                return;
            }
            
            ShipDesignerUI shipDesignerUI = new ShipDesignerUI(gameEngine.getPlayerFaction());
            showComponentInWindow(shipDesignerUI, "舰船设计器");
        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("错误", "无法打开舰船设计器: " + e.getMessage());
        }
    }
    
    @FXML
    private void showColonyManager() {
        System.out.println("显示殖民地管理器");
        try {
            // 检查gameEngine和playerFaction是否存在
            if (gameEngine == null || gameEngine.getPlayerFaction() == null) {
                showInfoDialog("错误", "游戏尚未初始化完成");
                return;
            }
            
            // 使用玩家派系的殖民地管理器
            ColonyManagerView colonyManagerView = new ColonyManagerView(gameEngine.getPlayerFaction());
            showComponentInWindow(colonyManagerView, "殖民地管理器");
        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("错误", "无法打开殖民地管理器 " + e.getMessage());
        }
    }
    
    @FXML
    private void showUniversalMarket() {
        System.out.println("显示宇宙资源市场");

        // 创建并显示宇宙资源市场界面
        try {
            // 检查gameEngine和playerFaction是否存在
            if (gameEngine == null || gameEngine.getPlayerFaction() == null) {
                showInfoDialog("错误", "游戏尚未初始化完成");
                return;
            }

            // 创建宇宙资源市场（如果不存在）
            if (gameEngine.getUniversalResourceMarket() == null) {
                gameEngine.initializeUniversalResourceMarket();
            }

            UniversalResourceMarketController marketController =
                new UniversalResourceMarketController(gameEngine.getPlayerFaction(),
                    gameEngine.getUniversalResourceMarket());
                    
            // 设置交易完成回调，以便更新主界面资源显示
            marketController.setOnTransactionComplete(this::updateResourceDisplay);
            
            marketController.showMarketWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("错误", "无法打开宇宙资源市场: " + e.getMessage());
        }
    }

    /**
     * 在新窗口中显示组件
     * @param component 要显示的组件
     * @param title 窗口标题
     */
    private void showComponentInWindow(javafx.scene.Parent component, String title) {
        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initModality(Modality.NONE); // 非模态窗口，允许同时打开多个界面
        
        // 设置窗口图标
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/icon.png"));
            dialog.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("无法加载窗口图标: " + e.getMessage());
        }
        
        Scene scene = new Scene(component);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void showVictoryDialog(String message) {
        Stage dialog = new Stage();
        dialog.setTitle("游戏胜利");
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        // 设置窗口图标
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/icon.png"));
            dialog.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("无法加载窗口图标: " + e.getMessage());
        }
        
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 16px; -fx-padding: 20px;");
        
        Button returnToMenuButton = new Button("返回主菜单");
        returnToMenuButton.setOnAction(e -> {
            dialog.close();
            returnToMainMenu();
        });
        
        VBox layout = new VBox(20);
        layout.getChildren().addAll(messageLabel, returnToMenuButton);
        layout.setAlignment(javafx.geometry.Pos.CENTER);
        layout.setPadding(new Insets(20));
        
        Scene scene = new Scene(layout, 400, 200);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void returnToMainMenu() {
        try {
            // 如果有回调函数，使用回调来返回主菜单，否则使用直接方式
            if (mainMenuCallback != null) {
                mainMenuCallback.returnToMainMenu();
            } else {
                // 获取当前窗口
                Stage currentStage = (Stage) mainContainer.getScene().getWindow();
                
                // 创建主菜单界面
                MainMenuUI mainMenu = new MainMenuUI(currentStage);
                
                // 设置主菜单的操作
                mainMenu.setNewGameAction(() -> {
                    // 开始新游戏的逻辑
                    // 这里可以重新初始化游戏
                });
                
                mainMenu.setContinueGameAction(() -> {
                    // 继续游戏的逻辑
                });
                
                mainMenu.setSettingsAction(() -> {
                    // 设置的逻辑
                });
                
                // 创建新场景并切换到主菜单
                Scene newScene = new Scene(mainMenu, 1400, 900);
                newScene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
                
                currentStage.setScene(newScene);
                currentStage.setTitle("星际殖民者 - 主菜单");
                
                // 如果有游戏引擎，重置游戏状态
                if (gameEngine != null) {
                    gameEngine.resetGame();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("返回主菜单失败: " + e.getMessage());
            
            // 如果无法加载主菜单，至少显示一个错误信息
            showInfoDialog("错误", "无法返回主菜单: " + e.getMessage());
        }
    }
}

