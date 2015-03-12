package optimizations;

import java.util.*;

import data.Instruction;
import data.Kind;
import data.OperationCodes;
import data.Result;
import datastructures.BasicBlock;
import lexical.Parser;

public class CommonSubexpression extends Optimization {
	
    private Map<Integer, List<InstructionCompartment>> index = new HashMap<Integer, List<InstructionCompartment>>();
    private Map<Integer, Integer> deletedIntermediates = new HashMap<Integer, Integer>();
	
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
		List<InstructionCompartment> anchorObject;
        for (Instruction currentInstruction : node.getInstructions()) //go through all the instructions on the BB
        {
            //preparing current instruction with the changed locations of prior deletions
            //Dont reform them if they are branch statements
            Integer opcode = currentInstruction.getOpcode();

            final Result xCurrent = currentInstruction.getX();
            final Result yCurrent = currentInstruction.getY();

            if ((opcode >= OperationCodes.add && opcode <= OperationCodes.phi) ||
                    (opcode == OperationCodes.kill))//Checking for arithmetic operations along with phi and kill
            {
                if (xCurrent != null) {
                    reformVariable(xCurrent);
                }
                if (yCurrent != null) {
                    reformVariable(yCurrent);
                }

                String currentInstructionString = currentInstruction.toString();
                InstructionCompartment instructionCompartment = new InstructionCompartment(currentInstruction, node);
                if (index.containsKey(opcode)) {
                    //Add opcode to an existing linked list
                    //Printer.debugMessage(((Integer)index).toString()+" ---- "+currentInstructionString);
                    anchorObject = index.get(opcode);
                    //Since we are adding to an existing linked list, we now check for CSE possibilities in the list
                    boolean cse = false;
                    InstructionCompartment instructionList = null;
                    for (InstructionCompartment i : anchorObject) {
                        instructionList = i;
                        String instructionListString = instructionList.getInstruction().toString();

                        if (instructionListString.equals(currentInstructionString) && (instructionList.getBasicBlock().getDominatedBlocks().contains(node) || instructionList.getBasicBlock().equals(node))
                                && currentInstruction.getOpcode() != OperationCodes.phi && currentInstruction.getOpcode() != OperationCodes.kill) {
                            //now that they are identical instructions, im going to check if their values haven't changed.
                            // set: cse = true; : when i know its a common sub expression elim case.
                            //It'll be a CSE elim case if 1. They both have constants as X and Y; 2. They have variables that dont change
                            //checking for constants
                            if (currentInstruction.getOpcode() == OperationCodes.load || currentInstruction.getOpcode() == OperationCodes.store) {
                                //Checking for load or store operations first since they are single operand instructions
                                if (!isVariableKilledBetween(xCurrent, node, instructionList)) {
                                    cse = true;
                                    break;
                                }
                            }

                            if (xCurrent.getKind()!=Kind.VAR && yCurrent.getKind() == Kind.VAR) {
                                if (!isVariableKilledBetween(yCurrent, node, instructionList)) {
                                    cse = true;
                                    //IS A Common sub expression
                                    break;
                                }

                            } else if (xCurrent.getKind() == Kind.VAR && yCurrent.getKind() != Kind.VAR) {
                                if (!isVariableKilledBetween(xCurrent, node, instructionList)) {
                                    cse = true;
                                    //IS A Common sub expression
                                    break;
                                }
                            } else if (xCurrent.getKind() == Kind.VAR && yCurrent.getKind() == Kind.VAR) {
                                if (!isVariableKilledBetween(xCurrent, node, instructionList) && !isVariableKilledBetween(yCurrent, node, instructionList)) {
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
                        anchorObject.add(instructionCompartment);
                        index.put(opcode, anchorObject);


                    } else {
                        //DEAL WITH THE REMOVAL OF THE CURRENT INSTRUCTION
                        System.out.println("WE HAVE FOUND A COMMON SUB EXPRESSION SITUATION");
                        System.out.println("" + currentInstruction.getInstructionNumber() + " " + currentInstructionString);
                        deletedIntermediates.put(currentInstruction.getInstructionNumber(), instructionList.getInstruction().getInstructionNumber());
                        currentInstruction.setDeleted(true, "CSE");
                    }
                } else {
                    //Put the op code in the index index
                    //Printer.debugMessage(((Integer) index).toString()+" ---- "+currentInstructionString);


                    //prep the element in a compartment
                    //Create a new op linked list
                    anchorObject = new ArrayList<InstructionCompartment>();
                    anchorObject.add(instructionCompartment);
                    index.put(opcode, anchorObject);

                }
            }
        }
		
	}
	
	
	private boolean isVariableKilledBetween(Result variable, BasicBlock node,
			InstructionCompartment priorCompartment) {
        List<InstructionCompartment> killAnchorObject;
        if (variable.getKind() == Kind.ARRAY) {
            killAnchorObject = index.get(OperationCodes.kill);
        } else {
            killAnchorObject = index.get(OperationCodes.phi);
            if(killAnchorObject == null) {
                killAnchorObject = index.get(OperationCodes.kill); //global variable changed in function call
            }
        }
        if (killAnchorObject == null) {
            return false;
        }
        ListIterator<InstructionCompartment> InstructionIterator = killAnchorObject.listIterator();
        InstructionCompartment holder;
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


	public class InstructionCompartment {
	    private Instruction instruction;
	    private BasicBlock basicBlock;

	    public InstructionCompartment(Instruction instruction, BasicBlock basicBlock)
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
