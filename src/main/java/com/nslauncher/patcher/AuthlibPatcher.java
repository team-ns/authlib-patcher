package com.nslauncher.patcher;


import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Jeb
 */
public class AuthlibPatcher {

    public static void main(String[] args) throws Exception {
        String url = args[1];
        String jar = args[0];
        byte[] bytes = getBytes("com/mojang/authlib/yggdrasil/YggdrasilMinecraftSessionService.class", jar);
        if (bytes != null)
            saveBytes(jar, "com/mojang/authlib/yggdrasil/YggdrasilMinecraftSessionService.class", transformSessionService(bytes, url));
        bytes = getBytes("com/mojang/authlib/properties/Property.class", jar);
        if (bytes != null)
            saveBytes(jar, "com/mojang/authlib/properties/Property.class", transformCheckSignature(bytes));
        bytes = getBytes("net/md_5/bungee/connection/InitialHandler.class", jar);
        if (bytes != null)
            saveBytes(jar, "net/md_5/bungee/connection/InitialHandler.class", transformBungee(bytes, url));
    }

    private static byte[] transformBungee(byte[] bytes, String url) {
        ClassReader classReader = new ClassReader(bytes);
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
                                .flatMap(abstractInsnNode ->  {
                                    if (abstractInsnNode instanceof VarInsnNode) {
                                        if (abstractInsnNode.getOpcode() == Opcodes.ALOAD){
                                            return Optional.of(((VarInsnNode) abstractInsnNode).var == 5);
                                        }
                                    }
                                    return Optional.empty();
                                })
                                .orElse(false);
                        if (isAuthUrl) {
                            ldcInsnNode.cst = url + "/hasJoined?username=";

                        }
                    }
                }));
        classNode.accept(new ClassVisitor(Opcodes.ASM5, classWriter) {
        });
        return classWriter.toByteArray();
    }

    private static byte[] transformCheckSignature(byte[] bytes) {
        ClassReader classReader = new ClassReader(bytes);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        classReader.accept(new ClassVisitor(Opcodes.ASM5, classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                if ("isSignatureValid".equals(name)) {
                    return new Visitor(visitor);
                }
                return visitor;
            }
        }, 0);
        return classWriter.toByteArray();
    }

    private static byte[] transformSessionService(byte[] bytes, String url) {
        ClassReader classReader = new ClassReader(bytes);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        classNode.methods.stream()
                .filter(methodNode -> methodNode.name.equals("<clinit>"))
                .forEach(methodNode -> methodNode.instructions.forEach(ins -> {
                    if (ins.getOpcode() == Opcodes.LDC) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode) ins;
                        if (ins.getNext().getNext().getOpcode() == Opcodes.PUTSTATIC) {
                            FieldInsnNode fieldInsnNode = (FieldInsnNode) ins.getNext().getNext();
                            if (fieldInsnNode.name.equals("CHECK_URL")) {
                                ldcInsnNode.cst = url + "/join";
                            } else if (fieldInsnNode.name.equals("JOIN_URL")) {
                                ldcInsnNode.cst = url + "/hasJoined";
                            }
                        }
                    }
                }));
        classNode.accept(new ClassVisitor(Opcodes.ASM5, classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                if ("isWhitelistedDomain".equals(name)) {
                    return new Visitor(visitor);
                }
                return visitor;
            }
        });
        return classWriter.toByteArray();
    }

    private static void saveBytes(String jar, String javaFileName, byte[] bytes) throws ZipException {
        ZipFile file = new ZipFile(jar);
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setFileNameInZip(javaFileName);
        file.addStream(new ByteArrayInputStream(bytes), zipParameters);
    }

    private static byte[] getBytes(String javaFileName, String jar) throws IOException {
        try (JarFile jarFile = new JarFile(jar)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class") &&
                        entry.getName().equals(javaFileName)) {
                    try (InputStream inputStream = jarFile.getInputStream(entry)) {
                        return getBytes(inputStream);
                    } catch (IOException ioException) {
                        System.out.println("Could not obtain class entry for " + entry.getName());
                        throw ioException;
                    }
                }
            }
        }
        return null;
    }

    private static byte[] getBytes(InputStream is) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[0xFFFF];
            for (int len; (len = is.read(buffer)) != -1; )
                os.write(buffer, 0, len);
            os.flush();
            return os.toByteArray();
        }
    }

    public static class Visitor extends MethodVisitor {
        private MethodVisitor visitor;

        public Visitor(MethodVisitor visitor) {
            super(Opcodes.ASM5);
            this.visitor = visitor;
        }

        @Override
        public void visitCode() {
            visitor.visitCode();
            visitor.visitInsn(Opcodes.ICONST_1);
            visitor.visitInsn(Opcodes.IRETURN);
            visitor.visitEnd();
        }
    }

}
