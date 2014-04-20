package xshader.fbo;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

import xshader.texture.Texture;

public class FBO {

	private int frameBuffer;
	private int dephtBuffer;
	private int[] colorBuffer;
	
	public FBO(int bufferWidth, int bufferHeight, DephtBufferUse dephtBufferUse, int numColorBuffers, int colorBufferType){
		this.frameBuffer = EXTFramebufferObject.glGenFramebuffersEXT();
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, this.frameBuffer);
		if(dephtBufferUse==DephtBufferUse.BUFFER){
			this.dephtBuffer = EXTFramebufferObject.glGenRenderbuffersEXT();
			EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, this.dephtBuffer);
			EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, bufferWidth, bufferHeight);
			EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, this.dephtBuffer);
		}else if(dephtBufferUse==DephtBufferUse.TEXTURE){
			this.dephtBuffer = Texture.createTexture(bufferWidth, bufferHeight, GL14.GL_DEPTH_COMPONENT24, GL11.GL_DEPTH_COMPONENT, GL11.GL_NEAREST, GL11.GL_NEAREST);
			EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, this.dephtBuffer, 0);
		}
		this.colorBuffer = new int[numColorBuffers];
		for(int i=0; i<numColorBuffers; i++){
			this.colorBuffer[i] = Texture.createTexture(bufferWidth, bufferHeight, colorBufferType, GL11.GL_RGB);
			EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT+i, GL11.GL_TEXTURE_2D, this.colorBuffer[i], 0);
		}
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
	}
	
	public void bind(){
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, this.frameBuffer);
		IntBuffer buffer = BufferUtils.createIntBuffer(this.colorBuffer.length);
		for(int i=0; i<this.colorBuffer.length; i++){
			buffer.put(i, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT+i);
		}
		GL20.glDrawBuffers(buffer);
	}
	
	public int getDephtBuffer(){
		return this.dephtBuffer;
	}
	
	public int getColorBuffer(int index){
		return this.colorBuffer[index];
	}
	
	public void release(){
		if(this.frameBuffer!=0){
			if(this.dephtBuffer!=0){
				if(EXTFramebufferObject.glIsRenderbufferEXT(this.dephtBuffer)){
					EXTFramebufferObject.glDeleteRenderbuffersEXT(this.dephtBuffer);
				}else{
					GL11.glDeleteTextures(this.dephtBuffer);
				}
			}
			this.dephtBuffer = 0;
			if(this.colorBuffer!=null){
				for(int i=0; i<this.colorBuffer.length; i++){
					GL11.glDeleteTextures(this.colorBuffer[i]);
				}
			}
			this.colorBuffer = null;
			EXTFramebufferObject.glDeleteFramebuffersEXT(this.frameBuffer);
		}
		this.frameBuffer = 0;
	}
	
	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	public static void unBind(){
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
	}
	
	public static enum DephtBufferUse{
		NON, BUFFER, TEXTURE
	}
	
}
