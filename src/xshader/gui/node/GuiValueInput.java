package xshader.gui.node;

import java.text.DecimalFormat;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import xshader.node.input.ConstFloatInput;


public class GuiValueInput extends Component {
	
	private static DecimalFormat df = new DecimalFormat("0.000");
	
	private ConstFloatInput floatInput;
	
	private float px;
	
	private float py;
	
	private float width;
	
	private float height;
	
	private String name;
	
	private float downX;
	
	private boolean move;
	
	public GuiValueInput(Component parent, String name, ConstFloatInput floatInput) {
		super(parent);
		this.name = name;
		this.floatInput = floatInput;
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
		return this.height;
	}
	
	@Override
	public void setHeight(float height) {
		this.height = height;
	}
	
	@Override
	public void setWidth(float width) {
		this.width = width;
	}
	
	@Override
	public void render(float x, float y) {
		float nx = this.px+x;
		float ny = this.py+y;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(0.3f, 0.3f, 0.3f, 0.8f);
		tessellator.addVertex(nx, ny+this.height-1, 0.0D);
	    tessellator.addVertex(nx+this.width, ny+this.height-1, 0.0D);
	    tessellator.addVertex(nx+this.width, ny+1, 0.0D);
	    tessellator.addVertex(nx, ny+1, 0.0D);
	    float min = this.floatInput.getMin();
	    float max = this.floatInput.getMax();
	    float range = max-min;
	    float f = (this.floatInput.getValue()-min)/range;
	    tessellator.setColorRGBA_F(0.2f, 0.2f, 0.2f, 0.8f);
	    tessellator.addVertex(nx, ny+this.height-1, 0.0D);
	    tessellator.addVertex(nx+this.width*f, ny+this.height-1, 0.0D);
	    tessellator.addVertex(nx+this.width*f, ny+1, 0.0D);
	    tessellator.addVertex(nx, ny+1, 0.0D);
	    tessellator.draw();
	    GL11.glEnable(GL11.GL_TEXTURE_2D);
	    drawString(this.name, nx+2, ny, this.width-14, this.height, -1);
	    drawString(df.format(this.floatInput.getValue()), nx+2, ny, this.width-4, this.height, 1);
	}
	
	@Override
	public Component getComponentAt(float x, float y) {
		float nx = x-this.px;
		float ny = y-this.py;
		return nx>=0 && ny>=1 && nx<this.width && ny<this.width-1?this:null;
	}
	
	@Override
	public void mouseDown(float x, float y, int which) {
		this.downX = x;
		this.move = false;
	}

	@Override
	public void mouseClickMove(float x, float y, int which) {
		if(this.downX-x>2 || this.downX-x<-2){
			this.move = true;
		}
		if(this.move){
			float f = x / this.width;
			float min = this.floatInput.getMin();
		    float max = this.floatInput.getMax();
		    float range = max-min;
		    f = f*range+min;
		    this.floatInput.setValue(f);
		}
	}
	
}
