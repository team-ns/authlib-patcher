package com.nslauncher.patcher.transformers.yaggdrasil;

import com.nslauncher.patcher.config.Config;
import com.nslauncher.patcher.instrument.SafeClassWriter;
import com.nslauncher.patcher.transformers.Transformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.IOException;
import java.util.Optional;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Jeb
 */
public class GameProfileRepositoryTransformer extends Transformer {
    public GameProfileRepositoryTransformer() {
        super("com/mojang/authlib/yggdrasil/YggdrasilGameProfileRepository.class");
    }

    @Override
    public byte[] transform(byte[] bytecode, Config config) {
        ClassReader classReader = new ClassReader(bytecode);
        ClassWriter classWriter = new SafeClassWriter(classReader, null, ClassWriter.COMPUTE_FRAMES, config);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        final Optional<FieldNode> searchPageUrlOptional = classNode.fields
                .stream()
                .filter(fn -> fn.name.equals("searchPageUrl")).findFirst();
        final boolean hasBaseUrl;
        if (searchPageUrlOptional.isPresent()) {
            hasBaseUrl = true;
            searchPageUrlOptional.get().access = ACC_PRIVATE;
        } else {
            hasBaseUrl = false;
        }
        classNode.accept(new ClassVisitor(ASM5, classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                final MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (hasBaseUrl && "<init>".equals(name) && "(Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;Lcom/mojang/authlib/Environment;)V".equals(descriptor)) {
                    return new SearchPageUrlVisitor(methodVisitor, config);
                }
                if (!hasBaseUrl && "findProfilesByNames".equals(name)) {
                    return new FindProfileByNamesTransformer(methodVisitor, config);
                }
                return methodVisitor;
            }
        });
        return classWriter.toByteArray();
    }

    private class SearchPageUrlVisitor extends MethodVisitor {
        private final Config config;

        public SearchPageUrlVisitor(MethodVisitor methodVisitor, Config config) {
            super(ASM5, methodVisitor);
            this.config = config;
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            super.visitFieldInsn(opcode, owner, name, descriptor);
            if (name.equals("searchPageUrl")) {
                Label label2 = new Label();
                super.visitLabel(label2);
                super.visitVarInsn(ALOAD, 0);
                super.visitLdcInsn(String.format("%sprofiles/", config.getBaseUrl()));
                super.visitFieldInsn(PUTFIELD, "com/mojang/authlib/yggdrasil/YggdrasilGameProfileRepository", "searchPageUrl", "Ljava/lang/String;");
            }
        }
    }

    private class FindProfileByNamesTransformer extends MethodVisitor {
        private final Config config;

        private int ldcCalls = 0;

        public FindProfileByNamesTransformer(MethodVisitor methodVisitor, Config config) {
            super(ASM5, methodVisitor);
            this.config = config;
        }

        @Override
        public void visitLdcInsn(Object value) {
            ldcCalls++;
            if (ldcCalls == 1) {
                super.visitLdcInsn(String.format("%sprofiles/", config.getBaseUrl()));
            } else {
                super.visitLdcInsn(value);
            }
        }
    }
}
