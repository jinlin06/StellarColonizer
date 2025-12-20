package com.stellarcolonizer.model.galaxy;

import javafx.geometry.Point2D;
import java.util.*;

public class HexGrid {

    public static final CubeCoord[] CUBE_DIRECTIONS = {
            new CubeCoord(1, -1, 0), new CubeCoord(1, 0, -1), new CubeCoord(0, 1, -1),
            new CubeCoord(-1, 1, 0), new CubeCoord(-1, 0, 1), new CubeCoord(0, -1, 1)
    };

    private Map<CubeCoord, Hex> hexMap;
    private int radius;
    private double hexSize;

    public HexGrid(int radius, double hexSize) {
        this.radius = radius;
        this.hexSize = hexSize;
        this.hexMap = new HashMap<>();
        generateGrid();
    }

    private void generateGrid() {
        for (int q = -radius; q <= radius; q++) {
            int r1 = Math.max(-radius, -q - radius);
            int r2 = Math.min(radius, -q + radius);

            for (int r = r1; r <= r2; r++) {
                CubeCoord coord = new CubeCoord(q, r, -q - r);
                Hex hex = new Hex(coord);
                hexMap.put(coord, hex);
            }
        }
    }

    public Hex getHex(CubeCoord coord) {
        return hexMap.get(coord);
    }

    public Hex getHexAt(int q, int r) {
        return getHex(new CubeCoord(q, r, -q - r));
    }

    public List<Hex> getNeighbors(Hex hex) {
        List<Hex> neighbors = new ArrayList<>();
        CubeCoord coord = hex.getCoord();

        for (CubeCoord dir : CUBE_DIRECTIONS) {
            CubeCoord neighborCoord = coord.add(dir);
            Hex neighbor = hexMap.get(neighborCoord);
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    public List<Hex> getHexesInRange(Hex center, int range) {
        List<Hex> results = new ArrayList<>();

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = Math.max(-range, -dx - range); dy <= Math.min(range, -dx + range); dy++) {
                int dz = -dx - dy;
                CubeCoord coord = center.getCoord().add(new CubeCoord(dx, dy, dz));
                Hex hex = hexMap.get(coord);
                if (hex != null) {
                    results.add(hex);
                }
            }
        }

        return results;
    }

    public Point2D cubeToPixel(CubeCoord coord) {
        // 增加六边形之间的间隙，使用更大的间距因子
        double spacingFactor = 1.2; // 20%的间隙（原来是1.1）
        double x = hexSize * spacingFactor * (Math.sqrt(3) * coord.q + Math.sqrt(3)/2 * coord.r);
        double y = hexSize * spacingFactor * (3.0/2 * coord.r);
        return new Point2D(x, y);
    }

    public CubeCoord pixelToCube(double x, double y) {
        // 在像素到立方体坐标转换中也要考虑相同的间距因子
        double spacingFactor = 1.2;
        double q = (Math.sqrt(3)/3 * x - 1.0/3 * y) / (hexSize * spacingFactor);
        double r = (2.0/3 * y) / (hexSize * spacingFactor);
        return roundCube(q, r, -q - r);
    }

    private CubeCoord roundCube(double q, double r, double s) {
        int rq = (int) Math.round(q);
        int rr = (int) Math.round(r);
        int rs = (int) Math.round(s);

        double qDiff = Math.abs(rq - q);
        double rDiff = Math.abs(rr - r);
        double sDiff = Math.abs(rs - s);

        if (qDiff > rDiff && qDiff > sDiff) {
            rq = -rr - rs;
        } else if (rDiff > sDiff) {
            rr = -rq - rs;
        } else {
            rs = -rq - rr;
        }

        return new CubeCoord(rq, rr, rs);
    }

    public List<Hex> getAllHexes() {
        return new ArrayList<>(hexMap.values());
    }

    public int getRadius() { return radius; }
    public double getHexSize() { return hexSize; }
}