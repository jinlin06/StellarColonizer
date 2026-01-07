package com.stellarcolonizer.view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainMenuUI extends BorderPane {
    
    private Stage primaryStage;
    private Button newGameButton;
    private Button continueGameButton;
    private Button settingsButton;
    
    public MainMenuUI(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeUI();
    }
    
    private void initializeUI() {
        // 创建背景图片
        try {
            Image backgroundImage = new Image("/images/img.png", 1400, 900, false, true);
            BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false)
            );
            
            // 设置整体布局为StackPane，使内容居中
            StackPane rootPane = new StackPane();
            rootPane.setBackground(new Background(bgImage));
            
            // 创建标题文本
            Text titleText = new Text("星际殖民者");
            titleText.setFill(Color.WHITE);
            titleText.setFont(Font.font("Arial", FontWeight.BOLD, 60));
            titleText.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 5, 5);");
            
            // 创建按钮容器
            VBox buttonContainer = new VBox(20);
            buttonContainer.setAlignment(Pos.CENTER);
            
            // 创建按钮
            newGameButton = createMenuButton("开始新游戏");
            continueGameButton = createMenuButton("继续游戏");
            settingsButton = createMenuButton("游戏设置");
            
            // 添加按钮到容器
            buttonContainer.getChildren().addAll(
                titleText,
                newGameButton,
                continueGameButton,
                settingsButton
            );
            
            // 设置按钮容器的样式
            buttonContainer.setPadding(new Insets(50));
            
            rootPane.getChildren().add(buttonContainer);
            setCenter(rootPane);
            
        } catch (Exception e) {
            System.err.println("无法加载背景图片: " + e.getMessage());
            // 如果无法加载背景图片，使用纯色背景
            setStyle("-fx-background-color: #000000;");
            
            // 创建标题文本
            Text titleText = new Text("星际殖民者");
            titleText.setFill(Color.WHITE);
            titleText.setFont(Font.font("Arial", FontWeight.BOLD, 60));
            titleText.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 5, 5);");
            
            // 创建按钮容器
            VBox buttonContainer = new VBox(20);
            buttonContainer.setAlignment(Pos.CENTER);
            
            // 创建按钮
            newGameButton = createMenuButton("开始新游戏");
            continueGameButton = createMenuButton("继续游戏");
            settingsButton = createMenuButton("游戏设置");
            
            // 添加按钮到容器
            buttonContainer.getChildren().addAll(
                titleText,
                newGameButton,
                continueGameButton,
                settingsButton
            );
            
            // 设置按钮容器的样式
            buttonContainer.setPadding(new Insets(50));
            
            setCenter(buttonContainer);
        }
    }
    
    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setPrefSize(300, 50);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        button.getStyleClass().add("button-primary"); // 使用CSS样式
        
        return button;
    }
    
    public void setNewGameAction(Runnable action) {
        newGameButton.setOnAction(e -> action.run());
    }
    
    public void setContinueGameAction(Runnable action) {
        continueGameButton.setOnAction(e -> action.run());
    }
    
    public void setSettingsAction(Runnable action) {
        settingsButton.setOnAction(e -> action.run());
    }
    
    public static void showMainMenu(Stage stage) {
        MainMenuUI mainMenu = new MainMenuUI(stage);
        
        Scene scene = new Scene(mainMenu, 1400, 900);
        scene.getStylesheets().add(MainMenuUI.class.getResource("/css/main.css").toExternalForm());
        
        stage.setScene(scene);
        stage.setTitle("星际殖民者 - 主菜单");
        stage.show();
    }
}