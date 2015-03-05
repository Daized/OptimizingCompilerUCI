package data;

import datastructures.Symbol;

public class Instruction {
	private Symbol symbol;
	private Result x;
	private Result y;
	private int instructionNumber;
	private final int opcode;
	
	public Instruction(Result x, Result y, int opcode, int instructionNumber){
		this.x = x;
		this.y = y;
		this.opcode = opcode;
		this.instructionNumber = instructionNumber;
	}

	public Result getX(){
		return this.x;
	}
	
	public Result getY(){
		return this.y;
	}
	
	public Symbol getSymbol(){
		return this.symbol;
	}
	
	@Override
	public String toString(){
		//TODO: Implement toString for instruction
		return null;
	}
}
