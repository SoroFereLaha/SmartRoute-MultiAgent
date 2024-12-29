// src/main/java/com/pathfinding/gui/GridWorldGUI.java
package com.pathFinding.gui;

import com.pathFinding.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GridWorldGUI extends JFrame {
    private static final int CELL_SIZE = 40;
    private static final int PADDING = 1;
    private final GridPanel gridPanel;
    private final GridWorld world;
    private final Map<String, Color> agentColors;
    private static final Color[] AGENT_COLORS = {
        new Color(255, 0, 0),    // Rouge
        new Color(0, 0, 255),    // Bleu
        new Color(0, 255, 0),    // Vert
        new Color(255, 165, 0),  // Orange
        new Color(128, 0, 128),  // Violet
        new Color(255, 192, 203),// Rose
        new Color(165, 42, 42),  // Marron
        new Color(0, 255, 255),  // Cyan
        new Color(255, 255, 0),  // Jaune
        new Color(0, 128, 0)     // Vert foncé
    };

    private void drawAgent(Graphics2D g2d, String agentId, Position pos, boolean isGoal) {
        Color color = agentColors.get(agentId);
        int px = pos.getX() * GridPanel.CELL_SIZE;
        int py = pos.getY() * GridPanel.CELL_SIZE;
        
        if (isGoal) {
            g2d.setColor(color);
            g2d.drawOval(px + GridPanel.PADDING + 4, py + GridPanel.PADDING + 4,
                        GridPanel.CELL_SIZE - 2*GridPanel.PADDING - 8, 
                        GridPanel.CELL_SIZE - 2*GridPanel.PADDING - 8);
            // Dessiner X pour marquer l'objectif
            g2d.drawLine(px + 10, py + 10, px + GridPanel.CELL_SIZE - 10, py + GridPanel.CELL_SIZE - 10);
            g2d.drawLine(px + GridPanel.CELL_SIZE - 10, py + 10, px + 10, py + GridPanel.CELL_SIZE - 10);
        } else {
            g2d.setColor(color);
            g2d.fillOval(px + GridPanel.PADDING + 4, py + GridPanel.PADDING + 4,
                        GridPanel.CELL_SIZE - 2*GridPanel.PADDING - 8, 
                        GridPanel.CELL_SIZE - 2*GridPanel.PADDING - 8);
            g2d.setColor(Color.WHITE);
            g2d.drawString(agentId, px + GridPanel.CELL_SIZE/4, py + GridPanel.CELL_SIZE/2);
        }
    }

    public GridWorldGUI(GridWorld world) {
        this.world = world;
        this.agentColors = new ConcurrentHashMap<>();
        
        setTitle("PathFinding Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        gridPanel = new GridPanel();
        add(gridPanel);
        
        pack();
        setLocationRelativeTo(null);
        
        // Assign colors to agents
        int colorIndex = 0;
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA};
        for (String agentId : world.getAgentIds()) {
            agentColors.put(agentId, colors[colorIndex % colors.length]);
            colorIndex++;
        }
    }

    public void refresh() {
        gridPanel.repaint();
    }

    private class GridPanel extends JPanel {
        private static final int CELL_SIZE = 40;
        private static final int PADDING = 1;

        GridPanel() {
            setPreferredSize(new Dimension(
                world.getWidth() * CELL_SIZE,
                world.getHeight() * CELL_SIZE
            ));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw grid and obstacles
            for (int y = 0; y < world.getHeight(); y++) {
                for (int x = 0; x < world.getWidth(); x++) {
                    int px = x * CELL_SIZE;
                    int py = y * CELL_SIZE;
                    
                    if (!world.isValidMove(new Position(x, y))) {
                        g2d.setColor(Color.BLACK);
                    } else {
                        g2d.setColor(Color.WHITE);
                    }
                    g2d.fillRect(px + PADDING, py + PADDING, CELL_SIZE - 2*PADDING, CELL_SIZE - 2*PADDING);
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.drawRect(px + PADDING, py + PADDING, CELL_SIZE - 2*PADDING, CELL_SIZE - 2*PADDING);
                }
            }


            // Draw agents and goals
            for (String agentId : world.getAgentIds()) {
                Position pos = world.getAgentPosition(agentId);
                Position goal = world.getAgentGoal(agentId);
                
                // Draw agent
                drawAgentOrGoal(g2d, agentId, pos, false);
                // Draw goal
                drawAgentOrGoal(g2d, agentId, goal, true);
            }
        }

        private void drawAgentOrGoal(Graphics2D g2d, String agentId, Position pos, boolean isGoal) {
            int px = pos.getX() * CELL_SIZE;
            int py = pos.getY() * CELL_SIZE;
            
            int index = Integer.parseInt(agentId.replace("Agent", "")) - 1;
            Color color = AGENT_COLORS[index % AGENT_COLORS.length];
            
            if (isGoal) {
                g2d.setColor(color);
                g2d.drawOval(px + PADDING + 4, py + PADDING + 4, 
                            CELL_SIZE - 2*PADDING - 8, CELL_SIZE - 2*PADDING - 8);
            } else {
                g2d.setColor(color);
                g2d.fillOval(px + PADDING + 4, py + PADDING + 4, 
                            CELL_SIZE - 2*PADDING - 8, CELL_SIZE - 2*PADDING - 8);
                g2d.setColor(Color.WHITE);
                g2d.drawString(agentId, px + CELL_SIZE/4, py + CELL_SIZE/2);
            }
        }
    }
}
