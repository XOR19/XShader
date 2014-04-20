package xshader.hook;

import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;


public class MethodHooks {

	private boolean clear;
	
	private InsnList start;
	
	private InsnList end;
	
	public MethodHooks(boolean clear, InsnList start, InsnList end) {
		this.clear = clear;
		this.start = start;
		this.end = end;
	}

	public boolean transform(MethodNode method) {
		boolean changed = false;
		if(this.clear){
			method.instructions = new InsnList();
			changed = true;
		}
		if(this.start!=null){
			method.instructions.insert(copyOf(this.start));
			changed = true;
		}
		if(this.end!=null){
			AbstractInsnNode last = method.instructions.getLast();
			if(last==null){
				method.instructions.insert(copyOf(this.end));
			}else{
				method.instructions.insert(last, copyOf(this.end));
				while(last!=null){
					while(last!=null && !isReturn(last.getOpcode())){
						last = last.getPrevious();
					}
					if(last!=null){
						method.instructions.insertBefore(last, copyOf(this.end));
						last = last.getPrevious();
					}
				}
			}
			changed = true;
		}
		//Textifier tf = new Textifier();
		//method.accept(new TraceMethodVisitor(tf));
		//tf.print(new PrintWriter(System.out));
		return changed;
	}
	
	private static InsnList copyOf(InsnList instList){
		InsnList copy = new InsnList();
		ListIterator<AbstractInsnNode> i = instList.iterator();
		while(i.hasNext()){
			copy.add(i.next().clone(null));
		}
		return copy;
	}
	
	private static boolean isReturn(int opcode){
		return opcode==Opcodes.IRETURN || opcode==Opcodes.LRETURN || opcode==Opcodes.FRETURN || opcode==Opcodes.DRETURN || opcode==Opcodes.ARETURN || opcode==Opcodes.RETURN;
	}
	
}
