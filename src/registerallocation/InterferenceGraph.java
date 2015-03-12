package registerallocation;

import java.util.*;

import data.Kind;
import data.Instruction;
import data.OperationCodes;
import data.Result;
import datastructures.BasicBlock;
import lexical.Parser;
import optimizations.Optimization;

public class InterferenceGraph extends Optimization{
	
	private List<IGNode> graphNodes = new ArrayList<IGNode>();
	private Map<Integer, IGNode> nodeMap = new HashMap<Integer, IGNode>();
	private IGNode root;

	public InterferenceGraph(Parser p) {
		super(p);
		// TODO Auto-generated constructor stub
	}
	
	public void makeInterferenceGraph(){
		processGraph(p.getMain().getCFG().getRoot());
		setNotVisited();
	}
	
	@Override
	public void visit(BasicBlock node){
		List<Instruction> instructions = node.getInstructions();
	    for (Instruction instruction : instructions) {
	        Set<Integer> liveRanges = instruction.getLiveRanges();
	        if(instruction.getOpcode() == OperationCodes.phi) {

	            }
	            if(liveRanges.isEmpty()) {
	                continue;
	            }
	            List<IGNode> nodes = new ArrayList<IGNode>();
	            for (Integer liveRange : liveRanges) {
	                final IGNode existing = nodeMap.get(liveRange);
	                final IGNode gNode = existing != null ? existing : new IGNode(liveRange);
	                nodes.add(gNode);
	                addNode(gNode);
	            }
	            for (IGNode node1 : nodes) {
	                for (IGNode node2 : nodes) {
	                    if (!node1.equals(node2)) {
	                        node1.addNeighbor(node2);
	                    }
	                }
	            }

	            if (root == null) {
	                root = nodes.get(0);
	                setRoot(nodes.get(0));
	            }
	    }
	}
	
	public Map<Integer, IGNode> getNodeMap(){
		return this.nodeMap;
	}
	
	public void setRoot(IGNode root){
		this.root = root;
	}
	
	public void addNode(IGNode node){
		if(node==null){
			return;
		}
		if(graphNodes.indexOf(node) != -1){
			return;
		}
		
		graphNodes.add(node);
		nodeMap.put(node.getNodeId(), node);
	}
	
	public void costSort(){
		 Collections.sort(graphNodes, new Comparator<IGNode>() {
	            @Override
	            public int compare(IGNode o1, IGNode o2) {
	                return o1.getNeighbors().size() - o2.getNeighbors().size();
	            }
	            //$TODO$ add the loop level weight
	        });
	}
	
	public void sortDescendingByClusterSize() {
        Collections.sort(graphNodes, new Comparator<IGNode>() {
            @Override
            public int compare(IGNode o1, IGNode o2) {
                return o2.getClustered().size() - o1.getClustered().size();
            }
            //$TODO$ add the loop level weight
        });
    }
	
	protected boolean interference(Result x, Result y){
		if (x.getKind() == Kind.CONSTANT || y.getKind() == Kind.CONSTANT)
			return false;
		IGNode m = getNodeForOperand(x);
		IGNode n = getNodeForOperand(y);
		
		return interference(m, n);
	}
	
	protected boolean interference(IGNode n, Result y) {
        if(y.getKind() == Kind.CONSTANT) {
            return true;
        }
        final IGNode operandNode = getNodeForOperand(y);
        return interference(n, operandNode);
	}
    

	private IGNode getNodeForOperand(Result x) {
		int node1key = getSearchKey(x);
        IGNode nodeForId = nodeMap.get(node1key);
        return nodeForId;
	}

	private int getSearchKey(Result x) {
		if(x.getKind() != Kind.INTERMEDIATE && x.getKind()!= Kind.VAR) {
            return -1;
        }
        if(x.getKind() == Kind.INTERMEDIATE) {
            return x.getIntermediateLocation();
        } else {
            return x.getLocation();
        }
	}

	protected boolean interference(IGNode m, IGNode n) {
		if(m.equals(n)) {
            return false;
        }
        final Set<IGNode> nodeNeighbors = m.getNeighbors();
        final Set<IGNode> operandNeighbors = n.getNeighbors();

        if(nodeNeighbors.contains(n)) {
            return true;
        }

        if(operandNeighbors.contains(m)) {
            return true;
        }

        return false;
	}
	
	public Set<IGNode> coalesce(IGNode node1, IGNode node2) {
        final Set<IGNode> node1Neighbors = node1.getNeighbors();
        final Set<IGNode> node2Neighbors = node2.getNeighbors();
        node1Neighbors.addAll(node2Neighbors);
        for (IGNode node : graphNodes) {
            if(node.getNeighbors().contains(node2)) {
                node.addNeighbor(node1);
            }
        }
        node1.addToCluster(node2);

        Set<Instruction> moveInstructions = node2.getMoveInstructions();
        for (Instruction moveInstruction : moveInstructions) {
            node2.addToMoveInstructions(moveInstruction);
        }
        graphNodes.remove(node2);
        return node1.getClustered();
    }
	
	public List<IGNode> getNodes() {
        return graphNodes;
    }

}
