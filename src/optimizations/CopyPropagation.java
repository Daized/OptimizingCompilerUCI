package optimizations;

import java.util.*;

import data.Instruction;
import data.Kind;
import data.OpCodes;
import data.Result;
import datastructures.BasicBlock;
import datastructures.Symbol;
import lexical.Parser;

public class CopyPropagation extends Optimization {

	private Map<String, Result> valueMap = new HashMap<String, Result>();
    private List<String> excludedValues = new ArrayList<String>();
    private Set<Instruction> phiInstructions = new HashSet<Instruction>();
    private Map<Integer, Result> deletedMoves = new HashMap<Integer, Result>();
	
	
	public CopyPropagation(Parser p) {
		super(p);
		
	}

	public void doCopyPropagation(){
		
		processGraph(p.getMain().getCFG().getRoot());
		processDominatingBlocks();
		
		setNotVisited(); //CLEANUP
	}

	@Override
	public void visit(BasicBlock block) {
		final List<Instruction> instructions = block.getInstructions();
        final List<BasicBlock> dominatesOver = block.getDominatedBlocks();
        dominatesOver.addAll(block.getChildren()); //For consecutive blocks that are non dominating of each other
        List<String> removed = new ArrayList<String>();
        final Map<String, Result> mapValues = getNodeValues(removed, block, instructions);
        for (BasicBlock node : dominatesOver) {
            node.updateValueMap(mapValues);
            node.updateExclude(node.getExclude());
            for (String s : removed) {
                node.getExclude().remove(s);
            }
        }
        for (Instruction instruction : phiInstructions) {
            if (instruction.getOpcode() == OpCodes.phi) {
                updatePhiInstruction(instruction);
            }
        }
        valueMap.putAll(mapValues);
    }


    private void updatePhiInstruction(Instruction instruction) {
        Result result = valueMap.get(instruction.getX().getUniqueName());
        if (result != null) {
            instruction.setX(result);
            valueMap.remove(instruction.getX().getUniqueName());
            valueMap.put(instruction.getSymbol().getName(), new Result(instruction.getSymbol()));
        }

        Result resulty = valueMap.get(instruction.getY().getUniqueName());
        if (resulty != null) {
            instruction.setY(resulty);
            valueMap.remove(instruction.getY().getUniqueName());
            valueMap.put(instruction.getSymbol().getName(), new Result(instruction.getSymbol()));
        }
    }

    private Map<String, Result> getNodeValues(List<String> removed, BasicBlock node, List<Instruction> instructions) {
        Map<String, Result> valueMap = node.getMapValues();
        List<String> excludedValues = node.getExclude();
        for (Instruction instruction : instructions) {
            final Integer opcode = instruction.getOpcode();
            if (instruction.getOpcode() == OpCodes.phi) {
                phiInstructions.add(instruction);
                final Result result = new Result(Kind.INTERMEDIATE);
                result.setIntermediateLocation(instruction.getInstructionNumber());
                valueMap.put(instruction.getSymbol().getName(), result);
                continue;
            }
            if (instruction.getOpcode() == OpCodes.kill) {
                final String variableName = instruction.getX().getVariableName();
                valueMap.remove(variableName);
                excludedValues.add(variableName);
                continue;
            } else {
                final Result x = instruction.getX();
                final Result y = instruction.getY();
                final List<Result> parameters = instruction.getParameters();
                if (opcode == OpCodes.move) {
                    final String variableName = instruction.getX().getVariableName();
                    updateMapValues(node.getMapValues(), instruction, variableName, instruction.getX().getUniqueName(), y);
                    excludedValues.remove(variableName);
                    removed.add(variableName);
                } else {
                    if (x != null) {
                        Result result = node.getMapValues().get(x.getVariableName());
                        if (result != null && result.getKind() != Kind.VAR) {
                            instruction.setX(result);
                        } else {
                            if(instruction.getX().getKind() == Kind.VAR) {
                                final Result zeroIfUninitialized = getZeroIfUninitialized(instruction.getX(), excludedValues);
                                if(zeroIfUninitialized == null) {
                                    final Result zero = new Result(Kind.CONSTANT);
                                    zero.setConstVal(0);
                                    instruction.setX(zero);
                                }
                            }
                        }
                    }
                }

                if(parameters != null) {
                    Map<Result, Result> parameterMap = new LinkedHashMap<Result, Result>();
                    for (Result parameter : parameters) {
                        parameterMap.put(parameter, null);

                        Result result = node.getMapValues().get(parameter.getVariableName());
                        if (result != null && result.getKind() != Kind.VAR) {
                            parameterMap.put(parameter, result);
                        } else {
                            if(parameter.getKind() == Kind.VAR) {
                                final Result zeroIfUninitialized = getZeroIfUninitialized(parameter, excludedValues);
                                if(zeroIfUninitialized == null) {
                                    final Result zero = new Result(Kind.CONSTANT);
                                    zero.setConstVal(0);
                                    parameterMap.put(parameter, zero);
                                }
                            }
                        }

                    }



                    List<Result> newParams = new ArrayList<Result>();
                    for (Result result : parameterMap.keySet()) {
                        if(parameterMap.get(result) != null) {
                            newParams.add(parameterMap.get(result));
                        } else {
                            newParams.add(result);
                        }
                    }
                    instruction.setParameters(newParams);
                }
            
                if (y != null) {
                    Result result = node.getMapValues().get(y.getVariableName());
                    if (result != null && result.getKind() != Kind.VAR) {
                        instruction.setY(result);
                    } else {
                        if(instruction.getY().getKind() == Kind.VAR) {
                            final Result zeroIfUninitialized = getZeroIfUninitialized(instruction.getY(), excludedValues);
                            if(zeroIfUninitialized == null) {
                                final Result zero = new Result(Kind.CONSTANT);
                                zero.setConstVal(0);
                                instruction.setY(zero);
                            }
                        }
                    }
                }

            }
        }
        return valueMap;
    }

