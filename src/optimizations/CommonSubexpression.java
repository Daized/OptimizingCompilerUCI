package optimizations;

import java.util.*;

import data.Instruction;
import data.Kind;
import data.OpCodes;
import data.Result;
import datastructures.BasicBlock;
import lexical.Parser;

public class CommonSubexpression extends Optimization {
	
    private Map<Integer, List<InstructionBlockTuple>> index = new HashMap<Integer, List<InstructionBlockTuple>>();
    private Map<Integer, Integer> deletedIntermediates = new HashMap<Integer, Integer>();
    private List<String> excluded = new ArrayList<String>();
    
	public CommonSubexpression(Parser p) {
		super(p);
		// TODO Auto-generated constructor stub
	}

	public void doCommonSubexpressionElimination(){
		processGraph(p.getMain().getCFG().getRoot());
		setNotVisited();
		
	}
	
	@Override
	public void visit(BasicBlock node) {
		List<InstructionBlockTuple> anchorObject;
        boolean ignoreNextInstruction = false;
        String target = "";

        for (Instruction currentInstruction : node.getInstructions()) 
        {
            //preparing current instruction with the changed locations of prior deletions
            //Dont reform them if they are branch statements
            int opcode = currentInstruction.getOpcode();

            Result x = currentInstruction.getX();
            Result y = currentInstruction.getY();

            if ((opcode >= OpCodes.add && opcode <= OpCodes.phi) || (opcode == OpCodes.kill))
            {

                if(opcode == OpCodes.kill) {
                    excluded.add(currentInstruction.getSymbol().getName());
                    continue;
                }
                if(opcode == OpCodes.adda) {
                    for (String name : excluded) {
                        if(name.equals(currentInstruction.getY().getVariableName())){
                            ignoreNextInstruction = true;
                            target = name;
                            break;
                        }
                    }
                }

                if(opcode == OpCodes.load && ignoreNextInstruction) {
                    ignoreNextInstruction = false;
                    final Iterator<String> iterator = excluded.iterator();
                    while (iterator.hasNext()) {
                        final String next = iterator.next();
                        if(next.equals(target)) {
                            iterator.remove();
//                            break;
                        }
                    }
                    continue;
                }
                if (x != null) {
                    reformVariable(x);
                }
                if (y != null) {
                    reformVariable(y);
                }

                String currentInstructionString = currentInstruction.toString();
                InstructionBlockTuple InstructionBlockTuple = new InstructionBlockTuple(currentInstruction, node);
                if (index.containsKey(opcode)) {
                    //Add opcode to an existing linked list
                    //Printer.debugMessage(((Integer)index).toString()+" ---- "+currentInstructionString);
                    anchorObject = index.get(opcode);
                    //Since we are adding to an existing linked list, we now check for CSE possibilities in the list
                    boolean cse = false;
                    InstructionBlockTuple instructionList = null;
                    for (InstructionBlockTuple i : anchorObject) {
                        instructionList = i;
                        String instructionListString = instructionList.getInstruction().toString();

                        if (instructionListString.equals(currentInstructionString) && (instructionList.getBasicBlock().getDominatedBlocks().contains(node) || instructionList.getBasicBlock().equals(node))
                                && currentInstruction.getOpcode() != OpCodes.phi && currentInstruction.getOpcode() != OpCodes.kill) {
                            //if values haven't changed set elimination to true
                            if (currentInstruction.getOpcode() == OpCodes.load || currentInstruction.getOpcode() == OpCodes.store) {
                                //Checking for load or store operations first since they are single operand instructions
                                if (!isVariableKilledBetween(x, node, instructionList)) {

                                		cse = true;
                                        break;

                               
                                }
                            }

                            if (x.getKind()!=Kind.VAR && y.getKind() == Kind.VAR) {
                                if (!isVariableKilledBetween(y, node, instructionList)) {
                                    cse = true;
                                    //IS A Common sub expression
                                    break;
                                }

                            } else if (x.getKind() == Kind.VAR && y.getKind() != Kind.VAR) {
                                if (!isVariableKilledBetween(x, node, instructionList)) {
                                    cse = true;
                                    //IS A Common sub expression
                                    break;
                                }
                            } else if (x.getKind() == Kind.VAR && y.getKind() == Kind.VAR) {
                                if (!isVariableKilledBetween(x, node, instructionList) && !isVariableKilledBetween(y, node, instructionList)) {
                                    cse = true;
                                    //IS A Common sub expression
                                    break;
                                }
                            } else {
                                cse = true;
                                //IS A Common sub expression
                                break;
                            }
                            break;
                        }
                    }
                    if (!cse) {
                        //Finally adding this guy
                        anchorObject.add(InstructionBlockTuple);
                        index.put(opcode, anchorObject);


                    } else {
                        //DEAL WITH THE REMOVAL OF THE CURRENT INSTRUCTION
                        //System.out.println("WE HAVE FOUND A COMMON SUB EXPRESSION SITUATION");
                        //System.out.println("" + currentInstruction.getInstructionNumber() + " " + currentInstructionString);
                        deletedIntermediates.put(currentInstruction.getInstructionNumber(), instructionList.getInstruction().getInstructionNumber());
                        currentInstruction.setDeleted(true, "CSE");
                    }
                } else {
                    //Put the op code in the index index
                    //Printer.debugMessage(((Integer) index).toString()+" ---- "+currentInstructionString);


                    //prep the element in a compartment
                    //Create a new op linked list
                    anchorObject = new ArrayList<InstructionBlockTuple>();
                    anchorObject.add(InstructionBlockTuple);
                    index.put(opcode, anchorObject);

                }
            }
        }
		
	}
	
	
	private boolean isVariableKilledBetween(Result variable, BasicBlock node,
			InstructionBlockTuple priorCompartment) {
        List<InstructionBlockTuple> killAnchorObject;
        if (variable.getKind() == Kind.ARRAY) {
            killAnchorObject = index.get(OpCodes.kill);
        } else {
            killAnchorObject = index.get(OpCodes.phi);
            if(killAnchorObject == null) {
                killAnchorObject = index.get(OpCodes.kill); //global variable changed in function call
            }
        }
        if (killAnchorObject == null) {
            return false;
        }
        ListIterator<InstructionBlockTuple> InstructionIterator = killAnchorObject.listIterator();
        InstructionBlockTuple holder;
        while (InstructionIterator.hasNext()) {
            holder = InstructionIterator.next();
            System.out.println("[139]" + holder.getInstruction().toString());
            System.out.println("a." + variable.getVariableName());
            final Result x = holder.getInstruction().getX();
//            Printer.debugMessage("b." + holder.getInstruction().getSymbol().getName());
            if (x.getKind() == Kind.VAR) {
                if (x.getVariableName().equals(variable.getVariableName())) {
                    if (holder.getBasicBlock().getDominatedBlocks().contains(node) && priorCompartment.getInstruction().getInstructionNumber() > holder.getInstruction().getInstructionNumber()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
	


	private void reformVariable(Result z) {
        if (z.getKind() == Kind.INTERMEDIATE) {
            Integer intermediateLoc = z.getIntermediateLocation();
            if (deletedIntermediates.containsKey(intermediateLoc)) {
                z.setIntermediateLocation(deletedIntermediates.get(intermediateLoc));
            }
        }
		
	}


	public class InstructionBlockTuple {
	    private Instruction instruction;
	    private BasicBlock basicBlock;

	    public InstructionBlockTuple(Instruction instruction, BasicBlock basicBlock)
	    {
	        this.instruction=instruction;
	        this.basicBlock=basicBlock;
	    }


	    public Instruction getInstruction() {
	        return instruction;
	    }


	    public BasicBlock getBasicBlock() {
	        return basicBlock;
	    }
	}

}
