package xshader.loader;

import java.io.File;
import java.util.Map;

import xshader.Logger;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@TransformerExclusions({"xshader", "xshader.loader"})
@MCVersion("1.7.2")
@Name("XShader")
public class CorePlugin implements IFMLLoadingPlugin {

	public static final String MODID = "xshader";
	public static final String NAME = "XShader";
	public static final String VERSION = "1.0.0";
	public static final String CREDITS = "";
	public static final String AUTHORS = "XOR";
	public static final String DESCRIPTION = "";
	public static final String URL = "";
	public static final String UPDATE_URL = "";
	public static final String LOGO_FILE = "";
	public static final String[] SCREEN_SHOTS = new String[0];
	
	public static File location;
	
	public CorePlugin(){
		
	}
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{"xshader.loader.transformer.TransformerMethodsHooks",
				"xshader.loader.transformer.TransformerCallToMine",
				"xshader.loader.transformer.TransformerClassReplacer"};
	}

	@Override
	public String getModContainerClass() {
		return "xshader.loader.CoreContainer";
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		location = (File) data.get("coremodLocation");
		Logger.init(new File((File) data.get("mcLocation"), "xshader/XShader.log"));
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
	
}
