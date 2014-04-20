package xshader.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;


public class GuiRename extends GuiScreen {
	
	private static final int BUTTON_RENAME_ID = 1;
	private static final int BUTTON_CANCEL_ID = 0;
	
	private GuiScreen parent;
	private IRename rename;
    private GuiTextField name;
    private GuiButton renameDo;
    
    public GuiRename(GuiScreen parent, IRename rename){
        this.parent = parent;
        this.rename = rename;
    }

    @Override
	public void updateScreen(){
        this.name.updateCursorCounter();
    }

    @SuppressWarnings("unchecked")
	@Override
	public void initGui(){
        Keyboard.enableRepeatEvents(true);
        this.buttonList.add(this.renameDo = new GuiButton(BUTTON_RENAME_ID, this.width / 2 - 100, this.height / 4 + 96 + 12, "Rename"));
        this.buttonList.add(new GuiButton(BUTTON_CANCEL_ID, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.format("gui.cancel")));
        this.name = new GuiTextField(this.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        this.name.setFocused(true);
        this.name.setText(this.rename.getOldText());
        checkName();
    }

    private void checkName(){
    	this.renameDo.enabled = this.rename.isNameOK(this.name.getText());
    }
    
    @Override
	public void onGuiClosed(){
        Keyboard.enableRepeatEvents(false);
    }

    @Override
	protected void actionPerformed(GuiButton guiButton){
    	if (guiButton.enabled){
        	switch(guiButton.id){
        	case BUTTON_CANCEL_ID:
        		this.mc.displayGuiScreen(this.parent);
        		break;
        	case BUTTON_RENAME_ID:
        		this.rename.rename(this.name.getText());
                this.mc.displayGuiScreen(this.parent);
                break;
			default:
				break;
			}
        }
    }

    @Override
	protected void keyTyped(char par1, int par2) {
        this.name.textboxKeyTyped(par1, par2);
        checkName();

        if (par2 == 28 || par2 == 156){
            this.actionPerformed(this.renameDo);
        }
    }

    @Override
	protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);
        this.name.mouseClicked(par1, par2, par3);
    }

    @Override
	public void drawScreen(int par1, int par2, float par3){
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Rename", this.width / 2, 20, 16777215);
        this.drawString(this.fontRendererObj, "New Name", this.width / 2 - 100, 47, 10526880);
        this.name.drawTextBox();
        super.drawScreen(par1, par2, par3);
    }
    
    public static interface IRename{

		public boolean isNameOK(String text);

		public void rename(String text);

		public String getOldText();
    	
    }
    
}
