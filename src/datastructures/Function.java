package datastructures;
import java.util.*;

import data.Instruction;
import data.OperationCodes;
import lexical.Parser;

public class Function {
	
	private String name;
	private List<Instruction> instructionList;
	private ControlFlowGraph functionCFG;
	private SymbolTable symbolTable;
	
	public Function(String name){
		this.instructionList = new ArrayList<Instruction>();
		this.functionCFG = new ControlFlowGraph();
		this.name = name;
		this.symbolTable = new SymbolTable();
	}

	public void appendInstruction(Instruction instruction){
		this.instructionList.add(instruction);
		this.functionCFG.getNextBlock().appendInstruction(instruction);
	}
	
	public int appendKillInstruction(Instruction instruction, int index){
		if (instruction.getOpcode() != OperationCodes.kill){
			throw new RuntimeException("Not a kill instruction");
		}
		
		BasicBlock joinBlock = functionCFG.getNextBlock().getJoin();
		if (index != -1)
			instructionList.add(index, instruction);
		else
			instructionList.add(instruction);
		joinBlock.appendInstruction(instruction);
		
		return instructionList.size();
	}
	
	public void appendPhiInstruction(Instruction phiInstruction, Instruction instruction){
		if (phiInstruction.getOpcode() != OperationCodes.phi){
			throw new RuntimeException("Not a phi instruction");
		}
		int location = instructionList.indexOf(instruction);
		
		if (location != -1){
				instructionList.add(location, phiInstruction);
		}
		else {
			instructionList.add(phiInstruction);
		}
		
	}
	
	public ControlFlowGraph getCFG(){
		return this.functionCFG;
	}
	
	public String getName(){
		return this.name;
	}
	
	public SymbolTable getSymbolTable(){
		return this.symbolTable;
	}
	
	public int getProgramCounter(){
		return this.instructionList.size();
	}

	public void fixUp(int fixuploc) {
		for (Instruction instruction: instructionList){
			if (instruction.getInstructionNumber() == fixuploc){
				instruction.fixUpInstruction(getProgramCounter());
				break;
			}
		}	
		
	}
	

	
}
