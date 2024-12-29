// src/main/java/com/pathfinding/agents/CoordinatorAgent.java
package com.pathFinding.agents;

import jade.lang.acl.MessageTemplate;
import java.util.*;
import com.pathFinding.model.*;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.*;
import java.util.logging.Logger;

public class CoordinatorAgent extends Agent {
    private static final Logger logger = Logger.getLogger(CoordinatorAgent.class.getName());
    private GridWorld world;
    private Map<String, List<Position>> paths;
    private final Object pathLock = new Object();
    private List<String> agents;
    private Map<String, Set<Position>> conflicts;

    @Override
    protected void setup() {
        world = new GridWorld(10, 10); // Default size
        paths = new HashMap<>();
        agents = new ArrayList<>();
        conflicts = new HashMap<>();

        // Initialize world with obstacles
        initializeWorld();
        
        // Create agents
        createAgents();

        addBehaviour(new TickerBehaviour(this, 1000) {
            protected void onTick() {
                detectAndResolveConflicts();
            }
        });
    }

    // Nouvelle méthode pour recevoir les chemins
    private class PathReceivingBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    String agentId = msg.getSender().getLocalName();
                    @SuppressWarnings("unchecked")
                    List<Position> path = (List<Position>) msg.getContentObject();
                    synchronized(pathLock) {
                        paths.put(agentId, path);
                    }
                } catch (Exception e) {
                    logger.severe("Error processing path: " + e.getMessage());
                }
            } else {
                block();
            }
        }
    }

    private void initializeWorld() {
        // Add some obstacles
        world.addObstacle(2, 2);
        world.addObstacle(2, 3);
        world.addObstacle(3, 2);
        
        // Add agent start and goal positions
        world.addAgent("Agent1", new Position(0, 0), new Position(9, 9));
        world.addAgent("Agent2", new Position(0, 9), new Position(9, 0));
    }

    private void createAgents() {
        try {
            AgentContainer container = getContainerController();
            
            // Create PathfindingAgent for each agent in the world
            for (String agentId : world.getAgentIds()) {
                Position start = world.getAgentPosition(agentId);
                Position goal = world.getAgentGoal(agentId);
                
                Object[] args = new Object[]{start, goal, world};
                AgentController ac = container.createNewAgent(
                    agentId,
                    "com.pathFinding.agents.PathFindingAgent",
                    args
                );
                ac.start();
                agents.add(agentId);
                logger.info("Created agent: " + agentId);
            }
        } catch (Exception e) {
            logger.severe("Error creating agents: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void detectAndResolveConflicts() {
        // Clear previous conflicts
        conflicts.clear();
        
        // Check for path conflicts between agents
        for (int i = 0; i < agents.size(); i++) {
            String agent1 = agents.get(i);
            List<Position> path1 = paths.get(agent1);
            
            if (path1 == null) continue;
            
            for (int j = i + 1; j < agents.size(); j++) {
                String agent2 = agents.get(j);
                List<Position> path2 = paths.get(agent2);
                
                if (path2 == null) continue;
                
                // Find positions where paths intersect
                Set<Position> conflictPositions = findPathConflicts(path1, path2);
                
                if (!conflictPositions.isEmpty()) {
                    // Store conflicts for each agent
                    conflicts.computeIfAbsent(agent1, k -> new HashSet<>())
                            .addAll(conflictPositions);
                    conflicts.computeIfAbsent(agent2, k -> new HashSet<>())
                            .addAll(conflictPositions);
                }
            }
        }
        
        // Notify agents of conflicts
        notifyAgentsOfConflicts();
    }

        // Méthode améliorée de détection des conflits
    private Set<Position> findPathConflicts(List<Position> path1, List<Position> path2) {
        Set<Position> conflictPositions = new HashSet<>();
        int maxLength = Math.max(path1.size(), path2.size());
        
        for (int i = 0; i < maxLength; i++) {
            Position pos1 = i < path1.size() ? path1.get(i) : path1.get(path1.size() - 1);
            Position pos2 = i < path2.size() ? path2.get(i) : path2.get(path2.size() - 1);
            
            if (pos1.equals(pos2)) {
                conflictPositions.add(new Position(pos1.getX(), pos1.getY(), i));
            }
            
            if (i > 0) {
                Position prev1 = i - 1 < path1.size() ? path1.get(i - 1) : path1.get(path1.size() - 1);
                Position prev2 = i - 1 < path2.size() ? path2.get(i - 1) : path2.get(path2.size() - 1);
                
                if (pos1.equals(prev2) && pos2.equals(prev1)) {
                    conflictPositions.add(new Position(pos1.getX(), pos1.getY(), i));
                    conflictPositions.add(new Position(pos2.getX(), pos2.getY(), i));
                }
            }
        }
        return conflictPositions;
    }

    private void notifyAgentsOfConflicts() {
        for (Map.Entry<String, Set<Position>> entry : conflicts.entrySet()) {
            String agentId = entry.getKey();
            Set<Position> agentConflicts = entry.getValue();
            
            if (!agentConflicts.isEmpty()) {
                // Create message to inform agent of conflicts
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID(agentId, AID.ISLOCALNAME));
                
                try {
                    // Convert Set<Position> to an ArrayList or an array
                    Position[] conflictArray = agentConflicts.toArray(new Position[0]);
                    msg.setContentObject(conflictArray); // Ensure serializability
                    send(msg);
                    logger.info("Notified agent " + agentId + " of conflicts");
                } catch (Exception e) {
                    logger.severe("Error sending conflict notification to " + agentId + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    protected void takeDown() {
        logger.info("Coordinator agent " + getAID().getName() + " terminating.");
    }
}