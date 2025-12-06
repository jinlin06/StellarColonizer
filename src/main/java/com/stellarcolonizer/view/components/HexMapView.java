// HexMapView.java - 六边形地图视图组件
package com.stellarcolonizer.view.components;

import com.stellarcolonizer.model.galaxy.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.geometry.Point2D;
import java.util.HashMap;
import java.util.Map;

public class HexMapView extends Pane {

    private Canvas canvas;
    private GraphicsContext gc;

    private HexGrid hexGrid;
    private Faction playerFaction;

    // 视图参数
    private double offsetX = 0;
    private double offsetY = 0;
    private double scale = 1.0;
    private double hexSize = 50.0;

    // 选择状态
    private Hex selectedHex;
    private Map<Hex, Color> highlightedHexes = new HashMap<>();

    // 交互状态
    private boolean isDragging = false;
    private double dragStartX, dragStartY;
    private double dragStartOffsetX, dragStartOffsetY;

    public HexMapView() {
        this.canvas = new Canvas();
        this.gc = canvas.getGraphicsContext2D();

        // 设置画布大小随父容器变化
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

        this.getChildren().add(canvas);

        // 重绘监听
        canvas.widthProperty().addListener((obs, oldVal, newVal) -> draw());
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> draw());

        // 设置交互事件
        setupMouseEvents();
        setupScrollEvents();
    }

    public void setHexGrid(HexGrid hexGrid) {
        this.hexGrid = hexGrid;
        if (hexGrid != null) {
            this.hexSize = hexGrid.getHexSize();
        }
        draw();
    }

    public void setPlayerFaction(Faction playerFaction) {
        this.playerFaction = playerFaction;
        draw();
    }

    public Hex getSelectedHex() {
        return selectedHex;
    }

    public void setSelectedHex(Hex hex) {
        this.selectedHex = hex;
        draw();
    }

    public void highlightHex(Hex hex, Color color) {
        highlightedHexes.put(hex, color);
        draw();
    }

    public void clearHighlights() {
        highlightedHexes.clear();
        draw();
    }

    private void setupMouseEvents() {
        // 点击选择
        this.setOnMouseClicked(this::handleMouseClick);

        // 拖动地图
        this.setOnMousePressed(this::handleMousePressed);
        this.setOnMouseDragged(this::handleMouseDragged);
        this.setOnMouseReleased(this::handleMouseReleased);

        // 鼠标悬停
        this.setOnMouseMoved(this::handleMouseMoved);
    }

    private void setupScrollEvents() {
        this.setOnScroll(this::handleScroll);
    }

    private void handleMouseClick(MouseEvent event) {
        if (hexGrid == null) return;

        // 计算点击位置对应的六边形坐标
        double screenX = event.getX();
        double screenY = event.getY();

        // 转换为世界坐标（考虑偏移和缩放）
        double worldX = (screenX - offsetX) / scale;
        double worldY = (screenY - offsetY) / scale;

        CubeCoord coord = hexGrid.pixelToCube(worldX, worldY);
        Hex clickedHex = hexGrid.getHex(coord);

        if (clickedHex != null) {
            selectedHex = clickedHex;
            draw();

            // 触发选择事件
            fireEvent(new HexSelectedEvent(HexSelectedEvent.HEX_SELECTED, clickedHex));
        }
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.isSecondaryButtonDown()) {
            isDragging = true;
            dragStartX = event.getX();
            dragStartY = event.getY();
            dragStartOffsetX = offsetX;
            dragStartOffsetY = offsetY;
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (isDragging) {
            double deltaX = event.getX() - dragStartX;
            double deltaY = event.getY() - dragStartY;
            offsetX = dragStartOffsetX + deltaX;
            offsetY = dragStartOffsetY + deltaY;
            draw();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        isDragging = false;
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

    private void draw() {
        if (hexGrid == null || canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
            return;
        }

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

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
        double screenSize = hexSize * scale;

        // 计算六边形顶点
        double[] xPoints = new double[6];
        double[] yPoints = new double[6];

        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI / 6 * i;
            xPoints[i] = screenX + screenSize * Math.cos(angle);
            yPoints[i] = screenY + screenSize * Math.sin(angle);
        }

        // 设置填充颜色（基于六边形类型）
        Color fillColor = getHexColor(hex);
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
    }

    private Color getHexColor(Hex hex) {
        // 根据六边形类型返回颜色
        switch (hex.getType()) {
            case EMPTY:
                return Color.rgb(10, 10, 30); // 深空
            case NEBULA:
                return Color.rgb(100, 50, 150); // 紫色星云
            case ASTEROID_FIELD:
                return Color.rgb(80, 80, 80); // 灰色小行星带
            case STAR_SYSTEM:
                return Color.rgb(20, 20, 50); // 深蓝色背景
            case WORMHOLE:
                return Color.rgb(0, 200, 200); // 青色虫洞
            case DARK_NEBULA:
                return Color.rgb(30, 20, 40); // 暗紫色
            default:
                return Color.BLACK;
        }
    }

    private void drawStarSystem(StarSystem system, double centerX, double centerY, double size) {
        // 绘制恒星
        Color starColor = Color.web(system.getStarType().getColor());
        gc.setFill(starColor);

        double starRadius = size * 0.3;
        gc.fillOval(centerX - starRadius, centerY - starRadius,
                starRadius * 2, starRadius * 2);

        // 添加光晕效果
        gc.setFill(Color.web(system.getStarType().getColor(), 0.3));
        double glowRadius = starRadius * 1.5;
        gc.fillOval(centerX - glowRadius, centerY - glowRadius,
                glowRadius * 2, glowRadius * 2);

        // 如果缩放足够大，绘制行星轨道
        if (scale > 0.5) {
            drawPlanetOrbits(system, centerX, centerY, size);
        }
    }

    private void drawPlanetOrbits(StarSystem system, double centerX, double centerY, double size) {
        List<Planet> planets = system.getPlanets();
        int planetCount = planets.size();

        for (int i = 0; i < planetCount; i++) {
            Planet planet = planets.get(i);
            double orbitRadius = size * 0.5 + i * size * 0.2;

            // 绘制轨道
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(0.5);
            gc.strokeOval(centerX - orbitRadius, centerY - orbitRadius,
                    orbitRadius * 2, orbitRadius * 2);

            // 绘制行星
            double angle = 2 * Math.PI * (System.currentTimeMillis() % 10000) / 10000;
            double planetX = centerX + orbitRadius * Math.cos(angle + i);
            double planetY = centerY + orbitRadius * Math.sin(angle + i);

            Color planetColor = getPlanetColor(planet.getType());
            gc.setFill(planetColor);
            double planetSize = Math.max(2, size * 0.1);
            gc.fillOval(planetX - planetSize, planetY - planetSize,
                    planetSize * 2, planetSize * 2);

            // 如果行星有殖民地，添加标记
            if (planet.getColony() != null) {
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
                double angle = 2 * Math.PI / 6 * i;
                xPoints[i] = centerX + size * Math.cos(angle);
                yPoints[i] = centerY + size * Math.sin(angle);
            }

            gc.fillPolygon(xPoints, yPoints, 6);
        }
    }

    private void drawHexHighlight(Hex hex, Color color) {
        CubeCoord coord = hex.getCoord();
        Point2D center = hexGrid.cubeToPixel(coord);

        double screenX = center.getX() * scale + offsetX;
        double screenY = center.getY() * scale + offsetY;
        double screenSize = hexSize * scale;

        // 绘制高亮边框
        gc.setStroke(color);
        gc.setLineWidth(3);

        double[] xPoints = new double[6];
        double[] yPoints = new double[6];

        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI / 6 * i;
            xPoints[i] = screenX + screenSize * Math.cos(angle);
            yPoints[i] = screenY + screenSize * Math.sin(angle);
        }

        gc.strokePolygon(xPoints, yPoints, 6);
    }
}

