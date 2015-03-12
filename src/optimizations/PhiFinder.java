package optimizations;

import java.util.*;

import data.Instruction;
import data.Kind;
import data.OperationCodes;
import data.Result;
import datastructures.BasicBlock;
import lexical.Parser;

public class PhiFinder extends Optimization{

	private BasicBlock join;
	private BasicBlock root;
    private String variableName;
    private boolean reachedEnd = false;
    private Result y;
	
	public PhiFinder(Parser p, BasicBlock root, BasicBlock join, String name) {
		super(p);
		this.root = root;
		this.join = join;
		this.variableName = name;
	}

	public Result getOperand(){
		processGraph(root);
		setNotVisited();
		return this.y;
	}
	
	
	
	@Override
	public void visit(BasicBlock node) {
		if(reachedEnd) {
            return;
        }

        if(node.equals(join)) {
            reachedEnd = true;
        }

        if(!node.getDominatedBlocks().contains(join)) {
            return;
        }
        final List<Instruction> instructions = node.getInstructions();
        for (Instruction instruction : instructions) {
            if(instruction.getOpcode() == OperationCodes.move) {
                if(instruction.getX().getVariableName().equals(variableName)) {
                    this.y = instruction.getY();
                }
            } else if(instruction.getOpcode() == OperationCodes.phi) {
                if(instruction.getSymbol().getName().equals(variableName)) {
                    this.y = new Result(Kind.INTERMEDIATE);
                    this.y.setIntermediateLocation(instruction.getInstructionNumber());
                }
            }
        }
		
	}

}
