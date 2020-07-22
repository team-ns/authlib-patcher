package com.nslauncher.patcher.transformers.bungee;

import com.nslauncher.patcher.config.Config;
import com.nslauncher.patcher.transformers.Transformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Optional;

/**
 * @author Jeb
 */
public class BungeeTransformer extends Transformer {
    public BungeeTransformer() {
        super("net/md_5/bungee/connection/InitialHandler.class");
    }

    @Override
    public byte[] transform(byte[] bytecode, Config config) {
        ClassReader classReader = new ClassReader(bytecode);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        classNode.methods.stream()
                .filter(methodNode -> methodNode.name.equals("handle"))
                .forEach(methodNode -> methodNode.instructions.forEach(ins -> {
                    if (ins.getOpcode() == Opcodes.LDC) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode) ins;
                        boolean isAuthUrl = Optional.ofNullable(ins.getNext())
                                .map(AbstractInsnNode::getNext)
                                .flatMap(abstractInsnNode -> {
                                    if (abstractInsnNode instanceof VarInsnNode) {
                                        if (abstractInsnNode.getOpcode() == Opcodes.ALOAD) {
                                            return Optional.of(((VarInsnNode) abstractInsnNode).var == 5);
                                        }
                                    }
                                    return Optional.empty();
                                })
                                .orElse(false);
                        if (isAuthUrl) {
                            ldcInsnNode.cst = config.getBaseUrl() + "/hasJoined?username=";

                        }
                    }
                }));
        classNode.accept(new ClassVisitor(Opcodes.ASM5, classWriter) {
        });
        return classWriter.toByteArray();
    }
}
