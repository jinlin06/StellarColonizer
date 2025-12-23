package com.stellarcolonizer;

import com.stellarcolonizer.core.GameEngine;
import com.stellarcolonizer.view.components.MainMenuUI;
import com.stellarcolonizer.view.controllers.MainController;
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
                // 开始新游戏
                startGame(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        mainMenu.setContinueGameAction(() -> {
            // 继续游戏功能（需要实现存档系统）
            System.out.println("继续游戏功能正在开发中...");
        });
        
        mainMenu.setSettingsAction(() -> {
            // 游戏设置功能
            System.out.println("游戏设置功能正在开发中...");
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
    
    private void startGame(Stage primaryStage) {
        try {
            // 加载主界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();

            // 初始化游戏引擎
            gameEngine = new GameEngine();
            gameEngine.initialize(); // 提前初始化游戏引擎
            controller.setGameEngine(gameEngine);

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

    public static GameEngine getGameEngine() {
        return gameEngine;
    }

    public static void main(String[] args) {
        launch(args);
    }
}