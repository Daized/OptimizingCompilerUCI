package lexical;
import java.util.*;

import data.Instruction;
import data.Kind;
import data.Result;
import datastructures.BasicBlock;
import datastructures.ControlFlowGraph;
import nodes.*;

public class Parser {

	private List<Token> tokenList;
	private List<Instruction> instructionList;
	private ControlFlowGraph cfg;
	private int currentIndex;
	private Token currentToken;
	private HashMap<String, ArrayList<String>> herp; //
	private String[] predefined = { "InputNum" , "OutputNum", "OutputNewLine" };
	
public static void main(String[] args){
	
	if( args.length < 1){
		System.out.println("Error. No file given.");
		System.exit(1);
	}
	
	Tokenizer t = new Tokenizer();
	t.tokenize(args[0]);
	Parser p = new Parser(t.getTokenList());
	p.computation();
}
	
	/*
	 * Constructor for Parser class 
	 */
	public Parser(List<Token> tokenList){
		this.tokenList = tokenList;
		this.cfg = new ControlFlowGraph();
		this.instructionList = new ArrayList<Instruction>();
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
		BasicBlock root = new BasicBlock();
		if (currentToken.getTokenType() != TokenTypes.mainToken){
		System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		//variable declarations if they exist
		if ( currentToken.getTokenType() == TokenTypes.varToken ||currentToken.getTokenType() == TokenTypes.arrToken){
			while (currentToken.getTokenType() == TokenTypes.varToken ||	currentToken.getTokenType() == TokenTypes.arrToken){
					varDecl();				
			}
		}
			
		//function declarations if they exist
		if ( currentToken.getTokenType() == TokenTypes.funcToken || currentToken.getTokenType() == TokenTypes.procToken){
			while (currentToken.getTokenType() == TokenTypes.funcToken || currentToken.getTokenType() == TokenTypes.procToken){
				funcDecl();
			}	
		}
		
		if (currentToken.getTokenType() != TokenTypes.openbracketToken){
		System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		
		//Assuming mandatory
		statSequence();
		
		if (currentToken.getTokenType() != TokenTypes.closebracketToken){
		System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		
		if (currentToken.getTokenType() != TokenTypes.periodToken){
		System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		System.out.println("Compiled successfully.");
		//DONE
		
	}

	
	//stage 2
	public void funcBody(){
		
		if (currentToken.getTokenType() == TokenTypes.varToken ||
		    currentToken.getTokenType() == TokenTypes.arrToken){
				varDecl();				
			}	
		
		if (currentToken.getTokenType() != TokenTypes.openbracketToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		
		if (currentToken.getTokenType() == TokenTypes.letToken ||
			currentToken.getTokenType() == TokenTypes.callToken ||
			currentToken.getTokenType() == TokenTypes.ifToken ||
			currentToken.getTokenType() == TokenTypes.whileToken ||
			currentToken.getTokenType() == TokenTypes.returnToken){
			
			statSequence();			
		}
		
		if (currentToken.getTokenType() != TokenTypes.closebracketToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		
		
		
	}
	
	public void formalParam(){
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.ident){
			nextToken();
			if (currentToken.getTokenType() == TokenTypes.commaToken){
				while (currentToken.getTokenType() == TokenTypes.commaToken){
					nextToken();
					if (currentToken.getTokenType() != TokenTypes.ident){
						System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
					}
					nextToken();
				}				
			}
		}
		if (currentToken.getTokenType() != TokenTypes.closeparenToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken(); //Done
	}
	
	public void funcDecl(){
		nextToken();
		if (currentToken.getTokenType() != TokenTypes.ident){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.openparenToken){
			formalParam();
		}
		
		if (currentToken.getTokenType() != TokenTypes.semiToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		funcBody();
		
		if (currentToken.getTokenType() != TokenTypes.semiToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken(); //done	
	}
	
	public Result varDecl(){
		
		typeDecl();
		if (currentToken.getTokenType() != TokenTypes.ident){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.commaToken){
			while (currentToken.getTokenType() == TokenTypes.commaToken){
				nextToken();
				if (currentToken.getTokenType() != TokenTypes.ident){
					System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
				}
				nextToken();				
			}
		}
		if (currentToken.getTokenType() != TokenTypes.semiToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();	//done	
		return null;
	}
	
	public void typeDecl(){
		
		if (currentToken.getTokenType() == TokenTypes.varToken){
			nextToken();
			return; //VAR declaration, done
		}
		nextToken();
		if (currentToken.getTokenType() != TokenTypes.openbracketToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		
		while (currentToken.getTokenType() == TokenTypes.openbracketToken){
			nextToken();
			if (currentToken.getTokenType() != TokenTypes.number){
					System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
			}
			nextToken();
			if (currentToken.getTokenType() != TokenTypes.closebracketToken){
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
			}
			nextToken(); //done
		}
	}
	
	//stage 3
	
	/*
	 *  statSequence = statement { ";" statement }
	 */
	
	public Node[] statSequence(BasicBlock scope){
		Block firstBlock = null;
		Block lastBlock = null;
		Node statement = null;
		
		if (currentToken.getTokenType() == TokenTypes.letToken ||
				currentToken.getTokenType() == TokenTypes.callToken ||
				currentToken.getTokenType() == TokenTypes.returnToken){
		
			statement = statement(scope);
			firstBlock = new Block(Token.getType("statSeqToken"));
			lastBlock = firstBlock;
			lastBlock.appendStatement(statement);
		}
		else if (currentToken.getTokenType() == TokenTypes.whileToken){
			statement = statement(scope);
		}
		else if (currentToken.getTokenType() == TokenTypes.ifToken){
			
		}
		
		if (currentToken.getTokenType() == TokenTypes.SEMICOLON){			
				nextToken();
				statement();
			}

		}
	}
	
	/*
	 * state = assignment | funcCall | ifStatement | whileStatement | returnStatement
	 */
	
	public Node statement(BasicBlock scope){
		
		Node statementNode = null;
		switch(currentToken.getTokenType()){
			case letToken:
				statementNode = assignment(scope);
				break;
			case callToken:
				statementNode = funcCall(scope);
				break;
			case ifToken:
				statementNode = ifStatement(scope);
				break;
			case whileToken:
				statementNode = whileStatement(scope);
				break;
			case returnToken:
				statementNode = returnStatement(scope);
				break;
			default:
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
				
		}
		//done
		return statementNode;
	}
	
	//state 4
	
	/*
	 *  returnStatement = "return" [ expression ]
	 */
	
	public Node returnStatement(BasicBlock scope){
		Node returnNode = null;
		returnNode = new Node(currentToken);
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.ident ||
			currentToken.getTokenType() == TokenTypes.number ||
			currentToken.getTokenType() == TokenTypes.openparenToken ||
			currentToken.getTokenType() == TokenTypes.callToken){
			
			Node expression = expression(scope);
			returnNode.setLeftNode(expression);
		}
		else {
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		
		return returnNode;
	}
	
	public void whileStatement(BasicBlock scope){
		nextToken();
		relation();
		if (currentToken.getTokenType() != TokenTypes.doToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		statSequence();
		if (currentToken.getTokenType() != TokenTypes.odToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		//done
	}
	
	public void ifStatement(BasicBlock scope){
		nextToken();
		relation();
		if (currentToken.getTokenType() != TokenTypes.thenToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		statSequence();
		if (currentToken.getTokenType() == TokenTypes.elseToken){
			nextToken();
			statSequence();
		}
		if (currentToken.getTokenType() != TokenTypes.fiToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();		
		//done
	}
	
	public Node funcCall(BasicBlock scope){
		nextToken();
		if (currentToken.getTokenType() != TokenTypes.ident){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.openparenToken){
			nextToken();
			
			if (currentToken.getTokenType() == TokenTypes.number ||
				currentToken.getTokenType() == TokenTypes.openparenToken ||
				currentToken.getTokenType() == TokenTypes.callToken ||
				currentToken.getTokenType() == TokenTypes.ident){
				
				expression();
				//Multiple arguments
				if (currentToken.getTokenType() == TokenTypes.commaToken){
					
					while (currentToken.getTokenType() == TokenTypes.commaToken){
						nextToken();
						expression();
					}
				}
			}

			if (currentToken.getTokenType() != TokenTypes.closeparenToken){
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
			}
			nextToken();
		}
		//done
	}
	
	/*
	 *  "let" designator "<-" expression
	 */
	public Node assignment(BasicBlock scope){
		nextToken();
		Result left = designator(scope);
		if (currentToken.getTokenType() != TokenTypes.becomesToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		Result right = expression(scope);
		
		//TODO: something regarding scope here
		
		return assignmentNode;
	}
	
	//stage 5
	
	/*
	 * relation = expression relOp expression
	 */
	
	public Result relation(BasicBlock scope){
		Result relation;
		relation = expression(scope);
		relation.setKind(Kind.RELATION);
		if (currentToken.getTokenType() == TokenTypes.eqlToken ||
			currentToken.getTokenType() == TokenTypes.leqToken ||
			currentToken.getTokenType() == TokenTypes.lssToken ||
			currentToken.getTokenType() == TokenTypes.gtrToken ||
			currentToken.getTokenType() == TokenTypes.geqToken ||
			currentToken.getTokenType() == TokenTypes.neqToken){
			//if it's a relation operator
			relation = new Node(currentToken);
			nextToken();
			Node rightExp = expression(scope);
			relation.setLeftNode(leftExp);
			relation.setRightNode(rightExp);
		}
		else {
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		//done
		return relation;
	}
	/*
	 * expression = term { ("+" | "-") term }
	 */
	public Result expression(BasicBlock scope){
		Result x, y;
		x = term(scope);
		if (currentToken.getTokenType() == TokenTypes.plusToken || currentToken.getTokenType() == TokenTypes.minusToken){
			TokenTypes op = currentToken.getTokenType();
			nextToken();
			y = expression(scope);
			x = combine(x, y, op);
		}
		//end
		return x;
	}
	
	/*
	 *  term = factor { ("*" | "/") factor }
	 */
	public Result term(BasicBlock scope){
		Result x, y;
		x = factor(scope);
		
		if (currentToken.getTokenType() == TokenTypes.timesToken || currentToken.getTokenType() == TokenTypes.divToken){
			TokenTypes op = currentToken.getTokenType();
			nextToken();
			y = term(scope);
			x = combine(x, y, op);
		}
		//end
		return x;
	}
	
	/*
	 * factor = designator | number | "(" expression ")" | funcCall
	 */
	
	public Result factor(BasicBlock scope){
		Result factor = null;
		if (currentToken.getTokenType() == TokenTypes.ident)
			factor = designator(scope);
		else if (currentToken.getTokenType() == TokenTypes.number){
			factor = new Result(Kind.CONSTANT);
			factor.setConstVal(Integer.parseInt(currentToken.getTokenString()));
			nextToken();
		}
		else if (currentToken.getTokenType() == TokenTypes.callToken)
			factor = funcCall(scope);
		else if (currentToken.getTokenType() == TokenTypes.openparenToken){
			nextToken();
			factor = expression(scope);
			if (currentToken.getTokenType() != TokenTypes.closeparenToken){
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
			}
			nextToken();
		}
		else {
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		
		return factor;
		
	}
	
	/*
	 * designator = ident{ "[" expression "]" }
	 */
	
	public Result designator(BasicBlock scope){
		Result designator;
		if (currentToken.getTokenType() != TokenTypes.ident){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		
		designator = new Result(Kind.VAR);
		
		if (true){
			/* CHECK IF IDENTIFIER IS IN SCOPE */
		}
		
		
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.openbracketToken)
			designator.setKind(Kind.ARRAY);
		while (currentToken.getTokenType() == TokenTypes.openbracketToken){
			nextToken();
			Result y = expression(scope);
			designator.appendArrayDimension(y.getConstVal());
			if (currentToken.getTokenType() != TokenTypes.closebracketToken){
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
			}
			nextToken();
		}
		
		//TODO: SCOPE WORK ON THIS PART
		
		return designator;
	}
	
	
	
	

	/*
	 * Combining results is useful for terms and expressions
	 */
	
	public Result combine(Result x, Result y, TokenTypes operation){
		Result combine = new Result(Kind.CONSTANT);
		switch (operation){
		case timesToken:
			
			
			
		case divToken:
			
			
			
			
		case plusToken:
		
		
		
		
		case minusToken:
		
		default:
			System.out.println("Incorrect operation.");
			System.exit(0);
		}
		
		return combine;
	}

	
	
	
}
