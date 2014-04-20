package xshader.shader;

import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import xshader.Logger;


public class Shader {
	
	private int program;
	
	private Shader(int program) {
		this.program = program;
	}

	public void bind(){
		ARBShaderObjects.glUseProgramObjectARB(this.program);
	}
	
	public void release(){
		if(this.program!=0){
			ARBShaderObjects.glDeleteObjectARB(this.program);
		}
		this.program = 0;
	}
	
	@Override
	public String toString() {
		return "Shader [program=" + this.program + "]";
	}

	public void uniformFloat(String uniform, float value) {
		int uniformIndex = ARBShaderObjects.glGetUniformLocationARB(this.program, uniform.subSequence(0, uniform.length()));
		ARBShaderObjects.glUniform1fARB(uniformIndex, value);
	}
	
	public void uniformVec2(String uniform, float x, float y) {
		int uniformIndex = ARBShaderObjects.glGetUniformLocationARB(this.program, uniform.subSequence(0, uniform.length()));
		ARBShaderObjects.glUniform2fARB(uniformIndex, x, y);
	}
	
	public void uniformVec3(String uniform, float x, float y, float z) {
		int uniformIndex = ARBShaderObjects.glGetUniformLocationARB(this.program, uniform.subSequence(0, uniform.length()));
		ARBShaderObjects.glUniform3fARB(uniformIndex, x, y, z);
	}
	
	public void uniformVec4(String uniform, float x, float y, float z, float w) {
		int uniformIndex = ARBShaderObjects.glGetUniformLocationARB(this.program, uniform.subSequence(0, uniform.length()));
		ARBShaderObjects.glUniform4fARB(uniformIndex, x, y, z, w);
	}
	
	public void uniformInteger(String uniform, int value) {
		int uniformIndex = ARBShaderObjects.glGetUniformLocationARB(this.program, uniform.subSequence(0, uniform.length()));
		ARBShaderObjects.glUniform1iARB(uniformIndex, value);
	}
	
	public void uniformVec2I(String uniform, int x, int y) {
		int uniformIndex = ARBShaderObjects.glGetUniformLocationARB(this.program, uniform.subSequence(0, uniform.length()));
		ARBShaderObjects.glUniform2iARB(uniformIndex, x, y);
	}
	
	public void uniformVec3I(String uniform, int x, int y, int z) {
		int uniformIndex = ARBShaderObjects.glGetUniformLocationARB(this.program, uniform.subSequence(0, uniform.length()));
		ARBShaderObjects.glUniform3iARB(uniformIndex, x, y, z);
	}
	
	public void uniformVec4I(String uniform, int x, int y, int z, int w) {
		int uniformIndex = ARBShaderObjects.glGetUniformLocationARB(this.program, uniform.subSequence(0, uniform.length()));
		ARBShaderObjects.glUniform4iARB(uniformIndex, x, y, z, w);
	}
	
	public int getAttributePos(String attribute){
		return GL20.glGetAttribLocation(this.program, attribute.subSequence(0, attribute.length()));
	}
	
	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}
	
	public static void unBind(){
		ARBShaderObjects.glUseProgramObjectARB(0);
	}
	
	private static String getLogInfo(int obj) {
		return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}
	
	private static int compileShader(int shaderType, String shaderSource) throws ShaderException{
		int shader = 0;
		try {
			shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);
			if(shader == 0)
				return 0;
			
			ARBShaderObjects.glShaderSourceARB(shader, shaderSource);
			ARBShaderObjects.glCompileShaderARB(shader);
			
			if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE){
				String error = getLogInfo(shader);
				Logger.severe(error);
				throw new ShaderException("Error creating shader: " + error);
			}
			
			return shader;
			
		}catch(ShaderException exc) {
			ARBShaderObjects.glDeleteObjectARB(shader);
			throw exc;
		}
	}
	
	public static Shader compileShader(String vertexShader, String fragmentShader) throws ShaderException{
		if(vertexShader==null || fragmentShader==null)
			throw new ShaderException("No Source");
		int vShader = 0;
		int fShader = 0;
		int program = 0;
		try {
			vShader = compileShader(ARBVertexShader.GL_VERTEX_SHADER_ARB, vertexShader);
			fShader = compileShader(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB, fragmentShader);
			program = ARBShaderObjects.glCreateProgramObjectARB();
			ARBShaderObjects.glAttachObjectARB(program, vShader);
			ARBShaderObjects.glAttachObjectARB(program, fShader);
			ARBShaderObjects.glLinkProgramARB(program);
			if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
				String error = getLogInfo(program);
				Logger.severe(error);
				throw new ShaderException("Error creating shader: " + error);
			} 
			ARBShaderObjects.glValidateProgramARB(program);
			if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
				String error = getLogInfo(program);
				Logger.severe(error);
				throw new ShaderException("Error creating shader: " + error);
			} 
			ARBShaderObjects.glDeleteObjectARB(vShader);
			vShader = 0;
			ARBShaderObjects.glDeleteObjectARB(fShader);
			fShader = 0;
			return new Shader(program);
		}catch(ShaderException e){
			if(program!=0){
				ARBShaderObjects.glDeleteObjectARB(program);
			}
			if(vShader!=0){
				ARBShaderObjects.glDeleteObjectARB(vShader);
			}
			if(fShader!=0){
				ARBShaderObjects.glDeleteObjectARB(fShader);
			}
			throw e;
		}
	}

	public boolean isBound() {
		return this.program == GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
	}
	
}
