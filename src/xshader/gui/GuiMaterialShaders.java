package xshader.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import xshader.Globals;
import xshader.Utils;
import xshader.gui.GuiList.GuiListEntryDoubleClick;
import xshader.gui.GuiList.IGuiListEntry;
import xshader.gui.GuiRename.IRename;
import xshader.node.MaterialGroup;
import xshader.node.MaterialShader;
import xshader.node.Node;
import xshader.node.NodeList;
import xshader.parser.Files;
import xshader.shader.ShaderException;


public class GuiMaterialShaders extends GuiScreen {
	
	private static final int BUTTON_CREATE_SHADER_ID = 100;
	private static final int BUTTON_EDIT_MATERIAL_ID = 101;
	private static final int BUTTON_EDIT_BLOCKS_ID = 102;
	private static final int BUTTON_RENAME_ID = 103;
	private static final int BUTTON_DELETE_ID = 104;
	private static final int BUTTON_DONE_ID = 200;
	
	private final GuiScreen parent;
	private String title = "Material Shader";
	private GuiList list;
    private List<IGuiListEntry> listEntries = new ArrayList<IGuiListEntry>();
	private MaterialShader ms;
    
	private GuiButton editMaterial;
	private GuiButton editBlocks;
	private GuiButton rename;
	private GuiButton delete;
	
    public GuiMaterialShaders(GuiScreen parent, MaterialShader ms){
        this.parent = parent;
        this.ms = ms;
        List<MaterialGroup> groups = ms.getGroups();
        for(MaterialGroup group:groups){
        	this.listEntries.add(new MaterialEntry(group));
        }
    }

    @SuppressWarnings("unchecked")
	@Override
	public void initGui(){
    	this.list = new GuiList(this.mc, this.width, this.height, 32, this.height-64, 26, this.listEntries);
    	this.buttonList.add(this.editMaterial = new GuiButton(BUTTON_EDIT_MATERIAL_ID, this.width / 2 - 154, this.height - 52, 150, 20, "Edit Material"));
        this.buttonList.add(new GuiButton(BUTTON_CREATE_SHADER_ID, this.width / 2 + 4, this.height - 52, 150, 20, "Create Material"));
        this.buttonList.add(this.editBlocks = new GuiButton(BUTTON_EDIT_BLOCKS_ID, this.width / 2 - 154, this.height - 28, 72, 20, "Edit Blocks"));
        this.buttonList.add(this.rename = new GuiButton(BUTTON_RENAME_ID, this.width / 2 - 76, this.height - 28, 72, 20, "Rename"));
        this.buttonList.add(this.delete = new GuiButton(BUTTON_DELETE_ID, this.width / 2 + 4, this.height - 28, 72, 20, "Delete"));
        this.buttonList.add(new GuiButton(BUTTON_DONE_ID, this.width / 2 + 82, this.height - 28, 72, 20, I18n.format("gui.done")));
        checkStates();
    }

    private void checkStates(){
    	IGuiListEntry entry = this.list.getSelected();
    	if(entry==null){
    		this.editMaterial.enabled = false;
    		this.editBlocks.enabled = false;
    		this.rename.enabled = false;
    		this.delete.enabled = false;
    	}else{
    		this.editMaterial.enabled = true;
    		this.editBlocks.enabled = true;
    		this.rename.enabled = true;
    		this.delete.enabled = true;
    	}
    }
    
    @Override
	protected void actionPerformed(GuiButton guiButton){
        if (guiButton.enabled){

        	switch(guiButton.id){
        	case BUTTON_CREATE_SHADER_ID:
        		MaterialGroup group = new MaterialGroup();
        		Node out = NodeList.getNodeTypeByName("out").generateNode();
        		Node diffuse = NodeList.getNodeTypeByName("diffuse").generateNode();
        		group.addNode(out);
        		group.addNode(diffuse);
        		out.connectTo(0, diffuse, 0);
        		diffuse.setXPos(-100);
        		diffuse.setYPos(-10);
        		out.setXPos(10);
        		out.setYPos(-10);
        		this.ms.addNodeGroup(group);
        		this.list.addEntry(new MaterialEntry(group));
        		break;
        	case BUTTON_DELETE_ID:
        		this.listEntries.remove(this.list.getSelected());
        		this.ms.getGroups().remove(((MaterialEntry)this.list.getSelected()).group);
        		this.list.setSelected(null);
        		break;
        	case BUTTON_EDIT_MATERIAL_ID:
        		this.mc.displayGuiScreen(new GuiNodeEditor(this, ((MaterialEntry)this.list.getSelected()).group));
        		break;
        	case BUTTON_EDIT_BLOCKS_ID:
        		this.mc.displayGuiScreen(new GuiBlocksSelect(this, ((MaterialEntry)this.list.getSelected()).group.getBlocks()));
        		break;
        	case BUTTON_RENAME_ID:
        		this.mc.displayGuiScreen(new GuiRename(this, (IRename) this.list.getSelected()));
        		break;
        	case BUTTON_DONE_ID:
        		this.mc.displayGuiScreen(this.parent);
        		break;
			default:
				break;
			}
        }
        checkStates();
    }

    @Override
    public void onGuiClosed() {
    	try {
			Globals.mShader = this.ms.compile();
			Globals.blockMaterial = Globals.materialShader.makeBlockMap();
		} catch (ShaderException e) {
			e.printStackTrace();
		}
    	Files.saveFile(new File(Utils.getXShaderDir(), "materialshader/ms.xml"), this.ms.getAsXMLNode().save());
    }
    
    @Override
	protected void mouseClicked(int mouseX, int mouseY, int which){
    	if (which != 0 || !this.list.func_148179_a(mouseX, mouseY, which)){
            super.mouseClicked(mouseX, mouseY, which);
    	}
    	checkStates();
    }

    @Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int which){
        if (which != 0 || !this.list.func_148181_b(mouseX, mouseY, which)){
            super.mouseMovedOrUp(mouseX, mouseY, which);
        }
        checkStates();
    }
    
    @Override
	public void drawScreen(int par1, int par2, float par3){
        this.drawDefaultBackground();
        this.list.drawScreen(par1, par2, par3);
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 8, 16777215);
        super.drawScreen(par1, par2, par3);
    }
	
    private class MaterialEntry extends GuiListEntryDoubleClick implements IRename{

    	MaterialGroup group;
    	
		public MaterialEntry(MaterialGroup group) {
			this.group = group;
		}

		@SuppressWarnings({ "synthetic-access", "hiding" })
		@Override
		public void drawEntry(int index, int x, int y, int width, int heigth, Tessellator tessellator, int var7, int var8, boolean var9) {
			drawCenteredString(GuiMaterialShaders.this.fontRendererObj, this.group.getName(), x+width/2, y+heigth/2-GuiMaterialShaders.this.fontRendererObj.FONT_HEIGHT/2, 0xFFFFFFFF);
		}

		@Override
		public void mouseDoubleClick(int index, int mouseX, int mouseY, int which, int relMouseX, int relMouseY) {
			GuiMaterialShaders.this.mc.displayGuiScreen(new GuiNodeEditor(GuiMaterialShaders.this, this.group));
		}

		@Override
		public boolean isNameOK(String text) {
			return text.trim().length()>0;
		}

		@Override
		public void rename(String text) {
			this.group.setName(text);
		}

		@Override
		public String getOldText() {
			return this.group.getName();
		}
    	
    }
    
}
