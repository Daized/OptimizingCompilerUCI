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

	private Map<String, Result> mapValues = new HashMap<String, Result>();
	private List<Instruction> phiList = new ArrayList<Instruction>();
	private List<String> exclude = new ArrayList<String>();
	private Map<Integer, Result> deletedMoves = new HashMap<Integer, Result>();
	
	
	public CopyPropagation(Parser p) {
		super(p);
		
	}

	public void doCopyPropagation(){
		List<Symbol> symbolList = p.getMain().getSymbolTable().getSymbolList();
		
		
		
		processGraph(p.getMain().getCFG().getRoot());
		dominatedBlocksUpdate();
		setNotVisited(); //CLEANUP
	}

	@Override
	public void visit(BasicBlock node) {
		List<Instruction> instructionList = node.getInstructions();
		List<BasicBlock> dominatedBlocks = node.getDominatedBlocks();
		List<String> removed = new ArrayList<String>();
		Map<String, Result> nodeValues = processInstructions(instructionList, node, removed);
		
		for (BasicBlock block : dominatedBlocks){
			block.updateValueMap(nodeValues);
			block.updateExclude(node.getExclude());
            for (String s : removed) {
                node.getExclude().remove(s);
            }
		}
		
		for (Instruction instruction: phiList){
			if (instruction.getOpcode() == OperationCodes.phi)
				changePhiInstruction(instruction);
		}
		
		mapValues.putAll(nodeValues);
		
	}
	
	private Map<String, Result> processInstructions(List<Instruction> instructionList, BasicBlock node, List<String> removed) {
		Map<String, Result> mapValues = node.getMapValues();
		List<String> exclude = node.getExclude();
		for (Instruction instruction : instructionList) {
            int opcode = instruction.getOpcode();
            if (instruction.getOpcode() == OperationCodes.phi) {
            	phiList.add(instruction);
            	Result result = new Result(Kind.INTERMEDIATE);
            	result.setIntermediateLocation(instruction.getInstructionNumber());
            	mapValues.put(instruction.getSymbol().getName(), result);
                //mapValues.remove(instruction.getSymbol().getName());
                //exclude.add(instruction.getSymbol().getName());
                continue;
            }
            if (instruction.getOpcode() == OperationCodes.kill) {
                String variableName = instruction.getX().getVariableName();
                mapValues.remove(variableName);
                exclude.add(variableName);
                continue;
            } else {
                Result x = instruction.getX();
                Result y = instruction.getY();
                List<Result> parameters = instruction.getParameters();
                if (opcode == OperationCodes.move) {
                    String variableName = instruction.getX().getVariableName();
                    updateValueMap(node.getMapValues(), instruction, variableName, 
                    		instruction.getX().getUniqueName(), y);
                    exclude.remove(variableName);
                    removed.add(variableName);
                } else {
                    if (x != null) {
                        Result result = node.getMapValues().get(x.getVariableName());
                        if (result != null && result.getKind() != Kind.VAR) {
                            instruction.setX(result);
                        }
                        else {
                        	if (instruction.getX().getKind() == Kind.VAR){
                        		Result zeroIfUninitialized = getZeroIfUninitialized(instruction.getX(), exclude);
                                if (zeroIfUninitialized == null) {
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
                    Result result = node.getMapValues().get(y.getVariableName());
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
        return mapValues;
	}

	private void updateValueMap(Map<String, Result> mapValues, Instruction instruction, String variableName, 
			String uniqueName, Result y) {
        //if (!exclude.contains(variableName)) {

            if (y.getKind() == Kind.VAR) {
                Result yValue = mapValues.get(y.getVariableName());
                if (yValue != null) {
                    if (yValue.getKind() == Kind.VAR) {
                        updateValueMap(mapValues, instruction, variableName, uniqueName, yValue);
                        return;
                    }
                    y = yValue;
                }
            }
            mapValues.put(variableName, y);
            mapValues.put(uniqueName, y);
            instruction.setY(y);
            instruction.setDeleted(true, "CP");
            deletedMoves.put(instruction.getInstructionNumber(), y);
            p.getMain().getSymbolTable().updateSymbol(variableName, y);
            System.out.println("Copying " + variableName + " for instruction " + instruction.getInstructionNumber());
        
		
	}

	private void dominatedBlocksUpdate() {
		List<Instruction> instructions = p.getMain().getInstructionList();
		Map<Integer, Result> remainingMoves = new HashMap<Integer, Result>();
		List<Instruction> phis = new ArrayList<Instruction>();
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
	        //if(operand.getKind() == Kind.INTERMEDIATE) {
	        //    final Result result = update.get(operand.getIntermediateLocation());
	         //   if(result != null) {
	         //       return result;
	        //    }
	       // }
	        if(instruction.getOpcode() == OperationCodes.phi && operand.getKind() == Kind.VAR) {
	            final Result result = update.get(operand.getLocation());
	            if(result != null) {
	                return result;
	            }
	        }
	        return operand;
	    }
	
	private Result getTarget(Map<Integer, Result> remainingMoves, Result operand, Instruction instruction) {
        if (instruction.getOpcode() == OperationCodes.phi && operand.getKind() == Kind.VAR) {
            final Result result = remainingMoves.get(operand.getLocation());
            if (result != null) {
                return result;
            }
        }
       // if (operand != null && operand.getKind() == Kind.INTERMEDIATE) {
        //   return remainingMoves.get(operand.getIntermediateLocation());
        //}

        return null;
    }
	
	private Result updateFirstOccurence(Result operand) {
        if (operand != null && operand.getKind() == Kind.VAR) {
            final String programName = p.getMain().getName();
            final Symbol recentOccurence = p.getMain().getSymbolTable().getRecentOccurence(operand.getVariableName());
            if (getZeroIfUninitialized(operand, exclude) != null) {
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

	private void changePhiInstruction(Instruction instruction){
		Result x = mapValues.get(instruction.getX().getUniqueName());
		if (x != null){
			instruction.setX(x);
			mapValues.remove(instruction.getX().getUniqueName());
			mapValues.put(instruction.getSymbol().getName(), new Result(instruction.getSymbol()));
		}
		
		Result y = mapValues.get(instruction.getY().getUniqueName());
		if (y != null){
			instruction.setY(y);
			mapValues.remove(instruction.getY().getUniqueName());
			mapValues.put(instruction.getSymbol().getName(), new Result(instruction.getSymbol()));
		}
		
		
	}
	
	
	
	
	
	
	
}
