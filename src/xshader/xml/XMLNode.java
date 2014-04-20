package xshader.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class XMLNode {

	private String name;
	
	private HashMap<String, String> properties = new HashMap<String, String>();
	
	private List<XMLNode> childs = new ArrayList<XMLNode>();
	
	private String text = "";
	
	public XMLNode(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setProperty(String key, String value){
		this.properties.put(key, value);
	}
	
	public String getProperty(String key){
		return this.properties.get(key);
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public String getText(){
		return this.text;
	}
	
	public void addChild(XMLNode child){
		if(!this.childs.contains(child))
			this.childs.add(child);
	}
	
	public int getChildCount(){
		return this.childs.size();
	}
	
	public XMLNode getChild(int i){
		return this.childs.get(i);
	}
	
	public String save(String ls){
		String out = ls + "<"+this.name;
		for(Entry<String, String> e:this.properties.entrySet()){
			out += " "+e.getKey()+" = \""+e.getValue()+"\"";
		}
		if(this.childs.isEmpty() && this.text.trim().isEmpty()){
			return out + "/>";
		}
		out += ">\n";
		String ls2 = ls+"\t";
		for(XMLNode child:this.childs){
			out += child.save(ls2)+"\n";
		}
		if(!(this.text==null || this.text.trim().isEmpty())){
			String[] s = this.text.split("\n");
			for(String ss:s){
				out += ls2+ss+"\n";
			}
		}
		return out + ls+"</"+this.name+">";
	}

	public String save() {
		return save("");
	}
	
}
