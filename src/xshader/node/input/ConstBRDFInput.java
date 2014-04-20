package xshader.node.input;

import xshader.parser.ParserException;
import xshader.xml.XMLNode;


public class ConstBRDFInput extends ConstInput {
	
	public ConstBRDFInput() {
		
	}

	@Override
	public String getSource() {
		return "vec3(0.0, 0.0, 0.0)";
	}

	@Override
	public boolean equals(Object other) {
		return other == this;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public void saveTo(XMLNode n) {
		//
	}

	@Override
	public void loadFrom(XMLNode n) throws ParserException {
		//
	}
	
}
