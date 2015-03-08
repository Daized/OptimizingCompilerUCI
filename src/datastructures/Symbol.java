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
	
	
	/*
	 * Value of 0 on ssa means it's a declaration value.
	 */
	public Symbol(String name, int ssa, int constVal, Kind kind){
		this.name = name;
		this.ssa = ssa;
		this.constVal = constVal;
		this.kind = kind;
	}
	
	public Symbol(String name, int ssa, Kind kind){
		this.name = name;
		this.ssa = ssa;
		this.kind = kind;
	}
	
	public int getSSA(){
		return this.ssa;
	}
	
	public String getName(){
		return this.name;
	}
	
	
	
	public int getConstVal(){
		return this.constVal;
	}
	
	public Kind getKind(){
		return this.kind;
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

}
