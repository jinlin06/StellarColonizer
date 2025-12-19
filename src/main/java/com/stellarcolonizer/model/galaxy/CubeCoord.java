package com.stellarcolonizer.model.galaxy;

import java.util.Objects;

public class CubeCoord {
    public final int q;
    public final int r;
    public final int s;

    public CubeCoord(int q, int r, int s) {
        this.q = q;
        this.r = r;
        this.s = s;
    }

    public CubeCoord add(CubeCoord other) {
        return new CubeCoord(q + other.q, r + other.r, s + other.s);
    }

    public CubeCoord subtract(CubeCoord other) {
        return new CubeCoord(q - other.q, r - other.r, s - other.s);
    }

    public double distance(CubeCoord other) {
        return subtract(other).length();
    }

    public double length() {
        return (Math.abs(q) + Math.abs(r) + Math.abs(s)) / 2.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CubeCoord cubeCoord = (CubeCoord) o;
        return q == cubeCoord.q && r == cubeCoord.r && s == cubeCoord.s;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r, s);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", q, r, s);
    }
}
