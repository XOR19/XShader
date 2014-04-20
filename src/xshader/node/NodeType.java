package xshader.node;

import java.util.ArrayList;
import java.util.List;

import xshader.node.input.ConstInput;
import xshader.node.input.Input;
import xshader.parser.ParserException;


public class NodeType {
	
	private String name;
	private String body;
	private String bodyColorCalc;
	private String functions;
	private Parameter[] inputs;
	private Parameter[] outputs;
	private Parameter[] configurations;
	
	public NodeType(String name, String body, String bodyColorCalc, String functions, Parameter[] inputs, Parameter[] outputs, Parameter[] configurations){
		this.name = name;
		this.body = body.trim().isEmpty()?null:body;
		this.bodyColorCalc = bodyColorCalc.trim().isEmpty()?null:bodyColorCalc;
		this.functions = functions.trim().isEmpty()?null:functions;
		this.inputs = inputs;
		this.outputs = outputs;
		this.configurations = configurations;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getBody(){
		return this.body;
	}
	
	public String getBodyColorCalc(){
		return this.bodyColorCalc;
	}
	
	public String getFunctions(){
		return this.functions;
	}
	
	public Parameter[] getInputs(){
		return this.inputs;
	}
	
	public Parameter[] getOutputs(){
		return this.outputs;
	}
	
	public Parameter[] getConfigurations() {
		return this.configurations;
	}
	
	public Node generateNode(){
		Input[] nodeInputs = new Input[this.inputs.length];
		for(int i=0; i<nodeInputs.length; i++){
			nodeInputs[i] = this.inputs[i].getConstInput();
		}
		ConstInput[] cfgs = new ConstInput[this.configurations.length];
		for(int i=0; i<cfgs.length; i++){
			cfgs[i] = this.configurations[i].getConstInput();
		}
		return new Node(this, nodeInputs, cfgs);
	}
	
	public static NodeType parse(String file, String fileName) throws ParserException{
		String[] lines = file.split("\\s*\n\\s*");
		int state = 0;
		String body = "";
		String bodyColorCalc = "";
		String functions = "";
		List<Parameter> inputs = new ArrayList<Parameter>();
		List<Parameter> outputs = new ArrayList<Parameter>();
		List<Parameter> configurations = new ArrayList<Parameter>();
		for(String line:lines){
			if(line.isEmpty())
				continue;
			if(state==0){
				if(line.startsWith("::begin")){
					line = line.substring(8).trim();
					if(line.equals("shader")){
						state = 1;
					}else if(line.equals("inputs")){
						state = 2;
					}else if(line.equals("outputs")){
						state = 3;
					}else if(line.equals("configurations")){
						state = 4;
					}else if(line.equals("functions")){
						state = 5;
					}else if(line.equals("colorcalc")){
						state = 6;
					}else{
						throw new ParserException("unknown begin "+line);
					}
				}
			}else if(line.startsWith("::end")){
				state = 0;
			}else if(state==1){
				body += line+"\n";
			}else if(state==2){
				inputs.add(Parameter.parse(line, true, false));
			}else if(state==3){
				outputs.add(Parameter.parse(line, false, false));
			}else if(state==4){
				configurations.add(Parameter.parse(line, true, true));
			}else if(state==5){
				functions += line+"\n";
			}else if(state==6){
				bodyColorCalc += line+"\n";
			}else{
				throw new ParserException("unexpected state "+state);
			}
		}
		if(state!=0)
			throw new ParserException("no ::end");
		return new NodeType(fileName, body, bodyColorCalc, functions, inputs.toArray(new Parameter[inputs.size()]), outputs.toArray(new Parameter[outputs.size()]), configurations.toArray(new Parameter[configurations.size()]));
	}
	
}
