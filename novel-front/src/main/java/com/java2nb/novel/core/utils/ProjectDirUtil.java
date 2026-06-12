package com.java2nb.novel.core.utils;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ProjectDirUtil {

    private ProjectDirUtil() {
    }

    public static String resolveProjectDir() {
        Path current = Path.of(System.getProperty("user.dir"));
        if (Files.isDirectory(current.resolve("templates"))) {
            return current.toString();
        }
        Path parent = current.getParent();
        if (parent != null && Files.isDirectory(parent.resolve("templates"))) {
            return parent.toString();
        }
        return current.toString();
    }
}
