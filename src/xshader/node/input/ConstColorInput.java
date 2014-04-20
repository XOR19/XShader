package xshader.node.input;
import xshader.node.Parameter;
import xshader.parser.ParserException;
import xshader.xml.XMLNode;


public class ConstColorInput extends ConstInput {

	private float r;
	private float g;
	private float b;
	
	public ConstColorInput(Parameter parameter) {
		Object[] def = parameter.getDefault();
		this.r = ((Float)def[0]).floatValue();
		this.g = ((Float)def[1]).floatValue();
		this.b = ((Float)def[2]).floatValue();
	}

	@Override
	public String getSource() {
		return "vec3("+f2s(this.r)+", "+f2s(this.g)+", "+f2s(this.b)+")";
	}

	public float getRed() {
		return this.r;
	}

	public float getGreen() {
		return this.g;
	}

	public float getBlue() {
		return this.b;
	}
	
	public void setRed(float r) {
		this.r = r;
	}
	
	public void setGreen(float g) {
		this.g = g;
	}
	
	public void setBlue(float b) {
		this.b = b;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(this.b);
		result = prime * result + Float.floatToIntBits(this.g);
		result = prime * result + Float.floatToIntBits(this.r);
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
		ConstColorInput other = (ConstColorInput) obj;
		if (Float.floatToIntBits(this.b) != Float.floatToIntBits(other.b))
			return false;
		if (Float.floatToIntBits(this.g) != Float.floatToIntBits(other.g))
			return false;
		if (Float.floatToIntBits(this.r) != Float.floatToIntBits(other.r))
			return false;
		return true;
	}

	@Override
	public void saveTo(XMLNode n) {
		n.setProperty("value", this.r+","+this.g+","+this.b);
	}

	@Override
	public void loadFrom(XMLNode n) throws ParserException {
		String s[] = n.getProperty("value").split(",");
		if(s.length!=3)
			throw new ParserException("Wrong argument count "+s.length);
		try{
			this.r = Float.parseFloat(s[0]);
			this.g = Float.parseFloat(s[1]);
			this.b = Float.parseFloat(s[2]);
		}catch(NumberFormatException e){
			throw new ParserException(e);
		}
	}
	
}
