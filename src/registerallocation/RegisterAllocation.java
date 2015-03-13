package registerallocation;

import java.util.*;

import data.Instruction;
import data.Kind;
import data.OpCodes;
import data.Result;
import datastructures.BasicBlock;
import optimizations.Optimization;
import lexical.Helper;
import lexical.Parser;

public class RegisterAllocation extends Optimization {
	
	private InterferenceGraph graph;
	private Stack<IGNode> nodeStack = new Stack<IGNode>();
	private Map<Integer, Integer> registerInfo = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> registerInfoAfterUpdate = new HashMap<Integer, Integer>();
	private Set<IGNode> nodes = new HashSet<IGNode>();
	private Set<Instruction> deletedPhis = new HashSet<Instruction>();
	
	public RegisterAllocation(Parser p, InterferenceGraph ig){
		super(p);
		this.graph = ig;
	}

	@Override
	public void visit(BasicBlock node) {
		final List<Instruction> _instructions = node.getInstructions();
        final List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.addAll(_instructions); 
        for (Instruction phi : instructions) {
            if(phi.getOpcode() != OpCodes.phi) {
                continue;
            }
            final Result operand1 = phi.getX();
            final Result operand2 = phi.getY();

            IGNode phiNode = graph.getNodeMap().get(phi.getInstructionNumber());
            if(phiNode == null) {
                phiNode = new IGNode(phi.getInstructionNumber()); //Need to do more here
                graph.addNode(phiNode);
            }

            if(graph.interference(operand1, operand2)) {
                introduceMove(phiNode, operand1, node.getLeft());
                introduceMove(phiNode, operand2, node.getRight());
            } else {
                if (graph.interference(phiNode, operand1)) {
                    introduceMove(phiNode, operand1, node.getLeft());
                }

                if (graph.interference(phiNode, operand2)) {
                    introduceMove(phiNode, operand2, node.getRight());
                }
            }
            coalesce(phiNode, node, operand1, operand2);
            phi.setDeleted(true, "coalesced");
            deletedPhis.add(phi);
        }
		
	}
	
	public void coalesce(IGNode phiNode, BasicBlock node, Result operand1, Result operand2) {

        if(operand1.getKind() == Kind.INTERMEDIATE) {
            graph.coalesce(phiNode, graph.getNodeMap().get(operand1.getIntermediateLocation()));
        }
        if(operand1.getKind() == Kind.VAR) {
            graph.coalesce(phiNode, graph.getNodeMap().get(operand1.getLocation()));
        }
        if(operand2.getKind() == Kind.VAR) {
            graph.coalesce(phiNode, graph.getNodeMap().get(operand2.getLocation()));
        }
        if(operand2.getKind() == Kind.INTERMEDIATE) {
            graph.coalesce(phiNode, graph.getNodeMap().get(operand2.getIntermediateLocation()));
        }

//        if(operand1.isConstant()) {
//            introduceMove(phiNode, operand1, node.getLeft());
//        }
//        if(operand2.isConstant()) {
//            introduceMove(phiNode, operand2, node.getRight());
//        }
    }

    protected void introduceMove(IGNode phiNode, Result interferingResult, BasicBlock targetNode) {
        final BasicBlock oldCurrent = p.getMain().getCFG().getNextBlock();
        final Result x = new Result(Kind.REG); 
        List<Instruction> instructions = targetNode.getInstructions();
        p.getMain().getCFG().setNextBlock(targetNode);
        int targetIndex = 0;
        if(!instructions.isEmpty()) {
            final Instruction instruction = instructions.get(instructions.size() - 1);
            targetIndex = p.getMain().getInstructionList().indexOf(instruction);
            if(instruction.getOpcode() >= OpCodes.bra && instruction.getOpcode() <= OpCodes.bgt) {
                targetIndex--;
            }
        }
        final Instruction addedInstruction = Helper.addInstruction(OpCodes.move, p.getMain(), x, interferingResult, targetIndex +1);
        p.getMain().getCFG().setNextBlock(oldCurrent);
        phiNode.addToMoveInstructions(addedInstruction);
    }

