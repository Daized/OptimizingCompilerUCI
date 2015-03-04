package datastructures;

import data.Instruction;

public class ControlFlowGraph {
	
	private BasicBlock root;
	private BasicBlock next;
	
	public ControlFlowGraph(){
		root = new BasicBlock();
		next = root;
	}
	
	public void addInstruction(Instruction instruction){
		next.addInstruction(instruction);
	}
	
	public BasicBlock getNextBlock(){
		return this.next;
	}

	public void setNextBlock(BasicBlock next){
		this.next = next;
	}
	
	public BasicBlock getRoot(){
		return this.root;
	}
}
