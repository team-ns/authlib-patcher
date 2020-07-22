package com.nslauncher.patcher;


import com.nslauncher.patcher.config.Config;
import com.nslauncher.patcher.helper.JarHelper;
import com.nslauncher.patcher.transformers.Transformer;
import com.nslauncher.patcher.transformers.bungee.BungeeTransformer;
import com.nslauncher.patcher.transformers.yaggdrasil.MinecraftSessionTransformer;
import com.nslauncher.patcher.transformers.yaggdrasil.PropertyTransformer;
import picocli.CommandLine;

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
        for (Transformer transformer :
                transformers) {
            byte[] bytes = JarHelper.getBytes(transformer.getClassLocation(), config.getJar());
            if (bytes != null) {
                JarHelper.saveBytes(config.getJar(), transformer.getClassLocation(), transformer.transform(bytes, config));
            }
        }
    }


}
