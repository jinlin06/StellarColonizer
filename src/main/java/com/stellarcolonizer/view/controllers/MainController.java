package com.stellarcolonizer.view.controllers;

import com.stellarcolonizer.core.GameEngine;
import com.stellarcolonizer.model.galaxy.Hex;
import com.stellarcolonizer.model.galaxy.StarSystem;
import com.stellarcolonizer.model.service.event.GameEvent;
import com.stellarcolonizer.model.service.event.GameEventListener;
import com.stellarcolonizer.view.components.HexMapView;
import com.stellarcolonizer.view.components.HexSelectedEvent;
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

public class MainController {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private Button nextTurnButton;
    
    @FXML
    private Label turnLabel;
    
    @FXML
    private Label energyLabel;
    
    @FXML
    private Label metalLabel;
    
    @FXML
    private Label foodLabel;
    
    @FXML
    private Label scienceLabel;

    @FXML
    private TextArea eventLog;

    private HexMapView hexMapView;

    private GameEngine gameEngine;

    @FXML
    public void initialize() {
        // 初始化地图视图
        hexMapView = new HexMapView();
        mainContainer.setCenter(hexMapView);
        // 确保地图视图能够接收键盘事件
        hexMapView.requestFocus();
    }

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;

        // 设置地图数据
        hexMapView.setHexGrid(gameEngine.getGalaxy().getHexGrid());
        hexMapView.setGalaxy(gameEngine.getGalaxy());
        hexMapView.setPlayerFaction(gameEngine.getPlayerFaction());
        hexMapView.setPlayerStartHex(gameEngine.getPlayerStartHex()); // 设置玩家起始位置

        // 设置六边形选择监听
        hexMapView.addEventHandler(HexSelectedEvent.HEX_SELECTED, event -> {
            Hex selectedHex = event.getSelectedHex();
            onHexSelected(selectedHex);
        });

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
                System.out.println("Focus requested for HexMapView");
            });
        });
    }

    private void setupEventListeners() {
        // 游戏事件监听
        gameEngine.addEventListener(new GameEventListener() {
            @Override
            public void onEvent(GameEvent event) {
                javafx.application.Platform.runLater(() -> {
                    addEventToLog(event.getMessage());
                });
            }
        });

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
        StringBuilder info = new StringBuilder();
        info.append("恒星系: ").append(system.getName()).append("\n");
        info.append("恒星类型: ").append(system.getStarType().getDisplayName()).append("\n");
        info.append("行星数量: ").append(system.getPlanets().size()).append("\n\n");

        info.append("行星列表:\n");
        for (int i = 0; i < system.getPlanets().size(); i++) {
            var planet = system.getPlanets().get(i);
            info.append((i + 1)).append(". ").append(planet.getName())
                    .append(" (").append(planet.getType().getDisplayName()).append(")\n");
        }

        showInfoDialog("恒星系信息", info.toString());
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
        
        // 创建标题标签
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title-label");
        
        // 创建内容标签
        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("content-label");
        contentLabel.setWrapText(true);
        
        // 创建滚动面板
        ScrollPane scrollPane = new ScrollPane(contentLabel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(400, 300);
        scrollPane.getStyleClass().add("scroll-pane");
        
        // 创建容器
        VBox vbox = new VBox(15);
        vbox.getChildren().addAll(titleLabel, scrollPane);
        vbox.setPadding(new Insets(20));
        vbox.getStyleClass().add("info-dialog");
        
        Scene scene = new Scene(vbox, 450, 350);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    private void updateResourceDisplay() {
        if (gameEngine == null || gameEngine.getPlayerFaction() == null) return;

        // 更新UI控件中的资源显示
        // 注意：这里应该从游戏引擎中获取真实的资源数值，目前使用示例值
        energyLabel.setText("能量: 1000");
        metalLabel.setText("金属: 500");
        foodLabel.setText("食物: 300");
        scienceLabel.setText("科研: 100");
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
    }
    
    @FXML
    private void showTechTree() {
        System.out.println("显示科技树");
        // TODO: 实现科技树界面
    }
    
    @FXML
    private void showDiplomacy() {
        System.out.println("显示外交界面");
        // TODO: 实现外交界面
    }
    
    @FXML
    private void showBuildMenu() {
        System.out.println("显示建造菜单");
        // TODO: 实现建造菜单界面
    }
    
    @FXML
    private void showColonyManager() {
        System.out.println("显示殖民地管理器");
        // TODO: 实现殖民地管理器界面
    }
}