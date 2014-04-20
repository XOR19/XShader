package xshader.node;

import xshader.node.input.ConstInput;
import xshader.parser.ParameterParser;
import xshader.parser.ParserException;


public class Parameter {

	private String name;
	private String sourceName;
	private Type type;
	private Object[] def;
	
	public Parameter(String name, String sourceName, Type type, Object[] def){
		this.name = name;
		this.sourceName = sourceName;
		this.type = type;
		this.def = def;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getSourceName(){
		return this.sourceName;
	}
	
	public Type getType() {
		return this.type;
	}

	public Object[] getDefault() {
		return this.def;
	}
	
	public ConstInput getConstInput() {
		return this.type.getConstInput(this);
	}

	public static Parameter parse(String line, boolean def, boolean conf) throws ParserException{
		ParameterParser pp = new ParameterParser(line);
		String type = pp.next();
		String sourceName = pp.next();
		String name = pp.next();
		Type t;
		try{
			t = Type.valueOf(type);
		}catch(IllegalArgumentException e){
			throw new ParserException("Wrong type "+type);
		}
		if(!t.getAcceptedAs().isOk(conf))
			throw new ParserException("Type not allowed here");
		if(def){
			return new Parameter(name, sourceName, t, t.parse(pp));
		}
		boolean b = false;
		try{
			pp.next();
			b = true;
		}catch(ParserException e){
			//
		}
		if(b){
			throw new ParserException("No parameters expected");
		}
		return new Parameter(name, sourceName, t, null);
	}
	
}
