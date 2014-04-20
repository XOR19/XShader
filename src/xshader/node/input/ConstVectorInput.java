package xshader.node.input;
import xshader.node.Parameter;
import xshader.parser.ParserException;
import xshader.xml.XMLNode;


public class ConstVectorInput extends ConstInput {

	private float x;
	private float y;
	private float z;
	
	public ConstVectorInput(Parameter parameter) {
		Object[] def = parameter.getDefault();
		this.x = ((Float)def[0]).floatValue();
		this.y = ((Float)def[1]).floatValue();
		this.z = ((Float)def[2]).floatValue();
	}

	@Override
	public String getSource() {
		return "vec3("+f2s(this.x)+", "+f2s(this.y)+", "+f2s(this.z)+")";
	}

	public void setVector(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(this.x);
		result = prime * result + Float.floatToIntBits(this.y);
		result = prime * result + Float.floatToIntBits(this.z);
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
		ConstVectorInput other = (ConstVectorInput) obj;
		if (Float.floatToIntBits(this.x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(this.y) != Float.floatToIntBits(other.y))
			return false;
		if (Float.floatToIntBits(this.z) != Float.floatToIntBits(other.z))
			return false;
		return true;
	}

	@Override
	public void saveTo(XMLNode n) {
		n.setProperty("value", this.x+","+this.y+","+this.z);
	}
	
	@Override
	public void loadFrom(XMLNode n) throws ParserException {
		String s[] = n.getProperty("value").split(",");
		if(s.length!=3)
			throw new ParserException("Wrong argument count "+s.length);
		try{
			this.x = Float.parseFloat(s[0]);
			this.y = Float.parseFloat(s[1]);
			this.z = Float.parseFloat(s[2]);
		}catch(NumberFormatException e){
			throw new ParserException(e);
		}
	}
	
}
