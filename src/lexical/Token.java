package lexical;


public class Token{
	private String token;
	private TokenTypes type;
	private int value; 
	
	public Token(String token, TokenTypes type, int value){
		this.token = token;
		this.type = type;	
		this.value = value;
	}
	
	public String getTokenString(){
		return this.token;
	}
	
	public TokenTypes getTokenType(){
		return this.type;
	}
	
	/* This function returns the token type */
	public static Token getType(String token){
		
		switch(token){
		
			case "*":
				return new Token(token, TokenTypes.timesToken,  1);
			case "/":
				return new Token(token, TokenTypes.divToken,  2);
			case "+":
				return new Token(token, TokenTypes.plusToken,  11);
			case "-":
				return new Token(token, TokenTypes.minusToken,  12);
			case "==":
				return new Token(token, TokenTypes.eqlToken,  20);
			case "!=":
				return new Token(token, TokenTypes.neqToken,  21);
			case "<":
				return new Token(token, TokenTypes.lssToken,  22);
			case ">=":
				return new Token(token, TokenTypes.geqToken,  23);
			case "<=":
				return new Token(token, TokenTypes.leqToken,  24);
			case ">":
				return new Token(token, TokenTypes.gtrToken,  25);
			case ".":
				return new Token(token, TokenTypes.periodToken,  30);
			case ",":
				return new Token(token, TokenTypes.commaToken,  31);
			case "[":
				return new Token(token, TokenTypes.openbracketToken,  32);
			case "]":
				return new Token(token, TokenTypes.closebracketToken,  34);
			case ")":
				return new Token(token, TokenTypes.closeparenToken,  35);
			case "<-":
				return new Token(token, TokenTypes.becomesToken,  40);
			case "then":
				return new Token(token, TokenTypes.thenToken,  41);
			case "do":
				return new Token(token, TokenTypes.doToken,  42);
			case "(":
				return new Token(token, TokenTypes.openparenToken,  50);
			case ";":
				return new Token(token, TokenTypes.semiToken,  70);
			case "}":
				return new Token(token, TokenTypes.endToken,  80);
			case "od":
				return new Token(token, TokenTypes.odToken,  81);
			case "fi":
				return new Token(token, TokenTypes.fiToken,  82);
			case "else":
				return new Token(token, TokenTypes.elseToken,  90);
			case "let":
				return new Token(token, TokenTypes.letToken,  100);
			case "call":
				return new Token(token, TokenTypes.callToken,  101);
			case "if":
				return new Token(token, TokenTypes.ifToken,  102);
			case "while":
				return new Token(token, TokenTypes.whileToken,  103);
			case "return":
				return new Token(token, TokenTypes.returnToken,  104);
			case "var":
				return new Token(token, TokenTypes.varToken,  110);
			case "array":
				return new Token(token, TokenTypes.arrToken,  111);
			case "function":
				return new Token(token, TokenTypes.funcToken,  112);
			case "procedure":
				return new Token(token, TokenTypes.procToken,  113);
			case "{":
				return new Token(token, TokenTypes.beginToken,  150);
			case "main":
				return new Token(token, TokenTypes.mainToken,  200);
			case "\0":
				return new Token(token, TokenTypes.eofToken,  255);
			default:
				if (Character.isDigit(token.charAt(0)))
					return new Token(token, TokenTypes.number, 60);
				else if (Character.isLetter(token.charAt(0)))
					return new Token(token, TokenTypes.ident, 61);
				else
					return new Token(token, TokenTypes.errorToken, 0); //Invalid characters
		
		}
		
	}
	
	
	
}	

