package de.blu.common.util;

import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

public final class LibraryUtils {

    @Getter
    private static File libsFolder;

    /**
     * Create the Library Directory
     * and store them
     *
     * @param rootFolder the rootDirectory where the "libs" directory
     *                   should be created
     */
    public static void createLibraryFolder(File rootFolder) {
        File libDirectory = new File(rootFolder, "libs");
        LibraryUtils.libsFolder = libDirectory;
        if (!libDirectory.isDirectory()) {
            if (!libDirectory.mkdirs()) {
                throw new NullPointerException("could not create Library Folder");
            }
        }
    }

    /**
     * Load Libraries in the LibraryFolder and download the default Libraries
     * which are needed by the Cloud Project
     */
    public static void loadLibraries() {
        if (!LibraryUtils.libsFolder.exists()) {
            return;
        }

        // Load default libraries
        LibraryUtils.downloadDefaultLibraries();
        for (File file : Objects.requireNonNull(LibraryUtils.libsFolder.listFiles())) {
            LibraryUtils.loadLibrary(file);
            //System.out.println("Loaded Library: " + file.getName());
        }
    }

    private static void downloadDefaultLibraries() {
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar", "log4j-1.2.17.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/jline/jline/2.12/jline-2.12.jar", "jline-2.12.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/jcraft/jsch/0.1.55/jsch-0.1.55.jar", "jsch-0.1.55.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/commons-io/commons-io/2.5/commons-io-2.5.jar", "commons-io-2.5.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar", "commons-lang-2.6.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/github/wolf480pl/jline-log4j2-appender/1.0.0/jline-log4j2-appender-1.0.0.jar", "jline-log4j2-appender-1.0.0.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/biz/paluch/redis/lettuce/4.2.2.Final/lettuce-4.2.2.Final.jar", "lettuce-4.2.2.Final.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/io/reactivex/rxjava/1.0.4/rxjava-1.0.4.jar", "rxjava-1.0.4.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.jar", "json-simple-1.1.1.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar", "slf4j-api-1.7.30.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/org/slf4j/slf4j-nop/1.7.30/slf4j-nop-1.7.30.jar", "slf4j-nop-1.7.30.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/io/github/classgraph/classgraph/4.8.87/classgraph-4.8.87.jar", "classgraph-4.8.87.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/typesafe/config/1.2.1/config-1.2.1.jar", "config-1.2.1.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.8.1/commons-lang3-3.8.1.jar", "commons-lang3-3.8.1.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar", "commons-math3-3.6.1.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/org/yaml/snakeyaml/1.17/snakeyaml-1.17.jar", "snakeyaml-1.17.jar");
        LibraryUtils.downloadLibrary("https://dl.bintray.com/typesafe/maven-releases/org/hyperic/sigar/1.6.4/sigar-1.6.4.jar", "sigar-1.6.4.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/sparkjava/spark-core/2.3/spark-core-2.3.jar", "spark-core-2.3.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/javax/servlet/servlet-api/2.3/servlet-api-2.3.jar", "servlet-api-2.3.jar");
    }

    /**
     * Load Library from File to Classpath
     *
     * @param file the jar File to load
     */
    public static void loadLibrary(File file) {
        try {
            URL url = file.toURI().toURL();

            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Download a Library for loading it later in the classpath
     *
     * @param urlString the url of the library
     * @param fileName  the target fileName of the Library
     */
    public static void downloadLibrary(String urlString, String fileName) {
        if (LibraryUtils.libsFolder == null) {
            return;
        }

        File targetFile = new File(LibraryUtils.libsFolder, fileName);
        try {
            if (!targetFile.exists()) {
                FileUtils.copyURLToFile(new URL(urlString), targetFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
