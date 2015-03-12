package machine;

import java.util.*;

import lexical.Parser;

public class MachineCode {
	
	private ArrayList<Integer> code = new ArrayList<Integer>();
	private Parser p;
	//private List<Instruction>
	
	//Frame Pointer
	private final int FP = 28;
	//Stack pointer
	private final int SP = 29;
	//Return address
	private final int RP = 31;

	
	
	public int[] toIntArray() {
	    int[] c = new int[code.size()];
	    for(int i = 0; i < c.length; i += 1) {
	      c[i] = code.get(i);
	    }
	    return c;
	  }
}
