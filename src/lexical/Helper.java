package lexical;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import data.Instruction;
import data.Kind;
import data.OperationCodes;
import data.Result;
import datastructures.BasicBlock;
import datastructures.Function;
import datastructures.Symbol;

public class Helper {
	
    public static void BJ(Function scope, int loc) {
        Result x = new Result(Kind.CONSTANT);
        x.setConstVal(loc);
        addInstruction(OperationCodes.bra, scope, x, null);
    }

    public static void FJLink(Function scope, Result follow) {
//        final Symbol symbol = new Symbol(String.valueOf(x.fixupLoc()), code.getPc(), null, false, x.fixupLoc());
//        symbol.setResult(x);
        Result equal = new Result(Kind.CONSTANT);
        equal.setConstVal(0);
        Result branchLocation = new Result(Kind.CONSTANT);
        branchLocation.setConstVal(0);
        addInstruction(OperationCodes.beq, scope, equal, branchLocation);
        follow.fixupLoc(scope.getProgramCounter() - 1);
    }

    public static void CJF(Function scope, Result currentState) {
        Result x = new Result(Kind.INTERMEDIATE);
        x.setIntermediateLocation(scope.getProgramCounter() - 1);
        Result y = new Result(Kind.CONSTANT);
        y.setConstVal(0);
        addInstruction(OperationCodes.getConditionNegation(currentState.getCondition()), scope, x, y);
        currentState.fixupLoc(scope.getProgramCounter() - 1);
    }
    
	
	public static void variableDeclaration(String name, Kind kind, List<Integer> arrayDimension, Function scope){
		Symbol s = null;
		if (kind == Kind.ARRAY){
			List<Result> arrayIdentifiers = new ArrayList<Result>();
			s = new Symbol(name, null, -1, arrayDimension.size()); 
			int dimensionCount = arrayDimension.size();
			int[] dimensions = new int[dimensionCount];
			for (int i = 0; i< dimensionCount; i++){
				dimensions[i] = arrayDimension.get(i);
				Result ident = new Result(Kind.CONSTANT);
				ident.setConstVal(arrayDimension.get(i));
				arrayIdentifiers.add(ident);
			}
			s.setArrayDimension(dimensionCount);
			s.setArrayValues(arrayIdentifiers);
			s.setArrayConstValue(Array.newInstance(Integer.class, dimensions));
			s.setKind(Kind.ARRAY);
		}
		else {
			s = new Symbol(name, -1 , 0 , Kind.VAR);
		}
		scope.getSymbolTable().addSymbol(s);
	}
	
	public static void addInstruction(int opCode, Function scope, Result x, Result y){
		Instruction instruction = new Instruction(x, y, opCode, scope.getProgramCounter());
		if(OperationCodes.getOperandCount(opCode) > 0) {
            if (scope.getSymbolTable() != null && (x.getKind() == Kind.VAR || x.getKind() == Kind.ARRAY)) {
                Symbol recent = scope.getSymbolTable().getRecentOccurence(x.getVariableName());
                if (x.getKind() == Kind.ARRAY) {
                    if (recent.getSSA() == -1) {
                        instruction.setSymbol(recent);
                        //Making sure arrays have only one entry in symbol table besides the declaration
                    }
                } else {
                    instruction.setSymbol(recent);
                }
            }
        }
        scope.appendInstruction(instruction);
	}
	
	public static void addMoveInstruction(Function scope, Result x, Result y){
		addToSymbolTable(scope, x);
		x.setLocation(scope.getProgramCounter());
		addInstruction(OperationCodes.move, scope, x, y);
	}
	
	public static void addToSymbolTable(Function scope, Result x){
		Symbol recent = scope.getSymbolTable().getRecentOccurence(x.getVariableName());
		if (recent == null)
			System.out.println(x.getVariableName());
		Symbol symbol;
		if (recent.getKind() == Kind.ARRAY){
			symbol = new Symbol(recent.getName(), cloneValue(recent.getConstVal()), scope.getProgramCounter(), recent.getArrayDimension());
			symbol.setKind(Kind.ARRAY);
		}
		else {
			symbol = new Symbol(recent.getName(), scope.getProgramCounter(), recent.getConstVal());
			symbol.setKind(Kind.VAR);
		}
		scope.getSymbolTable().addSymbol(symbol);
	}
	
