package datastructures;
import java.util.*;

import data.Instruction;
import lexical.Parser;

public class Function {
	
	private String name;
	private List<Instruction> instructionList;
	private ControlFlowGraph functionCFG;
	
	public Function(String name){
		this.instructionList = new ArrayList<Instruction>();
		this.functionCFG = new ControlFlowGraph();
		this.name = name;
	}

	public void appendInstruction(Instruction instruction){
		this.instructionList.add(instruction);
	}
	
	public ControlFlowGraph getCFG(){
		return this.functionCFG;
	}
	
	public String getName(){
		return this.name;
	}
	

	
}
