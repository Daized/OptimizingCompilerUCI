package datastructures;
import java.util.*;

public class SymbolTable {
	
	private List<Symbol> symbolList;
	private Map<String, Symbol> symbolMap;
	private SymbolTable mainSymbolTable;
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
	
	public Symbol getRecentOccurence(String symbolName) {
        Symbol s = null;
        for (Symbol symbol : symbolList) {
            if(symbol.getName().equals(symbolName)) {
                s = symbol;
            }
        }
        if (s == null && mainSymbolTable != null) {
            s = mainSymbolTable.getRecentOccurence(symbolName);
            s.setMainSymbol();
        }
        return s;
    }
	
	public Symbol getDeclaration(String symbolName) {
        Symbol s = null;
        for (Symbol symbol : symbolList) {
            if(symbol.getName().equals(symbolName) && symbol.getSSA() == -1) {
                return symbol;
            }
        }
        if(!mainSymbolTable.equals(this) && mainSymbolTable != null) {
            s = mainSymbolTable.getDeclaration(symbolName);
            s.setMainSymbol();
        }
        return s;
    }
	
	public SymbolTable getMainSymbolTable() {
        return mainSymbolTable;
    }

    public void setMainSymbolTable(SymbolTable mainSymbolTable) {
        this.mainSymbolTable = mainSymbolTable;
    }

	public Symbol getSymbol(Symbol symbol) {
        Symbol targetSymbol = null;
        for (Symbol symbol1 : symbolList) {
            if (symbol1.getName().equals(symbol.getName())) {
                targetSymbol = symbol1;
            }
        }
        if(targetSymbol == null && mainSymbolTable != null) {
            targetSymbol = mainSymbolTable.getSymbol(symbol);
            targetSymbol.setMainSymbol();
        }
        return targetSymbol;
	}

}
