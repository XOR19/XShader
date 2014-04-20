package xshader.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Mouse;

import xshader.node.NodeGroup;


public class GuiNodeEditor extends GuiScreen {
	
	private static final int BUTTON_DONE_ID = 200;
	
	private final GuiScreen parent;
    protected String title = "Node Editor";
    private List<GuiNodeLayer> layers = new ArrayList<GuiNodeLayer>();
    
    public GuiNodeEditor(GuiScreen parent, NodeGroup ng){
        this.parent = parent;
        this.layers.add(new GuiNodeLayer(ng));
    }

    @SuppressWarnings("unchecked")
	@Override
	public void initGui(){
    	for(GuiNodeLayer nl:this.layers){
    		nl.setSize(32, this.width-32, 32, this.height-32);
    	}
    	this.buttonList.add(new GuiButton(BUTTON_DONE_ID, this.width / 2 - 100, this.height - 29, I18n.format("gui.done")));
    }

    @Override
	protected void actionPerformed(GuiButton guiButton){
        if (guiButton.enabled){

        	switch(guiButton.id){
        	case BUTTON_DONE_ID:
        		this.mc.displayGuiScreen(this.parent);
        		break;
			default:
				break;
        	}

        }
    }
    
    @Override
	protected void keyTyped(char key, int keyCode) {
		super.keyTyped(key, keyCode);
	}

    @Override
    public void handleMouseInput(){
    	super.handleMouseInput();
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int wheel = Mouse.getEventDWheel();
        if(wheel!=0)
        	mouseWheel(x, y, wheel);
    }
    
    protected void mouseWheel(int x, int y, int wheel) {
    	if(x>32 && x<this.width-32 && y>32 && y<this.height-32){
			this.layers.get(this.layers.size()-1).mouseWheel(x-32, y-32, wheel);
		}
    }
    
	@Override
	protected void mouseClicked(int x, int y, int which) {
		if(x>32 && x<this.width-32 && y>32 && y<this.height-32){
			this.layers.get(this.layers.size()-1).mouseClicked(x-32, y-32, which);
		}else{
			super.mouseClicked(x, y, which);
		}
	}

	@Override
	protected void mouseMovedOrUp(int x, int y, int which) {
		if(x>32 && x<this.width-32 && y>32 && y<this.height-32){
			this.layers.get(this.layers.size()-1).mouseMovedOrUp(x-32, y-32, which);
		}else{
			super.mouseMovedOrUp(x, y, which);
		}
	}

	@Override
	protected void mouseClickMove(int x, int y, int which, long time) {
		if(x>32 && x<this.width-32 && y>32 && y<this.height-32){
			this.layers.get(this.layers.size()-1).mouseClickMove(x-32, y-32, which, time);
		}else{
			super.mouseClickMove(x, y, which, time);
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3){
        this.drawBackground(0);
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 8, 16777215);
        
        for(GuiNodeLayer layer:this.layers){
        	layer.drawLayer(par1, par2, par3, this.fontRendererObj);
        }
        super.drawScreen(par1, par2, par3);
    }
	
}
