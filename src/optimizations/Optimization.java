package optimizations;

import java.util.*;

import datastructures.BasicBlock;
import lexical.Parser;

public abstract class Optimization {

	protected Parser p;
	protected Set<BasicBlock> visitedBlocks = new HashSet<BasicBlock>();
	
	public Optimization(Parser p){
		this.p = p;
	}
	
	public Parser getParser(){
		return this.p;
	}
	
	public void setNotVisited(){
		for (BasicBlock node: visitedBlocks){
			node.setVisited(false);
		}
	}
	
    public void processGraph(BasicBlock node)
    {
        if (node == null) {
            return;
        }
        if(!node.getVisited()) {
            visit(node);
            visitedBlocks.add(node);
            node.setVisited(true);
        }

        for(BasicBlock n: node.getChildren())
        {
            if(!n.getVisited()) {
                processGraph(n);
            }
        }
    }
    
    public abstract void visit(BasicBlock node);
	
}
