package xshader.gui;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;


public class GuiList extends GuiListExtended {
	
	private List<IGuiListEntry> list;
	private IGuiListEntry selected;
	
	public GuiList(Minecraft mc, int width, int height, int top, int bottom, int slotHeight, List<IGuiListEntry> list) {
		super(mc, width, height, top, bottom, slotHeight);
		this.list = list;
		for(IGuiListEntry e:list){
			e.setList(this);
		}
	}

	@Override
	public IGuiListEntry getListEntry(int i) {
		return this.list.get(i);
	}
	
	@Override
	public int getSize() {
		return this.list.size();
	}
	
	public void addEntry(IGuiListEntry entry){
		this.list.add(entry);
		entry.setList(this);
	}
	
	@Override
	public boolean isSelected(int i){
		return this.selected == getListEntry(i) || getListEntry(i).isSelected();
	}
	
	public void setSelected(IGuiListEntry entry){
		this.selected = entry;
	}
	
	public IGuiListEntry getSelected(){
		return this.selected;
	}
	
	public static interface IGuiListEntry extends net.minecraft.client.gui.GuiListExtended.IGuiListEntry{
		
		public void setList(GuiList list);
		
		public boolean isSelected();
		
	}
	
	public static abstract class GuiListEntryDoubleClick implements IGuiListEntry{

		private long lastTime;
		protected GuiList list;
		
		public abstract void mouseDoubleClick(int index, int mouseX, int mouseY, int which, int relMouseX, int relMouseY);
		
		@Override
		public boolean mousePressed(int index, int mouseX, int mouseY, int which, int relMouseX, int relMouseY) {
			this.list.setSelected(this);
			if (Minecraft.getSystemTime() - this.lastTime < 250L){
				mouseDoubleClick(index, mouseX, mouseY, which, relMouseX, relMouseY);
	        }

	        this.lastTime = Minecraft.getSystemTime();
			return false;
		}

		@Override
		public void mouseReleased(int index, int mouseX, int mouseY, int which, int relMouseX, int relMouseY) {
			//
		}
		
		@Override
		public void setList(GuiList list){
			this.list = list;
		}
		
		@Override
		public boolean isSelected(){
			return false;
		}
		
	}
	
}
