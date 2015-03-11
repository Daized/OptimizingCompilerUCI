package data;
import java.util.*;

import lexical.TokenTypes;
import datastructures.BasicBlock;
import datastructures.Symbol;

public class Result {
	private Kind kind;
	private int condition; //Condition: ("==", 20), ("!=", 21), ("<", 22), (">=", 23), ("<=", 24), (">", 25)
	private String varName; //Name of variable
	private int address; //address of variable, if variable
	private int constVal; //otherwise constant value if constant
	private int regno;
	private int intermediateLocation;
	private int location;
	private int fixuploc;
	private List<Integer> arrayDimensions;
	private List<Result> arrayValues;
	private BasicBlock join;
	
	public Result(Kind kind){
		this.kind = kind;
		this.arrayValues = new ArrayList<Result>();
		this.arrayDimensions = new ArrayList<Integer>();
	}
	
    public Result(Symbol var){
        this.kind = Kind.VAR;
        this.varName = var.getName();
        this.location = var.getSSA();
    }
	
	public int getConstVal(){
		return this.constVal;
	}
	
	public int getCondition(){
		return this.condition;
	}
	
	public BasicBlock getJoin(){
		return this.join;
	}
	
	public Kind getKind(){
		return this.kind;
	}
	
	public String getUniqueName(){
		return this.varName + ":" + this.location;
	}
	
	public int getIntermediateLocation(){
		return this.intermediateLocation;
	}
	
	public int getLocation(){
		return this.location;
	}
	
	public String getVariableName(){
		return this.varName;
	}
	
	public List<Integer> getArrayDimensions(){
		return this.arrayDimensions;
	}
	
	public List<Result> getArrayValues(){
		return this.arrayValues;
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
	
	public void setLocation(int location){
		this.location = location;
	}
	
	public void setIntermediateLocation(int intermediateLocation){
		this.intermediateLocation = intermediateLocation;
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

	public void fixupLoc(int fixuploc) {
		this.fixuploc = fixuploc;
	}

	public int getFixuploc() {
		return this.fixuploc;
	}
	
	

}
