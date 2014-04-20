package xshader.loader.transformer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import xshader.Logger;


public class TransformerClassReplacer implements IClassTransformer {
	
	private static HashMap<String, String> overridings = new HashMap<String, String>();
	
	static{
		overridings.put("net.minecraft.client.renderer.Tessellator", "xshader.replace.Tessellator");
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		String overriding = overridings.get(transformedName);
		if(overriding==null){
			overriding = overridings.get(name);
		}
		if(overriding==null){
			return basicClass;
		}
		try {
			byte[] overridingBytes = Launch.classLoader.getClassBytes(overriding);
			ClassNode classNode = new ClassNode();
	        ClassReader classReader = new ClassReader(overridingBytes);
	        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
	        String oldOwner = overriding.replace('.', '/');
	        String newOwner = transformedName.replace('.', '/');
	        String oldOwnerDesk = "L"+oldOwner+";";
	        String newOwnerDesk = "L"+newOwner+";";
	        classNode.name = newOwner;
	        List<InnerClassNode> innerClassNodes = classNode.innerClasses;
	        for(InnerClassNode innerClassNode:innerClassNodes){
	        	innerClassNode.outerName = newOwner;
	        }
	        List<FieldNode> fields = classNode.fields;
	        for(FieldNode field:fields){
	        	field.desc = field.desc.replace(oldOwnerDesk, newOwnerDesk);
	        }
	        List<MethodNode> methods = classNode.methods;
	        for(MethodNode method:methods){
	        	for(LocalVariableNode lvn:method.localVariables){
	        		lvn.desc = lvn.desc.replace(oldOwnerDesk, newOwnerDesk);
	        	}
	        	method.desc = method.desc.replace(oldOwnerDesk, newOwnerDesk);
	        	ListIterator<AbstractInsnNode> li = method.instructions.iterator();
	        	while(li.hasNext()){
	        		AbstractInsnNode ain = li.next();
	        		if(ain instanceof MethodInsnNode){
	        			MethodInsnNode min = (MethodInsnNode)ain;
	        			if(min.owner.equals(oldOwner)){
	        				min.owner = newOwner;
	        			}
	        			min.desc = min.desc.replace(oldOwnerDesk, newOwnerDesk);
	        		}else if(ain instanceof FieldInsnNode){
	        			FieldInsnNode fin = (FieldInsnNode)ain;
	        			if(fin.owner.equals(oldOwner)){
	        				fin.owner = newOwner;
	        			}
	        			fin.desc = fin.desc.replace(oldOwnerDesk, newOwnerDesk);
	        		}else if(ain instanceof InvokeDynamicInsnNode){
	        			InvokeDynamicInsnNode idin = (InvokeDynamicInsnNode)ain;
	        			idin.desc = idin.desc.replace(oldOwnerDesk, newOwnerDesk);
	        			String owner = idin.bsm.getOwner();
	        			if(owner.equals(oldOwner)){
	        				owner = newOwner;
	        			}
	        			idin.bsm = new Handle(idin.bsm.getTag(), owner, idin.bsm.getName(), idin.bsm.getDesc().replace(oldOwnerDesk, newOwnerDesk));
	        		}else if(ain instanceof TypeInsnNode){
	        			TypeInsnNode tin = (TypeInsnNode)ain;
	        			if(tin.desc.equals(oldOwner))
	        				tin.desc = newOwner;
	        		}else if(ain instanceof FrameNode){
	        			FrameNode fn = (FrameNode)ain;
	        			for(int i=0; i<fn.local.size(); i++){
	        				Object obj = fn.local.get(i);
	        				if(obj.equals(oldOwner)){
	        					fn.local.set(i, newOwner);
	        				}
	        			}
	        			for(int i=0; i<fn.stack.size(); i++){
	        				Object obj = fn.stack.get(i);
	        				if(obj.equals(oldOwner)){
	        					fn.stack.set(i, newOwner);
	        				}
	        			}
	        		}else if(ain instanceof MultiANewArrayInsnNode){
	        			MultiANewArrayInsnNode main = (MultiANewArrayInsnNode)ain;
	        			main.desc = main.desc.replace(oldOwnerDesk, newOwnerDesk);
	        		}else if(ain instanceof LdcInsnNode){
	        			LdcInsnNode lin = (LdcInsnNode)ain;
	        			if(lin.cst instanceof Type){
	        				lin.cst = Type.getType(((Type)lin.cst).getDescriptor().replace(oldOwnerDesk, newOwnerDesk));
	        			}
	        		}
	        	}
	        }
	        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
	        classNode.accept(cw);
	        //classNode.accept(new TraceClassVisitor(new PrintWriter(System.out)));
	        Logger.info("REPLACED: %s with %s", transformedName, overriding);
			return cw.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return basicClass;
		}
	}
	
}
