package xshader;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;


public class Forge {
	
	private static Forge instance = new Forge();
	
	public static void init(){
		MinecraftForge.EVENT_BUS.register(instance);
	}
	
	private Forge(){
		
	}
	
	@SubscribeEvent
	public void renderHand(RenderHandEvent event){
		event.setCanceled(true);
	}
	
}
