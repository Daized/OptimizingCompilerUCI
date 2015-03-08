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
		
		File fout = null;
		FileOutputStream fos = null;
		BufferedWriter bw = null;
		final String s = "graph: {\nx: 150\ny: 20\nxmax: 960\nymax: 900\nwidth: 950\nheight: 900\nlayoutdownfactor: 100\nlayoutupfactor: 0\nlayoutnearfactor: 0\n"
				+ "yspace: 30\nsmanhattenedges: yes\nfasticons: yes\niconcolors: 32\n";
		try {
			fout = new File("output/" + p.fileName.substring(5, p.fileName.length() - 4)+".vcg");
			fos = new FileOutputStream(fout);
			bw = new BufferedWriter(new OutputStreamWriter(fos));
			
			bw.write(s);
			//process the graph now
			processGraph(root, bw);
			
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
	
	public static void processGraph(BasicBlock node, BufferedWriter bw) throws IOException{
		
		if (node == null){
			return;
		}
		String name = String.valueOf(node.getLabel());
		
		if(!node.getVisited()){
			
	        for (BasicBlock child : node.getChildren()) {
	            String destination = String.valueOf(child.getLabel());
	            bw.write((getEdgeString(name, destination)));
	        }
	        bw.write(getNodeString(node));
			node.setVisited();
		}
		
		for (BasicBlock b: node.getChildren()){
			if (!b.getVisited()){
				processGraph(b, bw);
			}
		}
	}
	
	public static String getNodeString(BasicBlock node){
		
		StringBuilder nodeBuilder = new StringBuilder();
		nodeBuilder.append("node: {title: \"").append(node.getLabel()).append("\"\n");
		nodeBuilder.append("label: \"").append(node.getLabel()).append("[\n");
		nodeBuilder.append(" ]\"");//.append("liveRanges: ");
		//final Set<Integer> liveRanges = node.getLiveRanges(); ??
		nodeBuilder.append("\n}\n");
		
		
		return nodeBuilder.toString();
	}
	
	public static String getInstructionString(BasicBlock node){
		return null;
	}
	
	public static String getEdgeString(String source, String destination){
        StringBuilder edge = new StringBuilder();
        edge.append("edge: { sourcename: \"").append(source).append("\"\n");
        edge.append("targetname: \"").append(destination).append("\"\n").append("}\n");
        return edge.toString();
	}
	

}
