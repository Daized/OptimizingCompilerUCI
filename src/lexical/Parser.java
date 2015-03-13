package lexical;
import java.util.*;

import data.Instruction;
import data.Kind;
import data.OpCodes;
import data.Result;
import datastructures.BasicBlock;
import datastructures.ControlFlowGraph;
import datastructures.Function;
import datastructures.Symbol;

public class Parser {

	private List<Token> tokenList;
	private Function main;
	private Map<String, Function> functions; 
	private int currentIndex;
	private Token currentToken;
	private String[] predefined = { "InputNum" , "OutputNum", "OutputNewLine" };
	public String fileName;
	
public static void main(String[] args){
	
	if( args.length < 1){
		System.out.println("Error. No file given.");
		System.exit(1);
	}
	
	Tokenizer t = new Tokenizer();
	t.tokenize(args[0]);
	List<Parser> parsers = new ArrayList<Parser>();
	Parser p = new Parser(t.getTokenList(), t.getFileName());
	p.computation();
	
	
}
	
	/*
	 * Constructor for Parser class 
	 */
	public Parser(List<Token> tokenList, String fileName){
		this.tokenList = tokenList;
		this.main = new Function("main");
		this.currentIndex = 0;
		this.currentToken = tokenList.get(0);
		this.fileName = fileName;
		this.functions = new HashMap<String, Function>();
		//TODO: add predefined functions
	}
	
	/*
	 * Get main Function with CFG/Instructions
	 */
	public Function getMain(){
		return this.main;
	}
	
