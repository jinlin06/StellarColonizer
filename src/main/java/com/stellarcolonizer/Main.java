package com.stellarcolonizer;

import com.stellarcolonizer.core.GameEngine;
import com.stellarcolonizer.view.components.GameSettingsUI;
import com.stellarcolonizer.view.components.MainMenuUI;
import com.stellarcolonizer.view.controllers.MainController;
import com.stellarcolonizer.view.controllers.MainMenuCallback;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    private static GameEngine gameEngine;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 创建主菜单实例
        MainMenuUI mainMenu = new MainMenuUI(primaryStage);
        
        // 设置主菜单按钮的事件处理
        mainMenu.setNewGameAction(() -> {
            try {
                // 显示游戏设置界面
                showGameSettings(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        mainMenu.setContinueGameAction(() -> {
            // 继续游戏功能（需要实现存档系统）
            System.out.println("继续游戏功能正在开发中...");
        });
        
        mainMenu.setSettingsAction(() -> {
            // 游戏设置功能 - 可以在这里让用户自定义AI数量和名称
            System.out.println("游戏设置功能正在开发中...");
            // 在实际实现中，这里会打开一个设置窗口，让用户自定义AI数量和名称
            showGameSettings(primaryStage);
        });
        
        // 显示主菜单
        Scene scene = new Scene(mainMenu, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        
        primaryStage.setTitle("星际殖民者 - 主菜单");
        primaryStage.getIcons().add(new Image("/images/icon.png"));
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.show();
    }
    
    private void startGame(Stage primaryStage, int aiCount, String[] aiNames) {
        try {
            // 加载主界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();

            // 初始化游戏引擎
            gameEngine = new GameEngine();
            gameEngine.initialize(aiCount, aiNames); // 使用自定义AI数量和名称初始化游戏引擎
            controller.setGameEngine(gameEngine);
            
            // 设置主菜单回调
            controller.setMainMenuCallback(() -> {
                try {
                    // 重新显示主菜单
                    start(primaryStage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // 设置舞台
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

            primaryStage.setTitle("星际殖民者 v1.0");
            primaryStage.getIcons().add(new Image("/images/icon.png"));
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showGameSettings(Stage primaryStage) {
        GameSettingsUI gameSettings = new GameSettingsUI();
        
        gameSettings.setGameStartCallback((aiCount, aiNames) -> {
            startGame(primaryStage, aiCount, aiNames);
        });
        
        gameSettings.setBackCallback(() -> {
            try {
                start(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        Scene scene = new Scene(gameSettings, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        
        primaryStage.setTitle("星际殖民者 - 游戏设置");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static GameEngine getGameEngine() {
        return gameEngine;
    }

    public static void main(String[] args) {
        launch(args);
    }
}