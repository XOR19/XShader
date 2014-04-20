package xshader.node;


public enum Primitive {
	
	FLOAT("float", "0.0") {
		
		@Override
		public String makeCastTo(Primitive as, String variableName) {
			switch(as){
			case FLOAT:
				return variableName;
			case INT:
				return "int("+variableName+")";
			case VEC3:
				return "vec3("+variableName+","+variableName+","+variableName+")";
			default:
				return as.getNull();
			}
		}
		
	}, INT("int", "0") {
		
		@Override
		public String makeCastTo(Primitive as, String variableName) {
			switch(as){
			case FLOAT:
				return "float("+variableName+")";
			case INT:
				return variableName;
			case VEC3:
				return "vec3(float("+variableName+"),float("+variableName+"),float("+variableName+"))";
			default:
				return as.getNull();
			}
		}
		
	}, VEC3("vec3", "vec3(0.0, 0.0, 0.0)") {
		
		@Override
		public String makeCastTo(Primitive as, String variableName) {
			switch(as){
			case FLOAT:
				return "length("+variableName+")";
			case INT:
				return "int(length("+variableName+"))";
			case VEC3:
				return variableName;
			default:
				return as.getNull();
			}
		}
		
	}, SAMPLER2D("sampler2D", "0") {
		
		@Override
		public String makeCastTo(Primitive as, String variableName) {
			switch(as){
			case SAMPLER2D:
				return variableName;
			default:
				return as.getNull();
			}
		}
		
	}, BRDF("vec3", "vec3(0.0, 0.0, 0.0)"){
		
		@Override
		public String makeCastTo(Primitive as, String variableName) {
			switch(as){
			case BRDF:
				return variableName;
			default:
				return as.getNull();
			}
		}
		
	};
	
	private String source;
	
	private String n;
	
	Primitive(String source, String n){
		this.source = source;
		this.n = n;
	}
	
	public String getNull() {
		return this.n;
	}

	public String getSource(){
		return this.source;
	}

	public abstract String makeCastTo(Primitive as, String variableName);
	
}
