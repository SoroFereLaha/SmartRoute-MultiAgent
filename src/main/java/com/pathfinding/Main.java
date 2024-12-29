// mvn exec:java -Dexec.mainClass="com.pathfinding.Main"
// src/main/java/com/pathfinding/Main.java
package com.pathfinding;

import java.util.List;
import java.util.ArrayList;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.*;
import com.pathFinding.model.*;
import com.pathFinding.gui.GridWorldGUI;
import java.util.logging.*;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static ContainerController cc;
    private static final List<AgentController> agents = new ArrayList<>();

    public static void main(String[] args) {
        try {
            // Setup logging
            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            logger.setLevel(Level.ALL);
            logger.addHandler(consoleHandler);
            
            // Initialize JADE container
            Runtime rt = Runtime.instance();
            rt.setCloseVM(true);
            Profile p = new ProfileImpl();
            p.setParameter(Profile.MAIN_HOST, "localhost");
            ContainerController cc = rt.createMainContainer(p);
            
            // Create world
            GridWorld world = new GridWorld(10, 10);
            
            // Initialize GUI
            GridWorldGUI gui = new GridWorldGUI(world);
            gui.setVisible(true);
            
            /*// Add obstacles
            world.addObstacle(2, 2);
            world.addObstacle(2, 3);
            world.addObstacle(3, 2);

            // Ajoutez avant de créer les agents:
            world.addAgent("Agent1", new Position(0, 0), new Position(9, 9));
            world.addAgent("Agent2", new Position(9, 0), new Position(0, 9));
                        
            // Create agent arguments
            Object[] agent1Args = {
                new Position(0, 0),
                new Position(9, 9),
                world,
                gui  // Pass GUI reference to agent
            };
            
            Object[] agent2Args = {
                new Position(9, 0),
                new Position(0, 9),
                world,
                gui  // Pass GUI reference to agent
            };

            // Gestion propre de l'arrêt
            rt.invokeOnTermination(() -> {
                try {
                    for (AgentController agent : agents) {
                        agent.kill();
                    }
                    cc.kill();
                } catch (Exception e) {
                    logger.severe("Error during shutdown: " + e.getMessage());
                }
            });
            
            // Start agents
            logger.info("Starting agents...");
            AgentController ac1 = cc.createNewAgent("Agent1", 
                "com.pathFinding.agents.PathFindingAgent", agent1Args);
            AgentController ac2 = cc.createNewAgent("Agent2", 
                "com.pathFinding.agents.PathFindingAgent", agent2Args);
                
            ac1.start();
            ac2.start();
            logger.info("Agents started successfully");*/

            // Plus d'obstacles
            int[][] obstacles = {
                {2, 2}, {2, 3}, {3, 2}, {5, 5}, {5, 6},
                {6, 5}, {7, 7}, {3, 7}, {7, 3}, {4, 4}
            };
            
            for (int[] obs : obstacles) {
                world.addObstacle(obs[0], obs[1]);
            }
            
            // Configuration des agents
            Object[][] agentConfigs = {
                {"Agent1", new Position(0, 0), new Position(9, 9)},
                {"Agent2", new Position(9, 0), new Position(0, 9)},
                {"Agent3", new Position(0, 9), new Position(9, 0)},
                {"Agent4", new Position(9, 9), new Position(0, 0)},
                {"Agent5", new Position(4, 0), new Position(4, 9)}
            };
            
            for (Object[] config : agentConfigs) {
                String agentName = (String) config[0];
                Position start = (Position) config[1];
                Position goal = (Position) config[2];
                
                world.addAgent(agentName, start, goal);
                
                Object[] agentArgs = {start, goal, world, gui};
                AgentController ac = cc.createNewAgent(agentName, 
                    "com.pathFinding.agents.PathFindingAgent", agentArgs);
                ac.start();
                agents.add(ac);
            }

            logger.info("Agents started successfully");
            
        } catch (Exception e) {
            logger.severe("Error starting application: " + e.getMessage());
            System.exit(1);
        }
    }
}