package xshader.hook;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import xshader.Globals;


public class Hooks_GL {
	
	public static void glDisable(int cap){
		GL11.glDisable(cap);
		if(cap==GL11.GL_TEXTURE_2D && Globals.gShader!=null && Globals.gShader.isBound()){
			int i = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE)-GL13.GL_TEXTURE0;
			Globals.gShader.uniformInteger("texEnabled"+i, 0);
		}
	}
	
	public static void glEnable(int cap){
		GL11.glEnable(cap);
		if(cap==GL11.GL_TEXTURE_2D && Globals.gShader!=null && Globals.gShader.isBound()){
			int i = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE)-GL13.GL_TEXTURE0;
			Globals.gShader.uniformInteger("texEnabled"+i, 1);
		}
	}
	
}
