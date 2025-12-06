package com.stellarcolonizer.view.controllers;

import com.stellarcolonizer.core.GameEngine;
import com.stellarcolonizer.model.galaxy.*;
import com.stellarcolonizer.view.components.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML private BorderPane mainPane;
    @FXML private HexMapView hexMapView;
    @FXML private VBox sidebar;
    @FXML private Label turnLabel;
    @FXML private Label energyLabel;
    @FXML private Label metalLabel;
    @FXML private Label foodLabel;
    @FXML private Label scienceLabel;
    @FXML private TextArea eventLog;
    @FXML private Button nextTurnButton;

    private GameEngine gameEngine;

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        initializeUI();
        setupEventListeners();
    }

    private void initializeUI() {
        if (gameEngine == null) return;

        // 设置地图视图
        hexMapView.setHexGrid(gameEngine.getGalaxy().getHexGrid());
        hexMapView.setPlayerFaction(gameEngine.getPlayerFaction());

        // 设置六边形选择监听
        hexMapView.addEventHandler(HexSelectedEvent.HEX_SELECTED, event -> {
            Hex selectedHex = event.getSelectedHex();
            onHexSelected(selectedHex);
        });

        // 初始化资源显示
        updateResourceDisplay();
        updateTurnDisplay();
    }

    private void setupEventListeners() {
        // 游戏事件监听
        gameEngine.addEventListener(event -> {
            javafx.application.Platform.runLater(() -> {
                addEventToLog(event.getMessage());
            });
        });

        // 下一回合按钮
        nextTurnButton.setOnAction(event -> {
            gameEngine.nextTurn();
            updateTurnDisplay();
            updateResourceDisplay();
        });
    }

    private void onHexSelected(Hex hex) {
        System.out.println("选中六边形: " + hex.getCoord());

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
        info.append("宜居度: ").append(String.format("%.1f%%", system.getHabitability() * 100)).append("\n");
        info.append("行星数量: ").append(system.getPlanets().size()).append("\n\n");

        for (Planet planet : system.getPlanets()) {
            info.append("行星 ").append(planet.getOrbitIndex() + 1).append(": ")
                    .append(planet.getName()).append("\n");
            info.append("  类型: ").append(planet.getType().getDisplayName()).append("\n");
            info.append("  大小: ").append(planet.getSize()).append("\n");
            info.append("  宜居度: ").append(String.format("%.1f%%", planet.getHabitability() * 100)).append("\n");

            if (planet.getColony() != null) {
                info.append("  殖民地: ").append(planet.getColony().getFaction().getName()).append("\n");
            }
            info.append("\n");
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateResourceDisplay() {
        if (gameEngine == null || gameEngine.getPlayerFaction() == null) return;

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
    private void showFleetManager() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("舰队管理器");
        dialog.setHeaderText("管理你的所有舰队");

        dialog.getDialogPane().setPrefSize(1400, 900);

        FleetManagerUI fleetManager = new FleetManagerUI(gameEngine.getPlayerFaction());

        dialog.getDialogPane().setContent(fleetManager);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    @FXML
    private void showShipDesigner() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("舰船设计器");
        dialog.setHeaderText("设计新的舰船");

        dialog.getDialogPane().setPrefSize(1200, 800);

        ShipDesignerUI shipDesigner = new ShipDesignerUI();

        dialog.getDialogPane().setContent(shipDesigner);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    @FXML
    private void showTechTree() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("科技树");
        dialog.setHeaderText("研究新科技");

        dialog.getDialogPane().setPrefSize(1400, 900);

        TechTree techTree = new TechTree("人类联邦科技树");
        TechTreeUI techTreeUI = new TechTreeUI(techTree);

        dialog.getDialogPane().setContent(techTreeUI);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    @FXML
    private void showColonyManager() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("殖民地管理器");
        dialog.setHeaderText("管理你的所有殖民地");

        dialog.getDialogPane().setPrefSize(1200, 800);

        ColonyManagerView colonyManager = new ColonyManagerView(gameEngine.getPlayerFaction());

        dialog.getDialogPane().setContent(colonyManager);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }
}