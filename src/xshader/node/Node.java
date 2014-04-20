package xshader.node;

import xshader.node.input.ConstInput;
import xshader.node.input.Input;
import xshader.node.input.NodeInput;
import xshader.parser.ParserException;
import xshader.xml.XMLNode;


public class Node {
	
	private NodeType nodeType;
	
	private Input[] inputs;
	
	private int[] outputs;
	
	private int colorID = -1;
	
	private ConstInput[] configurations;
	
	private int methodID;
	
	private int id;
	
	private float x;
	private float y;
	private float width = 100;
	private float widthSmall = 50;
	private boolean isSmall;
	
	public Node(NodeType nodeType, Input[] inputs, ConstInput[] configurations){
		this.nodeType = nodeType;
		this.inputs = inputs;
		this.configurations = configurations;
	}

	public NodeType getNodeType() {
		return this.nodeType;
	}

	public ConstInput[] getConigurations() {
		return this.configurations;
	}

	public int getOutput(int output) {
		return this.outputs[output];
	}

	public void generateSource(SourceGenerator generator) {
		if(this.outputs!=null)
			return;
		this.methodID = generator.addNode(this);
		Parameter[] inParameters = this.nodeType.getInputs();
		for(int i=0; i<this.inputs.length; i++){
			if(this.inputs[i]!=null)
				this.inputs[i].generateSource(generator);
		}
		Parameter[] parameters = this.nodeType.getOutputs();
		this.outputs = new int[parameters.length];
		if(this.nodeType.getBody()==null)
			return;
		for(int i=0; i<parameters.length; i++){
			if(parameters[i].getType() != Type.BRDF)
				this.outputs[i] = generator.reservateVariable(parameters[i].getType().getPrimitive());
		}
		String line = getMethodName()+"(";
		boolean first = true;
		for(int i=0; i<this.inputs.length; i++){
			if(this.inputs[i] != null && inParameters[i].getType()!=Type.BRDF){
				if(first){
					first = false;
				}else{
					line += ", ";
				}
				line += this.inputs[i].getSource(generator, inParameters[i].getType().getPrimitive());
			}
		}
		for(int i=0; i<parameters.length; i++){
			if(parameters[i].getType() != Type.BRDF){
				if(first){
					first = false;
				}else{
					line += ", ";
				}
				line += generator.getVariableName(this.outputs[i], null);
			}
		}
		if(this.nodeType.getBodyColorCalc()!=null){
			this.colorID = generator.reservateColorID();
			if(first){
				first = false;
			}else{
				line += ", ";
			}
			line += "rayTracer.actualRay.color["+this.colorID+"]";
		}
		line += ");";
		generator.addSourceLine(line);
	}

	public void generateSourceColorCalc(SourceGenerator generator) {
		if(this.nodeType.getBodyColorCalc()==null)
			return;
		Parameter[] inParameters = this.nodeType.getInputs();
		for(int i=0; i<inParameters.length; i++){
			if(this.inputs[i]!=null && inParameters[i].getType()==Type.BRDF)
				this.inputs[i].generateSourceColorCalc(generator);
		}
		Parameter[] parameters = this.nodeType.getOutputs();
		for(int i=0; i<parameters.length; i++){
			if(parameters[i].getType() == Type.BRDF)
				this.outputs[i] = generator.reservateVariable(Primitive.BRDF);
		}
		String line = getMethodColorCalcName()+"(";
		boolean first = true;
		if(this.colorID!=-1){
			first = false;
			line += "rayTracer.actualRay.color["+this.colorID+"]";
		}
		for(int i=0; i<this.inputs.length; i++){
			if(this.inputs[i] != null && inParameters[i].getType()==Type.BRDF){
				if(first){
					first = false;
				}else{
					line += ", ";
				}
				line += this.inputs[i].getSource(generator, Primitive.BRDF);
			}
		}
		for(int i=0; i<parameters.length; i++){
			if(parameters[i].getType() == Type.BRDF){
				if(first){
					first = false;
				}else{
					line += ", ";
				}
				line += generator.getVariableName(this.outputs[i], null);
			}
		}
		line += ");";
		generator.addSourceLine(line);
	}

