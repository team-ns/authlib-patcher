package com.nslauncher.patcher;


import com.nslauncher.patcher.config.Config;
import com.nslauncher.patcher.helper.JarHelper;
import com.nslauncher.patcher.transformers.Transformer;
import com.nslauncher.patcher.transformers.bungee.BungeeTransformer;
import com.nslauncher.patcher.transformers.yaggdrasil.MinecraftSessionTransformer;
import com.nslauncher.patcher.transformers.yaggdrasil.PropertyTransformer;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

/**
 * @author Jeb
 */
public class AuthlibPatcher {

    static Set<Transformer> transformers = Set.of(
            new PropertyTransformer(),
            new MinecraftSessionTransformer(),
            new BungeeTransformer()
    );

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        new CommandLine(config).parseArgs(args);
        final File jar = config.getJar();
        final String jarName = jar.getName();
        int pos = jarName.lastIndexOf(".");
        final String savedFileName;
        if (pos > 0 && pos < (jarName.length() - 1)) {
            savedFileName = String.format("%s-patched.jar", jarName.substring(0, pos));
        } else {
            savedFileName = "authlib-patched.jar";
        }
        final Path originalPath = Paths.get(jar.getAbsolutePath());
        final Path copiedPath = originalPath.getParent().resolve(savedFileName);
        Files.deleteIfExists(copiedPath);
        Files.copy(originalPath, copiedPath, StandardCopyOption.REPLACE_EXISTING);
        final File savedJar = new File(jar.getParent(), savedFileName);
        for (Transformer transformer :
                transformers) {
            byte[] bytes = JarHelper.getBytes(transformer.getClassLocation(), config.getJar());
            if (bytes != null) {
                JarHelper.saveBytes(savedJar, transformer.getClassLocation(), transformer.transform(bytes, config));
            }
        }
    }


}