	public static void createAddA(Function scope, String name, List<Result> arrayValues){
		if(arrayValues.size() == 0) {
            return;
        }
        Result previous = null;
        Result previousSumComponent = null;

        final Symbol declaration = scope.getSymbolTable().getDeclaration(name);
        final List<Result> originalIdentifiers = declaration.getArrayValues();

        for (int i=0; i<arrayValues.size(); i++) {
            previous = arrayValues.get(i);
            for(int j=i+1; j<originalIdentifiers.size(); j++) {
                final Result originalIdentifier = originalIdentifiers.get(j);
                Helper.addInstruction(OperationCodes.mul, scope, previous, originalIdentifier);
                previous = new Result(Kind.INTERMEDIATE);;
                previous.setIntermediateLocation(scope.getProgramCounter() - 1);
            }
            if(previousSumComponent != null) {
                Helper.addInstruction(OperationCodes.add, scope, previous, previousSumComponent);
                previous = new Result(Kind.INTERMEDIATE);;
                previous.setIntermediateLocation(scope.getProgramCounter() - 1);
            }
            previousSumComponent = previous;
        }

        final Result intSize = new Result(Kind.CONSTANT);
        intSize.setConstVal(4);
        Helper.addInstruction(OperationCodes.mul, scope, previous, intSize);
        final Result mulInstruction = new Result(Kind.INTERMEDIATE);
        mulInstruction.setIntermediateLocation(scope.getProgramCounter() - 1);

//        final Symbol lhsSymbol = getSymbolTable().getRecentOccurence(tokenName);
//        final int lhsBaseAddress = getSymbolTable().getOffset(lhsSymbol);
        final Result lBaseAddr = new Result(Kind.BASE_ADDRESS);
        lBaseAddr.setVarName(name);

        final Result addInstruction = new Result(Kind.INTERMEDIATE);
        addInstruction.setIntermediateLocation(scope.getProgramCounter());
        //Helper.addInstruction(OperationCodes.add, scope, scope.FP, lBaseAddr);
        Helper.addInstruction(OperationCodes.adda, scope, mulInstruction, addInstruction);
    }

    public static Result calculateMulInstructionValue(List<Result> arrayIdentifiers, Function scope) {
        Result previous = arrayIdentifiers.get(0);
        for (int i=1; i<arrayIdentifiers.size(); i++) {
            final Result arrayIdentifier = arrayIdentifiers.get(i);
            Helper.addInstruction(OperationCodes.mul, scope, arrayIdentifier, previous);
            previous = new Result(Kind.INTERMEDIATE);;
            previous.setIntermediateLocation(scope.getProgramCounter());
        }
        return previous;
    }

    
	
    public static int addKillInstruction(Function scope, Symbol recent) {
        return scope.appendKillInstruction(new Instruction(new Result(recent), null, OperationCodes.kill, scope.getProgramCounter()), -1);
    }
    
    public static void createPhiInstructions(Function scope, BasicBlock join){
    	//First do left side
    	BasicBlock left = join.getLeft();
    	List<Instruction> leftInstructions = left.getInstructions();
    	for (Instruction instruction: leftInstructions){
    		if (instruction.getOpcode() == OperationCodes.move || instruction.getOpcode() == OperationCodes.phi){
    			Symbol symbol = instruction.getSymbol();
    			Instruction phi;
    			if (join.getPhiInstruction(symbol.getName()) != null){
    				phi = join.getPhiInstruction(symbol.getName());
    			}
    			else {
    				phi = createPhiInstruction(scope, join, symbol);
    			}
    			phi.setX(new Result(symbol));
    		}
    	}
    	
    	//Now let's do right side
    	BasicBlock right = join.getRight();
    	List<Instruction> rightInstructions = right.getInstructions();
    	for (Instruction instruction: rightInstructions){
    		if (instruction.getOpcode() == OperationCodes.move || instruction.getOpcode() == OperationCodes.phi){
    			Symbol symbol = instruction.getSymbol();
    			if (join.getPhiInstruction(symbol.getName()) == null){
    				createPhiInstruction(scope, join, symbol);
    			}
    			Instruction phiInstruction = join.getPhiInstruction(symbol.getName());
    			phiInstruction.setY(new Result(instruction.getSymbol()));
    		}
    	}
    	
    	//Finish incomplete phi instructions from right side
    	Collection<Instruction> phiCollection = join.getPhiInstructions();
    	for (Instruction phi: phiCollection){
    		if (!phi.isComplete()){
    			Symbol target = scope.getSymbolTable().getSymbol(phi.getSymbol());
    			Result targetSym = new Result(target);
    			if (phi.getX() == null){
    				phi.setX(targetSym);
    			}
    			else {
    				phi.setY(targetSym);
    			}
    		}
    	}
    }
    
    private static Instruction createPhiInstruction(Function scope, BasicBlock join, Symbol symbol){
    	Symbol target = scope.getSymbolTable().getSymbol(symbol);
    	Symbol phi = new Symbol(target.getName(), target.getSSA(), target.getConstVal());
    	Instruction phiInstruction = new Instruction(null, null, OperationCodes.phi, scope.getProgramCounter());
    	phiInstruction.setSymbol(phi);
    	
    	Instruction instruction = join.addPhiInstruction(phiInstruction);
    	if(instruction != null){
    		scope.appendPhiInstruction(phiInstruction,instruction);
    	}
    	else {
    		instruction = new Instruction(null, null, -1, -1);
    		scope.appendPhiInstruction(phiInstruction, instruction);
    	}
    	phi.setSSA(phiInstruction.getInstructionNumber());
    	return phiInstruction;
    }
	
    //based on http://stackoverflow.com/questions/869033/how-do-i-copy-an-object-in-java
    public static Object cloneValue(Object obj) {
        try {
            if (obj.getClass() == Integer.class) {
                return new Integer((Integer) obj);
            }
            Object clone = obj.getClass().newInstance();
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                field.set(clone, field.get(obj));
            }
            return clone;
        } catch (Exception e) {
            return null;
        }
    }

}
