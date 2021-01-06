package com.nslauncher.patcher.transformers.yaggdrasil;

import com.nslauncher.patcher.config.Config;
import com.nslauncher.patcher.transformers.Transformer;
import com.nslauncher.patcher.visitors.MethodTrueVisitor;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

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
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        if (classNode.methods.stream().noneMatch(methodNode -> methodNode.name.equals("proxyUrl"))) {
            MethodVisitor methodVisitor;
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "proxyUrl", "(Ljava/lang/String;)Ljava/net/URL;", null, null);
            makeProxy(methodVisitor, config);
        }
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
                    return new ConstructorVisitor(visitor);
                }
                if ("<clinit>".equals(name)) {
                    return new ConstructorVisitor(visitor);
                }
                if ("isWhitelistedDomain".equals(name)) {
                    return new MethodTrueVisitor(visitor);
                }
                if ("joinServer".equals(name)) {
                    return new JoinServerVisitor(visitor);
                }
                if ("proxyUrl".equals(name)) {
                    return new ProxyVisitor(visitor, config);
                }
                return visitor;
            }

        });
        return classWriter.toByteArray();
    }

    private void makeProxy(MethodVisitor methodVisitor, Config config) {
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(10, label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitLdcInsn("join");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false);
        Label label1 = new Label();
        methodVisitor.visitJumpInsn(IFEQ, label1);
        Label label2 = new Label();
        methodVisitor.visitLabel(label2);
        methodVisitor.visitLineNumber(11, label2);
        methodVisitor.visitLdcInsn(config.getBaseUrl() + "/join");
        methodVisitor.visitMethodInsn(INVOKESTATIC, "com/mojang/authlib/HttpAuthenticationService", "constantURL", "(Ljava/lang/String;)Ljava/net/URL;", false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLineNumber(13, label1);
        methodVisitor.visitFrame(F_NEW, 0, null, 0, null);
        methodVisitor.visitLdcInsn(config.getBaseUrl() + "/hasJoined");
        methodVisitor.visitMethodInsn(INVOKESTATIC, "com/mojang/authlib/HttpAuthenticationService", "constantURL", "(Ljava/lang/String;)Ljava/net/URL;", false);
        methodVisitor.visitInsn(ARETURN);
        Label label3 = new Label();
        methodVisitor.visitLabel(label3);
        methodVisitor.visitLocalVariable("url", "Ljava/lang/String;", null, label0, label3, 0);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
    }

    public static class ProxyVisitor extends MethodVisitor {
        private final Config config;
        private final MethodVisitor methodVisitor;

        public ProxyVisitor(MethodVisitor visitor, Config config) {
            super(ASM5);
            this.config = config;
            this.methodVisitor = visitor;
        }

        @Override
        public void visitCode() {
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(10, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitLdcInsn("join");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false);
            Label label1 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label1);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(11, label2);
            methodVisitor.visitLdcInsn(config.getBaseUrl() + "/join");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "com/mojang/authlib/HttpAuthenticationService", "constantURL", "(Ljava/lang/String;)Ljava/net/URL;", false);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(13, label1);
            methodVisitor.visitFrame(F_NEW, 0, null, 0, null);
            methodVisitor.visitLdcInsn(config.getBaseUrl() + "/hasJoined");
            methodVisitor.visitMethodInsn(INVOKESTATIC, "com/mojang/authlib/HttpAuthenticationService", "constantURL", "(Ljava/lang/String;)Ljava/net/URL;", false);
            methodVisitor.visitInsn(ARETURN);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLocalVariable("url", "Ljava/lang/String;", null, label0, label3, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
    }

    public static class ConstructorVisitor extends MethodVisitor {

        public ConstructorVisitor(MethodVisitor methodVisitor) {
            super(ASM5, methodVisitor);
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
}
