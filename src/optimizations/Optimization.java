package optimizations;

import java.util.*;

import datastructures.BasicBlock;
import lexical.Parser;

public abstract class Optimization {

	protected Parser p;
	protected Set<BasicBlock> visitedBlocks = new HashSet<BasicBlock>();
	protected Set<BasicBlock> visitedBlocks2 = new HashSet<BasicBlock>();
	
	public Optimization(Parser p){
		this.p = p;
	}
	
	public Parser getParser(){
		return this.p;
	}
	
	public Set<BasicBlock> getVisitedBlocks(){
		return this.visitedBlocks;
	}
	
	public void addVisitedBlock(BasicBlock block){
		this.visitedBlocks.add(block);
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
    
    public void reverseProcessGraph(BasicBlock node)
    {
        if (node == null) {
            return;
        }
        if(!node.getVisited()) {
            visit(node);
            visitedBlocks.add(node);
            node.setVisited(true);
        }

        for(BasicBlock n: node.getParents())
        {
            if(!n.getVisited()) {
                processGraph(n);
            }
        }
    }
    
    public void processSubGraph(BasicBlock node, BasicBlock endNode)
    {
        if (node == null || node.equals(endNode)) {
            return;
        }
        if(!visitedBlocks2.contains(node)) {
            visitedBlocks2.add(node);
            visit(node);
        }

        for(BasicBlock n: node.getChildren())
        {
            if(!visitedBlocks2.contains(n)) {
                processSubGraph(n, endNode);
            }
        }
    }
    
    public void reverseProcessSubGraph(BasicBlock node, BasicBlock endNode)
    {
        if (node == null || node.equals(endNode)) {
            return;
        }
        if(!visitedBlocks2.contains(node)) {
            visitedBlocks2.add(node);
            visit(node);
        }

        for(BasicBlock n: node.getParents())
        {
            if(!visitedBlocks2.contains(n)) {
                processSubGraph(n, endNode);
            }
        }
    }
    
    public abstract void visit(BasicBlock node);
	
}