    public Set<IGNode> getNodes() {
        return nodes;
    }

    public Set<Instruction> getDeletedPhis() {
        return deletedPhis;
    }
	
	public void doRegisterAllocation() {
		processGraph(p.getMain().getCFG().getRoot());
		setNotVisited();

        graph.costSort();
        final Iterator<IGNode> costIterator = graph.getNodes().iterator();
        while (costIterator.hasNext()) {
            final IGNode next = costIterator.next();
            removeFromGraph(costIterator,next);
            nodeStack.push(next);
        }

        int colorNumber = 0;
        final List<IGNode> existingNodes = graph.getNodes();
        while (!nodeStack.isEmpty()) {
            IGNode top = nodeStack.pop();
            if(existingNodes.isEmpty()) {
                existingNodes.add(top);
                updateRegisterInfo(top, colorNumber, existingNodes);
                colorNumber++;
            } else {
                existingNodes.add(top);
                final Set<IGNode> neighbors = top.getNeighbors();
                for (IGNode neighbor : neighbors) {
                    neighbor.addNeighbor(top);
                }
                int existingColor = -1;
                for (IGNode existingNode : existingNodes) {
                    if(!existingNode.equals(top) && !graph.interference(existingNode, top)) {
                        existingColor = registerInfo.get(existingNode.getNodeId()); //$TODO$ taking too much time: needs to be optimized
                        break;
                    }
                }
                if(existingColor != -1) {
                    updateRegisterInfo(top, existingColor, existingNodes);
                } else {
                    updateRegisterInfo(top, colorNumber, existingNodes);
                    colorNumber++;
                }
            }
        }
        System.out.println("Used '" + colorNumber +"' color(s).");

        final List<Instruction> instructions = p.getMain().getInstructionList();
        final Iterator<Instruction> iterator = instructions.iterator();
        int i = 0;
        final Set<Instruction> deletedPhis = getDeletedPhis();
        List<Integer> deletedPhiLocations = new ArrayList<Integer>();
        for (Instruction deletedPhi : deletedPhis) {
            deletedPhiLocations.add(deletedPhi.getInstructionNumber());
        }
//
        for (Instruction instruction : instructions) {
            instruction.setX(phiReference(deletedPhiLocations, instruction.getX()));
            instruction.setY(phiReference(deletedPhiLocations, instruction.getY()));
            if(instruction.getOpcode() == OpCodes.call) {
                final List<Result> parameters = instruction.getParameters();
                if(parameters != null) {
                    Map<Result, Result> parameterMap = new LinkedHashMap<Result, Result>();
                    for (Result parameter : parameters) {
                        parameterMap.put(parameter, phiReference(deletedPhiLocations, parameter));
                    }

                    List<Result> newParams = new ArrayList<Result>();
                    for (Result result : parameterMap.keySet()) {
                        if (parameterMap.get(result) != null) {
                            newParams.add(parameterMap.get(result));
                        } else {
                            newParams.add(result);
                        }
                    }
                    instruction.setParameters(newParams);
                }
            }
        }
        
        RemoveInstructions reorder = new RemoveInstructions(p);
        reorder.doDeletions();
        final Map<Integer, Integer> oldNewLocations = reorder.getOldNewLocations();
        for (Integer old : registerInfo.keySet()) {
            final Integer newLocation = oldNewLocations.get(old);
            if(newLocation != null) {
                registerInfoAfterUpdate.put(newLocation, registerInfo.get(old));
            }
        }
        Map<Integer, Integer> moveRegisters = new HashMap<Integer, Integer>();
        for (Instruction instruction : p.getMain().getInstructionList()) {
            if(instruction.getOpcode() == OpCodes.move) {
                if(instruction.getX().getKind() != Kind.REG) {
                    System.out.println("ERROR EROOR");
                }
                moveRegisters.put(instruction.getInstructionNumber(), instruction.getX().getRegNo());
            }
        }

        for (Instruction instruction : p.getMain().getInstructionList()) {
            if(instruction.getX() != null && instruction.getX().getKind() == Kind.INTERMEDIATE) {
                final Integer integer = moveRegisters.get(instruction.getX().getIntermediateLocation());
                if(integer != null) {
                    final Result x = new Result(Kind.REG);
                    x.setRegno(integer);
                    instruction.setX(x);
                }
            }
            if(instruction.getY() != null && instruction.getY().getKind() == Kind.INTERMEDIATE) {
                final Integer integer = moveRegisters.get(instruction.getY().getIntermediateLocation());
                if(integer != null) {
                    final Result x = new Result(Kind.REG);
                    x.setRegno(integer);
                    instruction.setY(x);
                }
            }

            if(instruction.getOpcode() == OpCodes.call) {
                final List<Result> parameters = instruction.getParameters();
                if(parameters != null) {
                    Map<Result, Result> parameterMap = new LinkedHashMap<Result, Result>();
                    for (Result parameter : parameters) {
                        parameterMap.put(parameter, updateIntermediates(moveRegisters, parameter, instruction));
                    }

                    List<Result> newParams = new ArrayList<Result>();
                    for (Result result : parameterMap.keySet()) {
                        if (parameterMap.get(result) != null) {
                            newParams.add(parameterMap.get(result));
                        } else {
                            newParams.add(result);
                        }
                    }
                    instruction.setParameters(newParams);
                }
            }
        }

        for (Integer integer : registerInfoAfterUpdate.keySet()) {
            System.out.println("Allocating register ["+registerInfoAfterUpdate.get(integer)+"] for "+ integer);
        }

    }
	
