package xshader.parser;


public class ParserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8277368494247778402L;
	
	public ParserException(String message){
		super(message);
	}

	public ParserException(Throwable e) {
		super(e);
	}
	
}
