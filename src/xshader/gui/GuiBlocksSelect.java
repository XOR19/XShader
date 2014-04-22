package xshader.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import xshader.gui.GuiList.IGuiListEntry;


public class GuiBlocksSelect extends GuiScreen {
	
	private static final int BUTTON_DONE_ID = 200;
	
	private final GuiScreen parent;
	List<String> blocks;
	private GuiList list;
	
	private List<IGuiListEntry> listEntries = new ArrayList<IGuiListEntry>();
	
	public GuiBlocksSelect(GuiScreen parent, List<String> blocks){
		this.parent = parent;
		this.blocks = blocks;
		loadAllBlocks();
	}
	
	@SuppressWarnings("unchecked")
	private void loadAllBlocks(){
		this.listEntries.clear();
		Iterator<Block> i = Block.blockRegistry.iterator();
		while(i.hasNext()){
			this.listEntries.add(new BlockEntry(i.next()));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui(){
    	this.list = new GuiList(this.mc, this.width, this.height, 32, this.height-64, 26, this.listEntries);
    	this.buttonList.add(new GuiButton(BUTTON_DONE_ID, this.width / 2 - 100, this.height / 6 + 168, I18n.format("gui.done")));
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
		protected void mouseClicked(int mouseX, int mouseY, int which){
	    	if (which != 0 || !this.list.func_148179_a(mouseX, mouseY, which)){
	            super.mouseClicked(mouseX, mouseY, which);
	    	}
	    }

	    @Override
		protected void mouseMovedOrUp(int mouseX, int mouseY, int which){
	        if (which != 0 || !this.list.func_148181_b(mouseX, mouseY, which)){
	            super.mouseMovedOrUp(mouseX, mouseY, which);
	        }
	    }
	
	@Override
	public void drawScreen(int par1, int par2, float par3){
        this.drawDefaultBackground();
        this.list.drawScreen(par1, par2, par3);
        this.drawCenteredString(this.fontRendererObj, "Block Select", this.width / 2, 8, 16777215);
        super.drawScreen(par1, par2, par3);
    }
	
	static final RenderItem renderItem = new RenderItem();
	
	private class BlockEntry implements IGuiListEntry{
		
		private Block block;
		private String name;
		private boolean in;
		
		public BlockEntry(Block block) {
			this.block = block;
			this.name = Block.blockRegistry.getNameForObject(block);
			this.in = GuiBlocksSelect.this.blocks.contains(this.name);
		}

		@SuppressWarnings({ "synthetic-access", "hiding" })
		@Override
		public void drawEntry(int index, int x, int y, int width, int heigth, Tessellator tessellator, int var7, int var8, boolean var9) {
			Item item = Item.getItemFromBlock(this.block);
			if(item!=null){
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		        RenderHelper.enableGUIStandardItemLighting();
		        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		        GL11.glEnable(GL11.GL_DEPTH_TEST);
				renderItem.renderItemIntoGUI(GuiBlocksSelect.this.fontRendererObj, GuiBlocksSelect.this.mc.renderEngine, new ItemStack(item), x+8, y+heigth/2-8);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				RenderHelper.disableStandardItemLighting();
			    GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			}
			drawCenteredString(GuiBlocksSelect.this.fontRendererObj, this.name, x+width/2, y+heigth/2-GuiBlocksSelect.this.fontRendererObj.FONT_HEIGHT/2, 0xFFFFFFFF);
		}

		@Override
		public boolean mousePressed(int var1, int var2, int var3, int var4, int var5, int var6) {
			this.in = !this.in;
			if(this.in){
				GuiBlocksSelect.this.blocks.add(this.name);
			}else{
				GuiBlocksSelect.this.blocks.remove(this.name);
			}
			return false;
		}

		@Override
		public void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6) {
			//
		}

		@Override
		public void setList(GuiList list) {/**/}

		@Override
		public boolean isSelected() {
			return this.in;
		}
		
	}
	
}