	private String getMethodColorCalcName() {
		return this.nodeType.getName()+"_colorcalc_id"+this.methodID;
	}

	private String getMethodName() {
		return this.nodeType.getName()+"_id"+this.methodID;
	}

	public void reset() {
		this.colorID = -1;
		this.methodID = -1;
		this.outputs = null;
		for(Input i:this.inputs){
			if(i instanceof NodeInput){
				((NodeInput)i).reset();
			}
		}
	}

	@SuppressWarnings("hiding")
	public String getMethodSource(int methodID) {
		this.methodID = methodID;
		String source = "";
		for(int i=0; i<this.configurations.length; i++){
			source += "#define "+this.nodeType.getConfigurations()[i].getSourceName()+this.configurations[i].getDefinition()+"\n";
		}
		String functions = this.nodeType.getFunctions();
		boolean defFunc = functions!=null;
		if(defFunc){
			source += "#define func(n) n##_id"+this.methodID+"\n";
			source += functions;
		}
		String calc = this.nodeType.getBody();
		String colorCalc = this.nodeType.getBodyColorCalc();
		boolean isCalc = calc!=null;
		boolean isColorCalc = colorCalc!=null;
		boolean first = true;
		String paramList = "";
		for(Parameter input:this.nodeType.getInputs()){
			if(input.getType() != Type.BRDF){
				if(first){
					first = false;
				}else{
					paramList += ", ";
				}
				paramList += "in "+input.getType().getPrimitive().getSource()+" "+input.getSourceName();
			}
		}
		for(Parameter output:this.nodeType.getOutputs()){
			if(output.getType() != Type.BRDF){
				if(first){
					first = false;
				}else{
					paramList += ", ";
				}
				paramList += "out "+output.getType().getPrimitive().getSource()+" "+output.getSourceName();
			}
		}
		if(isCalc && isColorCalc){
			if(first){
				first = false;
			}else{
				paramList += ", ";
			}
			paramList += "out vec3 color_calc_color";
		}
		if(isCalc)
			source += "void "+getMethodName()+"("+paramList+"){\n"+calc+"}\n";
		paramList = "";
		first = true;
		if(isCalc && isColorCalc){
			first = false;
			paramList += "in vec3 color_calc_color";
		}
		for(Parameter input:this.nodeType.getInputs()){
			if(input.getType() == Type.BRDF){
				if(first){
					first = false;
				}else{
					paramList += ", ";
				}
				paramList += "in vec3 "+input.getSourceName();
			}
		}
		for(Parameter output:this.nodeType.getOutputs()){
			if(output.getType() == Type.BRDF){
				if(first){
					first = false;
				}else{
					paramList += ", ";
				}
				paramList += "out vec3 "+output.getSourceName();
			}
		}
		if(isColorCalc)
			source += "void "+getMethodColorCalcName()+"("+paramList+"){\n"+colorCalc+"}\n";
		if(defFunc)
			source += "#undef func\n";
		for(int i=0; i<this.configurations.length; i++){
			source += "#undef "+this.nodeType.getConfigurations()[i].getSourceName()+"\n";
		}
		return source;
	}

	public int getID() {
		return this.id;
	}
	
	public void setID(int id) {
		this.id = id;
	}

	public void connectTo(int port, Node from, int fromPort) {
		if(this.inputs[port] instanceof NodeInput){
			this.inputs[port] = ((NodeInput)this.inputs[port]).getOldInput();
		}
		this.inputs[port] = new NodeInput(from, fromPort, (ConstInput) this.inputs[port]);
	}

	public XMLNode getAsXMLNode(){
		XMLNode node = new XMLNode("Node");
		node.setProperty("id", ""+this.id);
		node.setProperty("type", this.nodeType.getName());
		node.setProperty("x", ""+this.x);
		node.setProperty("y", ""+this.y);
		node.setProperty("width", ""+this.width);
		node.setProperty("widthSmall", ""+this.widthSmall);
		node.setProperty("isSmall", ""+this.isSmall);
		for(int i=0; i<this.inputs.length; i++){
			if(this.inputs[i]!=null){
				XMLNode n;
				if(this.inputs[i] instanceof NodeInput){
					n = new XMLNode("NodeInput");
				}else{
					n = new XMLNode("Input");
				}
				n.setProperty("id", ""+i);
				this.inputs[i].saveTo(n);
				node.addChild(n);
			}
		}
		for(int i=0; i<this.configurations.length; i++){
			if(this.configurations[i]!=null){
				XMLNode n = new XMLNode("Configuration");
				n.setProperty("id", ""+i);
				this.configurations[i].saveTo(n);
				node.addChild(n);
			}
		}
		return node;
	}
	
