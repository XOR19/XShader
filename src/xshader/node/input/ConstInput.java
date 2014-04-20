package xshader.node.input;

import java.text.DecimalFormat;

import xshader.node.Primitive;
import xshader.node.SourceGenerator;
import xshader.parser.ParserException;
import xshader.xml.XMLNode;


public abstract class ConstInput implements Input {

	private static DecimalFormat df = new DecimalFormat("0.0"); 
	
	public static String f2s(float value){
		return df.format(value).replace(".", "").replace(',', '.');
	}
	
	public ConstInput(){

	}
	
	public abstract String getSource();

	public String getDefinition(){
		return " "+getSource();
	}
	
	@Override
	public void generateSource(SourceGenerator generator){
		//
	}

	@Override
	public void generateSourceColorCalc(SourceGenerator generator){
		//
	}
	
	@Override
	public String getSource(SourceGenerator generator, Primitive as){
		return getSource();
	}

	@Override
	public void reset(){
		//
	}
	
	@Override
	public abstract boolean equals(Object other);
	
	@Override
	public abstract int hashCode();

	public abstract void loadFrom(XMLNode n) throws ParserException;
	
}
