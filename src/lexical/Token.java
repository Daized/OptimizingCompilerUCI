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
	
	public int getValue(){
		return this.value;
	}
	
	/* This function returns the token type */
	public static Token getType(String token){

		if (token.equals("*")) {
			return new Token(token, TokenTypes.timesToken, 1);
		} else if (token.equals("/")) {
			return new Token(token, TokenTypes.divToken, 2);
		} else if (token.equals("+")) {
			return new Token(token, TokenTypes.plusToken, 11);
		} else if (token.equals("-")) {
			return new Token(token, TokenTypes.minusToken, 12);
		} else if (token.equals("==")) {
			return new Token(token, TokenTypes.eqlToken, 20);
		} else if (token.equals("!=")) {
			return new Token(token, TokenTypes.neqToken, 21);
		} else if (token.equals("<")) {
			return new Token(token, TokenTypes.lssToken, 22);
		} else if (token.equals(">=")) {
			return new Token(token, TokenTypes.geqToken, 23);
		} else if (token.equals("<=")) {
			return new Token(token, TokenTypes.leqToken, 24);
		} else if (token.equals(">")) {
			return new Token(token, TokenTypes.gtrToken, 25);
		} else if (token.equals(".")) {
			return new Token(token, TokenTypes.periodToken, 30);
		} else if (token.equals(",")) {
			return new Token(token, TokenTypes.commaToken, 31);
		} else if (token.equals("[")) {
			return new Token(token, TokenTypes.openbracketToken, 32);
		} else if (token.equals("]")) {
			return new Token(token, TokenTypes.closebracketToken, 34);
		} else if (token.equals(")")) {
			return new Token(token, TokenTypes.closeparenToken, 35);
		} else if (token.equals("<-")) {
			return new Token(token, TokenTypes.becomesToken, 40);
		} else if (token.equals("then")) {
			return new Token(token, TokenTypes.thenToken, 41);
		} else if (token.equals("do")) {
			return new Token(token, TokenTypes.doToken, 42);
		} else if (token.equals("(")) {
			return new Token(token, TokenTypes.openparenToken, 50);
		} else if (token.equals(";")) {
			return new Token(token, TokenTypes.semiToken, 70);
		} else if (token.equals("}")) {
			return new Token(token, TokenTypes.endToken, 80);
		} else if (token.equals("od")) {
			return new Token(token, TokenTypes.odToken, 81);
		} else if (token.equals("fi")) {
			return new Token(token, TokenTypes.fiToken, 82);
		} else if (token.equals("else")) {
			return new Token(token, TokenTypes.elseToken, 90);
		} else if (token.equals("let")) {
			return new Token(token, TokenTypes.letToken, 100);
		} else if (token.equals("call")) {
			return new Token(token, TokenTypes.callToken, 101);
		} else if (token.equals("if")) {
			return new Token(token, TokenTypes.ifToken, 102);
		} else if (token.equals("while")) {
			return new Token(token, TokenTypes.whileToken, 103);
		} else if (token.equals("return")) {
			return new Token(token, TokenTypes.returnToken, 104);
		} else if (token.equals("var")) {
			return new Token(token, TokenTypes.varToken, 110);
		} else if (token.equals("array")) {
			return new Token(token, TokenTypes.arrToken, 111);
		} else if (token.equals("function")) {
			return new Token(token, TokenTypes.funcToken, 112);
		} else if (token.equals("procedure")) {
			return new Token(token, TokenTypes.procToken, 113);
		} else if (token.equals("{")) {
			return new Token(token, TokenTypes.beginToken, 150);
		} else if (token.equals("main")) {
			return new Token(token, TokenTypes.mainToken, 200);
		} else if (token.equals("\0")) {
			return new Token(token, TokenTypes.eofToken, 255);
		} else {
			if (Character.isDigit(token.charAt(0)))
				return new Token(token, TokenTypes.number, 60);
			else if (Character.isLetter(token.charAt(0)))
				return new Token(token, TokenTypes.ident, 61);
			else
				return new Token(token, TokenTypes.errorToken, 0); //Invalid characters
		}
		
	}
	
	
	
}	