	private Result updateIntermediates(Map<Integer, Integer> moveRegisters, Result parameter, Instruction instruction) {
        final Integer integer = moveRegisters.get(instruction.getY().getIntermediateLocation());
        if(integer != null) {
            final Result x = new Result(Kind.REG);
            x.setRegno(integer);
            return x;
        }
        return parameter;
    }

    protected Result phiReference(List<Integer> deletedPhiLocations, Result operand) {
        if(operand != null && operand.getKind() == Kind.INTERMEDIATE) {
            if(deletedPhiLocations.contains(operand.getIntermediateLocation())) {
                final Result reg = new Result(Kind.REG);
                reg.setRegno(registerInfo.get(operand.getIntermediateLocation()));
                return reg;
            }
        }
        return operand;
    }

    protected void sortAndAddToStack(List<IGNode> clusteredNodes) {
        Collections.sort(clusteredNodes, new Comparator<IGNode>() {
            @Override
            public int compare(IGNode o1, IGNode o2) {
                return o1.getClustered().size() - o2.getClustered().size();
            }
        });

        for (IGNode clusteredNode : clusteredNodes) {
            nodeStack.push(clusteredNode);
        }
    }

    protected void removeFromGraph(Iterator<IGNode> nodesIterator, IGNode next) {
        final Iterator<IGNode> neighborsRemoverIterator = graph.getNodes().iterator();
        while (neighborsRemoverIterator.hasNext()) {
            final IGNode targetNode = neighborsRemoverIterator.next();
            targetNode.getNeighbors().remove(next);
        }
        nodesIterator.remove();
    }

    protected void updateRegisterInfo(IGNode top, int colorNumber, List<IGNode> existingNodes) {
        if(registerInfo.get(top.getNodeId()) != null) {
            return; //Already colored
        }
        registerInfo.put(top.getNodeId(), colorNumber);
        final Set<IGNode> clustered = top.getClustered();
        for (IGNode graphNode : clustered) {
            registerInfo.put(graphNode.getNodeId(), colorNumber);
            existingNodes.add(graphNode);
        }
        final Set<Instruction> moveInstructions = top.getMoveInstructions();
        for (Instruction moveInstruction : moveInstructions) {
            moveInstruction.getX().setRegno(colorNumber);
        }
    }

    public Map<Integer, Integer> getRegisterInfoAfterUpdate() {
        return registerInfoAfterUpdate;
    }



}
