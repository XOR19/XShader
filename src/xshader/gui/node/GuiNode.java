package xshader.gui.node;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import xshader.node.Node;
import xshader.node.NodeType;
import xshader.node.Parameter;
import xshader.node.input.ConstColorInput;
import xshader.node.input.ConstFloatInput;
import xshader.node.input.ConstInput;
import xshader.node.input.Input;


public class GuiNode extends Component {
	
	private Node node;
	
	private SmallBig smallBig = new SmallBig(this);
	
	private Component[] rows;
	
	private GuiNodePin[] inputPins;
	
	private GuiNodePin[] outputPins;
	
	private float height;
	
	private int selected;
	
	public GuiNode(Component parent, Node node){
		super(parent);
		this.node = node;
		NodeType nt = node.getNodeType();
		Parameter[] outParam = nt.getOutputs();
		Parameter[] cnfgParam = nt.getConfigurations();
		Parameter[] inParam = nt.getInputs();
		this.outputPins = new GuiNodePin[outParam.length];
		this.inputPins = new GuiNodePin[inParam.length];
		this.rows = new Component[outParam.length+cnfgParam.length+inParam.length];
		ConstInput[] conigurations = node.getConigurations();
		Input[] inputs = node.getInputs();
		int row = 0;
		for(int i=0; i<outParam.length; i++){
			this.outputPins[i] = new GuiNodePin(this, node, outParam[i].getType().getColor(), row, i, false);
			this.rows[row++] = getComponentForInput(null, outParam[i].getName(), 1);
		}
		for(int i=0; i<cnfgParam.length; i++){
			this.rows[row++] = getComponentForInput(conigurations[i], cnfgParam[i].getName(), -1);
		}
		for(int i=0; i<inParam.length; i++){
			this.inputPins[i] = new GuiNodePin(this, node, inParam[i].getType().getColor(), row, i, true);
			this.rows[row++] = getComponentForInput(inputs[i], inParam[i].getName(), -1);
		}
		layout();
	}

	private Component getComponentForInput(Input input, String name, int align){
		Component c;
		if(input instanceof ConstColorInput){
			ConstColorInput cci = (ConstColorInput)input;
			c = new GuiColorInput(this, name, cci);
		}else if(input instanceof ConstFloatInput){
			ConstFloatInput cfi = (ConstFloatInput)input;
			c = new GuiValueInput(this, name, cfi);
		}else{
			c = new GuiLabel(this, name, align);
		}
		c.setHeight(12);
		return c;
	}
	
	@Override
	public void setX(float x) {
		this.node.setXPos(x);
	}

	@Override
	public void setY(float y) {
		this.node.setYPos(y);
	}

	@Override
	public float getX() {
		return this.node.getXPos();
	}

	@Override
	public float getY() {
		return this.node.getYPos();
	}

	@Override
	public float getHeight() {
		return this.height;
	}
	
	@Override
	public void setHeight(float height) {
		//
	}
	
	@Override
	public void setWidth(float width) {
		this.node.setWidth(width);
	}
	
	private void layout(){
		if(this.node.isSmall()){
			this.height = (this.inputPins.length>this.outputPins.length?this.inputPins.length:this.outputPins.length)*6;
			if(this.height<10)
				this.height = 10;
			float diff = this.inputPins.length==0?0:this.height/this.inputPins.length;
			float y = diff/2;
			for(GuiNodePin c:this.inputPins){
				c.setX(0);
				c.setY(y);
				y+=diff;
			}
			diff = this.outputPins.length==0?0:this.height/this.outputPins.length;
			y = diff/2;
			float w = this.node.getWidth();
			for(GuiNodePin c:this.outputPins){
				c.setX(w);
				c.setY(y);
				y+=diff;
			}
			this.smallBig.setX(6);
			this.smallBig.setY(this.height/2);
		}else{
			this.height = 10;
			this.smallBig.setX(6);
			this.smallBig.setY(5);
			for(Component c:this.rows){
				c.setX(4);
				c.setY(this.height);
				c.setWidth(this.node.getWidth()-8);
				this.height += c.getHeight();
			}
			for(GuiNodePin c:this.inputPins){
				int row = c.getRow();
				c.setX(0);
				c.setY(this.rows[row].getY()+this.rows[row].getHeight()/2);
			}
			float w = this.node.getWidth();
			for(GuiNodePin c:this.outputPins){
				int row = c.getRow();
				c.setX(w);
				c.setY(this.rows[row].getY()+this.rows[row].getHeight()/2);
			}
		}
	}
	
