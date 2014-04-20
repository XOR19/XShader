package xshader.loader.transformer;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import xshader.Logger;


public class TransformerCallToMine implements IClassTransformer{

	private static HashMap<String, ClassMapping> mappings = new HashMap<String, ClassMapping>();
	
	private static class ClassMapping{
		
		String to;
	
		HashMap<String, String> methods = new HashMap<String, String>();
		
		public ClassMapping(String to) {
			this.to = to;
		}

		public void map(String both) {
			map(both, both);
		}
		
		@SuppressWarnings("hiding")
		public void map(String from, String to) {
			this.methods.put(from, to);
		}
		
	}
	
	static{
		ClassMapping cm;
		mappings.put("org/lwjgl/opengl/GL11", cm = new ClassMapping("xshader/hook/Hooks_GL"));
		cm.map("glEnable");
		cm.map("glDisable");
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
        if(transform(classNode.methods)){
        	Logger.info("TRANSFORM: %s", transformedName);
	        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
	        classNode.accept(cw);
			return cw.toByteArray();
        }
        return basicClass;
	}

	private static boolean transform(List<MethodNode> methods) {
		boolean changes = false;
		for(MethodNode method:methods){
			changes |= transform(method);
		}
		return changes;
	}

	private static boolean transform(MethodNode method) {
		ListIterator<AbstractInsnNode> i = method.instructions.iterator();
		boolean changed = false;
		while(i.hasNext()){
			AbstractInsnNode n = i.next();
			if(n instanceof MethodInsnNode){
				MethodInsnNode mn = (MethodInsnNode)n;
				ClassMapping cm = mappings.get(mn.owner);
				if(cm!=null){
					String to = cm.methods.get(mn.name);
					if(to!=null){
						mn.owner = cm.to;
						mn.name = to;
						changed = true;
					}
				}
			}
		}
		return changed;
	}
	
}
