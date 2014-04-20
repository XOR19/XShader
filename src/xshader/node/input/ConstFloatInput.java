package xshader.node.input;
import xshader.node.Parameter;
import xshader.parser.ParserException;
import xshader.xml.XMLNode;



public class ConstFloatInput extends ConstInput {
	
	private float min;
	private float max;
	private float value;
	
	public ConstFloatInput(Parameter parameter) {
		Object[] def = parameter.getDefault();
		this.min = ((Float)def[0]).floatValue();
		this.max = ((Float)def[1]).floatValue();
		this.value = ((Float)def[2]).floatValue();
	}

	@Override
	public String getSource() {
		return f2s(this.value);
	}

	public float getValue() {
		return this.value;
	}
	
	public float getMin() {
		return this.min;
	}

	public float getMax() {
		return this.max;
	}
	
	public void setValue(float value) {
		this.value = value;
		if(this.value>this.max){
			this.value = this.max;
		}
		if(this.value<this.min){
			this.value = this.min;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(this.value);
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
		ConstFloatInput other = (ConstFloatInput) obj;
		if (Float.floatToIntBits(this.value) != Float.floatToIntBits(other.value))
			return false;
		return true;
	}

	@Override
	public void saveTo(XMLNode n) {
		n.setProperty("value", ""+this.value);
	}

	@Override
	public void loadFrom(XMLNode n) throws ParserException {
		try{
			this.value = Float.parseFloat(n.getProperty("value"));
		}catch(NumberFormatException e){
			throw new ParserException(e);
		}
	}
	
}
