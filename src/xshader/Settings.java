package xshader;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;


public class Settings {
	
	private static Configuration config;
	
	public static int real3DType = Constants.REAL3D_NONE;
	
	@SuppressWarnings("hiding")
	public static void loadFrom(Configuration config){
		Settings.config = config;
		ConfigCategory settings = config.getCategory("Settings");
		real3DType = settings.get("real3DType").getInt(Constants.REAL3D_NONE);
	}
	
	@SuppressWarnings("hiding")
	public static void saveTo(Configuration config){
		ConfigCategory settings = config.getCategory("Settings");
		getProperty(settings, "real3DType", "Type of 3d rawing, 0 = normal, 1 = anaglyph").set(real3DType);
	}
	
	public static void save(){
		saveTo(config);
	}
	
	private static Property getProperty(ConfigCategory settings, String name, String comment){
		Property prop = settings.get(name);
		if(prop.comment==null || prop.comment.trim().isEmpty()){
			prop.comment = comment;
		}
		return prop;
	}
	
}
