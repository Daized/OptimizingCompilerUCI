package optimizations;

import java.util.*;

import datastructures.BasicBlock;
import lexical.Parser;

public class CommonSubexpression extends Optimization {
	
    //private Map<Integer, List<InstructionCompartment>> index = new HashMap<Integer, List<InstructionCompartment>>();
    private Map<Integer, Integer> deletedIntermediates = new HashMap<Integer, Integer>();
	
	public CommonSubexpression(Parser p) {
		super(p);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void visit(BasicBlock node) {
		// TODO Auto-generated method stub
		
	}

}
