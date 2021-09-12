package com.nslauncher.patcher.transformers.yaggdrasil;

import com.nslauncher.patcher.config.Config;
import com.nslauncher.patcher.transformers.Transformer;
import com.nslauncher.patcher.visitors.MethodTrueVisitor;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.Optional;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Jeb
 */
public class MinecraftSessionTransformer extends Transformer {

    public MinecraftSessionTransformer() {
        super("com/mojang/authlib/yggdrasil/YggdrasilMinecraftSessionService.class");
    }

    @Override
    public byte[] transform(byte[] bytecode, Config config) {
        ClassReader classReader = new ClassReader(bytecode);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        final Optional<FieldNode> baseUrlOptional = classNode.fields
                .stream()
                .filter(fn -> fn.name.equals("baseUrl")).findFirst();
        boolean hasBaseUrl;
        if (baseUrlOptional.isPresent()) {
            hasBaseUrl = true;
            baseUrlOptional.get().access = ACC_PRIVATE;
        } else {
            hasBaseUrl = false;
        }
        classNode.fields
                .stream()
                .filter(fn -> fn.name.equals("CHECK_URL"))
                .forEach(fn -> fn.access = ACC_PRIVATE | ACC_STATIC);
        if (classNode.methods.stream().noneMatch(methodNode -> methodNode.name.equals("launcherJoinRequest"))) {
            MethodVisitor methodVisitor;
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_NATIVE, "launcherJoinRequest", "(Lcom/mojang/authlib/yggdrasil/request/JoinMinecraftServerRequest;)V", null, null);
            methodVisitor.visitEnd();
        }
        classNode.accept(new ClassVisitor(ASM5, classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                if ("<init>".equals(name) && "(Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;Lcom/mojang/authlib/Environment;)V".equals(descriptor)) {
                    return new ConstructorVisitor(visitor, config);
                }
                if ("<clinit>".equals(name)) {
                    return new StaticInitializerVisitor(visitor, config);
                }
                if ("isWhitelistedDomain".equals(name)) {
                    return new MethodTrueVisitor(visitor);
                }
                if ("joinServer".equals(name)) {
                    return new JoinServerVisitor(visitor);
                }
                if ("fillGameProfile".equals(name) && !hasBaseUrl) {
                    return new FillGameProfileVisitor(visitor, config);
                }
                return visitor;
            }
        });
        return classWriter.toByteArray();
    }


    public static class StaticInitializerVisitor extends MethodVisitor {
        private final Config config;

        public StaticInitializerVisitor(MethodVisitor methodVisitor, Config config) {
            super(ASM5, methodVisitor);
            this.config = config;
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == RETURN) {
                Label label = new Label();
                super.visitLabel(label);
                super.visitLdcInsn(String.format("%shasJoined", config.getBaseUrl()));
                super.visitMethodInsn(INVOKESTATIC, "com/mojang/authlib/HttpAuthenticationService", "constantURL", "(Ljava/lang/String;)Ljava/net/URL;", false);
                super.visitFieldInsn(PUTSTATIC, "com/mojang/authlib/yggdrasil/YggdrasilMinecraftSessionService", "CHECK_URL", "Ljava/net/URL;");
            }
            super.visitInsn(opcode);
        }

    }

    public static class ConstructorVisitor extends MethodVisitor {
        private final Config config;

        public ConstructorVisitor(MethodVisitor methodVisitor, Config config) {
            super(ASM5, methodVisitor);
            this.config = config;
        }


        @Override
        public void visitInsn(int opcode) {
            if (opcode == RETURN) {
                Label label = new Label();
                super.visitLabel(label);
                super.visitVarInsn(ALOAD, 0);
                super.visitLdcInsn(config.getBaseUrl());
                super.visitFieldInsn(PUTFIELD, "com/mojang/authlib/yggdrasil/YggdrasilMinecraftSessionService", "baseUrl", "Ljava/lang/String;");
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitLabel(Label label) {
            super.visitLabel(label);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (opcode == INVOKESTATIC && name.equals("constantURL")) {
                super.visitMethodInsn(opcode, "com/mojang/authlib/yggdrasil/YggdrasilMinecraftSessionService", "proxyUrl", "(Ljava/lang/String;)Ljava/net/URL;", false);
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }

        }

    }


    public static class JoinServerVisitor extends MethodVisitor {
        private final MethodVisitor methodVisitor;

        public JoinServerVisitor(MethodVisitor methodVisitor) {
            super(ASM5, null);
            this.methodVisitor = methodVisitor;
        }

        @Override
        public void visitCode() {
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(78, label0);
            methodVisitor.visitTypeInsn(NEW, "com/mojang/authlib/yggdrasil/request/JoinMinecraftServerRequest");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "com/mojang/authlib/yggdrasil/request/JoinMinecraftServerRequest", "<init>", "()V", false);
            methodVisitor.visitVarInsn(ASTORE, 4);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(79, label1);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitFieldInsn(PUTFIELD, "com/mojang/authlib/yggdrasil/request/JoinMinecraftServerRequest", "accessToken", "Ljava/lang/String;");
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(80, label2);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "com/mojang/authlib/GameProfile", "getId", "()Ljava/util/UUID;", false);
            methodVisitor.visitFieldInsn(PUTFIELD, "com/mojang/authlib/yggdrasil/request/JoinMinecraftServerRequest", "selectedProfile", "Ljava/util/UUID;");
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLineNumber(81, label3);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitFieldInsn(PUTFIELD, "com/mojang/authlib/yggdrasil/request/JoinMinecraftServerRequest", "serverId", "Ljava/lang/String;");
            Label label4 = new Label();
            methodVisitor.visitLabel(label4);
            methodVisitor.visitLineNumber(83, label4);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "com/mojang/authlib/yggdrasil/YggdrasilMinecraftSessionService", "launcherJoinRequest", "(Lcom/mojang/authlib/yggdrasil/request/JoinMinecraftServerRequest;)V", false);
            Label label5 = new Label();
            methodVisitor.visitLabel(label5);
            methodVisitor.visitLineNumber(84, label5);
            methodVisitor.visitInsn(RETURN);
            Label label6 = new Label();
            methodVisitor.visitLabel(label6);
            methodVisitor.visitLocalVariable("this", "Lcom/mojang/authlib/yggdrasil/YggdrasilMinecraftSessionService;", null, label0, label6, 0);
            methodVisitor.visitLocalVariable("profile", "Lcom/mojang/authlib/GameProfile;", null, label0, label6, 1);
            methodVisitor.visitLocalVariable("authenticationToken", "Ljava/lang/String;", null, label0, label6, 2);
            methodVisitor.visitLocalVariable("serverId", "Ljava/lang/String;", null, label0, label6, 3);
            methodVisitor.visitLocalVariable("request", "Lcom/mojang/authlib/yggdrasil/request/JoinMinecraftServerRequest;", null, label1, label6, 4);
            methodVisitor.visitMaxs(3, 5);
            methodVisitor.visitEnd();
        }
    }

    private static class FillGameProfileVisitor extends MethodVisitor {
        private final Config config;
        private int constantCalls = 0;

        public FillGameProfileVisitor(MethodVisitor visitor, Config config) {
            super(ASM5, visitor);
            this.config = config;

        }

        @Override
        public void visitCode() {
            Label label0 = new Label();
            super.visitLabel(label0);
            super.visitInsn(ICONST_0);
            super.visitVarInsn(ISTORE, 2);
            super.visitCode();
        }

        @Override
        public void visitLdcInsn(Object value) {
            constantCalls++;
            if (constantCalls == 1) {
                super.visitLdcInsn(String.format("%sprofile", config.getBaseUrl()));
            } else {
                super.visitLdcInsn(value);
            }
        }

    }
}
