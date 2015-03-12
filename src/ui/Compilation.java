package ui;

import java.io.*;

import optimizations.CommonSubexpression;
import optimizations.CopyPropagation;
import optimizations.VCGGraph;
import registerallocation.InterferenceGraph;
import registerallocation.LiveRangeProcessor;
import registerallocation.RegisterAllocation;
import lexical.Parser;
import lexical.Tokenizer;

public class Compilation {
	
	public static void main(String[] args) {
		compileFiles();
	}
	
	public static void compileFiles(){
		final File folder = new File("test");
		for (final File file : folder.listFiles()){
			Tokenizer t = new Tokenizer();
			t.tokenize(file.toString());
			Parser p = new Parser(t.getTokenList(), t.getFileName());
			p.computation();
			
			CopyPropagation cp = new CopyPropagation(p);
			cp.doCopyPropagation();
			
			CommonSubexpression cs = new CommonSubexpression(p);
			cs.doCommonSubexpressionElimination();
			
			VCGGraph vcg = new VCGGraph(p, "");
			vcg.createControlFlowGraphFile();
			
			LiveRangeProcessor lvp = new LiveRangeProcessor(p);
			lvp.processLiveRanges();
			
			InterferenceGraph ig = new InterferenceGraph(p);
			ig.makeInterferenceGraph();
			
			RegisterAllocation ra = new RegisterAllocation(p, ig);
			ra.doRegisterAllocation();
			
			VCGGraph vcg2 = new VCGGraph(p, "RA");
			vcg2.createControlFlowGraphFile();
		}
		
	}
	
	
	

}