    private void updateMapValues(Map<String, Result> valueMap, Instruction instruction, String variableName, String uniqueIdentifier, Result y) {
//        if (!excludedValues.contains(variableName)) {

            if (y.getKind() == Kind.VAR) {
                final Result yValue = valueMap.get(y.getVariableName());
                if (yValue != null) {
                    if (yValue.getKind() == Kind.VAR) {
                        updateMapValues(valueMap, instruction, variableName, uniqueIdentifier, yValue);
                        return;
                    }
                    y = yValue;
                }
            }
            valueMap.put(variableName, y);
            valueMap.put(uniqueIdentifier, y);
            instruction.setY(y);
            instruction.setDeleted(true, "CP");
            deletedMoves.put(instruction.getInstructionNumber(), y);
            p.getMain().getSymbolTable().updateSymbol(variableName, y);
            System.out.println("Copying " + variableName + " --> instruction " + instruction.getInstructionNumber() + "");
//        }
        }

    
    public void processDominatingBlocks() {

        final List<Instruction> instructions = p.getMain().getInstructionList();
        Map<Integer, Result> remainingMoves = new HashMap<Integer, Result>();
        final List<Instruction> phis = new ArrayList<Instruction>();
        for (Instruction instruction : instructions) {
            if (instruction.getOpcode() == OpCodes.move && !instruction.isDeleted()) {
                final Result target = instruction.getY();
                final Result x = instruction.getX();
                if (x.getKind() == Kind.VAR) {
                    final Integer key = x.getLocation();
                    remainingMoves.put(key, target);
                    instruction.setDeleted(true, "CP");
                }
            }
            if(instruction.getOpcode() == OpCodes.phi) {
                phis.add(instruction);
            }
        }

        for (Instruction instruction : instructions) {
            Result target = getTarget(remainingMoves, instruction.getX(), instruction);
            if (target != null) {
                instruction.setX(target);
            }
            target = getTarget(remainingMoves, instruction.getY(), instruction);
            if (target != null) {
                instruction.setY(target);
            }
        }

        Map<Integer, Result> update = new HashMap<Integer, Result>();

        for (Instruction instruction : instructions) {
            if (instruction.isDeleted()) {
                if(instruction.getY().getKind() != Kind.VAR) {
                    update.put(instruction.getInstructionNumber(), instruction.getY());
                }
                continue;
            }
            instruction.setX(updateFirstOccurence(instruction.getX()));
            instruction.setY(updateFirstOccurence(instruction.getY()));
        }

        for (Instruction instruction : instructions) {
            instruction.setX(updateDeleted(update, instruction, instruction.getX()));
            instruction.setY(updateDeleted(update, instruction, instruction.getY()));
        }

        for (Instruction phi : phis) {
            if(phi.getX().getKind() == Kind.VAR) {
                final Result x = new Result(Kind.INTERMEDIATE);
                x.setIntermediateLocation(phi.getX().getLocation());
                phi.setX(x);
            }
            if(phi.getY().getKind() == Kind.VAR) {
                final Result y = new Result(Kind.INTERMEDIATE);
                y.setIntermediateLocation(phi.getY().getLocation());
                phi.setY(y);
            }
        }
    }

    private Result updateDeleted(Map<Integer, Result> update, Instruction instruction, Result operand) {
        if (operand == null) {
            return operand;
        }
        
        if(instruction.getOpcode() == OpCodes.phi && operand.getKind() == Kind.VAR) {
            final Result result = update.get(operand.getLocation());
            if(result != null) {
                return result;
            }
        }
        return operand;
    }

    protected Result updateFirstOccurence(Result operand) {
        if (operand != null && operand.getKind() == Kind.VAR) {
            final Symbol recentOccurence = p.getMain().getSymbolTable().getRecentOccurence(operand.getVariableName());
            if(getZeroIfUninitialized(operand, excludedValues) != null) {
                return operand;
            }

            if (recentOccurence.getSSA() != -1) {
                return operand;
            }
            
            final Result zero = new Result(Kind.CONSTANT);
            zero.setConstVal(0);
            return zero;
        }
        return operand;
    }

    private Result getZeroIfUninitialized(Result operand, List<String> excludedValues) {
        String programName = p.getMain().getName();
        final Symbol recentOccurence = p.getMain().getSymbolTable().getRecentOccurence(operand.getVariableName());
        if (!programName.equals("main")) {
            if (recentOccurence.isMainSymbol()) {
                return operand;
            }
            final List<String> argumentNames = p.getMain().getFormalParams();
            for (String argumentName : argumentNames) {
                if (operand.getVariableName().equals(argumentName)) {
                    return operand;
                }
            }
        }
        if(excludedValues.contains(operand.getVariableName())) {
            return operand;
        }
        return null;
    }

    protected Result getTarget(Map<Integer, Result> remainingMoves, Result operand, Instruction instruction) {
        if (instruction.getOpcode() == OpCodes.phi && operand.getKind() == Kind.VAR) {
            final Result result = remainingMoves.get(operand.getLocation());
            if (result != null) {
                return result;
            }
        }

        return null;
    }
}