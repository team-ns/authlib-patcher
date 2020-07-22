package com.nslauncher.patcher.helper;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;

import java.io.*;

/**
 * @author Jeb
 */
public final class JarHelper {
    public static void saveBytes(File jar, String javaFileName, byte[] bytes) throws ZipException {
        ZipFile file = new ZipFile(jar);
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setFileNameInZip(javaFileName);
        file.addStream(new ByteArrayInputStream(bytes), zipParameters);
    }

    public static byte[] getBytes(String javaFileName, File jar) {
        try {
            ZipFile file = new ZipFile(jar);
            FileHeader fileHeader = file.getFileHeader(javaFileName);
            return getBytes(file.getInputStream(fileHeader));
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] getBytes(InputStream is) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); is) {
            byte[] buffer = new byte[0xFFFF];
            for (int len; (len = is.read(buffer)) != -1; )
                os.write(buffer, 0, len);
            os.flush();
            return os.toByteArray();
        }
    }
}
