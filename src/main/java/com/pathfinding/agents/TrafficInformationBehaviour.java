// src/main/java/com/pathfinding/agents/TrafficInformationBehaviour.java
package com.pathFinding.agents;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import com.pathFinding.model.Position;
import java.util.Set;
import java.util.logging.Logger;

public class TrafficInformationBehaviour extends CyclicBehaviour {
    private static final Logger logger = Logger.getLogger(TrafficInformationBehaviour.class.getName());
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage msg = myAgent.receive(mt);
        
        if (msg != null) {
            String content = msg.getContent();
            if (content.startsWith("TRAFFIC:")) {
                String[] parts = content.substring(8).split(",");
                try {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    Position trafficPosition = new Position(x, y);
                    
                    // Update traffic information in the PathFindingAgent
                    if (myAgent instanceof PathFindingAgent) {
                        PathFindingAgent agent = (PathFindingAgent) myAgent;
                        Set<Position> trafficInfo = agent.getTrafficInfo();
                        trafficInfo.add(trafficPosition);
                        
                        // Remove old traffic information after some time
                        agent.addBehaviour(new jade.core.behaviours.WakerBehaviour(agent, 5000) {
                            @Override
                            protected void onWake() {
                                trafficInfo.remove(trafficPosition);
                            }
                        });
                    }
                } catch (NumberFormatException e) {
                    logger.severe("Error parsing traffic position: " + e.getMessage());
                }
            }
        } else {
            block();
        }
    }
}