package ui;

import java.io.*;

import optimizations.CopyPropagation;
import optimizations.VCGGraph;
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
			VCGGraph vcg = new VCGGraph(cp.getParser());
			vcg.createControlFlowGraphFile();
		}
		
	}
	
	
	

}
