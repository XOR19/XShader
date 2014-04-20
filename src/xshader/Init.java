package xshader;

import java.io.File;

import org.lwjgl.opengl.GL30;

import xshader.fbo.FBO;
import xshader.fbo.FBO.DephtBufferUse;
import xshader.node.MaterialShader;
import xshader.node.NodeList;
import xshader.parser.Files;
import xshader.parser.ParserException;
import xshader.shader.Shader;
import xshader.shader.ShaderException;
import xshader.xml.XMLLoader;


public class Init {
	
	public static void init(){
		Forge.init();
		NodeList.loadFrom(new File(Utils.getXShaderDir(), "nodes"));
		Globals.materialShader = new MaterialShader();
		try {
			Globals.materialShader.loadFrom(XMLLoader.load(Files.readFile(new File(Utils.getXShaderDir(), "materialshader/ms.xml"))));
		} catch (ParserException e) {
			Logger.severe("Phaser Exception %s", e.getMessage());
			e.printStackTrace();
		}
		Globals.gBuffer = new FBO(2048, 2048, DephtBufferUse.TEXTURE, 3, GL30.GL_RGBA32F);
		try{
			Globals.gShader = Shader.compileShader(Files.readFile(new File(Utils.getXShaderDir(), "shader/gShader.vs")), Files.readFile(new File(Utils.getXShaderDir(), "shader/gShader.fs")));
		}catch(ShaderException e){
			e.printStackTrace();
		}
		try {
			Globals.mShader = Globals.materialShader.compile();
			Globals.blockMaterial = Globals.materialShader.makeBlockMap();
		} catch (ShaderException e) {
			e.printStackTrace();
		}
		
	}
	
}
