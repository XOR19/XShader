package xshader.node;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xshader.Utils;
import xshader.parser.Files;
import xshader.parser.ParserException;
import xshader.shader.Shader;
import xshader.shader.ShaderException;
import xshader.xml.XMLNode;


public class MaterialShader {
	
	private List<MaterialGroup> groups = new ArrayList<MaterialGroup>();
	
	public List<MaterialGroup> getGroups(){
		return this.groups;
	}
	
	public void loadFrom(XMLNode node) throws ParserException{
		this.groups.clear();
		if(!node.getName().equals("Shader"))
			throw new ParserException("Wrong Node Type "+node.getName());
		for(int i=0; i<node.getChildCount(); i++){
			XMLNode n = node.getChild(i);
			MaterialGroup group = new MaterialGroup();
			group.loadFrom(n);
			group.setID(i);
			this.groups.add(group);
		}
	}
	
	public XMLNode getAsXMLNode(){
		XMLNode node = new XMLNode("Shader");
		for(MaterialGroup group:this.groups){
			node.addChild(group.getAsXMLNode());
		}
		return node;
	}
	
	public void addNodeGroup(MaterialGroup group){
		this.groups.add(group);
	}
	
	public void generateSource(SourceGenerator generator){
		for(MaterialGroup group:this.groups){
			group.generateSource(generator);
		}
		generator.addSourceLine("void makeRays(in int groupID){");
		generator.resetVariables();
		generator.addSourceLine("switch(groupID){");
		for(MaterialGroup group:this.groups){
			generator.addSourceLine("case "+group.getID()+":");
			generator.addSourceLine(group.getMethodCall()+";");
			generator.addSourceLine("break;");
		}
		generator.addSourceLine("default:");
		generator.addSourceLine("break;");
		generator.addSourceLine("}");
		generator.addSourceLine("}\n");
		generator.addSourceLine("void calcColor(in int groupID){");
		generator.resetVariables();
		generator.addSourceLine("switch(groupID){");
		for(MaterialGroup group:this.groups){
			generator.addSourceLine("case "+group.getID()+":");
			generator.addSourceLine(group.getMethodColorCalcCall()+";");
			generator.addSourceLine("break;");
		}
		generator.addSourceLine("default:");
		generator.addSourceLine("break;");
		generator.addSourceLine("}");
		generator.addSourceLine("}\n");
	}

	public String getSource() {
		SourceGenerator generator = new SourceGenerator();
		generateSource(generator);
		return generator.getSource();
	}

	public Shader compile() throws ShaderException {
		String vShader = Files.readFile(new File(Utils.getXShaderDir(), "shader/shader.vs"));
		String fShader = Files.readFile(new File(Utils.getXShaderDir(), "shader/shader.fs"));
		SourceGenerator generator = new SourceGenerator();
		generateSource(generator);
		int msn = generator.getMaxShaderNodes();
		if(msn==0){
			msn = 1;
		}
		fShader = fShader.replace("::max_shader_nodes_here::", ""+msn);
		fShader = fShader.replace("::nodes_here::", ""+generator.getSource());
		Files.saveFile(new File(Utils.getXShaderDir(), "shader.dump"), fShader);
		return Shader.compileShader(vShader, fShader);
	}
	
	public HashMap<String, Integer> makeBlockMap(){
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		for(MaterialGroup group:this.groups){
			List<String> blocks = group.getBlocks();
			Integer id = Integer.valueOf(group.getID());
			for(String block:blocks){
				hm.put(block, id);
			}
		}
		return hm;
	}
	
}
