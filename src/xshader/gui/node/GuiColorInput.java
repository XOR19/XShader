package xshader.gui.node;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import xshader.node.input.ConstColorInput;


public class GuiColorInput extends Component {
	
	private ConstColorInput colorInput;
	
	private float px;
	
	private float py;
	
	private float width;
	
	private float height;
	
	private String name;
	
	public GuiColorInput(Component parent, String name, ConstColorInput colorInput) {
		super(parent);
		this.colorInput = colorInput;
		this.name = name;
		this.height = fontRenderer.FONT_HEIGHT;
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
		tessellator.setColorRGBA_F(this.colorInput.getRed(), this.colorInput.getGreen(), this.colorInput.getBlue(), 1);
		tessellator.addVertex(nx, ny+this.height-1, 0.0D);
	    tessellator.addVertex(nx+12, ny+this.height-1, 0.0D);
	    tessellator.addVertex(nx+12, ny+1, 0.0D);
	    tessellator.addVertex(nx, ny+1, 0.0D);
	    tessellator.draw();
	    GL11.glEnable(GL11.GL_TEXTURE_2D);
	    drawString(this.name, nx+14, ny, this.width-14, this.height, -1);
	}
	
	@Override
	public Component getComponentAt(float x, float y) {
		float nx = x-this.px;
		float ny = y-this.py;
		return nx>=0 && ny>=1 && nx<12 && ny<this.height-1?this:null;
	}

	@Override
	public void mouseDown(float x, float y, int which) {
		if(x>=0 && y>=1 && x<12 && y<this.height-1){
			GuiColorPicker gcp = new GuiColorPicker(getParent().getParent(), this.colorInput);
			gcp.setX(getRealX());
			gcp.setY(getRealY()+this.height-1);
			((GuiNodes)getParent().getParent()).setWindow(gcp);
		}
	}
	
}
