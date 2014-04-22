package xshader.texture;

import org.lwjgl.opengl.GL11;

public class Texture {

	@SuppressWarnings("unused")
	public static int createTexture(int width, int height, int type, int readType){
		return createTexture(width, height, readType, readType, GL11.GL_LINEAR, GL11.GL_LINEAR);
	}
	
	public static int createTexture(int width, int height, int type, int readType, int minFilter, int maxFilter){
		return createTexture(width, height, type, readType, minFilter, maxFilter, GL11.GL_CLAMP, GL11.GL_CLAMP);
	}
	
	public static int createTexture(int width, int height, int type, int readType, int minFilter, int maxFilter, int warpS, int warpT){
		int id = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, maxFilter);

		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, warpS);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, warpT);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, type, width, height, 0, readType, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		return id;
	}
	
}
