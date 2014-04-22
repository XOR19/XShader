package xshader.loader;

import java.io.File;
import java.util.ArrayList;

import net.minecraftforge.common.config.Configuration;
import xshader.Init;
import xshader.Settings;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;


public class CoreContainer extends DummyModContainer {
	
	public CoreContainer(){
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId=CorePlugin.MODID;
        meta.name=CorePlugin.NAME;
        meta.version=CorePlugin.VERSION;
        meta.credits=CorePlugin.CREDITS;
        meta.authorList=new ArrayList<String>();
        meta.authorList.add(CorePlugin.AUTHORS);
        meta.description=CorePlugin.DESCRIPTION;
        meta.url=CorePlugin.URL;
        meta.updateUrl=CorePlugin.UPDATE_URL;
        meta.screenshots=CorePlugin.SCREEN_SHOTS;
        meta.logoFile=CorePlugin.LOGO_FILE;
    }
	
	@Override
	public boolean registerBus(EventBus bus, LoadController controller){
		bus.register(this);
		return true;
	}
	
	@SuppressWarnings("static-method")
	@Subscribe
	public void preInit(FMLPreInitializationEvent event){
		if(event.getSide()==Side.SERVER){
			throw new RuntimeException();
		}
		Settings.loadFrom(new Configuration(event.getModConfigurationDirectory()));
	}
	
	@SuppressWarnings({ "static-method", "unused" })
	@Subscribe
	public void init(FMLInitializationEvent event){
		Init.init();
	}
	
	@Override
	public Disableable canBeDisabled(){
		return Disableable.RESTART;
	}
	
	@Override
	public File getSource() {
		return CorePlugin.location;
	}

	@Override
    public Object getMod(){
        return this;
    }

	@Override
	public String getGuiClassName() {
		return "xshader.gui.GuiFactory";
	}
	
}
