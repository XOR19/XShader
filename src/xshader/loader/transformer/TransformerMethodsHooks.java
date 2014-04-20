package xshader.loader.transformer;

import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import xshader.Logger;
import xshader.hook.Hooks;
import net.minecraft.launchwrapper.IClassTransformer;


public class TransformerMethodsHooks implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
        if(transform(name, transformedName, classNode.methods)){
        	Logger.info("TRANSFORM: %s", transformedName);
	        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
	        classNode.accept(cw);
			return cw.toByteArray();
        }
        return basicClass;
	}
	
	private static boolean transform(String name, String transformedName, List<MethodNode> methods) {
		boolean changes = false;
		for(MethodNode method:methods){
			changes |= Hooks.transform(name, transformedName, method);
		}
		return changes;
	}
	
}
