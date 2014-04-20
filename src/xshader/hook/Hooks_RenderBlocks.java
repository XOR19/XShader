package xshader.hook;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import xshader.Globals;
import xshader.tessellator.ITessellator;


public class Hooks_RenderBlocks {
	
	public static void hook_RenderBlockByRenderType(RenderBlocks renderBlocks, Block block, int x, int y, int z){
		Integer materialID = Globals.blockMaterial.get(Block.blockRegistry.getNameForObject(block));
		int mid = 0;
		if(materialID!=null){
			mid = materialID.intValue();
		}
		((ITessellator)Tessellator.instance).setMaterialID(mid);
	}
	
	public static void hook_RenderFaceXNeg(){
		Tessellator tessellator = Tessellator.instance;
		tessellator.setNormal(-1, 0, 0);
	}
	
	public static void hook_RenderFaceXPos(){
		Tessellator tessellator = Tessellator.instance;
		tessellator.setNormal(1, 0, 0);
	}
	
	public static void hook_RenderFaceYNeg(){
		Tessellator tessellator = Tessellator.instance;
		tessellator.setNormal(0, -1, 0);
	}
	
	public static void hook_RenderFaceYPos(){
		Tessellator tessellator = Tessellator.instance;
		tessellator.setNormal(0, 1, 0);
	}
	
	public static void hook_RenderFaceZNeg(){
		Tessellator tessellator = Tessellator.instance;
		tessellator.setNormal(0, 0, -1);
	}
	
	public static void hook_RenderFaceZPos(){
		Tessellator tessellator = Tessellator.instance;
		tessellator.setNormal(0, 0, 1);
	}
	
}
