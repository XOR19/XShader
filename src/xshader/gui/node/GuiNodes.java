package xshader.gui.node;

import java.util.ArrayList;
import java.util.List;

import xshader.node.Node;


public class GuiNodes extends Component {

	private List<GuiNode> nodes = new ArrayList<GuiNode>();
	
	private Component window;
	
	public GuiNodes(){
		super(null);
	}
	
	public void addNode(Node node){
		this.nodes.add(new GuiNode(this, node));
	}
	
	@Override
	public void setX(float x) {
		//
	}

	@Override
	public void setY(float y) {
		//
	}

	@Override
	public float getX() {
		return 0;
	}

	@Override
	public float getY() {
		return 0;
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
		for(int i=this.nodes.size()-1; i>=0; i--){
			this.nodes.get(i).render(x, y);
		}
		if(this.window!=null){
			this.window.render(x, y);
		}
	}

	@Override
	public Component getComponentAt(float x, float y) {
		if(this.window!=null){
			Component c = this.window.getComponentAt(x, y);
			if(c!=null){
				return c;
			}
		}
		for(GuiNode node:this.nodes){
			Component c = node.getComponentAt(x, y);
			if(c!=null)
				return c;
		}
		return null;
	}

	public GuiNode getGuiNodeFor(Node node) {
		for(GuiNode n:this.nodes){
			if(n.getNode()==node){
				return n;
			}
		}
		return null;
	}

	public List<GuiNode> getNodes() {
		return this.nodes;
	}

	public void setWindow(Component window) {
		this.window = window;
	}

	public void onClickedChanged(Component clicked) {
		if(this.window!=null && clicked!=null){
			Component c = clicked;
			while(c!=null){
				if(c==this.window)
					return;
				c = c.getParent();
			}
			this.window.defocus();
		}
	}
	
}
