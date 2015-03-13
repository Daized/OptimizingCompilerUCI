package datastructures;
import java.util.*;

import data.Instruction;
import data.Kind;
import data.OpCodes;
import data.Result;
import lexical.Parser;

public class Function {
	
	protected static final Result FP = new Result(Kind.FRAME_POINTER);
	private String name;
	private List<Instruction> instructionList;
	private ControlFlowGraph functionCFG;
	private SymbolTable symbolTable;
	private List<String> formalParameters; // Empty in main
	
	public Function(String name){
		this.instructionList = new ArrayList<Instruction>();
		this.functionCFG = new ControlFlowGraph();
		this.name = name;
		this.symbolTable = new SymbolTable();
		this.formalParameters = new ArrayList<String>();
	}

	public void appendInstruction(Instruction instruction){
		this.instructionList.add(instruction);
		this.functionCFG.getNextBlock().appendInstruction(instruction);
	}
	
	public Result getFP(){
		return this.FP;
	}
	
	public void appendInstruction(Instruction instruction, int index){
		this.instructionList.add(index, instruction);
		//this.functionCFG.getNextBlock().appendInstruction(instruction, index);
	}
	
	
	public int appendKillInstruction(Instruction instruction, int index){
		if (instruction.getOpcode() != OpCodes.kill){
			throw new RuntimeException("Not a kill instruction");
		}
		
		BasicBlock joinBlock = functionCFG.getNextBlock().getJoin();
		if (joinBlock != null){
			if (index != -1)
				instructionList.add(index, instruction);
			else
				instructionList.add(instruction);
			joinBlock.appendInstruction(instruction);
		}
		else {
			if(index != -1) {
                instructionList.add(index, instruction);
            } else {
                instructionList.add(instruction);
            }
            if(instruction.getOpcode() != OpCodes.phi) {
                getCFG().addInstruction(instruction);
            }
			
		}
		return instructionList.size();
	}
	
	public void appendPhiInstruction(Instruction phiInstruction, Instruction instruction){
		if (phiInstruction.getOpcode() != OpCodes.phi){
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

	public List<Instruction> getInstructionList() {
		return this.instructionList;
	}

	public List<String> getFormalParams() {
		return this.formalParameters;
	}
	

	
}
