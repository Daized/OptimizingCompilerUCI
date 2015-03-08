package datastructures;
import java.util.*;

import data.Instruction;

public class BasicBlock {
	
	private List<Instruction> instructionList = new ArrayList<Instruction>();
	private Set<BasicBlock>  parents;
	private Set<BasicBlock> children;
	private BasicBlock join;
	private BasicBlock left;
	private BasicBlock right;
	private int label;
	private boolean visited;
	
	
	private static Set<BasicBlock> programBlocks = new HashSet<BasicBlock>();
	private static int count = 0;
	
	public BasicBlock(){
		this.instructionList = new ArrayList<Instruction>();
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
	
	public void setVisited(){
		this.visited = true;
	}
	
	public void addChild(BasicBlock child){
		children.add(child);
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


}
