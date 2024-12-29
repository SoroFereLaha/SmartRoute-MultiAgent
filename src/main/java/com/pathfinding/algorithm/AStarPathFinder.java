// src/main/java/com/pathfinding/algorithm/AStarPathFinder.java
package com.pathFinding.algorithm;

import com.pathFinding.model.*;
import java.util.*;

public class AStarPathFinder {
    private static final int MAX_ITERATIONS = 1000; // Évite les boucles infinies
    private int iterations;
    private final GridWorld world;
    
    public AStarPathFinder(GridWorld world) { 
        this.world = world;
    }
    
    public List<Position> findPath(Position start, Position goal, Set<Position> constraints) {
        iterations = 0;
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<Position, Node> allNodes = new HashMap<>();
        
        Node startNode = new Node(start);
        startNode.gScore = 0;
        startNode.fScore = heuristic(start, goal);
        
        openSet.add(startNode);
        allNodes.put(start, startNode);
        
        while (!openSet.isEmpty() && iterations++ < MAX_ITERATIONS) {
            Node current = openSet.poll();
            
            if (isGoalReached(current.position, goal)) {
                return reconstructPath(current);
            }
            
            for (Position neighbor : world.getNeighbors(current.position)) {
                if (isConstraintViolated(neighbor, constraints, current.gScore)) {
                    continue;
                }
                
                double tentativeGScore = current.gScore + 1;
                Node neighborNode = allNodes.computeIfAbsent(neighbor, Node::new);
                
                if (tentativeGScore < neighborNode.gScore) {
                    neighborNode.parent = current;
                    neighborNode.gScore = tentativeGScore;
                    neighborNode.fScore = tentativeGScore + heuristic(neighbor, goal);
                    
                    openSet.add(neighborNode);
                }
            }
        }
        
        return null;
    }

    private boolean isConstraintViolated(Position pos, Set<Position> constraints, double time) {
        return constraints.stream()
            .anyMatch(c -> c.getX() == pos.getX() && 
                         c.getY() == pos.getY() && 
                         Math.abs(c.getTime() - time) < 1.0);
    }

    private double heuristic(Position a, Position b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
    
    private boolean isGoalReached(Position current, Position goal) {
        return current.getX() == goal.getX() && current.getY() == goal.getY();
    }
    
    private List<Position> reconstructPath(Node node) {
        List<Position> path = new ArrayList<>();
        while (node != null) {
            path.add(0, node.position);
            node = node.parent;
        }
        return path;
    }
    
    private static class Node {
        Position position;
        Node parent;
        double gScore = Double.POSITIVE_INFINITY;
        double fScore = Double.POSITIVE_INFINITY;
        
        Node(Position position) {
            this.position = position;
        }
    }
}

