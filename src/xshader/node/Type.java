package xshader.node;

import xshader.node.input.ConstBRDFInput;
import xshader.node.input.ConstColorInput;
import xshader.node.input.ConstFloatInput;
import xshader.node.input.ConstInput;
import xshader.node.input.ConstNormalInput;
import xshader.node.input.ConstTexCoordInput;
import xshader.node.input.ConstTextureInput;
import xshader.node.input.ConstVectorInput;
import xshader.parser.ParameterParser;
import xshader.parser.ParserException;


public enum Type {

	VALUE(Primitive.FLOAT, AS.BOTH, 0x808080, Float.class, Float.class, Float.class) {

		@Override
		public ConstInput getConstInput(Parameter parameter) {
			return new ConstFloatInput(parameter);
		}

	}, 
	VECTOR(Primitive.VEC3, AS.BOTH, 0x3F5CFF, Float.class, Float.class, Float.class) {
		
		@Override
		public ConstInput getConstInput(Parameter parameter) {
			return new ConstVectorInput(parameter);
		}
		
	}, 
	COLOR(Primitive.VEC3, AS.BOTH, 0xFFD53D, Float.class, Float.class, Float.class) {

		@Override
		public ConstInput getConstInput(Parameter parameter) {
			return new ConstColorInput(parameter);
		}
		
	}, 
	BRDF(Primitive.BRDF, AS.IO, 0x359E25) {

		@Override
		public ConstInput getConstInput(Parameter parameter) {
			return new ConstBRDFInput();
		}
		
	},
	SAMPLER(Primitive.SAMPLER2D, AS.CONF, 0x0, Integer.class) {

		@Override
		public ConstInput getConstInput(Parameter parameter) {
			return new ConstTextureInput(parameter);
		}
		
	},
	TEXCOORD(Primitive.VEC3, AS.BOTH, 0x3F5CFF) {

		@Override
		public ConstInput getConstInput(Parameter parameter) {
			return new ConstTexCoordInput();
		}
		
	},
	NORMAL(Primitive.VEC3, AS.BOTH, 0x3F5CFF) {

		@Override
		public ConstInput getConstInput(Parameter parameter) {
			return new ConstNormalInput();
		}
		
	};
	
	private Primitive primitive;
	private AS acceptedAs;
	private Class<?>[] params;
	private int color;
	
	Type(Primitive primitive, AS acceptedAs, int color, Class<?>... params){
		this.primitive = primitive;
		this.acceptedAs = acceptedAs;
		this.color = color;
		this.params = params;
	}
	
	public Primitive getPrimitive(){
		return this.primitive;
	}

	public abstract ConstInput getConstInput(Parameter parameter);

	public Object[] parse(ParameterParser pp) throws ParserException{
		Object[] def = new Object[this.params.length];
		for(int i=0; i<def.length; i++){
			def[i] = parse(pp, this.params[i]);
		}
		return def;
	}
	
	private static Object parse(ParameterParser pp, Class<?> type) throws ParserException{
		try{
			if(type==Float.class || type==float.class){
				return Float.valueOf(Float.parseFloat(pp.next()));
			}else if(type==Integer.class || type==int.class){
				return Integer.valueOf(Integer.parseInt(pp.next()));
			}
		}catch(NumberFormatException e){
			throw new ParserException(e);
		}
		throw new ParserException("Unknown type "+type);
	}
	
	public AS getAcceptedAs(){
		return this.acceptedAs;
	}
	
	public Class<?>[] getParams(){
		return this.params;
	}
	
	public int getColor() {
		return this.color;
	}
	
	public enum AS{
		
		CONF, IO, BOTH;
		
		public boolean isOk(boolean conf){
			if(this==BOTH)
				return true;
			if(conf){
				return this==CONF;
			}
			return this==IO;
		}
		
	}
	
}
