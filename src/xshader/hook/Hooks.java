package xshader.hook;

import java.util.HashMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import xshader.Logger;


public class Hooks {
	
	private static HashMap<String, HashMap<String, MethodHooks>> methodHooks = new HashMap<String, HashMap<String, MethodHooks>>();
	
	static{
		HashMap<String, MethodHooks> hm;
		InsnList instructions;
		
		methodHooks.put("net.minecraft.client.gui.GuiOptions", hm = new HashMap<String, MethodHooks>());
		instructions = new InsnList();
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/GuiScreen", "buttonList", "Ljava/util/List;"));
		instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xshader/hook/Hooks_GuiOptions", "hook_InitGui", "(Ljava/util/List;)V"));
		hm.put("initGui()V", new MethodHooks(false, null, instructions));
		instructions = new InsnList();
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
		instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xshader/hook/Hooks_GuiOptions", "hook_ActionPerformed", "(Lnet/minecraft/client/gui/GuiOptions;Lnet/minecraft/client/gui/GuiButton;)V"));
		hm.put("actionPerformed(Lnet/minecraft/client/gui/GuiButton;)V", new MethodHooks(false, null, instructions));
		
		methodHooks.put("net.minecraft.client.renderer.RenderBlocks", hm = new HashMap<String, MethodHooks>());
		instructions = new InsnList();
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
		instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
		instructions.add(new VarInsnNode(Opcodes.ILOAD, 3));
		instructions.add(new VarInsnNode(Opcodes.ILOAD, 4));
		instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xshader/hook/Hooks_RenderBlocks", "hook_RenderBlockByRenderType", "(Lnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraft/block/Block;III)V"));
		hm.put("renderBlockByRenderType(Lnet/minecraft/block/Block;III)Z", new MethodHooks(false, instructions, null));
		instructions = new InsnList();
		instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xshader/hook/Hooks_RenderBlocks", "hook_RenderFaceXNeg", "()V"));
		hm.put("renderFaceXNeg(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V", new MethodHooks(false, instructions, null));
		instructions = new InsnList();
		instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xshader/hook/Hooks_RenderBlocks", "hook_RenderFaceXPos", "()V"));
		hm.put("renderFaceXPos(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V", new MethodHooks(false, instructions, null));
		instructions = new InsnList();
		instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xshader/hook/Hooks_RenderBlocks", "hook_RenderFaceYNeg", "()V"));
		hm.put("renderFaceYNeg(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V", new MethodHooks(false, instructions, null));
		instructions = new InsnList();
		instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xshader/hook/Hooks_RenderBlocks", "hook_RenderFaceYPos", "()V"));
		hm.put("renderFaceYPos(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V", new MethodHooks(false, instructions, null));
		instructions = new InsnList();
		instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xshader/hook/Hooks_RenderBlocks", "hook_RenderFaceZNeg", "()V"));
		hm.put("renderFaceZNeg(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V", new MethodHooks(false, instructions, null));
		instructions = new InsnList();
		instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xshader/hook/Hooks_RenderBlocks", "hook_RenderFaceZPos", "()V"));
		hm.put("renderFaceZPos(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V", new MethodHooks(false, instructions, null));
		
	}

	public static boolean transform(String name, String transformedName, MethodNode method) {
		HashMap<String, MethodHooks> hm = methodHooks.get(transformedName);
		if(hm==null){
			hm = methodHooks.get(name);
		}
		if(hm!=null){
			MethodHooks mh = hm.get(method.name+method.desc);
			if(mh!=null){
				Logger.info("TRANSFORM: %s", method.name+method.desc);
				return mh.transform(method);
			}
		}
		return false;
	}
	
}
