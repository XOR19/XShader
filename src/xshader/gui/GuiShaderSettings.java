package xshader.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import xshader.Globals;


public class GuiShaderSettings extends GuiScreen {
	
	private static final int BUTTON_MATERIAL_SHADERS_ID = 100;
	private static final int BUTTON_POST_SHADER_ID = 101;
	private static final int BUTTON_DONE_ID = 200;
	
	private final GuiScreen parent;
    protected String title = "Shader Settings";

    public GuiShaderSettings(GuiScreen parent){
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
	@Override
	public void initGui(){
    	this.buttonList.add(new GuiButton(BUTTON_MATERIAL_SHADERS_ID, this.width / 2 - 100, this.height / 6, "Material Shaders..."));
    	this.buttonList.add(new GuiButton(BUTTON_POST_SHADER_ID, this.width / 2 - 100, this.height / 6+20, "Post Shader..."));
        this.buttonList.add(new GuiButton(BUTTON_DONE_ID, this.width / 2 - 100, this.height / 6 + 168, I18n.format("gui.done")));
    }

    @Override
	protected void actionPerformed(GuiButton guiButton){
        if (guiButton.enabled){

        	switch(guiButton.id){
        	case BUTTON_MATERIAL_SHADERS_ID:
        		this.mc.displayGuiScreen(new GuiMaterialShaders(this, Globals.materialShader));
        		break;
        	case BUTTON_DONE_ID:
        		this.mc.displayGuiScreen(this.parent);
        		break;
			default:
				break;
        	}

        }
    }

    @Override
	public void drawScreen(int par1, int par2, float par3){
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 15, 16777215);
        super.drawScreen(par1, par2, par3);
    }
}
