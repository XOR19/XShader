package xshader.hook;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import xshader.Globals;
import xshader.Utils;
import xshader.fbo.FBO;
import xshader.shader.Shader;


public class Hooks_EntityRenderer {
	
	public static void hook_RenderWorld_Pre(EntityRenderer entityRenderer, float f, long l){
		Globals.gBuffer.bind();
		Globals.gShader.bind();
		Globals.gShader.uniformInteger("Texture1", 1);
	}
	
	public static void hook_RenderWorld_Post(EntityRenderer entityRenderer, float f, long l){
		FBO.unBind();
		Utils.mc().getFramebuffer().bindFramebuffer(false);
		Shader.unBind();
		//GL11.glPushMatrix();
		//GL11.glMatrixMode(GL11.GL_PROJECTION);
		//GL11.glPushMatrix();
		//GL11.glLoadIdentity();
		//GL11.glMatrixMode(GL11.GL_MODELVIEW);
		//GL11.glLoadIdentity();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Globals.gBuffer.getDephtBuffer());
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Globals.gBuffer.getColorBuffer(0));
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Globals.gBuffer.getColorBuffer(1));
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Globals.gBuffer.getColorBuffer(2));
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		Globals.mShader.bind();
		Globals.mShader.uniformInteger("gBuffer_Depth", 0);
		Globals.mShader.uniformInteger("gBuffer_Color", 1);
		Globals.mShader.uniformInteger("gBuffer_Normal", 2);
		Globals.mShader.uniformInteger("gBuffer_Material", 3);
		Globals.mShader.uniformFloat("textureSize", 2048);
		float width = Utils.mc().displayWidth;
		float height = Utils.mc().displayHeight;
		Globals.mShader.uniformVec2("textureScale", width/2048.0f, height/2048.0f);
		Globals.mShader.uniformFloat("depthRange_Near", 0.05F);
		Globals.mShader.uniformFloat("depthRange_Far", Utils.mc().gameSettings.renderDistanceChunks * 32);
		Globals.mShader.uniformFloat("depthRange_Diff", Utils.mc().gameSettings.renderDistanceChunks * 32 - 0.05F);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDepthMask(false);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(-1);
		tessellator.addVertexWithUV(-1, -1, 0, 0, 0);
		tessellator.addVertexWithUV(1, -1, 0, 1, 0);
		tessellator.addVertexWithUV(1, 1, 0, 1, 1);
		tessellator.addVertexWithUV(-1, 1, 0, 0,1);
		tessellator.draw();
		Shader.unBind();
		//GL11.glMatrixMode(GL11.GL_PROJECTION);
		//GL11.glPopMatrix();
		//GL11.glMatrixMode(GL11.GL_MODELVIEW);
		//GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(true);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
	}
	
}
