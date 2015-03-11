package data;

import datastructures.Symbol;

public class Instruction {
	private Symbol symbol;
	private Result x;
	private Result y;
	private int instructionNumber;
	private final int opcode;
	private boolean deleted;
	private String deleteReason;
	
	public Instruction(Result x, Result y, int opcode, int instructionNumber){
		this.x = x;
		this.y = y;
		this.opcode = opcode;
		this.instructionNumber = instructionNumber;
		this.deleted = false;
	}
	
	public Instruction(Result x, Result y, int opcode){
		this.x = x;
		this.y = y;
		this.opcode = opcode;
		this.deleted = false;
	}
	
	public void setX(Result x){
		this.x = x;
	}
	
	public void setY(Result y){
		this.y = y;
	}

	public Result getX(){
		return this.x;
	}
	
	public Result getY(){
		return this.y;
	}
	
	public Symbol getSymbol(){
		return this.symbol;
	}
	
	public void setSymbol(Symbol symbol){
		this.symbol = symbol;
	}
	
	public void setInstructionNumber(int instructionNumber){
		this.instructionNumber = instructionNumber;
	}
	
    public boolean isComplete() {
        if(opcode != OperationCodes.phi) {
            throw new UnsupportedOperationException("This operation is only for PHI instructions");
        }
        return y != null && x != null;
    }
	
    private String getOperand(Result x) {
        if(x.getKind() == Kind.INTERMEDIATE) {
            return "(" + x.getIntermediateLocation() +")";
        } else if(x.getKind() == Kind.CONSTANT) {
            return "#" + String.valueOf(x.getConstVal());
        } else if(x.getKind() == Kind.VAR || x.getKind() == Kind.PROCEDURE) {
            if(opcode == OperationCodes.cmp || opcode == OperationCodes.kill /*|| x.getLocation() == null*/) {
                return x.getVariableName();
            }
            return x.getVariableName() + ":" + x.getLocation();
        } else if (x.getKind() == Kind.ARRAY) {
            return x.getVariableName();
        }// else if (x.getKind() == Kind.FRAMEPOINTER) {
        //    return "FP:0";
        //} else if (x.isBaseAddress()) {
         //   return x.getVariableName() + ":baseaddress";
        //}
        return "";
    }
	
	@Override
	public String toString(){
        StringBuilder sb = new StringBuilder(OperationCodes.getOperationName(opcode));
        int operandCount = OperationCodes.getOperandCount(opcode);

        if(operandCount > 0) {
            if(opcode == OperationCodes.move || opcode == OperationCodes.store) {
                sb.append(" ").append(getOperand(y));
            } else {
                sb.append(" ").append(getOperand(x));
            }
        }
        if(operandCount > 1) {
            if(opcode == OperationCodes.move || opcode == OperationCodes.store) {
                sb.append(" ").append(getOperand(x));
            } else {
                sb.append(" ").append(getOperand(y));
            }
        }

        return sb.toString();
	}

	public int getInstructionNumber() {
		return this.instructionNumber;
	}

	public int getOpcode() {
		return this.opcode;
	}

	public void fixUpInstruction(int programCounter) {
		if (this.opcode == OperationCodes.bra){
	        if(x == null) {
	            setX(new Result(Kind.CONSTANT));
	        }
	        x.setConstVal(programCounter);
		}
		else {
	        if(y == null) {
	            setX(new Result(Kind.CONSTANT));
	        }
	        x.setConstVal(programCounter);
		}
		
	}

	public void setDeleted(boolean b, String string) {
		this.deleted = b;
		this.deleteReason = string;
	}

}
