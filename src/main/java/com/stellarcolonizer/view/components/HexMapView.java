// HexMapView.java - 六边形地图视图组件
package com.stellarcolonizer.view.components;

import com.stellarcolonizer.battle.BattleResult;
import com.stellarcolonizer.battle.BattleSystem;
import com.stellarcolonizer.model.faction.Faction;
import com.stellarcolonizer.model.fleet.Fleet;
import com.stellarcolonizer.model.galaxy.*;
import com.stellarcolonizer.model.galaxy.enums.PlanetType;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.geometry.Point2D;

import java.util.*;


public class HexMapView extends Pane {

    private Canvas canvas;
    private GraphicsContext gc;

    private HexGrid hexGrid;
    private Galaxy galaxy; // 添加对整个银河系的引用，以便访问连接信息
    private Faction playerFaction;
    
    // 玩家起始位置
    private Hex playerStartHex;

    // 视图参数
    private double offsetX = 0;
    private double offsetY = 0;
    private double scale = 1.0;
    private double hexSize = 80.0; // 与GalaxyGenerator中保持一致

    // 选择状态
    private Hex selectedHex;
    private Fleet selectedFleet; // 当前选中的舰队
    private Map<Hex, Color> highlightedHexes = new HashMap<>();

    // 交互状态
    private boolean isDragging = false;
    private double dragStartX, dragStartY;
    private double dragStartOffsetX, dragStartOffsetY;

    // 动画定时器
    private AnimationTimer animationTimer;
    
    // 背景图片
    private javafx.scene.image.Image backgroundImage;
    
    // 标记是否需要在下次绘制时居中显示玩家起始位置
    private boolean needsCentering = false;

    public HexMapView() {
        this.canvas = new Canvas();
        this.gc = canvas.getGraphicsContext2D();

        // 设置画布大小随父容器变化
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

        // 加载背景图片
        try {
            this.backgroundImage = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/images/img.png")
            );
        } catch (Exception e) {
            System.err.println("无法加载背景图片: " + e.getMessage());
        }

        this.getChildren().add(canvas);

