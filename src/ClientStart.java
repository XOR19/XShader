import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import cpw.mods.fml.relauncher.CoreModManager;

/**
 * 
 * Run this in case that Minecraft is another project
 * 
 * @author XOR
 *
 */
public class ClientStart {

	/**
	 * 
	 * Start the client
	 * 
	 * @param args only allowed --username yourName
	 */
	public static void main(String args[]){
		System.setProperty("fml.ignoreInvalidMinecraftCertificates", "true");
		String userName = System.getProperty("user.name", "XShaderPlayer"+(Minecraft.getSystemTime()%1000));
		String assetsDir="";
		List<String> coreMods = new ArrayList<String>();
		if(args!=null){
			for(int i=0; i<args.length-1; i++){
				if(args[i].equals("--username")){
					userName = args[i+1];
				}else if(args[i].equals("--assetsDir")){
					assetsDir = args[i+1];
				}else if(args[i].equals("--coreMod")){
					coreMods.add(args[i+1]);
				}
			}
		}
		if(!coreMods.isEmpty())
			addCoreMods(coreMods.toArray(new String[coreMods.size()]));
		Launch.main(new String[]{"--version", "1.6", "--tweakClass", "cpw.mods.fml.common.launcher.FMLTweaker", "--accessToken", "0", "--username", userName, assetsDir!=""?"--assetsDir":"", assetsDir});
	}
	
	private static void addCoreMods(String...names){
		try {
			Field f = CoreModManager.class.getDeclaredField("rootPlugins");
			f.setAccessible(true);
			String[] coreMods = (String[]) f.get(null);
			String[] moreCoreMods = new String[coreMods.length+names.length];
			System.arraycopy(coreMods, 0, moreCoreMods, 0, coreMods.length);
			System.arraycopy(names, 0, moreCoreMods, coreMods.length, names.length);
			f.set(null, moreCoreMods);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
}