	@Override
	public void render(float x, float y) {
		float nx = this.node.getXPos();
		float ny = this.node.getYPos();
		nx += x;
		ny += y;
		float w = this.node.getWidth();
		boolean small = this.node.isSmall();
		String title = this.node.getCustomText();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1, 1, 1);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		if(this.selected==0){
			tessellator.setColorRGBA_F(0.2f, 0.2f, 0.2f, 0.8f);
		}else if(this.selected==1){
			tessellator.setColorRGBA_F(0.9f, 0.7f, 0.1f, 0.8f);
		}else{
			tessellator.setColorRGBA_F(0.9f, 0.2f, 0.1f, 0.8f);
		}
		if(small){
			tessellator.addVertex(nx, ny+this.height, 0.0D);
		    tessellator.addVertex(nx+w, ny+this.height, 0.0D);
		    tessellator.addVertex(nx+w, ny, 0.0D);
		    tessellator.addVertex(nx, ny, 0.0D);
		}else{
	        tessellator.addVertex(nx, ny+10, 0.0D);
	        tessellator.addVertex(nx+w, ny+10, 0.0D);
	        tessellator.addVertex(nx+w, ny, 0.0D);
	        tessellator.addVertex(nx, ny, 0.0D);
	        tessellator.setColorRGBA_F(0.5f, 0.5f, 0.5f, 0.5f);
	        tessellator.addVertex(nx, ny+this.height, 0.0D);
	        tessellator.addVertex(nx+w, ny+this.height, 0.0D);
	        tessellator.addVertex(nx+w, ny+10, 0.0D);
	        tessellator.addVertex(nx, ny+10, 0.0D);
		}
        tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		drawString(title, nx+10, ny, w-15, 10, -1);
		if(!small){
			for(int i=this.rows.length-1; i>=0; i--){
				this.rows[i].render(nx, ny);
			}
		}
		for(int i=this.inputPins.length-1; i>=0; i--){
			this.inputPins[i].render(nx, ny);
		}
		for(int i=this.outputPins.length-1; i>=0; i--){
			this.outputPins[i].render(nx, ny);
		}
		this.smallBig.render(nx, ny);
	}

	@Override
	public Component getComponentAt(float x, float y) {
		float nx = x-this.node.getXPos();
		float ny = y-this.node.getYPos();
		Component comp = this.smallBig.getComponentAt(nx, ny);
		if(comp!=null)
			return comp;
		for(GuiNodePin c:this.outputPins){
			comp = c.getComponentAt(nx, ny);
			if(comp!=null)
				return comp;
		}
		for(GuiNodePin c:this.inputPins){
			comp = c.getComponentAt(nx, ny);
			if(comp!=null)
				return comp;
		}
		if(nx>=0 && ny>=0 && nx<this.node.getWidth() && ny<this.height){
			for(Component c:this.rows){
				comp = c.getComponentAt(nx, ny);
				if(comp!=null)
					return comp;
			}
			return this;
		}
		return null;
	}

	public GuiNodePin getGuiPinFor(int index, boolean in) {
		return in?this.inputPins[index]:this.outputPins[index];
	}

	public Node getNode() {
		return this.node;
	}

	public void setSelected(int selected) {
		this.selected = selected;
	}

	public void changeInput(int index) {
		Input[] inputs = this.node.getInputs();
		Parameter[] inParam = this.node.getNodeType().getInputs();
		this.rows[this.inputPins[index].getRow()] = getComponentForInput(inputs[index], inParam[index].getName(), -1);
		layout();
	}
	
	public void toggleSmall() {
		this.node.setSmall(!this.node.isSmall());
		layout();
	}
	
	private static class SmallBig extends Component{

		public SmallBig(Component parent) {
			super(parent);
		}

		private float px;
		
		private float py;
		
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
			boolean isSmall = ((GuiNode)getParent()).getNode().isSmall();
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawing(GL11.GL_TRIANGLES);
			tessellator.setColorRGBA_F(0.7f, 0.5f, 0.1f, 0.8f);
			tessellator.addVertex(nx-2, ny-2, 0.0D);
			if(isSmall){
				tessellator.addVertex(nx-2, ny+2, 0.0D);
				tessellator.addVertex(nx+2, ny, 0.0D);
			}else{
				tessellator.addVertex(nx, ny+2, 0.0D);
				tessellator.addVertex(nx+2, ny-2, 0.0D);
			}
			tessellator.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		@Override
		public Component getComponentAt(float x, float y) {
			float nx = x-this.px;
			float ny = y-this.py;
			int s = 2;
			return nx>=-s && nx<s && ny>=-s && ny<s?this:null;
		}

		@Override
		public void mouseDown(float x, float y, int which) {
			((GuiNode)getParent()).toggleSmall();
		}
		
	}
	
}
