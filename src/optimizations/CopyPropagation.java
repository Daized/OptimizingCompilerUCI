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
	
	
	public CopyPropagation(Parser p) {
		super(p);
		
	}

	public void doCopyPropagation(){
		List<Symbol> symbolList = p.getMain().getSymbolTable().getSymbolList();
		
		for (Symbol s: symbolList){
			if (s.getKind() == Kind.VAR){
				Result x = new Result(Kind.CONSTANT);
				x.setConstVal(0);
				mapValues.put(s.getName(), x);
			}
		}
		
		processGraph(p.getMain().getCFG().getRoot());
		setNotVisited(); //CLEANUP
	}

	@Override
	public void visit(BasicBlock node) {
		List<Instruction> instructionList = node.getInstructions();
		List<BasicBlock> dominatedBlocks = node.getDominatedBlocks();
		
		processInstructions(instructionList);
		
		for (BasicBlock block : dominatedBlocks){
			dominatedBlocksUpdate(block.getInstructions());
		}
		
		for (Instruction instruction: phiList){
			if (instruction.getOpcode() == OperationCodes.phi)
				changePhiInstruction(instruction);
		}
		
		mapValues.clear();
		
	}
	
	private void processInstructions(List<Instruction> instructionList) {
		for (Instruction instruction : instructionList) {
            int opcode = instruction.getOpcode();
            if (instruction.getOpcode() == OperationCodes.phi) {
                mapValues.remove(instruction.getSymbol().getName());
                exclude.add(instruction.getSymbol().getName());
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
                if (opcode == OperationCodes.move) {
                    String variableName = instruction.getX().getVariableName();
                    updateValueMap(instruction, variableName, instruction.getX().getUniqueName(), y);
                } else {
                    if (x != null) {
                        Result result = mapValues.get(x.getVariableName());
                        if (result != null) {
                            instruction.setX(result);
                        }
                    }
                }
                if (y != null) {
                    Result result = mapValues.get(y.getVariableName());
                    if (result != null) {
                        instruction.setY(result);
                    }
                }

            }
        }
	}

	private void updateValueMap(Instruction instruction, String variableName, String uniqueName, Result y) {
        if (!exclude.contains(variableName)) {

            if (y.getKind() == Kind.VAR) {
                Result yValue = mapValues.get(y.getVariableName());
                if (yValue != null) {
                    if (yValue.getKind() == Kind.VAR) {
                        updateValueMap(instruction, variableName, uniqueName, yValue);
                        return;
                    }
                    y = yValue;
                }
            }
            mapValues.put(variableName, y);
            mapValues.put(uniqueName, y);
            instruction.setY(y);
            instruction.setDeleted(true, "CP");
            p.getMain().getSymbolTable().removeSymbol(variableName, instruction.getInstructionNumber());
            System.out.println("Copying for " + variableName + " in instruction number[" + instruction.getInstructionNumber() + "]");
        }
		
	}

	private void dominatedBlocksUpdate(List<Instruction> instructions) {
		
		for (Instruction instruction : instructions){
			int operationCode = instruction.getOpcode();
			if (operationCode == OperationCodes.end){
				return;
			}
			if (operationCode == OperationCodes.phi){
				mapValues.remove(instruction.getSymbol().getName());
				phiList.add(instruction);
				continue;
			}
			if (operationCode == OperationCodes.kill){
				String name = instruction.getX().getVariableName();
				mapValues.remove(name);
				continue;
			}
			
			if (instruction.getX() == null && instruction.getY() == null){
				return;
			}
			Result x = instruction.getX();
			Result y = instruction.getY();
			if(operationCode  != OperationCodes.move){
				if (x != null) {
					Result result = mapValues.get(x.getVariableName());
					if (result != null){
						instruction.setX(result);
					}
				}
				
			}
			
			if (y != null){
				Result result = mapValues.get(y.getVariableName());
				if (result != null){
					instruction.setY(result);
				}
			}
			
		}
		
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
