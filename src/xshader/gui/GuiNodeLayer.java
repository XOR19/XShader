package xshader.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import xshader.gui.node.Component;
import xshader.gui.node.GuiNode;
import xshader.gui.node.GuiNodePin;
import xshader.gui.node.GuiNodes;
import xshader.node.Node;
import xshader.node.NodeGroup;
import xshader.node.NodeType;
import xshader.node.Parameter;
import xshader.node.Type;
import xshader.node.input.ConstColorInput;
import xshader.node.input.ConstFloatInput;
import xshader.node.input.ConstInput;
import xshader.node.input.Input;
import xshader.node.input.NodeInput;


public class GuiNodeLayer {
	
	private float x;
	private float y;
	private float scroll = 5;
	
	private int clickedX;
	private int clickedY;
	private int which;
	
	private int left;
	private int right;
	private int top;
	private int bottom;
	
	private NodeGroup ng;
	
	private List<GuiNode> selected = new ArrayList<GuiNode>();
	
	private GuiNodes nodes = new GuiNodes();
	
	private GuiNodePin dragPin;
	
	private Component clicked;
	
	public GuiNodeLayer(NodeGroup ng) {
		this.ng = ng;
		for(Node n:this.ng.getNodes()){
			this.nodes.addNode(n);
		}
	}

	public void setSize(int left, int right, int top, int bottom){
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}
	
	public void drawLayer(int par1, int par2, float par3, FontRenderer fontRenderer) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glPushMatrix();
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution sr = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
		GL11.glScissor(this.left*sr.getScaleFactor(), mc.displayHeight-this.bottom*sr.getScaleFactor(), (this.right-this.left)*sr.getScaleFactor(), (this.bottom-this.top)*sr.getScaleFactor());
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(0.5f, 0.5f, 0.5f, 0.5f);
        tessellator.addVertex(this.left, this.bottom, 0.0D);
        tessellator.addVertex(this.right, this.bottom, 0.0D);
        tessellator.addVertex(this.right, this.top, 0.0D);
        tessellator.addVertex(this.left, this.top, 0.0D);
        tessellator.draw();
        GL11.glTranslated(this.left+(this.right-this.left)/2, this.top+(this.bottom-this.top)/2, 0);
        GL11.glScalef(this.scroll, this.scroll, 0);
        GL11.glTranslated(this.x, this.y, 0);
        drawGrid();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glScalef(1.0f/10.0f, 1.0f/10.0f, 0);
        drawNodes(fontRenderer);
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
	