	/*
	 * 
	 */
	public Map<String, Function> getFunctions(){
		return this.functions;
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
		if (currentToken.getTokenType() != TokenTypes.mainToken){
		System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		//variable declarations if they exist
		if ( currentToken.getTokenType() == TokenTypes.varToken ||currentToken.getTokenType() == TokenTypes.arrToken){
			while (currentToken.getTokenType() == TokenTypes.varToken ||	currentToken.getTokenType() == TokenTypes.arrToken){
					varDecl(main);				
			}
		}
			
		//function declarations if they exist
		if ( currentToken.getTokenType() == TokenTypes.funcToken || currentToken.getTokenType() == TokenTypes.procToken){
			while (currentToken.getTokenType() == TokenTypes.funcToken || currentToken.getTokenType() == TokenTypes.procToken){
				funcDecl();
			}	
		}
		
		if (currentToken.getTokenType() != TokenTypes.beginToken){
		System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		
		//Assuming mandatory
		Result x = statSequence(main);
		
		if (currentToken.getTokenType() != TokenTypes.endToken){
		System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		
		if (currentToken.getTokenType() != TokenTypes.periodToken){
		System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		
		Helper.addInstruction(OpCodes.end,  main, null, null);
		
		System.out.println( this.fileName + ": Compiled successfully.");
		//DONE
		
	}

	
	//stage 2
	public void funcBody(Function scope){
		
		while (currentToken.getTokenType() == TokenTypes.varToken ||
		    currentToken.getTokenType() == TokenTypes.arrToken){
				varDecl(scope);				
			}	
		//function body
		if (currentToken.getTokenType() != TokenTypes.beginToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		
		if (currentToken.getTokenType() == TokenTypes.letToken ||
			currentToken.getTokenType() == TokenTypes.callToken ||
			currentToken.getTokenType() == TokenTypes.ifToken ||
			currentToken.getTokenType() == TokenTypes.whileToken ||
			currentToken.getTokenType() == TokenTypes.returnToken){
			
			statSequence(scope);			
		}
		
		if (currentToken.getTokenType() != TokenTypes.endToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		
		
		
	}
	
	public void formalParam(Function scope){
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.ident){
			scope.getSymbolTable().addSymbol(new Symbol(currentToken.getTokenString(), -1, 0, Kind.VAR));
			nextToken();
			if (currentToken.getTokenType() == TokenTypes.commaToken){
				while (currentToken.getTokenType() == TokenTypes.commaToken){
					nextToken();
					if (currentToken.getTokenType() != TokenTypes.ident){
						System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
					}
					scope.getSymbolTable().addSymbol(new Symbol(currentToken.getTokenString(), -1, 0, Kind.VAR));
					nextToken();
				}				
			}
		}
		if (currentToken.getTokenType() != TokenTypes.closeparenToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken(); //Done
		return;
	}
	
	public void funcDecl(){
		nextToken();
		Function function = new Function(currentToken.getTokenString());
		function.getSymbolTable().setMainSymbolTable(main.getSymbolTable());
		this.functions.put(currentToken.getTokenString(), function);
		//Parser functionParser = new Parser();
		//functionParser.setName("")
		//functionParser.subComputation();
		//addToParsers(functionParser);
		
		if (currentToken.getTokenType() != TokenTypes.ident){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.openparenToken){
			formalParam(function);
		}
		
		if (currentToken.getTokenType() != TokenTypes.semiToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		funcBody(function);
		
		if (currentToken.getTokenType() != TokenTypes.semiToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken(); //done	
	}
	
	public Result varDecl(Function scope){
		
		Result x = typeDecl(scope);
		Kind type = x.getKind();
		if (currentToken.getTokenType() != TokenTypes.ident){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		Helper.variableDeclaration(currentToken.getTokenString(), type, x.getArrayDimensions(), scope);
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.commaToken){
			while (currentToken.getTokenType() == TokenTypes.commaToken){
				nextToken();
				if (currentToken.getTokenType() != TokenTypes.ident){
					System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
				}
				Helper.variableDeclaration(currentToken.getTokenString(), type, x.getArrayDimensions(), scope);
				nextToken();				
			}
		}
		if (currentToken.getTokenType() != TokenTypes.semiToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();	//done	
		return x;
	}
	
	public Result typeDecl(Function scope){
		
		Result x = null;
		if (currentToken.getTokenType() == TokenTypes.varToken){
			x = new Result(Kind.VAR);
			nextToken();
			return x; //VAR declaration, done
			//TODO: Address?
		}
		nextToken();
		x = new Result(Kind.ARRAY);
		if (currentToken.getTokenType() != TokenTypes.openbracketToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		List<Integer> arrayDimensions = new ArrayList<Integer>();
		while (currentToken.getTokenType() == TokenTypes.openbracketToken){
			nextToken();
			if (currentToken.getTokenType() != TokenTypes.number){
					System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
			}
			arrayDimensions.add(Integer.parseInt(currentToken.getTokenString()));
			nextToken();
			if (currentToken.getTokenType() != TokenTypes.closebracketToken){
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
			}
			nextToken(); //done
		}
		x.setArrayDimensions(arrayDimensions);
		return x;
	}
	
	//stage 3
	
	/*
	 *  statSequence = statement { ";" statement }
	 */
	
	public Result statSequence(Function scope){
		Result x = null, y = null;
		ControlFlowGraph cfg = scope.getCFG();
		scope.getCFG().getNextBlock();
		if (currentToken.getTokenType() == TokenTypes.letToken ||
				currentToken.getTokenType() == TokenTypes.callToken ||
				currentToken.getTokenType() == TokenTypes.returnToken ||
			    currentToken.getTokenType() == TokenTypes.whileToken ||
			    currentToken.getTokenType() == TokenTypes.ifToken){
			
			x = statement(scope);
			
		} else {
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		while (currentToken.getTokenType() == TokenTypes.semiToken){	
			nextToken();
				if (currentToken.getTokenType() == TokenTypes.letToken ||
						currentToken.getTokenType() == TokenTypes.callToken ||
						currentToken.getTokenType() == TokenTypes.returnToken ||
					    currentToken.getTokenType() == TokenTypes.whileToken ||
					    currentToken.getTokenType() == TokenTypes.ifToken){
					
						y = statement(scope);
						//if (y.getJoin() != null) {
			           //     if (x.getJoin() != null) {
			            //        throw new RuntimeException("Joinblock clasing?");
			            //    }
						if (y.getJoin() != null)
			                x.setJoin(y.getJoin());
			           // }
						//
				}			
		 else {
					System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
			}
		}
		return x;
		}

	
	/*
	 * state = assignment | funcCall | ifStatement | whileStatement | returnStatement
	 */
	
	public Result statement(Function scope){
		
		Result statement = null;
		switch(currentToken.getTokenType()){
			case letToken:
				statement = assignment(scope);
				//Create instruction here
				//Then add instruction to next block
				break;
			case callToken:
				statement = funcCall(scope);
				//Create instruction here
				//Then add instruction to next block
				break;
			case ifToken:
				statement = ifStatement(scope);
				//Create instructions for ifStatement and global instruction list
				//Then add instructions to next block and global instruction list
				break;
			case whileToken:
				statement = whileStatement(scope);
				//Create instructions for whileLoop and global instruction list
				//Then add instructions to next block and global instruction list
				break;
			case returnToken:
				statement = returnStatement(scope);
				//Create instruction here
				//Then add instruction to next block
				break;
			default:
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
				
		}
		//done
		return statement;
	}
	
	//state 4
	
	/*
	 *  returnStatement = "return" [ expression ]
	 */
	
	public Result returnStatement(Function scope){
		Result x = null;
		nextToken();
		if (currentToken.getTokenType() == TokenTypes.ident ||
			currentToken.getTokenType() == TokenTypes.number ||
			currentToken.getTokenType() == TokenTypes.openparenToken ||
			currentToken.getTokenType() == TokenTypes.callToken){
			
			x = expression(scope);
		}
		else {
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		Helper.addInstruction(OpCodes.ret, scope, x, null);
		
		return x;
	}
	
	public Result whileStatement(Function scope){
		int loop = scope.getProgramCounter();
		BasicBlock nextBlock = scope.getCFG().getNextBlock();
		Set<BasicBlock> parents = nextBlock.getParents();
		if (parents.size() > 1){
			BasicBlock child = new BasicBlock();
			nextBlock.addChild(child, true);
			scope.getCFG().setNextBlock(child);			
		}
		
		BasicBlock previous = scope.getCFG().getNextBlock();
		BasicBlock current = new BasicBlock();
		current.setBlockKind(Kind.LOOPHEAD);
		previous.addChild(current, false);
		scope.getCFG().setNextBlock(current);
		BasicBlock loopBlock = scope.getCFG().getNextBlock();
		
		nextToken();
		Result x = relation(scope);
		//AuxiliaryFunctions.CJF(code, x, getSymbolTable()); ????
		Helper.CJF(scope, x);
		
		parents = current.getParents();
		BasicBlock parent = null;
		for (BasicBlock p: parents){
			parent = p;
			break;
		}
		
		if (parent == null){
			parent = new BasicBlock();
		}
		
		BasicBlock join = current;
		join.setLeft(parent);
		x.setJoin(join);
		
		current.setJoin(join);
		
		if (currentToken.getTokenType() != TokenTypes.doToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		
		BasicBlock right = new BasicBlock();
		right.setBlockKind(Kind.LOOPBODY);
		current.addChild(right, true);
		join.setRight(right);
		scope.getCFG().setNextBlock(right);
		
		Result rightTree = statSequence(scope);
		if(rightTree.getJoin() != null) {
	            join.setRight(rightTree.getJoin());
	            rightTree.getJoin().addChild(loopBlock, false); 
		 } else {
	            right.addChild(loopBlock, false);
	            
		 }
		
		//AuxiliaryFunctions.BJ(code, loop); //Backward Jump to the loop beginning.
		Helper.BJ(scope, loop);
		Helper.createPhiInstructions(scope, join);
		//code.Fixup(x.fixupLoc());
		scope.fixUp(x.getFixuploc());
        final BasicBlock followBlock = new BasicBlock();
        followBlock.setBlockKind(Kind.LOOPFOLLOW);
        join.addChild(followBlock, true); //Don't forget dominator information

        scope.getCFG().setNextBlock(followBlock);
		
		if (currentToken.getTokenType() != TokenTypes.odToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		//done
		x.setJoin(followBlock);
		return x;
	}
	
	public Result ifStatement(Function scope){
		nextToken();
		Result ifStatement, elseStatement, left, right;
		ifStatement = relation(scope);
		//Do something here with branching instructions hopefully, CONDITIONAL JUMP FORWARD , CJF
		Helper.CJF(scope, ifStatement);
		
		BasicBlock nextBlock = scope.getCFG().getNextBlock();
		BasicBlock joinBlock = new BasicBlock();
		joinBlock.setBlockKind(Kind.IF);
		
		ifStatement.setJoin(joinBlock);
		nextBlock.setJoin(joinBlock);
		
		BasicBlock ifBlock = new BasicBlock();
		joinBlock.setLeft(ifBlock);
		ifBlock.setJoin(joinBlock);
		nextBlock.addChild(ifBlock, true); //TODO: ADD DOMINATOR TREE INFORMATION TOO
		
		scope.getCFG().setNextBlock(ifBlock);
		
		if (currentToken.getTokenType() != TokenTypes.thenToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		left = statSequence(scope);
		if (left.getJoin() != null && left.getJoin() != joinBlock){ //In case of inner if blocks/while loops
			joinBlock.setLeft(left.getJoin());
		}
		
		//Probably need to do something like a fixup or something
		if (currentToken.getTokenType() == TokenTypes.elseToken){
			nextToken();
			elseStatement = new Result(Kind.RELATION);
			//elseState.fixupLoc(scope.getCFG().getInstructionListSize)
			//BJ(scope.getCFG, elseStatement.getFixUpLoc())
			//Do something here with branching instructions hopefully
			elseStatement.fixupLoc(scope.getProgramCounter());
			Helper.BJ(scope, elseStatement.getFixuploc());
			
			BasicBlock elseBlock = new BasicBlock();
			joinBlock.setRight(elseBlock);
			elseBlock.setJoin(joinBlock);
			nextBlock.addChild(elseBlock, true);
			
			scope.getCFG().setNextBlock(elseBlock);
			//FJlink????
			scope.fixUp(ifStatement.getFixuploc());
			
			right = statSequence(scope);
			if (right.getJoin() != null && right.getJoin() != joinBlock){ //In case of inner if blocks/while loops
				joinBlock.setRight(right.getJoin());
			}
			scope.fixUp(elseStatement.getFixuploc());
			
		}
		
		else {
			joinBlock.setRight(nextBlock);
			scope.fixUp(ifStatement.getFixuploc());
		}
		
		Helper.createPhiInstructions(scope, joinBlock);
		nextBlock.addDominatingBlocks(joinBlock);
		
		if (currentToken.getTokenType() != TokenTypes.fiToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();		
		
        if(joinBlock.getLeft() != null) {
            joinBlock.getLeft().addChild(joinBlock, false);
        }
        if(joinBlock.getRight() != null) {
            joinBlock.getRight().addChild(joinBlock, false);
        }
        
        scope.getCFG().setNextBlock(joinBlock);
		
		//done
		return ifStatement;
	}
	
	public Result funcCall(Function scope){
		Result x = new Result(Kind.CONSTANT); //ignore for now
		x.setConstVal(5);
		Token procedure;
		nextToken();
		if (currentToken.getTokenType() != TokenTypes.ident){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		procedure = currentToken;
		Instruction instruction;
		Result y = new Result(Kind.INTERMEDIATE);
		y.setIntermediateLocation(scope.getProgramCounter());
		nextToken();
		if (procedure.getTokenString().equals("InputNum")) {
			instruction = Helper.addInstruction(OpCodes.read, scope, null, null);
			if (currentToken.getTokenType() != TokenTypes.openparenToken) {
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());
				System.exit(3);
			}
			nextToken();
			if (currentToken.getTokenType() != TokenTypes.closeparenToken) {
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());
				System.exit(3);
			}
			nextToken();
			y.setIntermediateLocation(scope.getProgramCounter());
			return y;
		} else if (procedure.getTokenString().equals("OutputNum")) {
			if (currentToken.getTokenType() != TokenTypes.openparenToken) {
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());
				System.exit(3);
			}

			nextToken();
			x = expression(scope);
			instruction = Helper.addInstruction(OpCodes.write, scope, x, null);
			if (currentToken.getTokenType() != TokenTypes.closeparenToken) {
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());
				System.exit(3);
			}
			nextToken();
			y.setIntermediateLocation(scope.getProgramCounter());
			return y;
		} else if (procedure.getTokenString().equals("OutputNewLine")) {
			instruction = Helper.addInstruction(OpCodes.wln, scope, null, null);
			if (currentToken.getTokenType() != TokenTypes.openparenToken) {
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());
				System.exit(3);
			}
			nextToken();
			if (currentToken.getTokenType() != TokenTypes.closeparenToken) {
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());
				System.exit(3);
			}
			nextToken();
			y.setIntermediateLocation(scope.getProgramCounter());
			return y;
		} else {
		}

		if (currentToken.getTokenType() == TokenTypes.openparenToken){
			nextToken();

			if (currentToken.getTokenType() == TokenTypes.number ||
				currentToken.getTokenType() == TokenTypes.openparenToken ||
				currentToken.getTokenType() == TokenTypes.callToken ||
				currentToken.getTokenType() == TokenTypes.ident){

				Result y2 = expression(scope);
				//Multiple arguments
				if (currentToken.getTokenType() == TokenTypes.commaToken){

					while (currentToken.getTokenType() == TokenTypes.commaToken){
						nextToken();
						y2 = expression(scope);
					}
				}
			}

			if (currentToken.getTokenType() != TokenTypes.closeparenToken){
				System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
			}
			nextToken();
		}
		return x;
		//done
	}

	/*
	 *  "let" designator "<-" expression
	 */
	public Result assignment(Function scope){
		nextToken();
		Result left = designator(scope);
		if (currentToken.getTokenType() != TokenTypes.becomesToken){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		nextToken();
		Symbol leftSymbol = scope.getSymbolTable().getRecentOccurence(left.getVariableName());
        //final Computation mainProgram = this.getOutputContents().getMainProgram();
       // if(code.getProgramName() != null && recentLHS.isGlobal() && !mainProgram.getProgramName().equals(code.getProgramName())) {
        //    AuxiliaryFunctions.addKillInstruction(mainProgram.getCode(), recentLHS);
        //}


		Result right = expression(scope);

		if (left.getKind() != Kind.ARRAY && right.getKind() != Kind.ARRAY){
			Result x = new Result(Kind.VAR);
			x.setVarName(left.getVariableName());
			Helper.addMoveInstruction(scope, x, right);
			return x;
		}

		Result x;
		if (left.getKind() == Kind.ARRAY){
			final List<Integer> arrayDimensions = left.getArrayDimensions();
			final List<Result> d = getResultToConstant(arrayDimensions);
			Helper.createAddA(scope, left.getVariableName(), d);
			x = new Result(Kind.INTERMEDIATE);
			x.setIntermediateLocation(scope.getProgramCounter() - 1);
			Result storeInstruction;
			if (right.getKind() == Kind.ARRAY){
				Helper.loadYarray(right, scope);
				storeInstruction = new Result(Kind.INTERMEDIATE);
				storeInstruction.setIntermediateLocation(scope.getProgramCounter()-1);
			}
			else {
				storeInstruction = right;
			}
			Helper.addInstruction(OpCodes.store, scope, x, storeInstruction);
			Symbol recent = scope.getSymbolTable().getRecentOccurence(left.getVariableName());
			Helper.addKillInstruction(scope, recent);
		}
		else {
			x = new Result(Kind.VAR);
			x.setVarName(left.getVariableName());
			Result moveInstruction;
			if (right.getKind() == Kind.ARRAY){
				if (right.getKind() == Kind.ARRAY){
					Helper.loadYarray(right, scope);

				}
				String name = right.getVariableName();
				List<Result> arrayValues = right.getArrayValues();
				if (arrayValues != null && arrayValues.size() > 0){
					Helper.createAddA(scope, name, arrayValues);
					Result loadInstruction = new Result(Kind.INTERMEDIATE);
					loadInstruction.setIntermediateLocation(scope.getProgramCounter() - 1);
					Helper.addInstruction(OpCodes.load, scope, loadInstruction, null);
				}

				moveInstruction = new Result(Kind.INTERMEDIATE);
				moveInstruction.setIntermediateLocation(scope.getProgramCounter() - 1);
			}
			else {
				moveInstruction = right;
			}
			Helper.addMoveInstruction(scope, x, moveInstruction);
		}

		return x;
	}

	public static List<Result> getResultToConstant(List<Integer> arrayDimensions) {
		final List<Result> d = new ArrayList<Result>();
		for (Integer arrayDimension : arrayDimensions) {
            final Result result = new Result(Kind.CONSTANT);
            result.setConstVal(arrayDimension);
            d.add(result);
        }
		return d;
	}

	//stage 5
	
	/*
	 * relation = expression relOp expression
	 */
	
	public Result relation(Function scope){
		Result relation, x, y;
		x = expression(scope);
		relation = new Result(Kind.RELATION);
		if (currentToken.getTokenType() == TokenTypes.eqlToken ||
			currentToken.getTokenType() == TokenTypes.leqToken ||
			currentToken.getTokenType() == TokenTypes.lssToken ||
			currentToken.getTokenType() == TokenTypes.gtrToken ||
			currentToken.getTokenType() == TokenTypes.geqToken ||
			currentToken.getTokenType() == TokenTypes.neqToken){
			//if it's a relation operator
			relation.setCondition(OpCodes.getCondition(currentToken.getTokenType()));
			nextToken();
			y = expression(scope);
			Helper.addInstruction(OpCodes.cmp, scope, x, y);
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
	public Result expression(Function scope){
		Result x, y;
		x = term(scope);
		while (currentToken.getTokenType() == TokenTypes.plusToken || currentToken.getTokenType() == TokenTypes.minusToken){
			int opCode = currentToken.getTokenType() == TokenTypes.plusToken ? OpCodes.add : OpCodes.sub;
			nextToken();
			y = term(scope);
			
			Helper.addInstruction(opCode, scope, x, y);
			x = new Result(Kind.INTERMEDIATE);
			x.setIntermediateLocation(main.getProgramCounter() - 1);
		}
		//end
		return x;
	}
	
	/*
	 *  term = factor { ("*" | "/") factor }
	 */
	public Result term(Function scope){
		Result x, y;
		x = factor(scope);
		
		while (currentToken.getTokenType() == TokenTypes.timesToken || currentToken.getTokenType() == TokenTypes.divToken){
			int opCode = currentToken.getTokenType() == TokenTypes.timesToken ? OpCodes.mul : OpCodes.div;
			nextToken();
			y = factor(scope);
			Helper.addInstruction(opCode, scope, x, y);
			x = new Result(Kind.INTERMEDIATE);
			x.setIntermediateLocation(main.getProgramCounter() - 1);
			//TODO: Fix your logic here to generate the appropriate instruction
		}
		//end
		return x;
	}
	
	/*
	 * factor = designator | number | "(" expression ")" | funcCall
	 */
	
	public Result factor(Function scope){
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
	
	public Result designator(Function scope){
		Result designator;
		if (currentToken.getTokenType() != TokenTypes.ident){
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());System.exit(3);
		}
		
		designator = new Result(Kind.VAR);
		designator.setVarName(currentToken.getTokenString());
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
	
}
