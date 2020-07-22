package com.nslauncher.patcher.transformers.yaggdrasil;

import com.nslauncher.patcher.AuthlibPatcher;
import com.nslauncher.patcher.config.Config;
import com.nslauncher.patcher.transformers.Transformer;
import com.nslauncher.patcher.visitors.MethodTrueVisitor;
import org.objectweb.asm.*;

/**
 * @author Jeb
 */
public class PropertyTransformer extends Transformer {

    public PropertyTransformer() {
        super("com/mojang/authlib/properties/Property.class");
    }

    @Override
    public byte[] transform(byte[] bytecode, Config config) {
        ClassReader classReader = new ClassReader(bytecode);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        classReader.accept(new ClassVisitor(Opcodes.ASM5, classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                if ("isSignatureValid".equals(name)) {
                    return new MethodTrueVisitor(visitor);
                } else if ("hasSignature".equals(name)) {
                    return new MethodTrueVisitor(visitor);
                }
                return visitor;
            }
        }, 0);
        return classWriter.toByteArray();
    }
}