	private void drawNodes(FontRenderer fontRenderer){
		List<GuiNode> list = this.nodes.getNodes();
		for(int i=list.size()-1; i>=0; i--){
			drawConnections(list.get(i));
		}
		this.nodes.render(0, 0);
		if(this.dragPin!=null){
			Tessellator tessellator = Tessellator.instance;
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			tessellator.startDrawing(GL11.GL_LINES);
			float x = this.dragPin.getRealX();
			float y = this.dragPin.getRealY();

			tessellator.addVertex(x, y, 0);
			
			int tx = (this.right-this.left)/2;
			int ty = (this.bottom-this.top)/2;
			
			float mx = ((this.clickedX-tx)/this.scroll-this.x)*10;
			float my = ((this.clickedY-ty)/this.scroll-this.y)*10;
			
			Component c = this.nodes.getComponentAt(mx, my);
			
			if(c instanceof GuiNodePin && c!=this.dragPin && c.getParent()!=this.dragPin.getParent() && ((GuiNodePin)c).isIn()!=this.dragPin.isIn()){
				tessellator.addVertex(c.getRealX(), c.getRealY(), 0);
			}else{
				tessellator.addVertex(mx, my, 0);
			}
			
			tessellator.draw();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}
	
	private void drawConnections(GuiNode guiNode){
		Tessellator tessellator = Tessellator.instance;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		tessellator.startDrawing(GL11.GL_LINES);
		Input[] inputs = guiNode.getNode().getInputs();
		for(int i=0; i<inputs.length; i++){
			if(inputs[i] instanceof NodeInput){
				NodeInput ni = (NodeInput)inputs[i];
				GuiNodePin gnp1 = guiNode.getGuiPinFor(i, true);
				GuiNode n = this.nodes.getGuiNodeFor(ni.getNode());
				GuiNodePin gnp2 = n.getGuiPinFor(ni.getOutput(), false);
				tessellator.addVertex(gnp1.getRealX(), gnp1.getRealY(), 0);
				tessellator.addVertex(gnp2.getRealX(), gnp2.getRealY(), 0);
			}
		}
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	private static void drawNode(Node node, FontRenderer fontRenderer, int k){
		float x = node.getXPos();
		float y = node.getYPos();
		float w = node.getWidth();
		NodeType nodeType = node.getNodeType();
		Parameter[] inParameter = nodeType.getInputs();
		Parameter[] outParameter = nodeType.getOutputs();
		Parameter[] confParameter = nodeType.getConfigurations();
		Input[] inputs = node.getInputs();
		ConstInput[] configurations = node.getConigurations();
		float h = (1+inParameter.length+outParameter.length+confParameter.length)*10;
		String title = node.getCustomText();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1, 1, 1);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		if(k==-1){
			tessellator.setColorRGBA_F(0.1f, 0.1f, 0.1f, 0.5f);
		}else if(k==0){
			tessellator.setColorRGBA_F(0.9f, 0.7f, 0.1f, 0.5f);
		}else{
			tessellator.setColorRGBA_F(0.9f, 0.2f, 0.1f, 0.5f);
		}
        tessellator.addVertex(x, y+10, 0.0D);
        tessellator.addVertex(x+w, y+10, 0.0D);
        tessellator.addVertex(x+w, y, 0.0D);
        tessellator.addVertex(x, y, 0.0D);
        tessellator.setColorRGBA_F(0.5f, 0.5f, 0.5f, 0.5f);
        tessellator.addVertex(x, y+h, 0.0D);
        tessellator.addVertex(x+w, y+h, 0.0D);
        tessellator.addVertex(x+w, y+10, 0.0D);
        tessellator.addVertex(x, y+10, 0.0D);
        float p = 15+y;
		for(int i=0; i<outParameter.length; i++){
			int color = outParameter[i].getType().getColor();
			tessellator.setColorRGBA_I(color, 128);
	        tessellator.addVertex(x+w-2, p+2, 0.0D);
	        tessellator.addVertex(x+w+2, p+2, 0.0D);
	        tessellator.addVertex(x+w+2, p-2, 0.0D);
	        tessellator.addVertex(x+w-2, p-2, 0.0D);
			p+=10;
		}
		for(int i=0; i<confParameter.length; i++){
			drawConstInput(configurations[i], tessellator, x, p, w);
			p+=10;
		}
		for(int i=0; i<inParameter.length; i++){
			Type t = inParameter[i].getType();
			int color = t.getColor();
			tessellator.setColorRGBA_I(color, 128);
	        tessellator.addVertex(x-2, p+2, 0.0D);
	        tessellator.addVertex(x+2, p+2, 0.0D);
	        tessellator.addVertex(x+2, p-2, 0.0D);
	        tessellator.addVertex(x-2, p-2, 0.0D);
	        drawConstInput(inputs[i], tessellator, x, p, w);
			p+=10;
		}
        tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		drawString(title, x+5, y+1, w-10, 10, 0, fontRenderer);
		p = 11+y;
		for(int i=0; i<outParameter.length; i++){
			drawString(outParameter[i].getName(), x+5, p, w-10, 10, 1, fontRenderer);
			p+=10;
		}
		for(int i=0; i<confParameter.length; i++){
			drawString(confParameter[i].getName(), x+5+getConstInputStringStart(configurations[i]), p, w-10, 10, 0, fontRenderer);
			p+=10;
		}
		for(int i=0; i<inParameter.length; i++){
			drawString(inParameter[i].getName(), x+5+getConstInputStringStart(inputs[i]), p, w-10, 10, -1, fontRenderer);
			p+=10;
		}
	}
	
	private static float getConstInputStringStart(Input input){
		if(input instanceof ConstColorInput){
			return 14;
		}else if(input instanceof ConstFloatInput){
			return 4;
		}
		return 0;
	}
	
	private static void drawConstInput(Input input, Tessellator tessellator, float x, float y, float w){
		if(input instanceof ConstColorInput){
			ConstColorInput cci = (ConstColorInput)input;
			tessellator.setColorRGBA_F(cci.getRed(), cci.getGreen(), cci.getBlue(), 1.0f);
			tessellator.addVertex(x+4, y+4, 0);
			tessellator.addVertex(x+14, y+4, 0);
			tessellator.addVertex(x+14, y-4, 0);
			tessellator.addVertex(x+4, y-4, 0);
		}else if(input instanceof ConstFloatInput){
			tessellator.setColorRGBA_F(0.2f, 0.2f, 0.2f, 0.8f);
			tessellator.addVertex(x+4, y+4, 0);
			tessellator.addVertex(x+w-4, y+4, 0);
			tessellator.addVertex(x+w-4, y-4, 0);
			tessellator.addVertex(x+4, y-4, 0);
		}
	}
	
	private static void drawString(String text, float x, float y, float w, float h, int align, FontRenderer fontRenderer){
		String t = text;
		float wi = fontRenderer.getStringWidth(t);
		if(wi>w){
			wi = w-fontRenderer.getStringWidth("...");
			t = fontRenderer.trimStringToWidth(t, (int)(wi))+"...";
		}
		wi = fontRenderer.getStringWidth(t);
		GL11.glPushMatrix();
		if(align==0){
			GL11.glTranslatef(x+w/2-wi/2, y, 0);
		}else if(align==-1){
			GL11.glTranslatef(x, y, 0);
		}else if(align==1){
			GL11.glTranslatef(x+w-wi, y, 0);
		}
		fontRenderer.drawString(t, 0, 0, 0xFFFFFFFF);
		GL11.glPopMatrix();
	}
	
	private static void drawGrid(){
		GL11.glLineWidth(1);
		Tessellator tessellator = Tessellator.instance;
		
	    tessellator.startDrawing(GL11.GL_LINES);
	    tessellator.setColorRGBA_F(0.2f, 0.2f, 0.2f, 0.8f);
	    for(int i=-100; i<=100; i++){
	    	if(i%5!=0){
	        	tessellator.addVertex(i, -100, 0.0D);
	        	tessellator.addVertex(i, 100, 0.0D);
	        	tessellator.addVertex(-100, i, 0.0D);
	        	tessellator.addVertex(100, i, 0.0D);
        	}
	    }
	    tessellator.draw();
	    
	    GL11.glLineWidth(2);
	    tessellator.startDrawing(GL11.GL_LINES);
	    tessellator.setColorRGBA_F(0.2f, 0.2f, 0.2f, 0.8f);
	    for(int i=-100; i<=100; i++){
	    	if(i%5==0 && i!=0){
	    		tessellator.addVertex(i, -100, 0.0D);
	        	tessellator.addVertex(i, 100, 0.0D);
	        	tessellator.addVertex(-100, i, 0.0D);
	        	tessellator.addVertex(100, i, 0.0D);
        	}
	    }
	    tessellator.draw();
	    
        GL11.glLineWidth(3);
	    tessellator.startDrawing(GL11.GL_LINES);
	    tessellator.setColorRGBA_F(0.2f, 0.2f, 0.2f, 0.8f);
	    tessellator.addVertex(0, -100, 0.0D);
	    tessellator.addVertex(0, 100, 0.0D);
	    tessellator.addVertex(-100, 0, 0.0D);
	    tessellator.addVertex(100, 0, 0.0D);
        tessellator.draw();
        GL11.glLineWidth(1);
	}
	
	public void mouseClicked(int x, int y, int which) {
		this.which = which;
		this.clickedX = x;
		this.clickedY = y;
		if(this.which==0){
			this.clicked = null;
			boolean clear = !(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)||Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
			int tx = (this.right-this.left)/2;
			int ty = (this.bottom-this.top)/2;
			
			float mx = ((x-tx)/this.scroll-this.x)*10;
			float my = ((y-ty)/this.scroll-this.y)*10;
			
			Component c = this.nodes.getComponentAt(mx, my);
			
			if(c instanceof GuiNodePin){
				this.clicked = c;
				this.dragPin = (GuiNodePin) c;
				if(this.dragPin.isIn()){
					Input input = this.dragPin.getNode().getInputs()[this.dragPin.getIndex()];
					if(input instanceof NodeInput){
						this.dragPin.deconnect();
						this.dragPin.getNode().getInputs()[this.dragPin.getIndex()] = ((NodeInput) input).getOldInput();
						GuiNode gn = this.nodes.getGuiNodeFor(((NodeInput) input).getNode());
						this.dragPin = gn.getGuiPinFor(((NodeInput) input).getOutput(), false);
					}
				}
			}else if(c instanceof GuiNode){
				this.clicked = c;
				if(clear){
					for(GuiNode gn:this.selected){
						gn.setSelected(0);
					}
					this.selected.clear();
				}else{
					this.selected.remove(c);
				}
				for(GuiNode gn:this.selected){
					gn.setSelected(2);
				}
				this.selected.add(0, (GuiNode) c);
				((GuiNode) c).setSelected(1);
			}else{
				this.clicked = c;
			}
			this.nodes.onClickedChanged(this.clicked);
			if(this.clicked!=null){
				this.clicked.mouseDown(mx-c.getRealX(), my-c.getRealY(), which);
			}
		}
	}
	
	public void mouseMovedOrUp(int x, int y, int which) {
		if(this.dragPin!=null){
			int tx = (this.right-this.left)/2;
			int ty = (this.bottom-this.top)/2;
			
			float mx = ((x-tx)/this.scroll-this.x)*10;
			float my = ((y-ty)/this.scroll-this.y)*10;
			
			Component c = this.nodes.getComponentAt(mx, my);
			
			if(c instanceof GuiNodePin && c!=this.dragPin && c.getParent()!=this.dragPin.getParent() && ((GuiNodePin)c).isIn()!=this.dragPin.isIn()){
				this.dragPin.connetTo((GuiNodePin)c);
			}
			
		}
		this.dragPin = null;
	}

	public void mouseClickMove(int x, int y, int which, long time) {
		if(which==2){
			this.x += (x-this.clickedX)/this.scroll;
			this.y += (y-this.clickedY)/this.scroll;
		}else if(which==0 && !this.selected.isEmpty() && this.dragPin==null && (this.clicked==null || this.clicked instanceof GuiNode)){
			float dx = (x-this.clickedX)/this.scroll*10;
			float dy = (y-this.clickedY)/this.scroll*10;
			for(GuiNode n:this.selected){
				n.setX(n.getX()+dx);
				n.setY(n.getY()+dy);
			}
		}else if(this.clicked!=null){
			int tx = (this.right-this.left)/2;
			int ty = (this.bottom-this.top)/2;
			
			float mx = ((x-tx)/this.scroll-this.x)*10;
			float my = ((y-ty)/this.scroll-this.y)*10;
			this.clicked.mouseClickMove(mx-this.clicked.getRealX(), my-this.clicked.getRealY(), which);
		}
		this.clickedX = x;
		this.clickedY = y;
	}

	public void mouseWheel(int i, int j, int wheel) {
		if(wheel>0){
			this.scroll*=1.1;
		}else if(wheel<0){
			this.scroll/=1.1;
		}
	}
	
}
