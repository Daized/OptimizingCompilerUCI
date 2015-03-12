package registerallocation;

import java.util.*;

import data.Instruction;

public class IGNode {
	
	private int nodeId;
    private Set<IGNode> neighbors = new HashSet<IGNode>();
    private Set<IGNode> clustered = new HashSet<IGNode>();
    private Set<Instruction> moveInstructions = new HashSet<Instruction>();

    public IGNode(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }


    public Set<IGNode> getNeighbors() {
        return neighbors;
    }

    public void addNeighbor(IGNode neighbor) {
        neighbors.add(neighbor);
    }

    @Override
    public String toString() {
        return "IGNode: " + String.valueOf(nodeId);
    }

    public void addToCluster(IGNode node) {
        clustered.add(node);
    }

    public Set<IGNode> getClustered() {
        return clustered;
    }

    public void addToMoveInstructions(Instruction instruction) {
        moveInstructions.add(instruction);
    }

    public Set<Instruction> getMoveInstructions() {
        return moveInstructions;
    }

}
