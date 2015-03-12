package datastructures;

import java.util.List;

import data.Kind;
import data.Result;

public class Symbol {
	private String name;
	private int ssa; //suffix we add for static single assignment
	private Kind kind; // Kind of symbol in {array, procedure, variable}
	private int constVal; 
	private List<Result> arrayValues;
	private Object arrayConstValue;
	private int arrayDimension;
	private boolean mainSymbol;
	
	/*
	 * Value of -1 on ssa means it's a declaration value.
	 */
	public Symbol(String name, int ssa, int constVal, Kind kind){
		this.name = name;
		this.ssa = ssa;
		this.constVal = constVal;
		this.kind = kind;
	}
	
	public Symbol(String name, int ssa, int constVal){
		this.name = name;
		this.ssa = ssa;
		this.constVal = constVal;
	}
	
	public Symbol(String name, Object arrayConstValue, int ssa, int arrayDimension){
		this.name = name;
		this.ssa = ssa;
		this.arrayDimension = arrayDimension;
		this.arrayConstValue = arrayConstValue;
	}
	
	public int getSSA(){
		return this.ssa;
	}
	
	public String getName(){
		return this.name;
	}
	
	public Object getArrayConstValue(){
		return this.arrayConstValue;
	}
	
	public boolean isMainSymbol(){
		return mainSymbol;
	}
	
	public int getConstVal(){
		return this.constVal;
	}
	
	public int getArrayDimension(){
		return this.arrayDimension;
	}
	
	public Kind getKind(){
		return this.kind;
	}
	
	public void setMainSymbol(){
		this.mainSymbol = true;
	}
	
	public void setArrayConstValue(Object arrayConstValue){
		this.arrayConstValue = arrayConstValue;
	}
	
	public List<Result> getArrayValues(){
		return this.arrayValues;
	}
	
	public void setKind(Kind kind){
		this.kind = kind;
	}
	
	public void addArrayValue(Result arrayValue){
		this.arrayValues.add(arrayValue);
	}

	public void setArrayDimension(int arrayDimension) {
		this.arrayDimension = arrayDimension;
	}
	
	public void setArrayValues(List<Result> arrayValues) {
		this.arrayValues = arrayValues;
	}

	public void setSSA(int ssa) {
		this.ssa = ssa;
	}

	public void setConstVal(int constVal) {
		this.constVal = constVal;
		
	}

}
