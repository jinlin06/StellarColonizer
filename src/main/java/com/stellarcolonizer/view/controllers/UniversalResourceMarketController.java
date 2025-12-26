package com.stellarcolonizer.view.controllers;

import com.stellarcolonizer.model.economy.UniversalResourceMarket;
import com.stellarcolonizer.model.faction.PlayerFaction;
import com.stellarcolonizer.model.galaxy.enums.ResourceType;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;

public class UniversalResourceMarketController {
    private PlayerFaction playerFaction;
    private UniversalResourceMarket market;
    private Label moneyLabel; // 金钱显示标签，供整个类使用
    private Runnable onTransactionComplete; // 交易完成时的回调函数
    
    public UniversalResourceMarketController(PlayerFaction playerFaction, UniversalResourceMarket market) {
        this.playerFaction = playerFaction;
        this.market = market;
    }
    
    // 添加设置交易完成回调的方法
    public void setOnTransactionComplete(Runnable callback) {
        this.onTransactionComplete = callback;
    }
    
    public void showMarketWindow() {
        Stage marketStage = new Stage();
        marketStage.initModality(Modality.APPLICATION_MODAL);
        marketStage.setTitle("宇宙资源市场");
        marketStage.setWidth(900);
        marketStage.setHeight(700);
        
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));
        mainLayout.setStyle("-fx-background-color: #1e1e1e;");
        
        // 标题
        Label titleLabel = new Label("宇宙资源市场");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");
        
        // 显示玩家当前金钱
        moneyLabel = new Label("当前金钱: " + String.format("%.2f", 
            playerFaction.getResourceStockpile().getResource(ResourceType.MONEY)));
        moneyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #FFD700; -fx-font-weight: bold;");
        
        // 创建定时器来更新金钱显示
        AnimationTimer moneyUpdateTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double currentMoney = playerFaction.getResourceStockpile().getResource(ResourceType.MONEY);
                moneyLabel.setText("当前金钱: " + String.format("%.2f", currentMoney));
            }
        };
        moneyUpdateTimer.start();
        
        // 确保窗口关闭时停止定时器
        marketStage.setOnCloseRequest(event -> moneyUpdateTimer.stop());
        
        // 创建选项卡
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #2b2b2b;");
        
        // 购买选项卡
        Tab buyTab = new Tab("购买资源");
        buyTab.setContent(createBuyTabContent());
        buyTab.setClosable(false);
        
        // 出售选项卡
        Tab sellTab = new Tab("出售资源");
        sellTab.setContent(createSellTabContent());
        sellTab.setClosable(false);
        
        tabPane.getTabs().addAll(buyTab, sellTab);
        
        mainLayout.getChildren().addAll(titleLabel, moneyLabel, tabPane);
        
        Scene scene = new Scene(mainLayout);
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        marketStage.setScene(scene);
        marketStage.show();
    }
    
    private VBox createBuyTabContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: #2d2d2d;");
        
        // 说明文字
        Label infoLabel = new Label("在宇宙资源市场购买资源，购买越多价格越高");
        infoLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // 资源选择
        ComboBox<ResourceType> resourceCombo = new ComboBox<>();
        for (ResourceType type : ResourceType.values()) {
            if (type != ResourceType.MONEY) { // 不能买卖金钱
                resourceCombo.getItems().add(type);
            }
        }
        // 设置单元格工厂以显示中文名称
        resourceCombo.setCellFactory(param -> new ListCell<ResourceType>() {
            @Override
            protected void updateItem(ResourceType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        resourceCombo.setButtonCell(new ListCell<ResourceType>() {
            @Override
            protected void updateItem(ResourceType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        resourceCombo.setPromptText("选择要购买的资源");
        resourceCombo.setPrefWidth(200);
        
        // 数量输入
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 10000, 1);
        quantitySpinner.setEditable(true);
        quantitySpinner.setPrefWidth(100);
        
        // 价格显示
        Label priceLabel = new Label("请选择资源和数量");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
        
        // 购买按钮
        Button buyButton = new Button("购买");
        buyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        // 事件处理
        resourceCombo.setOnAction(e -> updateBuyPrice(resourceCombo, quantitySpinner, priceLabel));
        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            updateBuyPrice(resourceCombo, quantitySpinner, priceLabel));
        
        buyButton.setOnAction(e -> executeBuy(resourceCombo, quantitySpinner));
        
        // 创建选择面板
        HBox selectionBox = new HBox(20);
        selectionBox.setPadding(new Insets(10));
        selectionBox.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-padding: 10;");
        selectionBox.getChildren().addAll(
            createLabeledComponent("资源类型:", resourceCombo),
            createLabeledComponent("数量:", quantitySpinner),
            buyButton
        );
        
        content.getChildren().addAll(infoLabel, selectionBox, priceLabel);
        
        return content;
    }
    
    private HBox createLabeledComponent(String labelText, javafx.scene.Node component) {
        HBox container = new HBox(5);
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        container.getChildren().addAll(label, component);
        return container;
    }
    
    private VBox createSellTabContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: #2d2d2d;");
        
        // 说明文字
        Label infoLabel = new Label("向宇宙资源市场出售资源，出售越多价格越低");
        infoLabel.setStyle("-fx-text-fill: #f44336; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // 资源选择
        ComboBox<ResourceType> resourceCombo = new ComboBox<>();
        for (ResourceType type : ResourceType.values()) {
            if (type != ResourceType.MONEY) { // 不能买卖金钱
                resourceCombo.getItems().add(type);
            }
        }
        // 设置单元格工厂以显示中文名称
        resourceCombo.setCellFactory(param -> new ListCell<ResourceType>() {
            @Override
            protected void updateItem(ResourceType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        resourceCombo.setButtonCell(new ListCell<ResourceType>() {
            @Override
            protected void updateItem(ResourceType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        resourceCombo.setPromptText("选择要出售的资源");
        resourceCombo.setPrefWidth(200);
        
        // 数量输入
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 10000, 1);
        quantitySpinner.setEditable(true);
        quantitySpinner.setPrefWidth(100);
        
        // 价格显示
        Label priceLabel = new Label("请选择资源和数量");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
        
        // 出售按钮
        Button sellButton = new Button("出售");
        sellButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        // 事件处理
        resourceCombo.setOnAction(e -> updateSellPrice(resourceCombo, quantitySpinner, priceLabel));
        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> 
            updateSellPrice(resourceCombo, quantitySpinner, priceLabel));
        
        sellButton.setOnAction(e -> executeSell(resourceCombo, quantitySpinner));
        
        // 创建选择面板
        HBox selectionBox = new HBox(20);
        selectionBox.setPadding(new Insets(10));
        selectionBox.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-padding: 10;");
        selectionBox.getChildren().addAll(
            createLabeledComponent("资源类型:", resourceCombo),
            createLabeledComponent("数量:", quantitySpinner),
            sellButton
        );
        
        content.getChildren().addAll(infoLabel, selectionBox, priceLabel);
        
        return content;
    }
    
    private void updateBuyPrice(ComboBox<ResourceType> resourceCombo, Spinner<Integer> quantitySpinner, Label priceLabel) {
        ResourceType selectedResource = resourceCombo.getValue();
        Integer quantity = quantitySpinner.getValue();
        
        if (selectedResource != null && quantity != null) {
            double pricePerUnit = market.getCurrentPrice(selectedResource);
            double totalPrice = pricePerUnit * quantity;
            double multiplier = market.getCurrentPriceMultiplier(selectedResource);
            
            String priceText = String.format("单价: %.2f 金币/单位 | 总价: %.2f 金币 | 当前倍数: %.2fx", 
                pricePerUnit, totalPrice, multiplier);
            
            // 根据倍数设置颜色
            if (multiplier > 2.0) {
                priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f44336; -fx-font-size: 14px;"); // 红色，价格很高
            } else if (multiplier > 1.5) {
                priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF9800; -fx-font-size: 14px;"); // 橙色，价格较高
            } else {
                priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-font-size: 14px;"); // 绿色，价格合理
            }
            
            priceLabel.setText(priceText);
        } else {
            priceLabel.setText("请选择资源和数量");
            priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
        }
    }
    
    private void updateSellPrice(ComboBox<ResourceType> resourceCombo, Spinner<Integer> quantitySpinner, Label priceLabel) {
        ResourceType selectedResource = resourceCombo.getValue();
        Integer quantity = quantitySpinner.getValue();
        
        if (selectedResource != null && quantity != null) {
            double revenue = market.getSaleRevenue(selectedResource, quantity);
            double revenuePerUnit = revenue / quantity;
            
            priceLabel.setText(String.format("单价: %.2f 金币/单位 | 总收入: %.2f 金币", revenuePerUnit, revenue));
        } else {
            priceLabel.setText("请选择资源和数量");
        }
    }
    
    private void executeBuy(ComboBox<ResourceType> resourceCombo, Spinner<Integer> quantitySpinner) {
        ResourceType resourceType = resourceCombo.getValue();
        Integer quantity = quantitySpinner.getValue();
        
        if (resourceType == null) {
            showAlert("错误", "请选择要购买的资源");
            return;
        }
        
        if (quantity == null || quantity <= 0) {
            showAlert("错误", "请输入有效的购买数量");
            return;
        }
        
        // 直接调用市场购买方法，它会处理所有验证和资源转移
        boolean success = market.buyResource(resourceType, quantity);
        
        if (!success) {
            double cost = market.getPurchaseCost(resourceType, quantity);
            double playerMoney = playerFaction.getResourceStockpile().getResource(ResourceType.MONEY);
            showAlert("错误", "金钱不足！需要 " + String.format("%.2f", cost) + " 金币，您只有 " + 
                String.format("%.2f", playerMoney) + " 金币");
            return;
        }
        
        showAlert("购买成功", "成功购买 " + quantity + " 单位 " + resourceType.getDisplayName() + 
            "，交易已完成");
        
        // 交易后立即更新金钱显示
        double currentMoney = playerFaction.getResourceStockpile().getResource(ResourceType.MONEY);
        moneyLabel.setText("当前金钱: " + String.format("%.2f", currentMoney));
        
        // 触发交易完成回调，以更新主界面的资源显示
        if (onTransactionComplete != null) {
            onTransactionComplete.run();
        }
    }
    
    private void executeSell(ComboBox<ResourceType> resourceCombo, Spinner<Integer> quantitySpinner) {
        ResourceType resourceType = resourceCombo.getValue();
        Integer quantity = quantitySpinner.getValue();
        
        if (resourceType == null) {
            showAlert("错误", "请选择要出售的资源");
            return;
        }
        
        if (quantity == null || quantity <= 0) {
            showAlert("错误", "请输入有效的出售数量");
            return;
        }
        
        // 直接调用市场出售方法，它会处理所有验证和资源转移
        boolean success = market.sellResource(resourceType, quantity);
        
        if (!success) {
            double playerResourceAmount = playerFaction.getResourceStockpile().getResource(resourceType);
            showAlert("错误", "资源不足！您有 " + String.format("%.2f", playerResourceAmount) + 
                " 单位 " + resourceType.getDisplayName() + "，但要出售 " + quantity + " 单位");
            return;
        }
        
        double revenue = market.getSaleRevenue(resourceType, quantity);
        
        showAlert("出售成功", "成功出售 " + quantity + " 单位 " + resourceType.getDisplayName() + 
            "，获得 " + String.format("%.2f", revenue) + " 金币");
        
        // 交易后立即更新金钱显示
        double currentMoney = playerFaction.getResourceStockpile().getResource(ResourceType.MONEY);
        moneyLabel.setText("当前金钱: " + String.format("%.2f", currentMoney));
        
        // 触发交易完成回调，以更新主界面的资源显示
        if (onTransactionComplete != null) {
            onTransactionComplete.run();
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}