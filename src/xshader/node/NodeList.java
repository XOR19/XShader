package xshader.node;

import java.io.File;
import java.util.HashMap;

import xshader.Logger;
import xshader.parser.ParserException;
import xshader.parser.Files;


public class NodeList {
	
	private static HashMap<String, NodeType> nodeTypes = new HashMap<String, NodeType>();
	
	public static void loadFrom(File file){
		String name = file.getName();
		if(file.isDirectory()){
			File[] files = file.listFiles();
			for(File f:files){
				loadFrom(f);
			}
		}else if(file.isFile() && name.endsWith(".node")){
			try {
				NodeType nt = NodeType.parse(Files.readFile(file), name.substring(0, name.length()-5));
				if(!nodeTypes.containsKey(nt.getName())){
					Logger.info("Loaded Node: %s", nt.getName());
					nodeTypes.put(nt.getName(), nt);
				}
			} catch (ParserException e) {
				Logger.severe("Phaser Exception %s in %s", e.getMessage(), name.substring(0, name.length()-5));
				e.printStackTrace();
			}
		}
	}
	
	public static NodeType getNodeTypeByName(String name){
		return nodeTypes.get(name);
	}
	
}
