// src/main/java/com/pathfinding/agents/PathfindingAgent.java
package com.pathFinding.agents;

import com.pathFinding.algorithm.AStarPathFinder;
import com.pathFinding.model.*;
import com.pathFinding.gui.GridWorldGUI;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.*;
import java.util.logging.Logger;

public class PathFindingAgent extends Agent {
    private static final Logger logger = Logger.getLogger(PathFindingAgent.class.getName());
    private Position currentPosition;
    private Position goalPosition;
    private GridWorld world;
    private List<Position> path;
    private Set<Position> constraints;
    private AStarPathFinder pathFinder;
    private GridWorldGUI gridWorldGUI;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length >= 4) {
            currentPosition = (Position) args[0];
            goalPosition = (Position) args[1];
            world = (GridWorld) args[2];
            gridWorldGUI = (GridWorldGUI) args[3];
            constraints = new HashSet<>();
            pathFinder = new AStarPathFinder(world);
            
            logger.info(getLocalName() + " initialized at " + currentPosition + ", goal: " + goalPosition);
            addBehaviour(new PathPlanningBehaviour());
            addBehaviour(new ConflictResolutionBehaviour());
            // Initial path planning
            addBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    path = pathFinder.findPath(currentPosition, goalPosition, constraints);
                    if (path != null) {
                        addBehaviour(new MoveAlongPathBehaviour(PathFindingAgent.this, 1000));
                        logger.info(getLocalName() + " found initial path");
                    } else {
                        logger.warning(getLocalName() + " could not find initial path");
                    }
                }
            });
        } else {
            logger.severe(getLocalName() + " missing required arguments");
            doDelete();
        }
    }

    private class PathPlanningBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            
            if (msg != null) {
                path = pathFinder.findPath(currentPosition, goalPosition, constraints);
                logger.info(getLocalName() + " replanning path");
                
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                try {
                    // Convert the path to an array to ensure serializability
                    Position[] pathArray = path.toArray(new Position[0]);
                    myAgent.send(reply);
                } catch (Exception e) {
                    logger.severe(getLocalName() + " error sending path: " + e.getMessage());
                }
            } else {
                block(1000);
            }
        }
    }

    private class ConflictResolutionBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            
            if (msg != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Set<Position> newConstraints = (Set<Position>) msg.getContentObject();
                    constraints.addAll(newConstraints);
                    logger.info(getLocalName() + " received new constraints");
                    
                    // Replan path with new constraints
                    path = pathFinder.findPath(currentPosition, goalPosition, constraints);
                    if (path != null) {
                        addBehaviour(new MoveAlongPathBehaviour(PathFindingAgent.this, 1000));
                        logger.info(getLocalName() + " found new path after constraints");
                    }
                } catch (Exception e) {
                    logger.severe(getLocalName() + " error processing constraints: " + e.getMessage());
                }
            } else {
                block();
            }
        }
    }

    private class MoveAlongPathBehaviour extends TickerBehaviour {
        private int pathIndex = 0;
        private int retryCount = 0;
        private static final int MAX_RETRIES = 5;
        
        public MoveAlongPathBehaviour(Agent a, long period) {
            super(a, period);
        }
        
        @Override
        protected void onTick() {
            if (path == null || pathIndex >= path.size()) {
                stop();
                return;
            }

            Position nextPosition = path.get(pathIndex);
            boolean moved = world.moveAgent(getLocalName(), nextPosition);

            // Force GUI refresh regardless of move success
            if (gridWorldGUI != null) {
                gridWorldGUI.refresh();
            }
            
            if (moved) {
                currentPosition = nextPosition;
                pathIndex++;
                retryCount = 0;
                
                if (currentPosition.equals(goalPosition)) {
                    logger.info(getLocalName() + " reached goal position");
                    stop();
                }
            } else {
                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    logger.warning(getLocalName() + " recalculating path after " + MAX_RETRIES + " failed attempts");
                    // Replan path
                    path = pathFinder.findPath(currentPosition, goalPosition, constraints);
                    pathIndex = 0;
                    retryCount = 0;

                    if (path == null) {}
                    logger.warning(getLocalName() + " could not find alternative path");
                    stop();
                }
            }
        }
    }

    @Override
    protected void takeDown() {
        logger.info(getLocalName() + " terminating.");
    }
}