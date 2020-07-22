package com.nslauncher.patcher.visitors;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Jeb
 */
public class MethodTrueVisitor extends MethodVisitor {
    private MethodVisitor visitor;

    public MethodTrueVisitor(MethodVisitor visitor) {
        super(Opcodes.ASM5);
        this.visitor = visitor;
    }

    @Override
    public void visitCode() {
        visitor.visitCode();
        visitor.visitInsn(Opcodes.ICONST_1);
        visitor.visitInsn(Opcodes.IRETURN);
        visitor.visitMaxs(1, 1);
        visitor.visitEnd();
    }
}
