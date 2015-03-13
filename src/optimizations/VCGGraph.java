package optimizations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

import data.Instruction;
import datastructures.BasicBlock;
import lexical.Parser;

public class VCGGraph extends Optimization {

	String suffix;
	
	public VCGGraph(Parser p, String suffix) {
		super(p);
		this.suffix = suffix;
	}
	
	public void createControlFlowGraphFile(){
		BasicBlock root = p.getMain().getCFG().getRoot();
		
		File fout = null;
		FileOutputStream fos = null;
		BufferedWriter bw = null;
		final String s = "graph: {\nx: 150\ny: 20\nxmax: 960\nymax: 900\nwidth: 950\nheight: 900\nlayoutdownfactor: 100\nlayoutupfactor: 0\nlayoutnearfactor: 0\n"
				+ "yspace: 30\nsmanhattenedges: yes\nfasticons: yes\niconcolors: 32\n";
		try {
			fout = new File("output/" + p.fileName.substring(5, p.fileName.length() - 4)+suffix+".vcg");
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
		
		setNotVisited();
	}
	
	/**
	 * Standard VCG processor
	 * @param node
	 * @param bw
	 * @throws IOException
	 */
	public void processGraph(BasicBlock node, BufferedWriter bw) throws IOException{
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
			node.setVisited(true);
			visitedBlocks.add(node);
		}
		
		for (BasicBlock b: node.getChildren()){
			if (!b.getVisited()){
				processGraph(b, bw);
			}
		}
	}
	
	public static String getNodeString(BasicBlock node){
		Set<Integer> liveRanges = node.getLiveRanges(); 
		StringBuilder nodeBuilder = new StringBuilder();
		nodeBuilder.append("node: {title: \"").append(node.getLabel()).append("\"\n");
		nodeBuilder.append("label: \"").append(node.getLabel()).append("[\n").append(getInstructionString(node));
		nodeBuilder.append("liveValues: ").append(liveRanges.toString()).append("\n ]\"\n");;
		nodeBuilder.append("\n}\n");
		
		return nodeBuilder.toString();
	}
	
	
	
	public static String getInstructionString(BasicBlock node){
		StringBuilder sb = new StringBuilder();
		for (Instruction instruction: node.getInstructions()){
			sb.append("").append(instruction.getInstructionNumber()).append(" ").append(instruction.toString()).append("\n");
		}
		return sb.toString();
	}
	
	public static String getEdgeString(String source, String destination){
        StringBuilder edge = new StringBuilder();
        edge.append("edge: { sourcename: \"").append(source).append("\"\n");
        edge.append("targetname: \"").append(destination).append("\"\n").append("}\n");
        return edge.toString();
	}

	@Override
	public void visit(BasicBlock node) {
		//Maybe do it later
		
	}

}
