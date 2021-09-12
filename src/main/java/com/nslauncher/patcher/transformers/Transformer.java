package com.nslauncher.patcher.transformers;

import com.nslauncher.patcher.config.Config;

import java.io.IOException;

/**
 * @author Jeb
 */
public abstract class Transformer {
    private final String classLocation;

    public Transformer(String classLocation) {
        this.classLocation = classLocation;
    }

    public abstract byte[] transform(byte[] bytecode, Config config) throws IOException;

    public String getClassLocation() {
        return classLocation;
    }
}
