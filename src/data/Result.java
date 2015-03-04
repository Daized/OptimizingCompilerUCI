package data;
import java.util.*;

import lexical.TokenTypes;
import datastructures.BasicBlock;
import datastructures.Symbol;

public class Result {
	private Kind kind;
	private int condition; //Condition: EQ, NE, GEQ, LEQ, GTR, LSS
	
	private String varName; //Name of variable
	private int address; //address of variable, if variable
	private int constVal; //otherwise constant value if constant
	private int regno;
	private List<Integer> arrayDimensions;
	private List<Result> arrayValues;
	private BasicBlock join;
	
	public Result(Kind kind){
		this.kind = kind;
		this.arrayValues = new ArrayList<Result>();
		this.arrayDimensions = new ArrayList<Integer>();
	}
	
	//public Result(Symbol symbol){
		//
	//}
	
	public int getConstVal(){
		return this.constVal;
	}
	
	public void appendArrayDimension(int dimension){
		this.arrayDimensions.add(dimension);
	}
	
	public void setKind(Kind kind){
		this.kind = kind;
	}
	
	public void setConstVal(int constVal){
		this.constVal = constVal;
	}
	
	public void setCondition(int condition){
		this.condition = condition;
	}
	
	public void setVarName(String varName){
		this.varName = varName;		
	}
	
	public void setAddress(int address){
		this.address = address;
	}
	
	public void setRegno(int regno){
		this.regno = regno;
	}
	
	public void setArrayValues(List<Result> arrayValues){
		this.arrayValues = arrayValues;
	}

	public void setArrayDimensions(List<Integer> arrayDimensions){
		this.arrayDimensions = arrayDimensions;
	}
	
	public void setJoin(BasicBlock join){
		this.join = join;
	}
	
	

}
