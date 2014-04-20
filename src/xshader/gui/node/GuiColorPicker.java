package xshader.gui.node;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import xshader.Utils;
import xshader.node.input.ConstColorInput;


public class GuiColorPicker extends Component{

	private ConstColorInput colorInput;
	
	private float px;
	
	private float py;
	
	public GuiColorPicker(Component parent, ConstColorInput colorInput) {
		super(parent);
		this.colorInput = colorInput;
	}

	@Override
	public void render(float x, float y) {
		float[] hsv = Utils.RGB2HSV(this.colorInput.getRed(), this.colorInput.getBlue(), this.colorInput.getGreen());
		float nx = x+this.px;
		float ny = y+this.py;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1, 1, 1);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(0.2f, 0.2f, 0.2f, 0.8f);
		tessellator.addVertex(nx, ny+60, 0.0D);
		tessellator.addVertex(nx+70, ny+60, 0.0D);
		tessellator.addVertex(nx+70, ny, 0.0D);
		tessellator.addVertex(nx, ny, 0.0D);
        tessellator.setColorRGBA_F(0, 0, 0, 1);
        tessellator.addVertex(nx+60, ny+55, 0.0D);
		tessellator.addVertex(nx+65, ny+55, 0.0D);
		tessellator.setColorRGBA_F(1, 1, 1, 1);
		tessellator.addVertex(nx+65, ny+5, 0.0D);
		tessellator.addVertex(nx+60, ny+5, 0.0D);
		tessellator.draw();
        drawColorCircle(nx+30, ny+30, 25, hsv[2]);
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(0.5f, 0.5f, 0.5f, 0.8f);
        float yy = (1-hsv[2])*50+5;
		tessellator.addVertex(nx+61.5f, ny+yy+1, 0.0D);
		tessellator.addVertex(nx+63.5f, ny+yy+1, 0.0D);
		tessellator.addVertex(nx+63.5f, ny+yy-1, 0.0D);
		tessellator.addVertex(nx+61.5f, ny+yy-1, 0.0D);
		float xx = nx+30+(float)Math.cos(hsv[0]/180.0f*Math.PI)*hsv[1]*25;
		yy = ny+30-(float)Math.sin(hsv[0]/180.0f*Math.PI)*hsv[1]*25;
		tessellator.addVertex(xx-1, yy+1, 0.0D);
		tessellator.addVertex(xx+1, yy+1, 0.0D);
		tessellator.addVertex(xx+1, yy-1, 0.0D);
		tessellator.addVertex(xx-1, yy-1, 0.0D);
        tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private static void drawColorCircle(float x, float y, float r, float h){
		Tessellator tessellator = Tessellator.instance;
		GL11.glShadeModel(GL11.GL_SMOOTH);
		tessellator.startDrawing(GL11.GL_TRIANGLES);
		float x1 = x+r;
	    float y1 = y;
	    for(int i=0; i<=29; i++){
	    	float x2 = x+(float) (Math.cos((i+1)/15.0f*Math.PI)*r);
	        float y2 = y-(float) (Math.sin((i+1)/15.0f*Math.PI)*r);
	        tessellator.setColorRGBA_F(c(i+10)*h, c(i+20)*h, c(i)*h, 1);
	        tessellator.addVertex(x1, y1, 0.0D);
	        tessellator.setColorRGBA_F(c(i+11)*h, c(i+21)*h, c(i+1)*h, 1);
	    	tessellator.addVertex(x2, y2, 0.0D);
	    	tessellator.setColorRGBA_F(h, h, h, 1);
	    	tessellator.addVertex(x, y, 0.0D);
	    	x1 = x2;
	    	y1 = y2;
	    }
		tessellator.draw();
	}
	
	private static float c(int i){
		i = i%30;
		if(i<10){
			return i/10.0f;
		}else if(i<20){
			return (20-i)/10.0f;
		}
		return 0;
	}
	
	@Override
	public Component getComponentAt(float x, float y) {
		float nx = x-this.px;
		float ny = y-this.py;
		return nx>=0 && ny>=0 && nx<70 && ny<60?this:null;
	}

	@Override
	public void setX(float x) {
		this.px = x;
	}

	@Override
	public void setY(float y) {
		this.py = y;
	}

	@Override
	public float getX() {
		return this.px;
	}

	@Override
	public float getY() {
		return this.py;
	}

	@Override
	public float getHeight() {
		return 60;
	}

	@Override
	public void setHeight(float height) {
		//
	}

	@Override
	public void setWidth(float width) {
		//
	}
	
	@Override
	public void defocus() {
		((GuiNodes)this.parent).setWindow(null);
	}

	@Override
	public void mouseDown(float x, float y, int which) {
		mouseClickMove(x, y, which);
	}
	
	@Override
	public void mouseClickMove(float x, float y, int which) {
		float[] hsv = Utils.RGB2HSV(this.colorInput.getRed(), this.colorInput.getGreen(), this.colorInput.getBlue());
		if(x>=58 && x<67 && y>=3 && y<57){
			hsv[2] = 1-(y-5)/50.0f;
			if(hsv[2]>1.0f)
				hsv[2] = 1.0f;
			if(hsv[2]<0.0f)
				hsv[2] = 0.0f;
		}else if(x>=3 && x<57 && y>=3 && y<57){
			float yy = y-30;
			float xx = x-30;
			hsv[0] = (float) (Math.atan2(yy, xx)/Math.PI*180);
			if(hsv[0]<0){
				hsv[0] += 360;
			}
			hsv[1] = (float) Math.sqrt(xx*xx+yy*yy)/25.0f;
			if(hsv[1]>1){
				hsv[1] = 1;
			}
		}
		float[] rgb = Utils.HSV2RGB(hsv[0], hsv[1], hsv[2]);
		this.colorInput.setRed(rgb[0]);
		this.colorInput.setGreen(rgb[1]);
		this.colorInput.setBlue(rgb[2]);
	}
	
}
