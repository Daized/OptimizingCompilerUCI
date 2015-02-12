package lexical;


public class Token{
	private String token;
	private TokenTypes type;
	
	public Token(String token, TokenTypes type){
		this.token = token;
		this.type = type;			
	}
	
	public String getTokenString(){
		return this.token;
	}
	
	public TokenTypes getTokenType(){
		return this.type;
	}
	
	/* This function returns the token type */
	public static TokenTypes getType(String token){
		
		switch(token){
		
			case "main":
				return TokenTypes.MAIN;
			case "function":
				return TokenTypes.FUNCTION;
			case "procedure":
				return TokenTypes.PROCEDURE;
			case "var":
				return TokenTypes.VAR;
			case "array":
				return TokenTypes.ARRAY;
			case "return":
				return TokenTypes.RETURN;
			case "while":
				return TokenTypes.WHILE;
			case "do":
				return TokenTypes.DO;
			case "od":
				return TokenTypes.OD;
			case "if":
				return TokenTypes.IF;
			case "then":
				return TokenTypes.THEN;
			case "else":
				return TokenTypes.ELSE;
			case "fi":
				return TokenTypes.FI;
			case "call":
				return TokenTypes.CALL;
			case "let":
				return TokenTypes.LET;
			case "[":
				return TokenTypes.LBRACKET;
			case "]":
				return TokenTypes.RBRACKET;
			case "{":
				return TokenTypes.LSBRACKET;
			case "}":
				return TokenTypes.RSBRACKET;
			case "(":
				return TokenTypes.LPAREN;
			case ")":
				return TokenTypes.RPAREN;
			case ".":
				return TokenTypes.DOT;
			case ",":
				return TokenTypes.COMMA;
			case ";":
				return TokenTypes.SEMICOLON;
			case "+":
				return TokenTypes.PLUS;
			case "-":
				return TokenTypes.MINUS;
			case "*":
				return TokenTypes.TIMES;
			case "/":
				return TokenTypes.DIVIDE;
			case "<-":
				return TokenTypes.ASSIGNMENT;
			case "==":
				return TokenTypes.EQUALS;
			case "<":
				return TokenTypes.LESS;
			case "<=":
				return TokenTypes.LEQUAL;
			case ">":
				return TokenTypes.GREATER;
			case ">=":
				return TokenTypes.GEQUAL;
			default:
				if (Character.isDigit(token.charAt(0)))
					return TokenTypes.NUMBER;
				else if (Character.isLetter(token.charAt(0)))
					return TokenTypes.IDENT;
				else
					return null; //Invalid characters
		
		}
		
	}
	
	enum TokenTypes{
		
		MAIN, 
		FUNCTION, 
		PROCEDURE, 
		VAR, 
		ARRAY, 
		RETURN, 
		WHILE, 
		DO, 
		OD, 
		IF, 
		THEN,
		ELSE,
		FI,
		CALL,
		LET,
		LBRACKET,
		RBRACKET,
		LSBRACKET,
		RSBRACKET,
		LPAREN,
		RPAREN,
		DOT,
		COMMA,
		SEMICOLON,
		PLUS,
		MINUS,
		TIMES,
		DIVIDE,
		ASSIGNMENT,
		EQUALS,
		DNEQUAL,
		LESS,
		LEQUAL,
		GREATER,
		GEQUAL,
		IDENT,
		NUMBER
		
	}
	
	
}	

