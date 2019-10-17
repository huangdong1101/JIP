package com.mamba.java.iface.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JIP {

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Input arguments is empty!");
        }
        if (args.length > 1) {
            throw new IllegalArgumentException("Input arguments is error: " + Arrays.toString(args));
        }
        String desc = parse(new File(args[0]));
        System.out.println(desc);
    }

    public static String parse(File file) throws Exception {
        if (file == null) {
            throw new NullPointerException("input file is null!");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("Invalid file path: " + file.getPath());
        }
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, ClassLoader.getSystemClassLoader())) {
            if (file.isDirectory()) {
                return parseClassFiles(classLoader, file);
            } else if (file.getName().endsWith(".jar")) {
                try (JarFile jarFile = new JarFile(file)) {
                    return parseJarFile(classLoader, jarFile);
                }
            } else {
                throw new RuntimeException("Invalid file: " + file.getPath());
            }
        }
    }

    /**
     * 解析Jar文件
     *
     * @param classLoader
     * @param jarFile
     * @return
     * @throws Exception
     */
    public static String parseJarFile(URLClassLoader classLoader, JarFile jarFile) throws Exception {
        if (classLoader == null) {
            throw new NullPointerException("ClassLoader is null!");
        }
        if (jarFile == null) {
            throw new NullPointerException("JarFile is null!");
        }
        Enumeration<JarEntry> enumeration = jarFile.entries();
        return parseInterfaces(JIP::parseJarFile, classLoader, enumeration);
    }

    private static void parseJarFile(URLClassLoader classLoader, Enumeration<JarEntry> enumeration, StringBuilder sb) throws Exception {
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = enumeration.nextElement();
            if (jarEntry.isDirectory()) {
                continue;
            }
            String name = jarEntry.getName();
            if (!name.endsWith(".class")) {
                continue;
            }
            String className = name.substring(0, name.length() - 6).replace(File.separatorChar, '.');
            Class<?> clazz = classLoader.loadClass(className);
            appendInterface(clazz, sb);
            sb.append(',');
        }
    }

    /**
     * 解析class文件（xx/*.class）
     *
     * @param classLoader
     * @param classpath
     * @return
     * @throws Exception
     */
    public static String parseClassFiles(URLClassLoader classLoader, File classpath) throws Exception {
        if (classLoader == null) {
            throw new NullPointerException("ClassLoader is null!");
        }
        if (classpath == null) {
            throw new NullPointerException("classpath is null!");
        }
        if (!classpath.isDirectory()) {
            throw new FileNotFoundException("Invalid dict path: " + classpath.getPath());
        }
        return parseInterfaces(JIP::parseClasspath, classLoader, classpath);
    }

    private static void parseClasspath(URLClassLoader classLoader, File classpath, StringBuilder sb) throws Exception {
        parseClassFiles(classLoader, classpath.getAbsolutePath(), classpath.listFiles(), sb);
    }

    private static void parseClassFiles(URLClassLoader classLoader, String root, File[] files, StringBuilder sb) throws Exception {
        for (File file : files) {
            if (file.isDirectory()) {
                parseClassFiles(classLoader, root, file.listFiles(), sb);
            } else {
                String path = file.getAbsolutePath();
                if (!path.endsWith(".class")) {
                    continue;
                }
                String className = path.substring(root.length() + 1, path.length() - 6).replace(File.separatorChar, '.');
                Class<?> clazz = classLoader.loadClass(className);
                appendInterface(clazz, sb);
                sb.append(',');
            }
        }
    }

    private static <T> String parseInterfaces(IParser<T> parser, URLClassLoader classLoader, T data) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"interfaces\":[");
        parser.append(classLoader, data, sb);
        if (sb.length() == 1) {
            sb.append(']');
        } else {
            sb.setCharAt(sb.length() - 1, ']');
        }
        sb.append('}');
        return sb.toString();
    }

    private static void appendInterface(Class<?> clazz, StringBuilder sb) {
        if (!clazz.isInterface()) {
            return;
        }
        sb.append("{\"name\":\"").append(clazz.getName()).append('"');
        Method[] methods = clazz.getDeclaredMethods();
        sb.append(",\"methods\":[");
        if (methods == null || methods.length == 0) {
            sb.append(']');
            return;
        }
        for (Method method : methods) {
            appendMethod(method, sb);
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ']');
        sb.append('}');
    }

    private static void appendMethod(Method method, StringBuilder sb) {
        sb.append("{\"name\":\"").append(method.getName()).append('"');

        sb.append(",\"parameterTypes\":");
        appendTypes(method.getParameterTypes(), sb);

        Class<?> returnType = method.getReturnType();
        sb.append(",\"returnType\":");
        if (returnType == null) {
            sb.append("\"void\"");
        } else {
            sb.append('"').append(returnType.getName()).append('"');
        }

        Class<?>[] exceptionTypes = method.getExceptionTypes();
        if (exceptionTypes != null && exceptionTypes.length > 0) {
            sb.append(",\"exceptionTypes\":");
            appendTypes(exceptionTypes, sb);
        }
        sb.append('}');
    }

    private static void appendTypes(Class<?>[] types, StringBuilder sb) {
        if (types == null || types.length == 0) {
            sb.append("[]");
            return;
        }
        sb.append('[');
        for (Class<?> type : types) {
            sb.append('"').append(type.getName()).append('"').append(',');
        }
        sb.setCharAt(sb.length() - 1, ']');
    }

    private interface IParser<T> {
        void append(URLClassLoader classLoader, T data, StringBuilder sb) throws Exception;
    }
}
