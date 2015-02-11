package lexical;
import java.util.*;

import lexical.Token.TokenTypes;

public class Parser {

	private List<Token> tokenList;
	private int currentIndex;
	private Token currentToken;
	
public static void main(String[] args){
	
	if( args.length < 1){
		System.out.println("Error. No file given.");
		System.exit(1);
	}
	
	Tokenizer t = new Tokenizer();
	t.tokenize(args[0]);
}
	
	/*
	 * Constructor for Parser class 
	 */
	public Parser(List<Token> tokenList){
		this.tokenList = tokenList;
		this.currentIndex = 0;
		this.currentToken = tokenList.get(0);
	}
	
	
	/*
	 * This function is what is used to iterate through our tokenList obtained from file.
	 */
	public void nextToken(){
		this.currentIndex++;
		if (currentIndex < tokenList.size()){
			this.currentToken = this.tokenList.get(this.currentIndex);
		}
		else {
			System.out.println("Error. Went over file boundary!");
			System.exit(2);
		}
	}
	
	
	/*
	 * Now begins our recursive descent tree. We start at the top with computation
	 * and work our way down. We already identified identifiers and numbers from
	 * the project specification for the tree, so no need to analyze their digits and 
	 * letters again. We break this into blocks of the analysis.
	 */
	public void computation(){
		if (currentToken.getTokenType() != TokenTypes.MAIN){
		//syntax error
		}
		nextToken();
		//variable declarations if they exist
		if ( currentToken.getTokenType() == TokenTypes.VAR ||currentToken.getTokenType() == TokenTypes.ARRAY){
			while (currentToken.getTokenType() == TokenTypes.VAR ||	currentToken.getTokenType() == TokenTypes.ARRAY){
					varDecl();				
			}
		}
			
		//function declarations if they exist
		if ( currentToken.getTokenType() == TokenTypes.FUNCTION || currentToken.getTokenType() == TokenTypes.PROCEDURE){
			while (currentToken.getTokenType() == TokenTypes.FUNCTION || currentToken.getTokenType() == TokenTypes.PROCEDURE){
				funcDecl();
			}	
		}
		
		if (currentToken.getTokenType() != TokenTypes.LSBRACKET){
		//syntax error
		}
		nextToken();
		
		//Assuming mandatory
		statSequence();
		
		if (currentToken.getTokenType() != TokenTypes.RSBRACKET){
		//syntax error
		}
		nextToken();
		
		if (currentToken.getTokenType() != TokenTypes.DOT){
		//syntax error
		}
		
		//DONE
		
	}

	
	//stage 2
	public void funcBody(){
		
		if (currentToken.getTokenType() == TokenTypes.VAR ||
		    currentToken.getTokenType() == TokenTypes.ARRAY){
				varDecl();				
			}	
		
		if (currentToken.getTokenType() != TokenTypes.LSBRACKET){
			//syntax error
		}
		nextToken();
		
		if (currentToken.getTokenType() == TokenTypes.LET ||
			currentToken.getTokenType() == TokenTypes.CALL ||
			currentToken.getTokenType() == TokenTypes.IF ||
			currentToken.getTokenType() == TokenTypes.WHILE ||
			currentToken.getTokenType() == TokenTypes.RETURN){
			
			statSequence();			
		}
		
		if (currentToken.getTokenType() != TokenTypes.RSBRACKET){
			//syntax error
		}
		nextToken();
		
		
		
	}
	
