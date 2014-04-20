package xshader.parser;


public class ParameterParser {
	
	private String line;
	
	private int pos;
	
	public ParameterParser(String line){
		this.line = line;
	}
	
	public String next() throws ParserException{
		char c;
		try{
			c = this.line.charAt(this.pos++);
			while(c==' '||c=='\t'){
				c = this.line.charAt(this.pos++);
			}
		}catch(IndexOutOfBoundsException e){
			throw new ParserException("To view arguments");
		}
		String out = "";
		try{
			if(c=='"'){
				c = this.line.charAt(this.pos++);
				while(c!='"'){
					if(c=='\\'){
						c = this.line.charAt(this.pos++);
						if(c=='"'){
							out += '"';
						}else{
							out += "\\"+c;
						}
					}else{
						out += c;
						c = this.line.charAt(this.pos++);
					}
				}
			}else{
				while(c!=' '&&c!='\t'){
					out += c;
					c = this.line.charAt(this.pos++);
				}
			}
		}catch(IndexOutOfBoundsException e){
			//
		}
		return out;
	}
	
}
