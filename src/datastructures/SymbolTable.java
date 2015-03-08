package datastructures;
import java.util.*;

public class SymbolTable {
	
	private List<Symbol> symbolList;
	private Map<String, Symbol> symbolMap;
	//Maybe need a global symbol table?
	
	public SymbolTable(){
		this.symbolList = new ArrayList<Symbol>();
		this.symbolMap = new HashMap<String, Symbol>();
	}
	
	public void addSymbol(Symbol s){
		if (s == null) {
			throw new RuntimeException("Null symbol");
		}
		
		String symbolKey = getSymbolKey(s);
		Symbol target = symbolMap.get(symbolKey);
		if (target == null){
			symbolList.add(s);
			symbolMap.put(symbolKey, target);
		}
		else {
			//HMM THINK ABOUT THIS
			throw new RuntimeException("Duplicate symbol.");
		}
	}
	
	public String getSymbolKey(Symbol s){
		return s.getName()+s.getSSA();
	}

}