	public void formalParam(){
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.IDENT){
			nextToken();
			if (currentToken.getTokenType() == TokenTypes.COMMA){
				while (currentToken.getTokenType() == TokenTypes.COMMA){
					nextToken();
					if (currentToken.getTokenType() != TokenTypes.IDENT){
						//syntax error
					}
					nextToken();
				}				
			}
		}
		if (currentToken.getTokenType() != TokenTypes.RPAREN){
			//syntax error
		}
		nextToken(); //Done
	}
	
	public void funcDecl(){
		nextToken();
		if (currentToken.getTokenType() != TokenTypes.IDENT){
			//syntax error
		}
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.LPAREN){
			formalParam();
		}
		
		if (currentToken.getTokenType() != TokenTypes.SEMICOLON){
			//syntax error
		}
		nextToken();
		funcBody();
		
		if (currentToken.getTokenType() != TokenTypes.SEMICOLON){
			//syntax error
		}
		nextToken(); //done	
	}
	
	public void varDecl(){
		
		typeDecl();
		if (currentToken.getTokenType() != TokenTypes.IDENT){
			//syntax error
		}
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.COMMA){
			while (currentToken.getTokenType() == TokenTypes.COMMA){
				nextToken();
				if (currentToken.getTokenType() != TokenTypes.IDENT){
					//syntax error
				}
				nextToken();				
			}
		}
		if (currentToken.getTokenType() != TokenTypes.SEMICOLON){
			//syntax error
		}
		nextToken();	//done	
	}
	
	public void typeDecl(){
		
		if (currentToken.getTokenType() == TokenTypes.VAR){
			nextToken();
			return; //VAR declaration, done
		}
		nextToken();
		if (currentToken.getTokenType() != TokenTypes.LBRACKET){
			//syntax error
		}
		
		while (currentToken.getTokenType() == TokenTypes.LBRACKET){
			nextToken();
			if (currentToken.getTokenType() != TokenTypes.NUMBER){
					//Syntax error
			}
			nextToken();
			if (currentToken.getTokenType() != TokenTypes.RBRACKET){
				//syntax error
			}
			nextToken(); //done
		}
	}
	
	//stage 3
	public void statSequence(){
		statement();
		if (currentToken.getTokenType() == TokenTypes.SEMICOLON){			
			while (currentToken.getTokenType() == TokenTypes.SEMICOLON){
				nextToken();
				statement();
			}

		}
	}
	
	public void statement(){
		
		switch(currentToken.getTokenType()){
			case LET:
				assignment();
				break;
			case CALL:
				funcCall();
				break;
			case IF:
				ifStatement();
				break;
			case WHILE:
				whileStatement();
				break;
			case RETURN:
				returnStatement();
				break;
			default:
				//syntax error
				
		}
		//done
		
	}
	
	//state 4
	
	public void returnStatement(){
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.IDENT ||
			currentToken.getTokenType() == TokenTypes.NUMBER ||
			currentToken.getTokenType() == TokenTypes.LPAREN ||
			currentToken.getTokenType() == TokenTypes.CALL ||
			currentToken.getTokenType() == TokenTypes.IDENT){
			
			expression();
		}
		else {
			//syntax error
		}
		
	}
	
	public void whileStatement(){
		nextToken();
		relation();
		if (currentToken.getTokenType() != TokenTypes.DO){
			//syntax error
		}
		nextToken();
		statSequence();
		if (currentToken.getTokenType() != TokenTypes.OD){
			//syntax error
		}
		nextToken();
		//done
	}
	
	public void ifStatement(){
		nextToken();
		relation();
		if (currentToken.getTokenType() != TokenTypes.THEN){
			//syntax error
		}
		nextToken();
		statSequence();
		if (currentToken.getTokenType() == TokenTypes.ELSE){
			nextToken();
			statSequence();
		}
		if (currentToken.getTokenType() != TokenTypes.FI){
			//syntax error
		}
		nextToken();		
		//done
	}
	
	public void funcCall(){
		nextToken();
		if (currentToken.getTokenType() != TokenTypes.IDENT){
			//syntax error
		}
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.LPAREN){
			nextToken();
			
			if (currentToken.getTokenType() == TokenTypes.IDENT ||
				currentToken.getTokenType() == TokenTypes.NUMBER ||
				currentToken.getTokenType() == TokenTypes.LPAREN ||
				currentToken.getTokenType() == TokenTypes.CALL ||
				currentToken.getTokenType() == TokenTypes.IDENT){
				
				expression();
				//Multiple arguments
				if (currentToken.getTokenType() == TokenTypes.COMMA){
					
					while (currentToken.getTokenType() == TokenTypes.COMMA){
						nextToken();
						expression();
					}
				}
			}

			if (currentToken.getTokenType() != TokenTypes.RPAREN){
				//syntax error
			}
			nextToken();
		}
		//done
	}
	
	public void assignment(){
		nextToken();
		designator();
		if (currentToken.getTokenType() != TokenTypes.ASSIGNMENT){
			//syntax error
		}
		nextToken();
		expression();
		//done
	}
	
	//stage 5
	public void relation(){
		expression();
		if (currentToken.getTokenType() != TokenTypes.EQUALS &&
			currentToken.getTokenType() != TokenTypes.LEQUAL &&
			currentToken.getTokenType() != TokenTypes.LESS &&
			currentToken.getTokenType() != TokenTypes.GREATER &&
			currentToken.getTokenType() != TokenTypes.GEQUAL &&
			currentToken.getTokenType() != TokenTypes.DNEQUAL){
			//syntax error
		}
		expression();
		//done
	}
	
	public void expression(){
		term();
		if (currentToken.getTokenType() == TokenTypes.PLUS || currentToken.getTokenType() == TokenTypes.MINUS){
			while(currentToken.getTokenType() == TokenTypes.PLUS || currentToken.getTokenType() == TokenTypes.MINUS){
				nextToken();
				term();
			}
		}
		//end
		
	}
	
	public void term(){
		factor();
		if (currentToken.getTokenType() == TokenTypes.TIMES || currentToken.getTokenType() == TokenTypes.DIVIDE){
			while(currentToken.getTokenType() == TokenTypes.TIMES || currentToken.getTokenType() == TokenTypes.DIVIDE){
				nextToken();
				factor();
			}
		}
		//end
		
	}
	
	public void factor(){
		if (currentToken.getTokenType() == TokenTypes.IDENT)
			designator();
		else if (currentToken.getTokenType() == TokenTypes.NUMBER)
			nextToken();
		else if (currentToken.getTokenType() == TokenTypes.CALL)
			funcCall();
		else if (currentToken.getTokenType() == TokenTypes.LPAREN){
			nextToken();
			expression();
			if (currentToken.getTokenType() != TokenTypes.RPAREN){
				//syntax error
			}
			nextToken();
		}
		else {
			//syntax error
		}
		
	}
	
	public void designator(){
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.LBRACKET){
			while (currentToken.getTokenType() == TokenTypes.LBRACKET){
				nextToken();
				expression();
				if (currentToken.getTokenType() != TokenTypes.RBRACKET){
					//syntax error
				}
				nextToken();
			}
		}
		
	}
	
	
	
	




	
	
	
}
