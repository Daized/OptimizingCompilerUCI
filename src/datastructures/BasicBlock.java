package datastructures;
import java.util.*;

import data.Instruction;
import data.OperationCodes;

public class BasicBlock {
	
	private List<Instruction> instructionList;
	private List<BasicBlock> dominatedBlocks;
	private Map<String, Instruction> phiMap;
	private Set<BasicBlock>  parents;
	private Set<BasicBlock> children;
	private BasicBlock join;
	private BasicBlock left;
	private BasicBlock right;
	private int label;
	private boolean visited;
	
	
	private static volatile Set<BasicBlock> programBlocks = new HashSet<BasicBlock>();
	private static int count = 0;
	
	public BasicBlock(){
		this.instructionList = new ArrayList<Instruction>();
		this.dominatedBlocks = new ArrayList<BasicBlock>();
		this.phiMap = new HashMap<String, Instruction>();
		this.parents = new HashSet<BasicBlock>();
		this.children = new HashSet<BasicBlock>();
		this.label = count++;
		this.visited = false;
		programBlocks.add(this);
		
	}
	
	public void setLeft(BasicBlock left){
		this.left = left;
	}
	
	public void setRight(BasicBlock right){
		this.right = right;
	}
	
	public void setJoin(BasicBlock join){
		this.join = join;
	}
	
	public void addParent(BasicBlock parent){
		parents.add(parent);
	}
	
	public void setVisited(boolean b){
		this.visited = b;
	}
	
	public void addChild(BasicBlock child, boolean dominates){
		children.add(child);
		child.getParents().add(this);
		if (dominates)
			this.addDominatingBlocks(child);
	}
	
	public void addDominatingBlocks(BasicBlock child){
		dominates(child);
		for (BasicBlock b: programBlocks){
			if (b.getDominatedBlocks().contains(this)){
				b.getDominatedBlocks().add(child);
			}
		}
	}
	
	public void dominates(BasicBlock child){
		this.dominatedBlocks.add(child);
	}
	
	public Set<BasicBlock> getChildren(){
		return this.children;
	}
	
	public Set<BasicBlock> getParents(){
		return this.parents;
	}
	
	public void appendInstruction(Instruction instruction){
		this.instructionList.add(instruction);
	}
	
	public int getLabel(){
		return this.label;
	}
	
	public List<BasicBlock> getDominatedBlocks(){
		return this.dominatedBlocks;
	}
	
	public BasicBlock getLeft(){
		return this.left;
	}
	
	public BasicBlock getRight(){
		return this.right;
	}

	public BasicBlock getJoin(){
		return this.join;
	}
	
	public boolean getVisited(){
		return this.visited;
	}
	
	public Collection<Instruction> getPhiInstructions(){
		return phiMap.values();
	}
	
	public Instruction addPhiInstruction(Instruction instruction) {
        int index = 0;
        Instruction nonPhi = null;
        for (Instruction instruction1 : instructionList) {
            if( !(instruction1.getOpcode() == OperationCodes.phi)) {
                nonPhi = instruction1;
                break;
            }
            index++;
        }
        instructionList.add(index, instruction); //PHI should always be added to the top
        phiMap.put(instruction.getSymbol().getName(), instruction);
        return nonPhi;
    }
	
	public List<Instruction> getInstructions(){
		return this.instructionList;
	}

	public Instruction getPhiInstruction(String name) {
		return phiMap.get(name);
	}


}
