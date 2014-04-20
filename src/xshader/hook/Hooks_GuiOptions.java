package xshader.hook;

import java.util.Iterator;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import xshader.gui.GuiShaderSettings;


public class Hooks_GuiOptions {
	
	private static final int BUTTON_SHADER_SETTINGS_ID = 1001;
	
	public static void hook_InitGui(List<GuiButton> list){
		Iterator<GuiButton> i = list.iterator();
		GuiButton sss = null;
		while(i.hasNext()){
			GuiButton guiButton = i.next();
			if(guiButton.id==8675309){
				sss = guiButton;
				i.remove();
			}
		}
		if(sss!=null){
			list.add(new GuiButton(BUTTON_SHADER_SETTINGS_ID, sss.xPosition, sss.yPosition, sss.getButtonWidth(), 20,  "Shader Settings..."));
		}
	}
	
	public static void hook_ActionPerformed(GuiOptions guiOptions, GuiButton guiButton){
		if(guiButton.enabled){
			if(guiButton.id==BUTTON_SHADER_SETTINGS_ID){
				guiOptions.mc.gameSettings.saveOptions();
				guiOptions.mc.displayGuiScreen(new GuiShaderSettings(guiOptions));
			}
		}
	}
	
}
