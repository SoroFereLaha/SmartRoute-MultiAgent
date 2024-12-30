// src/main/java/com/pathfinding/algorithm/Node.java
package com.pathFinding.algorithm;

import com.pathFinding.model.Position;

public class Node {
    public Position position;
    public Node parent;
    public double gScore;
    public double fScore;

    public Node(Position position) {
        this.position = position;
        this.parent = null;
        this.gScore = Double.POSITIVE_INFINITY;
        this.fScore = Double.POSITIVE_INFINITY;
    }
}