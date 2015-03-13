package optimizations;

import java.util.*;

import data.Instruction;
import data.Kind;
import data.OperationCodes;
import data.Result;
import datastructures.BasicBlock;
import datastructures.Symbol;
import lexical.Parser;

public class CopyPropagation extends Optimization {

	private Map<String, Result> valueMap = new HashMap<String, Result>();
    private List<String> exclude = new ArrayList<String>();
    private Set<Instruction> phiInstructions = new HashSet<Instruction>();
    private Map<Integer, Result> deletedMoves = new HashMap<Integer, Result>();
	
	
	public CopyPropagation(Parser p) {
		super(p);
		
	}

	public void doCopyPropagation(){
		List<Symbol> symbolList = p.getMain().getSymbolTable().getSymbolList();
		
		
		
		processGraph(p.getMain().getCFG().getRoot());
		processDominatingBlocks();
		
		setNotVisited(); //CLEANUP
	}

	@Override
	public void visit(BasicBlock node) {
		final List<Instruction> instructions = node.getInstructions();
        final List<BasicBlock> dominatesOver = node.getDominatedBlocks();
        List<String> removed = new ArrayList<String>();
        final Map<String, Result> thisNodeValues = processInstructions(removed, node, instructions);
        for (BasicBlock basicBlock : dominatesOver) {
            basicBlock.updateValueMap(thisNodeValues);
            basicBlock.updateExclude(node.getExclude());
            for (String s : removed) {
                node.getExclude().remove(s);
        }
        }
        for (Instruction instruction : phiInstructions) {
            if (instruction.getOpcode() == OperationCodes.phi) {
                updatePhiInstruction(instruction);
            }
        }
        valueMap.putAll(thisNodeValues);
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

    private Map<String, Result> processInstructions(List<String> removed, BasicBlock basicBlock, List<Instruction> instructions) {
        Map<String, Result> valueMap = basicBlock.getMapValues();
        List<String> exclude = basicBlock.getExclude();
        for (Instruction instruction : instructions) {
            final Integer opcode = instruction.getOpcode();
            if (instruction.getOpcode() == OperationCodes.phi) {
                phiInstructions.add(instruction);
                final Result result = new Result(Kind.INTERMEDIATE);
                result.setIntermediateLocation(instruction.getInstructionNumber());
                valueMap.put(instruction.getSymbol().getName(), result);
                continue;
            }
            if (instruction.getOpcode() == OperationCodes.kill) {
                final String variableName = instruction.getX().getVariableName();
                valueMap.remove(variableName);
                exclude.add(variableName);
                continue;
            } else {
                final Result x = instruction.getX();
                final Result y = instruction.getY();
                final List<Result> parameters = instruction.getParameters();
                if (opcode == OperationCodes.move) {
                    final String variableName = instruction.getX().getVariableName();
                    updateValueMap(basicBlock.getMapValues(), instruction, variableName, instruction.getX().getUniqueName(), y);
                    exclude.remove(variableName);
                    removed.add(variableName);
                } else {
                    if (x != null) {
                        Result result = basicBlock.getMapValues().get(x.getVariableName());
                        if (result != null && result.getKind() != Kind.VAR) {
                            instruction.setX(result);
                        } else {
                            if(instruction.getX().getKind() == Kind.VAR) {
                                final Result zeroIfUninitialized = getZeroIfUninitialized(instruction.getX(), exclude);
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

                        Result result = basicBlock.getMapValues().get(parameter.getVariableName());
                        if (result != null && result.getKind() == Kind.VAR) {
                            parameterMap.put(parameter, result);
                        } else {
                            if(parameter.getKind() == Kind.VAR) {
                                final Result zeroIfUninitialized = getZeroIfUninitialized(parameter, exclude);
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
                    Result result = basicBlock.getMapValues().get(y.getVariableName());
                    if (result != null && result.getKind() != Kind.VAR) {
                        instruction.setY(result);
                    } else {
                        if(instruction.getY().getKind() == Kind.VAR) {
                            final Result zeroIfUninitialized = getZeroIfUninitialized(instruction.getY(), exclude);
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

    private void updateValueMap(Map<String, Result> valueMap, Instruction instruction, String variableName, String uniqueIdentifier, Result y) {
//        if (!exclude.contains(variableName)) {

            if (y.getKind() == Kind.VAR) {
                final Result yValue = valueMap.get(y.getVariableName());
                if (yValue != null) {
                    if (yValue.getKind() == Kind.VAR) {
                        updateValueMap(valueMap, instruction, variableName, uniqueIdentifier, yValue);
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
            System.out.println("Copying for " + variableName + " in instruction number[" + instruction.getInstructionNumber() + "]");
//        }
        }

    
    public void processDominatingBlocks() {

        final List<Instruction> instructions = p.getMain().getInstructionList();
        Map<Integer, Result> remainingMoves = new HashMap<Integer, Result>();
        final List<Instruction> phis = new ArrayList<Instruction>();
        for (Instruction instruction : instructions) {
            if (instruction.getOpcode() == OperationCodes.move && !instruction.isDeleted()) {
                final Result target = instruction.getY();
                final Result x = instruction.getX();
                if (x.getKind() == Kind.VAR) {
                    final Integer key = x.getLocation();
                    remainingMoves.put(key, target);
                    instruction.setDeleted(true, "CP");
                }
            }
            if(instruction.getOpcode() == OperationCodes.phi) {
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
//        if(operand.isIntermediate()) {
//            final Result result = update.get(operand.getIntermediateLoation());
//            if(result != null) {
//                return result;
//            }
//        }
        if(instruction.getOpcode() == OperationCodes.phi && operand.getKind() == Kind.VAR) {
            final Result result = update.get(operand.getLocation());
            if(result != null) {
                return result;
            }
        }
        return operand;
    }

    protected Result updateFirstOccurence(Result operand) {
        if (operand != null && operand.getKind() == Kind.VAR) {
            final String programName = p.getMain().getName();
            final Symbol recentOccurence = p.getMain().getSymbolTable().getRecentOccurence(operand.getVariableName());
            if(getZeroIfUninitialized(operand, exclude) != null) {
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

    private Result getZeroIfUninitialized(Result operand, List<String> exclude) {
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
        if(exclude.contains(operand.getVariableName())) {
            return operand;
        }
        return null;
    }

    protected Result getTarget(Map<Integer, Result> remainingMoves, Result operand, Instruction instruction) {
        if (instruction.getOpcode() == OperationCodes.phi && operand.getKind() == Kind.VAR) {
            final Result result = remainingMoves.get(operand.getLocation());
            if (result != null) {
                return result;
            }
        }
//        if (operand != null && operand.isIntermediate()) {
//            return remainingMoves.get(operand.getIntermediateLoation());
//        }

        return null;
    }
}