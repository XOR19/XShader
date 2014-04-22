package xshader.gui.node;

import org.lwjgl.opengl.GL11;

import xshader.Utils;
import net.minecraft.client.gui.FontRenderer;


public abstract class Component {
	
	protected static FontRenderer fontRenderer = Utils.mc().fontRenderer;
	
	protected Component parent;
	
	public Component(Component parent){
		this.parent = parent;
	}
	
	public abstract void render(float x, float y);
	
	public abstract Component getComponentAt(float x, float y);
	
	public abstract void setX(float x);
	
	public abstract void setY(float y);
	
	public abstract float getX();
	
	public abstract float getY();
	
	public abstract float getHeight();
	
	public abstract void setHeight(float height);
	
	public abstract void setWidth(float width);
	
	public Component getParent() {
		return this.parent;
	}
	
	public float getRealX() {
		return this.parent==null?getX():getX()+this.parent.getRealX();
	}

	public float getRealY() {
		return this.parent==null?getY():getY()+this.parent.getRealY();
	}
	
	@SuppressWarnings("unused")
	public void mouseDown(float x, float y, int which) {
		//
	}
	
	@SuppressWarnings("unused")
	public void mouseClickMove(float x, float y, int which) {
		//
	}
	
	public void defocus() {
		//
	}
	
	protected static void drawString(String text, float x, float y, float w, float h, int align){
		String t = text;
		float wi = fontRenderer.getStringWidth(t);
		if(wi>w){
			wi = w-fontRenderer.getStringWidth("...");
			t = fontRenderer.trimStringToWidth(t, (int)(wi))+"...";
		}
		wi = fontRenderer.getStringWidth(t);
		GL11.glPushMatrix();
		float yy = y+h/2-fontRenderer.FONT_HEIGHT/2;
		if(align==0){
			GL11.glTranslatef(x+w/2-wi/2, yy, 0);
		}else if(align==-1){
			GL11.glTranslatef(x, yy, 0);
		}else if(align==1){
			GL11.glTranslatef(x+w-wi, yy, 0);
		}
		fontRenderer.drawString(t, 0, 0, 0xFFFFFFFF);
		GL11.glPopMatrix();
	}
	
}
