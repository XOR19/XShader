package xshader;

import java.util.HashMap;

import xshader.fbo.FBO;
import xshader.node.MaterialShader;
import xshader.shader.Shader;


public class Globals {
	
	public static Shader gShader;
	
	public static Shader mShader;
	
	public static Shader pShader;
	
	public static FBO gBuffer;
	
	public static MaterialShader materialShader;
	
	public static HashMap<String, Integer> blockMaterial = new HashMap<String, Integer>();
	
}
