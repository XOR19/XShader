package xshader.node.input;

import xshader.parser.ParserException;
import xshader.xml.XMLNode;



public class ConstTexCoordInput extends ConstInput {
	
	public ConstTexCoordInput() {
		
	}

	@Override
	public String getSource() {
		return "rayTracer.actualRay.pos";
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
