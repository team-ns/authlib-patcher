package com.nslauncher.patcher.config;

import picocli.CommandLine;

import java.io.File;

/**
 * @author Jeb
 */
public class Config {
    @CommandLine.Option(names = {"-u", "--url"}, description = "Auth url")
    private String baseUrl;

    @CommandLine.Option(names = {"-j", "--jar"}, description = "Jar file for patching")
    private File jar;

    public String getBaseUrl() {
        return baseUrl;
    }

    public File getJar() {
        return jar;
    }
}
