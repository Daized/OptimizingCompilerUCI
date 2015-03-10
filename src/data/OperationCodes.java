package data;
import java.util.*;

import lexical.TokenTypes;

public class OperationCodes {

	    public static int neg = 0;
	    public static int add = 1;
	    public static int sub = 2;
	    public static int mul = 3;
	    public static int div = 4;
	    public static int cmp = 5;
	    public static int adda = 6;
	    public static int load = 7;
	    public static int store = 8;
	    public static int move = 9;
	    public static int phi = 10;
	    public static int end = 11;
	    public static int bra = 12;
	    public static int bne = 13;
	    public static int beq = 14;
	    public static int ble = 15;
	    public static int blt = 16;
	    public static int bge = 17;
	    public static int bgt = 18;
	    public static int read = 19;
	    public static int write = 20;
	    public static int wln = 21;
	    public static int kill = 22;
	    public static int call = 23;
	    public static int ret = 24;

	    public static final Map<Integer, Integer> intermediatesOperandCount = new HashMap<Integer, Integer>();
	    public static final Map<Integer, String> opcodeMap = new HashMap<Integer, String>();
	    private static Map<Integer, Integer> conditionsVsNegations = new HashMap<Integer, Integer>();

	    static {
	        intermediatesOperandCount.put(neg, 1);
	        intermediatesOperandCount.put(add, 2);
	        intermediatesOperandCount.put(sub, 2);
	        intermediatesOperandCount.put(mul, 2);
	        intermediatesOperandCount.put(div, 2);
	        intermediatesOperandCount.put(cmp, 2);
	        intermediatesOperandCount.put(adda, 2);
	        intermediatesOperandCount.put(load, 1);
	        intermediatesOperandCount.put(store, 2);
	        intermediatesOperandCount.put(move, 2);
	        intermediatesOperandCount.put(phi, 2);
	        intermediatesOperandCount.put(end, 0);
	        intermediatesOperandCount.put(bra, 1);
	        intermediatesOperandCount.put(bne, 2);
	        intermediatesOperandCount.put(beq, 2);
	        intermediatesOperandCount.put(ble, 2);
	        intermediatesOperandCount.put(blt, 2);
	        intermediatesOperandCount.put(bge, 2);
	        intermediatesOperandCount.put(bgt, 2);
	        intermediatesOperandCount.put(read, 0);
	        intermediatesOperandCount.put(write, 1);
	        intermediatesOperandCount.put(wln, 0);
	        intermediatesOperandCount.put(kill, 1);
	        intermediatesOperandCount.put(call, 1);
	        intermediatesOperandCount.put(ret, 1);

	        opcodeMap.put(neg,"neg");
	        opcodeMap.put(add,"add");
	        opcodeMap.put(sub,"sub");
	        opcodeMap.put(mul,"mul");
	        opcodeMap.put(div, "div");
	        opcodeMap.put(cmp, "cmp");
	        opcodeMap.put(adda, "adda");
	        opcodeMap.put(load, "load");
	        opcodeMap.put(store, "store");
	        opcodeMap.put(move,"move");
	        opcodeMap.put(phi,"phi");
	        opcodeMap.put(end,"end");
	        opcodeMap.put(bra,"bra");
	        opcodeMap.put(bne, "bne");
	        opcodeMap.put(beq, "beq");
	        opcodeMap.put(ble, "ble");
	        opcodeMap.put(blt,"blt");
	        opcodeMap.put(bge, "bge");
	        opcodeMap.put(bgt, "bgt");
	        opcodeMap.put(read, "read");
	        opcodeMap.put(write, "write");
	        opcodeMap.put(wln, "writenl");
	        opcodeMap.put(kill, "kill");
	        opcodeMap.put(call, "call");
	        opcodeMap.put(ret, "return");

	        conditionsVsNegations.put(OperationCodes.beq, OperationCodes.bne);
	        conditionsVsNegations.put(OperationCodes.blt, OperationCodes.bge);
	        conditionsVsNegations.put(OperationCodes.bgt, OperationCodes.ble);
	        conditionsVsNegations.put(OperationCodes.ble, OperationCodes.bgt);
	        conditionsVsNegations.put(OperationCodes.bge, OperationCodes.blt);
	        conditionsVsNegations.put(OperationCodes.bne, OperationCodes.beq);
	    }
	    
	    public static Integer getOperandCount(Integer op) {
	        return intermediatesOperandCount.get(op);
	    }

	    public static String getOperationName(Integer op) {
	        return opcodeMap.get(op);
	    }

		public static int getConditionNegation(int condition) {
			return conditionsVsNegations.get(condition);
		}

		public static int getCondition(TokenTypes tokenType) {
			int value;
	        if (tokenType == TokenTypes.gtrToken) {
	            value = OperationCodes.bgt;
	        } else if (tokenType == TokenTypes.geqToken) {
	            value = OperationCodes.bge;
	        } else if (tokenType == TokenTypes.lssToken) {
	            value = OperationCodes.blt;
	        } else if (tokenType == TokenTypes.leqToken) {
	            value = OperationCodes.ble;
	        } else if (tokenType == TokenTypes.eqlToken) {
	            value = OperationCodes.beq;
	        } else if (tokenType == TokenTypes.neqToken) {
	            value = OperationCodes.bne;
	        } else {
	            throw new RuntimeException("Invalid condition token");
	        }
	        
	        return value;
		}

	
}
