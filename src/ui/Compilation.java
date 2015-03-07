package ui;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import datastructures.BasicBlock;
import datastructures.ControlFlowGraph;
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
			createControlFlowGraphFile(p);
		}
		
	}
	
	public static void createControlFlowGraphFile(Parser p){
		BasicBlock root = p.getMain().getCFG().getRoot();
		final String s = "graph: {\nx: 150\ny: 20\nxmax: 960\nymax: 900\nwidth: 950\nheight: 900\nlayoutdownfactor: 100\nlayoutupfactor: 0\nlayoutnearfactor: 0\n"
				+ "yspace: 30\nsmanhattenedges: yes\nfasticons: yes\niconcolors: 32";
		
		File fout = null;
		FileOutputStream fos = null;
		BufferedWriter bw = null;
		try {
			
			fout = new File("output/" + p.fileName.substring(5)+".vcg");
			fos = new FileOutputStream(fout);
			bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			bw.write(s);
			//FIGURE OUT HOW TO TRAVERSE GRAPH
			bw.write("}");
			bw.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		
		
	}

}
