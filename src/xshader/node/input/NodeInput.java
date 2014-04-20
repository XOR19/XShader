package xshader.node.input;

import xshader.node.Node;
import xshader.node.NodeGroup;
import xshader.node.Primitive;
import xshader.node.SourceGenerator;
import xshader.parser.ParserException;
import xshader.xml.XMLNode;


public class NodeInput implements Input {
	
	private Node node;
	
	private int output;
	
	private ConstInput oldInput;
	
	private int id;
	
	public NodeInput(Node node, int output, ConstInput oldInput) {
		this.node = node;
		this.output = output;
		this.oldInput = oldInput;
	}

	private NodeInput(int id, int output, ConstInput oldInput) {
		this.id = id;
		this.output = output;
		this.oldInput = oldInput;
	}

	@Override
	public void generateSource(SourceGenerator generator) {
		this.node.generateSource(generator);
	}

	@Override
	public void generateSourceColorCalc(SourceGenerator generator) {
		this.node.generateSourceColorCalc(generator);
	}
	
	@Override
	public String getSource(SourceGenerator generator, Primitive as) {
		return generator.getVariableName(this.node.getOutput(this.output), as);
	}

	@Override
	public void reset() {
		this.node.reset();
	}
	
	public Node getNode() {
		return this.node;
	}

	public int getOutput() {
		return this.output;
	}

	public ConstInput getOldInput(){
		return this.oldInput;
	}

	public void resolve(NodeGroup ng) throws ParserException{
		this.node = ng.getNodeByID(this.id);
		if(this.node==null){
			throw new ParserException("Wrong node id "+this.id);
		}
	}
	
	@Override
	public void saveTo(XMLNode n) {
		n.setProperty("from", ""+this.node.getID());
		n.setProperty("port", ""+this.output);
		if(this.oldInput!=null)
			this.oldInput.saveTo(n);
	}

	public static Input loadFrom(XMLNode n, ConstInput oldInput) throws ParserException {
		try{
			int id = Integer.parseInt(n.getProperty("from"));
			int output = Integer.parseInt(n.getProperty("port"));
			return new NodeInput(id, output, oldInput);
		}catch(NumberFormatException e){
			throw new ParserException(e);
		}
	}
	
}
