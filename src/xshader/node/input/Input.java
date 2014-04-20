package xshader.node.input;

import xshader.node.Primitive;
import xshader.node.SourceGenerator;
import xshader.xml.XMLNode;


public interface Input {
	
	public void generateSource(SourceGenerator generator);

	public void generateSourceColorCalc(SourceGenerator generator);
	
	public String getSource(SourceGenerator generator, Primitive as);

	public void reset();

	public void saveTo(XMLNode n);
	
}