	private static float parseFloatOrDefault(String f, float def){
		if(f==null)
			return def;
		return Float.parseFloat(f);
	}
	
	public static Node loadFrom(XMLNode node) throws ParserException {
		if(!node.getName().equals("Node")){
			throw new ParserException("Wrong Node Type "+node.getName());
		}
		String type = node.getProperty("type");
		int nid;
		float x, y, width, widthSmall;
		boolean isSmall;
		try{
			nid = Integer.parseInt(node.getProperty("id"));
			x = parseFloatOrDefault(node.getProperty("x"), 0);
			y = parseFloatOrDefault(node.getProperty("y"), 0);
			width = parseFloatOrDefault(node.getProperty("width"), 100);
			widthSmall = parseFloatOrDefault(node.getProperty("widthSmall"), 50);
			String s = node.getProperty("isSmall");
			isSmall = s!=null && s.equals("true");
		}catch(NumberFormatException e){
			throw new ParserException(e);
		}
		NodeType nodeType = NodeList.getNodeTypeByName(type);
		if(nodeType==null){
			throw new ParserException("Can't find nodetype "+type);
		}
		Parameter[] inParameters = nodeType.getInputs();
		Input[] inputs = new Input[inParameters.length];
		for(int i=0; i<inputs.length; i++){
			inputs[i] = inParameters[i].getConstInput();
		}
		Parameter[] confParameters = nodeType.getConfigurations();
		ConstInput[] configurations = new ConstInput[confParameters.length];
		for(int i=0; i<configurations.length; i++){
			configurations[i] = confParameters[i].getConstInput();
		}
		for(int i=0; i<node.getChildCount(); i++){
			XMLNode n = node.getChild(i);
			boolean ni = n.getName().equals("NodeInput");
			boolean ip = n.getName().equals("Input");
			boolean cnf = n.getName().equals("Configuration");
			if(!(ni || ip || cnf))
				throw new ParserException("Unknown node "+n.getName());
			int id = Integer.parseInt(n.getProperty("id"));
			if(ip || ni){
				if(id<inputs.length && id>=0){
					if(inputs[id] instanceof ConstInput)
						((ConstInput)inputs[id]).loadFrom(n);
					if(ni){
						inputs[id] = NodeInput.loadFrom(n, (ConstInput)inputs[id]);
					}
				}
			}else{
				if(id<configurations.length && id>=0){
					configurations[id].loadFrom(n);
				}
			}
		}
		Node n = new Node(nodeType, inputs, configurations);
		n.setID(nid);
		n.x = x;
		n.y = y;
		n.width = width;
		n.widthSmall = widthSmall;
		n.isSmall = isSmall;
		return n;
	}

	public void resolve(NodeGroup nodeGroup) throws ParserException {
		for(Input in:this.inputs){
			if(in instanceof NodeInput){
				((NodeInput) in).resolve(nodeGroup);
			}
		}
	}

	public boolean isOutput() {
		return this.nodeType.getName().equals("out");
	}

	public float getXPos() {
		return this.x;
	}

	public float getYPos() {
		return this.y;
	}

	public float getWidth() {
		return isSmall()?this.widthSmall:this.width;
	}

	public boolean isSmall() {
		return this.isSmall;
	}

	public String getCustomText() {
		return this.nodeType.getName();
	}

	public void setXPos(float x) {
		this.x = x;
	}
	
	public void setYPos(float y) {
		this.y = y;
	}
	
	public void setWidth(float width) {
		if(isSmall()){
			this.widthSmall = width;
		}else{
			this.width = width;
		}
	}
	
	public void setSmall(boolean small) {
		this.isSmall = small;
	}

	public Input[] getInputs(){
		return this.inputs;
	}
	
}
