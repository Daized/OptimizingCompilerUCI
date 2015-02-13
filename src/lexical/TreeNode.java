package lexical;

public interface TreeNode {
	
	public static final int TERMINAL = 1;
	public static final int designator = 2;
	public static final int term = 3;
	public static final int expression = 4;
	public static final int relation = 5;
	public static final int assignment = 6;
	public static final int funcCall = 7;
	public static final int ifStatement = 8;
	public static final int whileStatement = 9;
	public static final int returnStatement = 10;
	public static final int statement = 11;
	public static final int statSequence = 12;
	public static final int typeDecl = 13;
	public static final int varDecl = 14;
	public static final int funcDecl = 15;
	public static final int formalParam = 16;
	public static final int funcBody = 17;
	public static final int computation = 18;
	
	
	public int getType();	



}

class TerminalNode implements TreeNode {
	
	Token terminal;
	
	public int getType(){
		return TERMINAL;
	}
	
	public TerminalNode(Token terminal){
		this.terminal = terminal;
	}
	
}

class DesignatorNode implements TreeNode{
	
	public int getType(){
		return designator;
	}
	
}






