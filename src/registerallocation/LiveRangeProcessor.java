package registerallocation;

import java.util.*;

import data.Instruction;
import data.Kind;
import data.OperationCodes;
import data.Result;
import datastructures.BasicBlock;
import lexical.Parser;
import optimizations.Optimization;

public class LiveRangeProcessor extends Optimization{

	public LiveRangeProcessor(Parser p) {
		super(p);
		// TODO Auto-generated constructor stub
	}
	
	public void processLiveRanges(){
		processGraph(p.getMain().getCFG().getRoot());
        setNotVisited();
	}

	@Override
    public void visit(BasicBlock node) {
        Set<Integer> liveRanges = new HashSet<Integer>();
        final Set<BasicBlock> children = node.getChildren();
        if (node.getKind() != Kind.LOOPHEAD) {
            handleUnvisitedChildren(children);
        }
        for (BasicBlock child : children) {
            liveRanges.addAll(child.getLiveRanges());
        }

        final List<Instruction> instructions = node.getInstructions();
        for (int i = instructions.size() - 1; i >= 0; i--) {
            final Instruction instruction = instructions.get(i);
            final Integer opcode = instruction.getOpcode();
            final Integer operandCount = OperationCodes.getOperandCount(opcode);
            if (operandCount > 0) {
                updateLiveRange(instruction, instruction.getX(), liveRanges);
            }
            if (operandCount > 1) {
                updateLiveRange(instruction, instruction.getY(), liveRanges);
            }
            instruction.addToLiveRanges(liveRanges); //Not used at this point
        }

        System.out.println("Live Ranges for BB:[" + node.getLabel() + "] are {");
        for (Integer liveRange : liveRanges) {
            System.out.println(String.valueOf(liveRange) + ",");
        }
        System.out.println("}");
        handleWhileBodyBlock(node, liveRanges);
        node.setLiveRanges(liveRanges); //Used at this point
        if (node.getKind() == Kind.LOOPHEAD) {
            node.setVisited(true);
            handleUnvisitedChildren(children);
        }
    }

    protected void handleWhileBodyBlock(BasicBlock node, Set<Integer> liveRanges) {
        if (node.getKind() == Kind.LOOPBODY) {
            node.setBlockKind(null);
            BasicBlock whileHead = getWhileHead(node.getParents());
            final Set<BasicBlock> allParents = whileHead.getParents();
            Set<BasicBlock> parent = new HashSet<BasicBlock>();
            parent.add(node);
            whileHead.setParents(parent);
            reverseProcessSubGraph(whileHead, node);
            whileHead.setParents(allParents);
            liveRanges.addAll(whileHead.getLiveRanges());
            node.setBlockKind(Kind.LOOPBODY);
        }
    }

    protected BasicBlock getWhileHead(Set<BasicBlock> children) {
        BasicBlock whileHead = null;

        for (BasicBlock child : children) {
            if (child.getKind() == Kind.LOOPHEAD) {
                whileHead = child;
            }
        }

        return whileHead;
    }

    protected void handleUnvisitedChildren(Set<BasicBlock> children) {
        for (BasicBlock child : children) {
            if (!child.getVisited()) {
                visit(child);
                child.setVisited(true);
                addVisitedBlock(child);
            }
        }
    }

    private void updateLiveRange(Instruction instruction, Result result, Set<Integer> liveRanges) {
        if (result.getKind() == Kind.INTERMEDIATE) {
            liveRanges.add(result.getIntermediateLocation());
        }
        if(instruction.getOpcode() == OperationCodes.phi && result.getKind() == Kind.VAR) {
            liveRanges.add(result.getLocation());
        }
        liveRanges.remove(instruction.getInstructionNumber());
    }

}
