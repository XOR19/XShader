package xshader.node;

import java.util.ArrayList;
import java.util.List;

import xshader.parser.ParserException;
import xshader.xml.XMLNode;


public class NodeGroup {
	
	protected List<Node> nodes = new ArrayList<Node>();
	
	public NodeGroup(){
		
	}
	
	public void addNode(Node node){
		this.nodes.add(node);
	}
	
	public void generateSource(SourceGenerator generator, Node output){
		generator.addSourceLine("void "+getMethodCall()+"{");
		generator.resetVariables();
		output.reset();
		output.generateSource(generator);
		generator.addSourceLine("}\n");
		generator.addSourceLine("void "+getMethodColorCalcCall()+"{");
		generator.resetVariables();
		output.generateSourceColorCalc(generator);
		generator.addSourceLine("}\n");
	}

	public String getSource(Node output) {
		SourceGenerator generator = new SourceGenerator();
		generateSource(generator, output);
		return generator.getSource();
	}

	@SuppressWarnings("static-method")
	public String getMethodCall() {
		return "group()";
	}
	
	@SuppressWarnings("static-method")
	public String getMethodColorCalcCall() {
		return "group_colorcalc()";
	}
	
	public void loadFrom(XMLNode node) throws ParserException{
		this.nodes.clear();
		if(!node.getName().equals("Group"))
			throw new ParserException("Wrong Node Type "+node.getName());
		for(int i=0; i<node.getChildCount(); i++){
			XMLNode n = node.getChild(i);
			this.nodes.add(Node.loadFrom(n));
		}
		for(Node n:this.nodes){
			n.resolve(this);
		}
	}
	
	public XMLNode getAsXMLNode(){
		XMLNode node = new XMLNode("Group");
		int i = 1;
		for(Node n:this.nodes){
			n.setID(i++);
		}
		for(Node n:this.nodes){
			node.addChild(n.getAsXMLNode());
		}
		return node;
	}

	public Node getNodeByID(int id) {
		for(Node n:this.nodes){
			if(n.getID()==id){
				return n;
			}
		}
		return null;
	}
	
	public List<Node>getNodes(){
		return this.nodes;
	}
	
}
