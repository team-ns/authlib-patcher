package com.nslauncher.patcher.transformers;

import com.nslauncher.patcher.config.Config;

/**
 * @author Jeb
 */
public abstract class Transformer {
    private final String classLocation;

    public Transformer(String classLocation) {
        this.classLocation = classLocation;
    }

    public abstract byte[] transform(byte[] bytecode, Config config);

    public String getClassLocation() {
        return classLocation;
    }
}
