package lexical;

import java.util.*;

import data.Kind;
import datastructures.Function;
import datastructures.Symbol;

public class Helper {
	
	public static void variableDeclaration(String name, Kind kind, List<Integer> arrayDimension, Function scope){
		Symbol s = null;
		if (kind == Kind.ARRAY){
			//Do something
		}
		else {
			
			s = new Symbol(name, 0 ,Kind.VAR);
			
		}
		
		scope.getSymbolTable().addSymbol(s);
	}

}
