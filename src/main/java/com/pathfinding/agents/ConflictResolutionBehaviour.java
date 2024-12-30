// src/main/java/com/pathfinding/agents/ConflictResolutionBehaviour.java
package com.pathFinding.agents;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import com.pathFinding.model.Position;
import java.util.*;

public class ConflictResolutionBehaviour extends CyclicBehaviour {
    private final PathFindingAgent myPathFindingAgent;

    public ConflictResolutionBehaviour(PathFindingAgent agent) {
        super(agent);
        this.myPathFindingAgent = agent;
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage msg = myAgent.receive(mt);
        
        if (msg != null) {
            String content = msg.getContent();
            try {
                // Analyser le contenu du message au format String
                if (content.startsWith("CONFLICT:")) {
                    String[] parts = content.substring(9).split(";");
                    Set<Position> constraintSet = new HashSet<>();
                    
                    for (String part : parts) {
                        String[] coords = part.split(",");
                        if (coords.length >= 3) {
                            int x = Integer.parseInt(coords[0]);
                            int y = Integer.parseInt(coords[1]);
                            int time = Integer.parseInt(coords[2]);
                            constraintSet.add(new Position(x, y, time));
                        }
                    }
                    
                    myPathFindingAgent.updateConstraints(constraintSet);
                    myPathFindingAgent.recalculatePath();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }
}