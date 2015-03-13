package machine;

import java.util.*;

import data.OpCodes;
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
	
//	private static int toDlxOpCode(Instruction currentInstruction, boolean immediateInstruction) {
//        if(immediateInstruction)
//        {
//            if(currentInstruction.getOpcode() == OpCodes.add)
//                return MachineInstructions.ADDI;
//            else if(currentInstruction.getOpcode() == OpCodes.sub)
//                return MachineInstructions.SUBI;
//            else if (currentInstruction.getOpcode() == OpCodes.mul)
//                return MachineInstructions.MULI;
//            else if(currentInstruction.getOpcode() == OpCodes.div)
//                return MachineInstructions.DIVI;
//            else
//                return MachineInstructions.CMPI;
//        }
//        else
//        {
//            if(currentInstruction.getOpcode() == OpCodes.add)
//                return MachineInstructions.ADD;
//            else if(currentInstruction.getOpcode() == OpCodes.sub)
//                return MachineInstructions.SUB;
//            else if (currentInstruction.getOpcode() == OpCodes.mul)
//                return MachineInstructions.MUL;
//            else if(currentInstruction.getOpcode() == OpCodes.div)
//                return MachineInstructions.DIV;
//            else
//                return MachineInstructions.CMP;
//        }
//    }
}
