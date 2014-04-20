package xshader.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import xshader.node.input.ConstInput;


public class SourceGenerator {
	
	private List<List<Primitive>> variables = new ArrayList<List<Primitive>>();
	
	private HashMap<Conf, Integer> configurations = new HashMap<Conf, Integer>();
	
	private String methodSource = "";
	
	private String source = "";
	
	private int colorIDs;
	
	private int maxColorIDs;
	
	public int reservateVariable(Primitive primitive){
		List<Primitive> list = new ArrayList<Primitive>();
		list.add(primitive);
		this.variables.add(list);
		int i = this.variables.size();
		addSourceLine(primitive.getSource()+" "+getVariableName(i, primitive)+";");
		return i;
	}
	
	public String getVariableName(int i, Primitive as){
		if(i<1 || i>this.variables.size())
			throw new RuntimeException();
		List<Primitive> type = this.variables.get(i-1);
		Primitive _as = as;
		if(_as==null){
			_as = type.get(0);
		}else{
			int index = type.indexOf(_as);
			if(index==-1){
				type.add(_as);
				addSourceLine(_as.getSource()+" "+getVariableName(i, _as)+" = "+type.get(0).makeCastTo(_as, getVariableName(i, type.get(0)))+";");
			}
		}
		return "var"+i+"_"+_as;
	}
	
	public Primitive getVariableType(int i){
		if(i<1 || i>this.variables.size())
			throw new RuntimeException();
		return this.variables.get(i-1).get(0);
	}
	
	public String getSource(){
		return this.methodSource + this.source;
	}
	
	public void addSourceLine(String line){
		this.source += line+"\n";
	}
	
	public int addNode(Node node){
		Conf conf = new Conf(node.getNodeType(), node.getConigurations());
		Integer methodID = this.configurations.get(conf);
		if(methodID==null){
			methodID = Integer.valueOf(this.configurations.size()+1);
			this.configurations.put(conf, methodID);
			this.methodSource += node.getMethodSource(methodID.intValue())+"\n";
		}
		return methodID.intValue();
	}

	public void resetVariables() {
		this.colorIDs = 0;
		this.variables.clear();
	}

	public int reservateColorID() {
		if(this.maxColorIDs<this.colorIDs+1){
			this.maxColorIDs = this.colorIDs+1;
		}
		return this.colorIDs++;
	}

	public int getMaxShaderNodes() {
		return this.maxColorIDs;
	}
	
	private static class Conf{
		
		private NodeType nodeType;
		private ConstInput[] conf;
		
		public Conf(NodeType nodeType, ConstInput[] conf) {
			this.nodeType = nodeType;
			this.conf = conf;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(this.conf);
			result = prime * result + ((this.nodeType == null) ? 0 : this.nodeType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Conf other = (Conf) obj;
			if (!Arrays.equals(this.conf, other.conf)) return false;
			if (this.nodeType == null) {
				if (other.nodeType != null) return false;
			} else if (!this.nodeType.equals(other.nodeType)) return false;
			return true;
		}
		
	}
	
}
