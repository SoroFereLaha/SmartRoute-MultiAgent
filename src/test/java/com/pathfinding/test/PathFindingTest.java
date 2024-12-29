// src/test/java/com/pathfinding/test/PathFindingTest.java
package com.pathFinding.test;

import com.pathFinding.model.*;
import com.pathFinding.algorithm.AStarPathFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class PathFindingTest {
    private GridWorld world;
    private AStarPathFinder pathFinder;

    @BeforeEach 
    void setUp() {
        world = new GridWorld(10, 10);
        pathFinder = new AStarPathFinder(world);
    }

    @Test
    void testBasicPathFinding() {
        Position start = new Position(0, 0);
        Position goal = new Position(2, 2);
        Set<Position> constraints = new HashSet<>();

        List<Position> path = pathFinder.findPath(start, goal, constraints);
        
        assertNotNull(path);
        assertEquals(start, path.get(0));
        assertEquals(goal, path.get(path.size() - 1));
        assertEquals(5, path.size()); // Shortest path should be 5 steps
    }

    @Test
    void testPathWithObstacles() {
        world.addObstacle(1, 1);
        assert world.hasObstacle(1, 1) : "L'obstacle n'a pas été correctement placé";
        
        Position start = new Position(0, 0);
        Position goal = new Position(2, 2);
        Set<Position> constraints = new HashSet<>();
        
        List<Position> path = pathFinder.findPath(start, goal, constraints);
        assertNotNull(path);
        
        // Vérifier que le chemin évite l'obstacle
        assertTrue(path.stream().noneMatch(p -> p.getX() == 1 && p.getY() == 1), 
                "Le chemin ne devrait pas passer par l'obstacle");
        
        // Vérifier que le chemin est valide (commence au départ et arrive au but)
        assertEquals(start, path.get(0), "Le chemin doit commencer à la position de départ");
        assertEquals(goal, path.get(path.size()-1), "Le chemin doit finir à la position d'arrivée");
    }

    @Test
    void testNoPathPossible() {
        // Surround goal with obstacles
        world.addObstacle(1, 1);
        world.addObstacle(1, 2);
        world.addObstacle(2, 1);
        world.addObstacle(2, 2);
        
        Position start = new Position(0, 0);
        Position goal = new Position(1, 1);
        Set<Position> constraints = new HashSet<>();

        List<Position> path = pathFinder.findPath(start, goal, constraints);
        
        assertNull(path); // No path should be possible
    }

    @Test
    void testGridWorldConstraints() {
        GridWorld world = new GridWorld(5, 5);
        assertFalse(world.isValidPosition(-1, 0));
        assertFalse(world.isValidPosition(5, 0));
        assertTrue(world.isValidPosition(0, 0));
        assertTrue(world.isValidPosition(4, 4));
    }

    @Test
    void testAgentMovement() {
        GridWorld world = new GridWorld(5, 5);
        Position start = new Position(0, 0);
        Position goal = new Position(4, 4);
        
        assertTrue(world.addAgent("agent1", start, goal));
        assertEquals(start, world.getAgentPosition("agent1"));
        
        Position newPos = new Position(0, 1);
        assertTrue(world.moveAgent("agent1", newPos));
        assertEquals(newPos, world.getAgentPosition("agent1"));
    }
}
