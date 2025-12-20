package com.stellarcolonizer;

import com.stellarcolonizer.core.GameEngine;
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

    }

    public static GameEngine getGameEngine() {
        return gameEngine;
    }

    public static void main(String[] args) {
        launch(args);
    }
}