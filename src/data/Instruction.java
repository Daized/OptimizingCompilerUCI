package data;

import datastructures.Symbol;

public class Instruction {
	private Symbol symbol;
	private Result x;
	private Result y;
	private final int opcode;
	
	public Instruction(Result x, Result y, int opcode){
		this.x = x;
		this.y = y;
		this.opcode = opcode;
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
}
