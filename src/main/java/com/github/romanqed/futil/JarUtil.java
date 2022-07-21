package com.github.romanqed.futil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtil {
    public static URLClassLoader loadJar(File jar, ClassLoader parent) throws IOException, ClassNotFoundException {
        String pathToJar = jar.getAbsolutePath();
        JarFile jarFile = new JarFile(pathToJar);
        Enumeration<JarEntry> jars = jarFile.entries();
        URLClassLoader loader = URLClassLoader.newInstance(
                new URL[]{new URL("jar:file:" + pathToJar + "!/")},
                parent
        );
        while (jars.hasMoreElements()) {
            JarEntry entry = jars.nextElement();
            if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                continue;
            }
            String className = entry.getName()
                    .substring(0, entry.getName().length() - 6)
                    .replace('/', '.');
            Class.forName(className, true, loader);
        }
        jarFile.close();
        return loader;
    }
}
