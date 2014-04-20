package xshader.node.input;
import xshader.node.Parameter;
import xshader.parser.ParserException;
import xshader.xml.XMLNode;


public class ConstTextureInput extends ConstInput {

	private int slot;
	
	public ConstTextureInput(Parameter parameter) {
		this.slot = ((Integer)parameter.getDefault()[0]).intValue();
	}

	@Override
	public String getSource() {
		return "texUnit"+this.slot;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.slot;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConstTextureInput other = (ConstTextureInput) obj;
		if (this.slot != other.slot)
			return false;
		return true;
	}

	@Override
	public void saveTo(XMLNode n) {
		n.setProperty("value", ""+this.slot);
	}

	@Override
	public void loadFrom(XMLNode n) throws ParserException {
		try{
			this.slot = Integer.parseInt(n.getProperty("value"));
		}catch(NumberFormatException e){
			throw new ParserException(e);
		}
	}

}
