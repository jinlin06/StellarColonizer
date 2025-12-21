package com.stellarcolonizer.view.controllers;

import com.stellarcolonizer.core.GameEngine;
import com.stellarcolonizer.model.colony.Colony;
import com.stellarcolonizer.model.economy.ResourceStockpile;
import com.stellarcolonizer.model.galaxy.Hex;
import com.stellarcolonizer.model.galaxy.StarSystem;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import com.stellarcolonizer.model.service.event.GameEvent;
import com.stellarcolonizer.model.service.event.GameEventListener;
import com.stellarcolonizer.model.technology.TechTree;
import com.stellarcolonizer.view.components.*;
import com.stellarcolonizer.view.components.StarSystemInfoView; // 添加导入
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
            onHexSelected(selectedHex);
        });
    }

    private void setupEventListeners() {
        // 游戏事件监听
        if (gameEngine != null) {
            gameEngine.addEventListener(new GameEventListener() {
                @Override
                public void onEvent(GameEvent event) {
                    javafx.application.Platform.runLater(() -> {
                        addEventToLog(event.getMessage());
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
            showStarSystemInfo(system);
        } else {
            showHexInfo(hex);
        }
    }

    private void showStarSystemInfo(StarSystem system) {
        // 使用新的星系信息展示窗口
        StarSystemInfoView.showSystemInfo(system, gameEngine.getPlayerFaction());
    }

    private void showHexInfo(Hex hex) {
        String info = "坐标: " + hex.getCoord() + "\n" +
                "类型: " + hex.getType().getDisplayName() + "\n" +
                "可见度: " + String.format("%.1f%%", hex.getVisibility() * 100);

        showInfoDialog("六边形信息", info);
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

        // 获取玩家阵营的真实资源数据（从第一个殖民地获取）
        Map<ResourceType, Float> totalResources = new EnumMap<>(ResourceType.class);
        Map<ResourceType, Float> totalNetProduction = new EnumMap<>(ResourceType.class);
        
        // 初始化所有资源类型
        for (ResourceType type : ResourceType.values()) {
            totalResources.put(type, 0f);
            totalNetProduction.put(type, 0f);
        }
        
        System.out.println("更新资源显示，殖民地数量: " + gameEngine.getPlayerFaction().getColonies().size());
        
        // 遍历所有殖民地计算资源总量和净产量
        for (Colony colony : gameEngine.getPlayerFaction().getColonies()) {
            ResourceStockpile stockpile = colony.getResourceStockpile();
            Map<ResourceType, Float> resources = stockpile.getAllResources();
            Map<ResourceType, Float> netProduction = colony.getNetProduction();
            
            System.out.println("殖民地: " + colony.getName());
            System.out.println("资源库存: " + resources.size() + " 种");
            System.out.println("净产量: " + netProduction.size() + " 种");
            
            // 累加资源总量
            for (ResourceType type : ResourceType.values()) {
                float resourceAmount = resources.getOrDefault(type, 0f);
                float netAmount = netProduction.getOrDefault(type, 0f);
                
                totalResources.put(type, totalResources.get(type) + resourceAmount);
                totalNetProduction.put(type, totalNetProduction.get(type) + netAmount);
                
                if (resourceAmount != 0 || netAmount != 0) {
                    System.out.println("  " + type.getDisplayName() + 
                        " 库存: " + String.format("%.2f", resourceAmount) + 
                        " 净产量: " + String.format("%.2f", netAmount));
                }
            }
        }

        // 清空资源容器
        resourcesContainer.getChildren().clear();
        
        // 为每种资源创建标签并添加到容器中
        for (ResourceType type : ResourceType.values()) {
            float amount = totalResources.get(type);
            float net = totalNetProduction.get(type); // 使用实际的净产量
            
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
            
            // 创建一个新的TechTree实例
            TechTree techTree = new TechTree("玩家科技树");
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
        // TODO: 实现外交界面
        showInfoDialog("外交界面", "外交界面正在开发中...\n\n在这里您可以:\n- 查看与其他派系的关系\n- 发送外交提案\n- 管理条约和协议\n- 进行贸易谈判");
    }
    
    @FXML
    private void showShipDesigner() {
        System.out.println("显示舰船设计");
        try {
            // 先检查gameEngine是否存在
            if (gameEngine == null) {
                showInfoDialog("错误", "游戏尚未初始化完成");
                return;
            }
            
            ShipDesignerUI shipDesignerUI = new ShipDesignerUI();
            showComponentInWindow(shipDesignerUI, "舰船设计器");
        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("错误", "无法打开舰船设计器: " + e.getMessage());
        }
    }
    
    @FXML
    private void showColonyManager() {
        System.out.println("显示殖民地管理器");
        // TODO: 实现殖民地管理器界面
        // showInfoDialog("殖民地管理器", "殖民地管理器界面正在开发中...\n\n在这里您可以:\n- 管理殖民地人口\n- 分配劳动力\n- 建设设施\n- 查看殖民地生产");
        
        // 创建并显示殖民地管理器界面
        try {
            // 先检查gameEngine和playerFaction是否存在
            if (gameEngine == null || gameEngine.getPlayerFaction() == null) {
                showInfoDialog("错误", "游戏尚未初始化完成");
                return;
            }
            
            ColonyManagerView colonyManagerView = new ColonyManagerView(gameEngine.getPlayerFaction());
            showComponentInWindow(colonyManagerView, "殖民地管理器");
        } catch (Exception e) {
            e.printStackTrace();
            showInfoDialog("错误", "无法打开殖民地管理器: " + e.getMessage());
        }
    }
    
    /**
     * 在新窗口中显示组件
     * @param component 要显示的组件
     * @param title 窗口标题
     */
    private void showComponentInWindow(javafx.scene.Parent component, String title) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.setTitle(title);
        dialog.initModality(javafx.stage.Modality.NONE); // 非模态窗口，允许同时打开多个界面
        
        // 设置窗口图标
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/icon.png"));
            dialog.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("无法加载窗口图标: " + e.getMessage());
        }
        
        javafx.scene.Scene scene = new javafx.scene.Scene(component);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        
        dialog.setScene(scene);
        dialog.show();
    }
}