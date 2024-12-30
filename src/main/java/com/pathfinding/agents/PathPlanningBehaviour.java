// src/main/java/com/pathfinding/agents/PathPlanningBehaviour.java
package com.pathFinding.agents;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import com.pathFinding.model.Position;

public class PathPlanningBehaviour extends CyclicBehaviour {
    private final PathFindingAgent myPathFindingAgent;

    public PathPlanningBehaviour(PathFindingAgent agent) {
        super(agent);
        this.myPathFindingAgent = agent;
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage msg = myAgent.receive(mt);
        
        if (msg != null) {
            myPathFindingAgent.recalculatePath();
        } else {
            block();
        }
    }
}