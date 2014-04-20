package xshader.node;

import java.util.ArrayList;
import java.util.List;

import xshader.parser.ParserException;
import xshader.xml.XMLNode;


public class MaterialGroup extends NodeGroup {
	
	private int id;
	
	private List<String> blocks = new ArrayList<String>();
	
	private String name = "MaterialShader";
	
	@Override
	public String getMethodCall() {
		return "material_mid"+this.id+"()";
	}
	
	@Override
	public String getMethodColorCalcCall() {
		return "material_colorcalc_mid"+this.id+"()";
	}
	
	@Override
	public void loadFrom(XMLNode node) throws ParserException{
		this.nodes.clear();
		this.blocks.clear();
		if(!node.getName().equals("Material"))
			throw new ParserException("Wrong Node Type "+node.getName());
		this.name = node.getProperty("name");
		if(this.name==null){
			this.name = "MaterialShader";
		}
		for(int i=0; i<node.getChildCount(); i++){
			XMLNode n = node.getChild(i);
			if(n.getName().equals("Block")){
				this.blocks.add(n.getProperty("name"));
			}else{
				this.nodes.add(Node.loadFrom(n));
			}
		}
		for(Node n:this.nodes){
			n.resolve(this);
		}
	}
	
	@Override
	public XMLNode getAsXMLNode(){
		XMLNode node = new XMLNode("Material");
		node.setProperty("name", this.name);
		int i = 1;
		for(Node n:this.nodes){
			n.setID(i++);
		}
		for(Node n:this.nodes){
			node.addChild(n.getAsXMLNode());
		}
		for(String block:this.blocks){
			XMLNode b = new XMLNode("Block");
			b.setProperty("name", block);
			node.addChild(b);
		}
		return node;
	}
	
	public List<String> getBlocks(){
		return this.blocks;
	}

	public void setID(int id) {
		this.id = id;
	}

	public void generateSource(SourceGenerator generator) {
		for(Node node:this.nodes){
			node.reset();
		}
		for(Node node:this.nodes){
			if(node.isOutput()){
				generateSource(generator, node);
				return;
			}
		}
	}

	public int getID() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