        // 重绘监听
        canvas.widthProperty().addListener((obs, oldVal, newVal) -> draw());
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> draw());

        // 设置交互事件
        setupMouseEvents();
        setupScrollEvents();
        setupKeyEvents();
        
        // 启动动画定时器
        setupAnimationTimer();
        
        // 确保面板可以获得焦点以接收键盘事件
        this.setFocusTraversable(true);
        this.setOnMouseClicked(e -> this.requestFocus()); // 点击时也请求焦点
        
        // 让画布接收鼠标事件
        canvas.setMouseTransparent(false);
        
        // 添加场景图属性变更监听器，确保在节点加入场景图后能获得焦点
        this.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // 在下一帧请求焦点
                javafx.application.Platform.runLater(() -> {
                    this.requestFocus();
                });
            }
        });
    }


    public void setHexGrid(HexGrid hexGrid) {
        this.hexGrid = hexGrid;
        if (hexGrid != null) {
            this.hexSize = hexGrid.getHexSize();
        }
        draw();
    }
    
    public void setGalaxy(Galaxy galaxy) {
        this.galaxy = galaxy;
        draw();
    }

    public void setPlayerFaction(Faction playerFaction) {
        this.playerFaction = playerFaction;
        draw();
    }
    
    public void setPlayerStartHex(Hex playerStartHex) {
        this.playerStartHex = playerStartHex;
        // 标记需要居中显示
        if (playerStartHex != null) {
            needsCentering = true;
            // 使用 Platform.runLater 确保在下一个 UI 循环中执行居中操作
            javafx.application.Platform.runLater(() -> {
                centerOnHex(playerStartHex);
                needsCentering = false;
            });
        }
    }

    public Hex getSelectedHex() {
        return selectedHex;
    }

    public void setSelectedHex(Hex hex) {
        this.selectedHex = hex;
        draw();
    }
    
    public void setSelectedFleet(Fleet fleet) {
        this.selectedFleet = fleet;
        if (fleet != null) {
            // 高亮显示可移动的六边形
            highlightMovableHexes(fleet);
        } else {
            // 清除高亮
            clearHighlights();
        }
        draw();
    }
    
    public Fleet getSelectedFleet() {
        return selectedFleet;
    }
    
    /**
     * 高亮显示可移动的六边形
     */
    private void highlightMovableHexes(Fleet fleet) {
        clearHighlights();
        
        if (fleet == null || fleet.getCurrentHex() == null) {
            return;
        }
        
        // 如果舰队本回合已移动过，则不显示移动范围
        if (fleet.hasMovedThisTurn()) {
            // 只高亮当前六边形，不显示移动范围
            highlightedHexes.put(fleet.getCurrentHex(), Color.YELLOW);
            return;
        }
        
        Hex currentHex = fleet.getCurrentHex();
        int moveRange = calculateFleetMoveRange(fleet);
        
        // 计算在移动范围内的所有六边形
        List<Hex> movableHexes = getReachableHexes(currentHex, moveRange);
        
        // 高亮可移动的六边形
        for (Hex hex : movableHexes) {
            highlightedHexes.put(hex, Color.LIGHTBLUE);
        }
        
        // 特别高亮当前六边形
        highlightedHexes.put(currentHex, Color.YELLOW);
    }
    
    /**
     * 计算舰队的移动范围
     * 低级舰船(1-3级)可移动2格，高级舰船(4-6级)只能移动1格
     */
    private int calculateFleetMoveRange(Fleet fleet) {
        if (fleet == null || fleet.getShips().isEmpty()) {
            return 1; // 默认移动范围
        }
        
        // 计算舰队中最高等级舰船的等级
        int highestTechLevel = fleet.getShips().stream()
            .mapToInt(ship -> ship.getDesign().getShipClass().getTechLevel())
            .max()
            .orElse(1);
        
        // 根据舰船等级确定移动范围
        if (highestTechLevel <= 3) {
            return 2; // 低级舰船可移动2格
        } else {
            return 1; // 高级舰船只能移动1格
        }
    }
    
    /**
     * 获取在指定范围内可到达的六边形
     * 只有有路径连接的六边形才能到达
     */
    private List<Hex> getReachableHexes(Hex startHex, int range) {
        List<Hex> reachableHexes = new ArrayList<>();
        
        // 使用广度优先搜索(BFS)找到范围内所有可到达的六边形
        Queue<Hex> queue = new LinkedList<>();
        Set<Hex> visited = new HashSet<>();
        Map<Hex, Integer> distances = new HashMap<>();
        
        queue.offer(startHex);
        visited.add(startHex);
        distances.put(startHex, 0);
        reachableHexes.add(startHex);
        
        while (!queue.isEmpty()) {
            Hex current = queue.poll();
            int currentDistance = distances.get(current);
            
            if (currentDistance >= range) {
                continue; // 如果已达到最大距离，不再扩展
            }
            
            // 检查所有邻居六边形
            List<Hex> neighbors = hexGrid.getNeighbors(current);
            for (Hex neighbor : neighbors) {
                // 检查银河系中是否有连接
                if (galaxy != null && !galaxy.getHexConnections().isEmpty()) {
                    Set<Hex> connectedHexes = galaxy.getHexConnections().get(current);
                    // 检查连接是否双向存在（确保两个六边形之间确实有连接）
                    Set<Hex> neighborConnections = galaxy.getHexConnections().get(neighbor);
                    boolean hasConnection = connectedHexes != null && connectedHexes.contains(neighbor);
                    
                    if (hasConnection && !visited.contains(neighbor)) {
                        visited.add(neighbor);
                        distances.put(neighbor, currentDistance + 1);
                        reachableHexes.add(neighbor);
                        queue.offer(neighbor);
                    }
                } else {
                    // 如果没有连接信息，假设所有邻居都可到达
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        distances.put(neighbor, currentDistance + 1);
                        reachableHexes.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
        }
        
        return reachableHexes;
    }

    public void highlightHex(Hex hex, Color color) {
        highlightedHexes.put(hex, color);
        draw();
    }

    public void clearHighlights() {
        highlightedHexes.clear();
        draw();
    }
    
    /**
     * 将指定的六边形居中显示
     */
    public void centerOnHex(Hex hex) {
        if (hex == null || hexGrid == null) return;
        
        Point2D center = hexGrid.cubeToPixel(hex.getCoord());
        offsetX = getWidth() / 2 - center.getX() * scale;
        offsetY = getHeight() / 2 - center.getY() * scale;

        paint();
    }

    private void setupAnimationTimer() {
        animationTimer = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                // 每16毫秒更新一次（约60 FPS），或者根据需要调整频率
                if (now - lastUpdate >= 16_000_000) { // 16毫秒
                    // 重新启用动画定时器
                    draw(); // 重新绘制以更新行星位置
                    lastUpdate = now;
                }
            }
        };
        animationTimer.start();
    }

    private void setupMouseEvents() {

        this.setOnMouseClicked(null); // 取消 Pane 自身的点击处理
        this.canvas.setOnMouseClicked(this::handleMouseClick);

        // 鼠标按下/拖拽/释放：用于右键拖拽地图
        this.setOnMousePressed(this::handleMousePressed);
        this.canvas.setOnMousePressed(this::handleMousePressed);
        this.setOnMouseDragged(this::handleMouseDragged);
        this.setOnMouseReleased(this::handleMouseReleased);
        this.canvas.setOnMouseReleased(this::handleMouseReleased);

        // 鼠标悬停
        this.setOnMouseMoved(this::handleMouseMoved);
    }

    private void setupScrollEvents() {
        this.setOnScroll(this::handleScroll);
    }
    
    private void setupKeyEvents() {
        // 添加按键事件监听器
        this.setOnKeyPressed(this::handleKeyPress);
        this.setFocusTraversable(true); // 确保可以接收键盘事件
    }
    
    private void handleKeyPress(KeyEvent event) {
        switch (event.getCode()) {
            case SPACE:
                // 按空格键将视角调整到玩家起始位置
                if (playerStartHex != null) {
                    centerOnHex(playerStartHex);
                }
                event.consume(); // 消费事件防止传播
                break;
        }
    }

    private void handleMouseClick(MouseEvent event) {
        // 只处理左键点击
        if (event.getButton() != javafx.scene.input.MouseButton.PRIMARY) {
            return;
        }
        
        if (hexGrid == null) {
            return;
        }

        // 计算点击位置对应的六边形坐标
        double screenX = event.getX();
        double screenY = event.getY();

        // 转换为世界坐标（考虑偏移和缩放）
        double worldX = (screenX - offsetX) / scale;
        double worldY = (screenY - offsetY) / scale;

        CubeCoord coord = hexGrid.pixelToCube(worldX, worldY);
        Hex clickedHex = hexGrid.getHex(coord);

        if (clickedHex != null) {
            // 检查点击的六边形中是否有敌对舰队可以战斗
            if (BattleSystem.hasEnemiesInHex(clickedHex)) {
                // 创建战斗UI并显示
                BattleUI battleUI = new BattleUI(clickedHex);
                
                // 创建一个新的Stage来显示战斗UI
                javafx.stage.Stage battleStage = new javafx.stage.Stage();
                battleStage.setTitle("舰队战斗");
                battleStage.setScene(new javafx.scene.Scene(battleUI, 500, 400));
                
                // 设置窗口属性
                battleStage.initOwner(this.getScene().getWindow());
                battleStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
                
                battleStage.showAndWait();
                
                // 重新绘制地图以更新舰队状态
                draw();
                
                // 触发六边形选择事件，以便更新UI
                HexSelectedEvent hexEvent = new HexSelectedEvent(HexSelectedEvent.HEX_SELECTED, clickedHex);
                fireEvent(hexEvent);
                
                return; // 战斗处理完成后直接返回，不再执行其他逻辑
            }
            
            // 如果存在选中的舰队并且该舰队有当前位置，则优先处理移动逻辑
            if (selectedFleet != null && selectedFleet.getCurrentHex() != null) {
                // 检查舰队是否属于玩家派系，如果不是，则不允许玩家移动
                if (selectedFleet.getFaction() != null && selectedFleet.getFaction().isAI()) {
                    // AI舰队不能被玩家移动，而是由AI自动控制
                    setSelectedFleet(null);
                    clearHighlights();
                    draw();
                    showAlert("无法移动", "AI舰队由AI自动控制，不能手动移动");
                    return;
                }
                
                if (selectedFleet.hasMovedThisTurn()) {
                    // 本回合已移动，点击当前格则取消选择
                    if (clickedHex.equals(selectedFleet.getCurrentHex())) {
                        setSelectedFleet(null);
                        clearHighlights();
                        draw();
                    }
                    showAlert("移动限制", "该舰队本回合已移动过，无法再次移动");
                } else {
                    int moveRange = calculateFleetMoveRange(selectedFleet);
                    List<Hex> reachableHexes = getReachableHexes(selectedFleet.getCurrentHex(), moveRange);

                    if (reachableHexes.contains(clickedHex) && !clickedHex.equals(selectedFleet.getCurrentHex())) {
                        boolean moveSuccessful = selectedFleet.moveTo(clickedHex);

                        if (moveSuccessful) {
                            System.out.println("舰队 " + selectedFleet.getName() + " 已移动到 " + clickedHex.getCoord());

                            setSelectedFleet(null);
                            clearHighlights();
                            draw();

                            HexSelectedEvent hexEvent = new HexSelectedEvent(HexSelectedEvent.HEX_SELECTED, clickedHex);
                            fireEvent(hexEvent);
                        }
                    } else if (reachableHexes.contains(clickedHex)) {
                        // 点击的是当前六边形，取消选择
                        setSelectedFleet(null);
                        clearHighlights();
                        draw();
                    } else {
                        showAlert("无法移动", "该六边形不可到达，舰队移动范围不足。");
                    }
                }
            } else {
                // 没有选中的舰队，仅处理六边形选择
                selectedHex = clickedHex;
                draw();

                // 触发选择事件，由 MainController 统一处理弹窗逻辑
                HexSelectedEvent hexEvent = new HexSelectedEvent(HexSelectedEvent.HEX_SELECTED, clickedHex);
                fireEvent(hexEvent);
            }
        }

        // 消费事件，防止继续冒泡触发其他 MouseClicked 处理器
        event.consume();
    }

    private void handleMousePressed(MouseEvent event) {
        // 仅处理右键按下用于拖拽地图，左键按下交由 handleMouseClick 处理
        if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
            isDragging = true;
            dragStartX = event.getX();
            dragStartY = event.getY();
            dragStartOffsetX = offsetX;
            dragStartOffsetY = offsetY;
        }
        event.consume();
    }
    
    // 显示警告对话框
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleMouseDragged(MouseEvent event) {
        if (isDragging) {
            double deltaX = event.getX() - dragStartX;
            double deltaY = event.getY() - dragStartY;
            offsetX = dragStartOffsetX + deltaX;
            offsetY = dragStartOffsetY + deltaY;
            draw();
        }
        event.consume(); // 消费事件防止传播
    }

    private void handleMouseReleased(MouseEvent event) {
        isDragging = false;
        event.consume(); // 消费事件防止传播
    }

    private void handleMouseMoved(MouseEvent event) {
        // 悬停效果
        if (hexGrid != null) {
            double worldX = (event.getX() - offsetX) / scale;
            double worldY = (event.getY() - offsetY) / scale;

            CubeCoord coord = hexGrid.pixelToCube(worldX, worldY);
            Hex hoveredHex = hexGrid.getHex(coord);

            if (hoveredHex != null) {
                // 可以显示工具提示或高亮
            }
        }
        event.consume(); // 消费事件防止传播
    }

    private void handleScroll(ScrollEvent event) {
        double zoomFactor = 1.1;
        double oldScale = scale;

        if (event.getDeltaY() > 0) {
            // 放大
            scale *= zoomFactor;
        } else {
            // 缩小
            scale /= zoomFactor;
        }

        // 限制缩放范围
        scale = Math.max(0.1, Math.min(5.0, scale));

        // 调整偏移，使鼠标位置保持相对固定
        double mouseX = event.getX();
        double mouseY = event.getY();

        offsetX = mouseX - (mouseX - offsetX) * (scale / oldScale);
        offsetY = mouseY - (mouseY - offsetY) * (scale / oldScale);

        draw();
        event.consume();
    }
    
    /**
     * 创建连接标识符以避免重复绘制（星系连接）
     */
    private String createConnectionId(StarSystem system1, StarSystem system2) {
        // 使用系统名称排序确保一致性
        String firstName = system1.getName();
        String secondName = system2.getName();
        return firstName.compareTo(secondName) < 0 ? 
            firstName + "|" + secondName : secondName + "|" + firstName;
    }
    
    /**
     * 创建连接标识符以避免重复绘制（六边形连接）
     */
    private String createConnectionId(Hex hex1, Hex hex2) {
        // 使用坐标排序确保一致性
        CubeCoord coord1 = hex1.getCoord();
        CubeCoord coord2 = hex2.getCoord();
        
        // 创建坐标字符串
        String coordStr1 = coord1.q + "," + coord1.r + "," + coord1.s;
        String coordStr2 = coord2.q + "," + coord2.r + "," + coord2.s;
        
        // 排序确保一致性
        return coordStr1.compareTo(coordStr2) < 0 ? 
            coordStr1 + "|" + coordStr2 : coordStr2 + "|" + coordStr1;
    }
    
    /**
     * 绘制六边形网格线和所有连接路径（包括空六边形）
     */
    private void drawAllConnections() {
        gc.setLineWidth(2);
        gc.setStroke(Color.rgb(100, 150, 200, 0.5)); // 浅蓝绿色半透明连接线
        
        // 全局去重集合，确保每对单元格之间最多只有一条连线
        Set<String> allDrawnConnections = new HashSet<>();
        
        // 无论是否有星系信息，都绘制所有相邻六边形之间的连线
        if (hexGrid != null) {
            // 如果有银河系连接信息，使用连接信息来决定绘制哪些连线
            if (galaxy != null && !galaxy.getHexConnections().isEmpty()) {
                // 使用银河系中存储的六边形连接信息
                for (Map.Entry<Hex, Set<Hex>> entry : galaxy.getHexConnections().entrySet()) {
                    Hex hex = entry.getKey();
                    Set<Hex> connectedHexes = entry.getValue();
                    
                    Point2D center = hexGrid.cubeToPixel(hex.getCoord());
                    
                    // 应用缩放和偏移
                    double screenX = center.getX() * scale + offsetX;
                    double screenY = center.getY() * scale + offsetY;
                    
                    for (Hex connectedHex : connectedHexes) {
                        // 创建连接标识符确保每对单元格之间最多只有一条连线
                        String hexCoord = hex.getCoord().toString();
                        String connectedCoord = connectedHex.getCoord().toString();
                        String lineId = hexCoord.compareTo(connectedCoord) < 0 ? 
                            hexCoord + "|" + connectedCoord : connectedCoord + "|" + hexCoord;
                        
                        if (allDrawnConnections.contains(lineId)) {
                            continue;
                        }
                        
                        allDrawnConnections.add(lineId);
                        
                        Point2D connectedCenter = hexGrid.cubeToPixel(connectedHex.getCoord());
                        
                        // 应用缩放和偏移
                        double connectedScreenX = connectedCenter.getX() * scale + offsetX;
                        double connectedScreenY = connectedCenter.getY() * scale + offsetY;
                        
                        // 绘制连接线
                        gc.strokeLine(screenX, screenY, connectedScreenX, connectedScreenY);
                    }
                }
            } else {
                // 如果没有连接信息，绘制所有相邻六边形的连接
                for (Hex hex : hexGrid.getAllHexes()) {
                    Point2D center = hexGrid.cubeToPixel(hex.getCoord());
                    
                    // 应用缩放和偏移
                    double screenX = center.getX() * scale + offsetX;
                    double screenY = center.getY() * scale + offsetY;
                    
                    // 获取邻居并绘制到邻居的连线
                    for (Hex neighbor : hexGrid.getNeighbors(hex)) {
                        // 创建连接标识符确保每对单元格之间最多只有一条连线
                        String hexCoord = hex.getCoord().toString();
                        String neighborCoord = neighbor.getCoord().toString();
                        String lineId = hexCoord.compareTo(neighborCoord) < 0 ? 
                            hexCoord + "|" + neighborCoord : neighborCoord + "|" + hexCoord;
                        
                        if (allDrawnConnections.contains(lineId)) {
                            continue;
                        }
                        
                        allDrawnConnections.add(lineId);
                        
                        Point2D neighborCenter = hexGrid.cubeToPixel(neighbor.getCoord());
                        
                        // 应用缩放和偏移
                        double neighborScreenX = neighborCenter.getX() * scale + offsetX;
                        double neighborScreenY = neighborCenter.getY() * scale + offsetY;
                        
                        // 绘制连接线
                        gc.strokeLine(screenX, screenY, neighborScreenX, neighborScreenY);
                    }
                }
            }
        }
    }


    private void draw() {
        if (hexGrid == null || canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
            return;
        }

        // 如果需要居中显示玩家起始位置，则执行居中操作
        if (needsCentering && playerStartHex != null) {
            centerOnHex(playerStartHex);
            needsCentering = false;
        }

        paint();
    }
    
    /**
     * 实际的绘制方法，避免在居中操作时产生递归调用
     */
    private void paint() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // 绘制背景图片
        if (backgroundImage != null) {
            gc.setGlobalAlpha(0.2); // 设置透明度为0.2
            gc.drawImage(backgroundImage, 0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setGlobalAlpha(1.0); // 恢复默认透明度
        }

        // 绘制所有连接（包括空六边形之间的连接）
        drawAllConnections();

        // 绘制所有六边形
        for (Hex hex : hexGrid.getAllHexes()) {
            drawHex(hex);
        }

        // 绘制高亮的六边形
        for (Map.Entry<Hex, Color> entry : highlightedHexes.entrySet()) {
            drawHexHighlight(entry.getKey(), entry.getValue());
        }

        // 绘制选中的六边形
        if (selectedHex != null) {
            drawHexHighlight(selectedHex, Color.YELLOW);
        }
    }

    private void drawHex(Hex hex) {
        CubeCoord coord = hex.getCoord();
        Point2D center = hexGrid.cubeToPixel(coord);

        // 应用缩放和偏移
        double screenX = center.getX() * scale + offsetX;
        double screenY = center.getY() * scale + offsetY;
        // 减小六边形的视觉大小以创建更大的间隙
        double screenSize = hexSize * 0.8 * scale; // 从0.9减小到0.8以增加间隙

        // 计算六边形顶点
        double[] xPoints = new double[6];
        double[] yPoints = new double[6];

        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI / 6 * (i + 0.5); // 增加0.5使六边形的一个角朝上
            xPoints[i] = screenX + screenSize * Math.cos(angle);
            yPoints[i] = screenY + screenSize * Math.sin(angle);
        }

        // 设置填充颜色（基于六边形类型）
        Color fillColor = getHexColor(hex);
        
        // 如果是玩家起始位置，使用特殊的颜色
        if (hex == playerStartHex) {
            fillColor = Color.rgb(255, 215, 0); // 金色表示玩家起始位置，更加醒目
        }
        
        gc.setFill(fillColor);
        gc.fillPolygon(xPoints, yPoints, 6);

        // 绘制边框
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        gc.strokePolygon(xPoints, yPoints, 6);

        // 如果六边形有星系，绘制特殊标记
        if (hex.hasStarSystem()) {
            drawStarSystem(hex.getStarSystem(), screenX, screenY, screenSize);
        }

        // 绘制可见度（战争迷雾）
        if (playerFaction != null) {
            drawVisibility(hex, screenX, screenY, screenSize);
        }

        // 绘制坐标（调试用）
        if (scale > 0.8) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(10));
            String coordText = String.format("(%d,%d,%d)", coord.q, coord.r, coord.s);
            gc.fillText(coordText, screenX - 15, screenY + 5);
        }
        
        // 绘制舰船图标（如果六边形中有舰船）
        if (!hex.getEntities().isEmpty()) {
            // 使用不同颜色表示不同外交关系的舰队
            // 计算六边形中心点
            double centerX = screenX - 5;
            double centerY = screenY - 5;
            
            // 为每个舰队绘制图标
            List<Fleet> fleets = hex.getFleets();
            for (int i = 0; i < fleets.size(); i++) {
                Fleet fleet = fleets.get(i);
                Color fleetColor = getFleetColor(fleet);
                
                // 绘制舰队图标（使用圆角矩形表示舰队）
                gc.setFill(fleetColor);
                gc.fillRoundRect(centerX + (i * 8), centerY, 6, 6, 2, 2); // 小圆角矩形
                
                // 如果是选中的舰队，添加边框高亮
                if (selectedFleet != null && selectedFleet.equals(fleet)) {
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(1.5);
                    gc.strokeRoundRect(centerX + (i * 8), centerY, 6, 6, 2, 2);
                }
            }
            
            // 如果缩放足够大，显示舰队数量
            if (scale > 1.0 && fleets.size() > 1) {
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(8));
                String fleetCount = String.valueOf(fleets.size());
                gc.fillText(fleetCount, screenX - 3, screenY + 3);
            }
        }

        // 绘制控制派系名称
        if (hex.hasStarSystem()) {
            StarSystem system = hex.getStarSystem();
            if (system.getControllingFaction() != null) {
                String factionName = system.getControllingFaction().getName();
                if (factionName != null && !factionName.isEmpty()) {
                    // 根据六边形大小调整字体大小
                    double fontSize = Math.max(8, screenSize * 0.15);
                    gc.setFont(Font.font(fontSize));

                    // 计算文本宽度以居中显示
                    Text text = new Text(factionName);
                    text.setFont(gc.getFont());
                    double textWidth = text.getBoundsInLocal().getWidth();

                    // 将派系名称显示在六边形的下方
                    gc.setFill(Color.WHITE);
                    gc.fillText(factionName, screenX - textWidth / 2, screenY + screenSize * 0.7);
                }
            }
        }
    }

    private Color getFleetColor(Fleet fleet) {
        // 如果是玩家派系的舰队，使用白色；否则使用派系颜色
        if (fleet.getFaction().equals(playerFaction)) {
            return Color.WHITE;
        } else {
            return fleet.getFaction().getColor();
        }
    }
    private Color getHexColor(Hex hex) {
        // 如果六边形有星系且该星系有控制派系，返回派系颜色
        if (hex.hasStarSystem()) {
            StarSystem system = hex.getStarSystem();
            if (system.getControllingFaction() != null) {
                Color factionColor = system.getControllingFaction().getColor();
                if (factionColor != null) {
                    // 将JavaFX颜色转换为Canvas颜色，大幅增加亮度
                    int r = (int)(factionColor.getRed() * 255);
                    int g = (int)(factionColor.getGreen() * 255);
                    int b = (int)(factionColor.getBlue() * 255);
                    
                    // 大幅提高亮度
                    r = Math.min(255, (int)(r * 2.0)); // 增加100%亮度
                    g = Math.min(255, (int)(g * 2.0)); // 增加100%亮度
                    b = Math.min(255, (int)(b * 2.0)); // 增加100%亮度
                    
                    return Color.rgb(r, g, b, 0.5); // 进一步提高透明度以使颜色更亮
                }
            }
        }
        
        // 其他情况下，根据六边形类型返回颜色，不因六边形中的舰队改变颜色
        switch (hex.getType()) {
            case EMPTY:
                return Color.rgb(240, 240, 240); // 浅灰色，更协调
            case NEBULA:
                return Color.rgb(200, 150, 220); // 浅紫色
            case ASTEROID_FIELD:
                return Color.rgb(200, 200, 200); // 浅灰色
            case STAR_SYSTEM:
                return Color.rgb(230, 230, 250); // 浅蓝色
            case WORMHOLE:
                return Color.rgb(200, 220, 250); // 浅青色
            case DARK_NEBULA:
                return Color.rgb(220, 200, 230); // 浅紫色
            default:
                return Color.LIGHTGRAY;
        }
    }

    private void drawStarSystem(StarSystem system, double centerX, double centerY, double size) {
        // 绘制恒星
        Color starColor = Color.web(system.getStarType().getColor());
        gc.setFill(starColor);

        double starRadius = size * 0.25; // 减小恒星尺寸
        gc.fillOval(centerX - starRadius, centerY - starRadius,
                starRadius * 2, starRadius * 2);

        // 添加光晕效果
        gc.setFill(Color.web(system.getStarType().getColor(), 0.3));
        double glowRadius = starRadius * 1.3; // 减小光晕尺寸
        gc.fillOval(centerX - glowRadius, centerY - glowRadius,
                glowRadius * 2, glowRadius * 2);

        // 如果缩放足够大，绘制行星轨道
        if (scale > 0.7) { // 提高缩放阈值
            drawPlanetOrbits(system, centerX, centerY, size);
        }
    }

    private void drawPlanetOrbits(StarSystem system, double centerX, double centerY, double size) {
        List<Planet> planets = system.getPlanets();
        int planetCount = planets.size();

        // 限制显示的行星数量以减少视觉混乱
        int maxPlanetsToShow = Math.min(planetCount, 4); // 最多显示4个行星

        for (int i = 0; i < maxPlanetsToShow; i++) {
            Planet planet = planets.get(i);
            // 调整轨道半径计算方式，使轨道更紧凑
            double orbitRadius = size * 0.4 + i * size * 0.25; // 减小轨道间距

            // 绘制轨道（只在较高缩放级别时显示）
            if (scale > 1.0) {
                gc.setStroke(Color.GRAY);
                gc.setLineWidth(0.3); // 减细轨道线
                gc.strokeOval(centerX - orbitRadius, centerY - orbitRadius,
                        orbitRadius * 2, orbitRadius * 2);
            }

            // 绘制行星
            double angle = 2 * Math.PI * (System.currentTimeMillis() % 10000) / 10000;
            double planetX = centerX + orbitRadius * Math.cos(angle + i);
            double planetY = centerY + orbitRadius * Math.sin(angle + i);

            Color planetColor = getPlanetColor(planet.getType());
            gc.setFill(planetColor);
            // 调整行星大小比例，使其更小以适应更紧凑的布局
            double planetSize = Math.max(2, size * 0.1); // 减小行星尺寸
            gc.fillOval(planetX - planetSize, planetY - planetSize,
                    planetSize * 2, planetSize * 2);

            // 如果行星有殖民地，添加标记（只在高缩放级别显示）
            if (planet.getColony() != null && scale > 1.2) {
                drawColonyMarker(planetX, planetY, planetSize, planet.getColony().getFaction());
            }
        }
    }

    private Color getPlanetColor(PlanetType type) {
        switch (type) {
            case TERRA: return Color.GREEN;
            case DESERT: return Color.SANDYBROWN;
            case ARID: return Color.TAN;
            case TUNDRA: return Color.LIGHTBLUE;
            case ICE: return Color.ALICEBLUE;
            case OCEAN: return Color.DARKBLUE;
            case JUNGLE: return Color.DARKGREEN;
            case LAVA: return Color.DARKRED;
            case GAS_GIANT: return Color.ORANGE;
            case BARREN: return Color.DARKGRAY;
            case ASTEROID: return Color.GRAY;
            default: return Color.WHITE;
        }
    }

    private void drawColonyMarker(double x, double y, double size, Faction faction) {
        // 绘制派系颜色的旗帜
        Color factionColor = faction.getColor();
        gc.setFill(factionColor);

        // 绘制三角形旗帜
        double[] flagX = {x + size, x + size * 2, x + size};
        double[] flagY = {y - size, y, y + size};
        gc.fillPolygon(flagX, flagY, 3);
    }

    private void drawVisibility(Hex hex, double centerX, double centerY, double size) {
        float visibility = hex.getVisibility();

        if (visibility < 1.0f) {
            // 绘制战争迷雾
            Color fogColor = Color.rgb(0, 0, 0, 0.8 - visibility * 0.6);
            gc.setFill(fogColor);

            // 绘制六边形形状的迷雾
            double[] xPoints = new double[6];
            double[] yPoints = new double[6];

            for (int i = 0; i < 6; i++) {
                double angle = 2 * Math.PI / 6 * (i + 0.5); // 保持与drawHex一致的角度
                // 使用完整的尺寸绘制迷雾，以完全覆盖六边形
                xPoints[i] = centerX + (hexSize * 0.8 * scale) * Math.cos(angle);
                yPoints[i] = centerY + (hexSize * 0.8 * scale) * Math.sin(angle);
            }

            gc.fillPolygon(xPoints, yPoints, 6);
        }
    }

    private void drawHexHighlight(Hex hex, Color color) {
        CubeCoord coord = hex.getCoord();
        Point2D center = hexGrid.cubeToPixel(coord);

        double screenX = center.getX() * scale + offsetX;
        double screenY = center.getY() * scale + offsetY;
        // 与drawHex保持一致的大小
        double screenSize = hexSize * 0.8 * scale;

        // 绘制高亮边框
        gc.setStroke(color);
        gc.setLineWidth(3);

        double[] xPoints = new double[6];
        double[] yPoints = new double[6];

        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI / 6 * (i + 0.5); // 保持与drawHex一致的角度
            xPoints[i] = screenX + screenSize * Math.cos(angle);
            yPoints[i] = screenY + screenSize * Math.sin(angle);
        }

        gc.strokePolygon(xPoints, yPoints, 6);
    }
}
