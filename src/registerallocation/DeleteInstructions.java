package registerallocation;

import java.util.*;

import data.Instruction;
import data.Kind;
import data.OpCodes;
import data.Result;
import datastructures.BasicBlock;
import lexical.Parser;
import optimizations.Optimization;

public class DeleteInstructions extends Optimization{
	
    private int instructionNumber = 0;
    private Map<Integer, Integer> oldNewLocations = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> deletedLocations = new HashMap<Integer, Integer>();

	public DeleteInstructions(Parser p) {
		super(p);
	}
	
	public void doDeletions(){
		instructionNumber = 0;
        final List<Instruction> instructions = p.getMain().getInstructionList();
        for (Instruction instruction : instructions) {
            if(!instruction.isDeleted()) {
                oldNewLocations.put(instruction.getInstructionNumber(), instructionNumber);
                instructionNumber++;
            } else {
                deletedLocations.put(instruction.getInstructionNumber(), instructionNumber);
            }
        }
        
        processGraph(p.getMain().getCFG().getNextBlock());
        
        final Iterator<Instruction> iterator = instructions.iterator();
        instructionNumber = 0;
        while(iterator.hasNext()) {
            Instruction instruction = iterator.next();
            if(instruction.isDeleted()) {
                iterator.remove();
            } else {
                if (instruction.getOpcode() == OpCodes.phi) {
                    instruction.setX(handlePhiInstructionOperand(instruction.getX()));
                    instruction.setY(handlePhiInstructionOperand(instruction.getY()));
                }
                instruction.setInstructionNumber(instructionNumber++);
            }
        }
        
        setNotVisited(); //CLEANUP
	}

	@Override
	public void visit(BasicBlock node) {
		final List<Instruction> instructions = node.getInstructions();
        final Iterator<Instruction> iterator = instructions.iterator();
        while (iterator.hasNext()) {
            final Instruction instruction = iterator.next();
            final Integer opcode = instruction.getOpcode();
            if(!instruction.isDeleted()) {
                if(opcode == OpCodes.bra) {
                    instruction.setX(updateBranchDestinationTargets(instruction.getX()));
                } else if(opcode >= OpCodes.bne && opcode <= OpCodes.bgt) {
                    instruction.setX(updateIntermediates(instruction.getX()));
                    instruction.setY(updateBranchDestinationTargets(instruction.getY()));
                }  else {
                    instruction.setX(updateIntermediates(instruction.getX()));
                    instruction.setY(updateIntermediates(instruction.getY()));

                    if(instruction.getOpcode() == OpCodes.call) {
                        final List<Result> parameters = instruction.getParameters();
                        if(parameters != null) {
                            Map<Result, Result> parameterMap = new LinkedHashMap<Result, Result>();
                            for (Result parameter : parameters) {
                                parameterMap.put(parameter, updateIntermediates(parameter));
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
            } else {
                iterator.remove();
            }
        }
	}
	
	private Result updateBranchDestinationTargets(Result result) {
        if(result == null) {
            return result;
        }
        if(result.getKind() == Kind.CONSTANT) {
            final Result result1 = new Result(Kind.CONSTANT);
            if(oldNewLocations.get(result.getConstVal()) != null) {
                result1.setConstVal(oldNewLocations.get(result.getConstVal()));
            } else {
                int target = getNextAvailableLocation(result.getConstVal());
                result1.setConstVal(target);
            }
            return result1;
        }
        return result;
    }

    private int getNextAvailableLocation(Integer deletedInstruction) {
        Integer target = deletedLocations.get(deletedInstruction);
        if(target == null) {
            throw new RuntimeException("deletedInstruction [" + deletedInstruction + "] is not mapped to any other potential instructions");
        }
        while (!oldNewLocations.containsValue(target) && target < oldNewLocations.size()) {
            target++;
        }
        return target;
    }


    private Result updateIntermediates(Result result) {
        if(result == null) {
            return result;
        }
        if(result.getKind() == Kind.INTERMEDIATE) {
            if(oldNewLocations.get(result.getIntermediateLocation()) != null) {
                final Result result1 = new Result(Kind.INTERMEDIATE);
                result1.setIntermediateLocation(oldNewLocations.get(result.getIntermediateLocation()));
                return result1;
            }
        }
        return result;
    }


    private Set<Instruction> getSet(Map<Integer, Set<Instruction>> map, Integer key) {
        if(map.get(key) == null) {
            Set<Instruction> instructions = new HashSet<Instruction>();
            map.put(key, instructions);
            return instructions;
        }
        return map.get(key);
    }
    
    private Result handlePhiInstructionOperand(Result result) {
        if(result == null) {
            return result;
        }
        if(result.getKind() == Kind.VAR) {
            final Result result1 = new Result(Kind.VAR);
            result1.setVarName(result.getVariableName());
            final Integer targetOldLocation = result.getLocation();
            if(oldNewLocations.get(targetOldLocation) != null) {
                result1.setLocation(oldNewLocations.get(targetOldLocation));
            }
            return result1;
        }
        return result;
    }

    public Map<Integer, Integer> getOldNewLocations() {
        return oldNewLocations;
    }

}
