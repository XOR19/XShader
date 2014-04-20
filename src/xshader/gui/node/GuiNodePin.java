package xshader.gui.node;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import xshader.node.Node;
import xshader.node.input.Input;
import xshader.node.input.NodeInput;


public class GuiNodePin extends Component {
	
	private Node node;
	
	private int color;
	
	private int row;
	
	private int index;
	
	private boolean in;
	
	private float px;
	
	private float py;
	
	public GuiNodePin(Component parent, Node node, int color, int row, int index, boolean in) {
		super(parent);
		this.node = node;
		this.color = color;
		this.row = row;
		this.index = index;
		this.in = in;
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
		return 0;
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
	public void render(float x, float y) {
		float nx = x+this.px;
		float ny = y+this.py;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_I(this.color, 200);
		tessellator.addVertex(nx-2, ny+2, 0.0D);
		tessellator.addVertex(nx+2, ny+2, 0.0D);
		tessellator.addVertex(nx+2, ny-2, 0.0D);
		tessellator.addVertex(nx-2, ny-2, 0.0D);
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	@Override
	public Component getComponentAt(float x, float y) {
		float nx = x-this.px;
		float ny = y-this.py;
		int s = this.node.isSmall()?3:5;
		return nx>=-s && nx<s && ny>=-s && ny<s?this:null;
	}

	public int getRow() {
		return this.row;
	}

	public boolean isIn() {
		return this.in;
	}

	public Node getNode() {
		return this.node;
	}

	public int getIndex() {
		return this.index;
	}

	public void connetTo(GuiNodePin gnp) {
		if(isIn()!=gnp.isIn()){
			if(isIn()){
				this.node.connectTo(this.index, gnp.getNode(), gnp.getIndex());
				((GuiNode)this.parent).changeInput(this.index);
			}else{
				gnp.connetTo(this);
			}
		}
	}

	public void deconnect() {
		if(isIn()){
			Input input = this.node.getInputs()[this.index];
			if(input instanceof NodeInput)
				this.node.getInputs()[this.index] = ((NodeInput) input).getOldInput();
			((GuiNode)this.parent).changeInput(this.index);
		}
	}
	
}
