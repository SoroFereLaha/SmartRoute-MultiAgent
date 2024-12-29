// src/main/java/com/pathfinding/model/GridWorld.java
package com.pathFinding.model;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.*;
import java.io.Serializable;

public class GridWorld implements Serializable {
    public static final int EMPTY = 0;
    public static final int OBSTACLE = -99;
    
    private final int[][] grid;
    private final int height;
    private final int width;
    private final Map<String, Position> agentPositions;
    private final Map<String, Position> agentGoals;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public GridWorld(int height, int width) {
        this.height = height;
        this.width = width;
        this.grid = new int[height][width];
        this.agentPositions = new HashMap<>();
        this.agentGoals = new HashMap<>();
    }

    public synchronized void addObstacle(int x, int y) {
        if (isValidPosition(x, y)) {
            lock.writeLock().lock();
            try {
                grid[y][x] = OBSTACLE;
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    public boolean hasObstacle(int x, int y) {
        if (!isValidPosition(x, y)) {
            return false;
        }
        return grid[y][x] == OBSTACLE;
    }

    public synchronized boolean addAgent(String agentId, Position start, Position goal) {
        if (!isValidPosition(start.getX(), start.getY()) || !isValidPosition(goal.getX(), goal.getY())) {
            return false;
        }
        if (grid[start.getY()][start.getX()] != EMPTY) {
            return false;
        }
        agentPositions.put(agentId, start);
        agentGoals.put(agentId, goal);
        grid[start.getY()][start.getX()] = 1;
        return true;
    }

    public synchronized boolean moveAgent(String agentId, Position newPos) {
        lock.writeLock().lock();
        try {
            if (!isValidPosition(newPos.getX(), newPos.getY()) || hasObstacle(newPos.getX(), newPos.getY())) {
                return false;
            }
            
            // Don't check current agent's position as occupied
            boolean occupiedByOther = agentPositions.entrySet().stream()
                .filter(e -> !e.getKey().equals(agentId))
                .anyMatch(e -> e.getValue().getX() == newPos.getX() && 
                            e.getValue().getY() == newPos.getY());
                            
            if (occupiedByOther) {
                return false;
            }

            Position oldPos = agentPositions.get(agentId);
            if (oldPos != null) {
                grid[oldPos.getY()][oldPos.getX()] = EMPTY;
                grid[newPos.getY()][newPos.getX()] = 1;
                agentPositions.put(agentId, newPos);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean isOccupied(Position pos) {
        return agentPositions.values().stream()
            .anyMatch(p -> p.getX() == pos.getX() && p.getY() == pos.getY());
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    
    public boolean isValidMove(Position pos) {
        if (!isValidPosition(pos.getX(), pos.getY())) {
            return false;
        }
        return !hasObstacle(pos.getX(), pos.getY());
    }

    public List<Position> getNeighbors(Position pos) {
        List<Position> neighbors = new ArrayList<>();
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        
        for (int[] dir : directions) {
            int newX = pos.getX() + dir[0];
            int newY = pos.getY() + dir[1];
            Position newPos = new Position(newX, newY, pos.getTime() + 1);
            if (isValidMove(newPos)) {
                neighbors.add(newPos);
            }
        }
        return neighbors;
    }

    // Getters
    public Position getAgentPosition(String agentId) {
        return agentPositions.get(agentId);
    }

    public Position getAgentGoal(String agentId) {
        return agentGoals.get(agentId);
    }

    public int getHeight() { return height; }
    public int getWidth() { return width; }
    public Set<String> getAgentIds() { return agentPositions.keySet(); }
}
