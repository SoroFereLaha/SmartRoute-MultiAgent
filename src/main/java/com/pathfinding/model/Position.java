// src/main/java/com/pathfinding/model/Position.java
package com.pathFinding.model;

import java.io.Serializable;
import java.util.Objects; 

public class Position implements Serializable {
    private final int x;
    private final int y;
    private final int time;

    public Position(int x, int y) {
        this(x, y, 0);
    }

    public Position(int x, int y, int time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getTime() { return time; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, time);
    }

    @Override
    public String toString() {
        return String.format("(%d,%d,t=%d)", x, y, time);
    }
}