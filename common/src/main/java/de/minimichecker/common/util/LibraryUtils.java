package de.minimichecker.common.util;

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
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.44/mysql-connector-java-5.1.44.jar", "mysql-connector-java-5.1.44.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/jcraft/jsch/0.1.55/jsch-0.1.55.jar", "jsch-0.1.55.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/commons-io/commons-io/2.5/commons-io-2.5.jar", "commons-io-2.5.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/commons-lang/commons-lang/2.6/commons-lang-2.6.jar", "commons-lang-2.6.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/github/wolf480pl/jline-log4j2-appender/1.0.0/jline-log4j2-appender-1.0.0.jar", "jline-log4j2-appender-1.0.0.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/biz/paluch/redis/lettuce/4.2.2.Final/lettuce-4.2.2.Final.jar", "lettuce-4.2.2.Final.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/io/netty/netty-all/4.1.15.Final/netty-all-4.1.15.Final.jar", "netty-all-4.1.15.Final.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/io/reactivex/rxjava/1.0.4/rxjava-1.0.4.jar", "rxjava-1.0.4.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.jar", "json-simple-1.1.1.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/io/atomix/atomix/3.1.8/atomix-3.1.8.jar", "atomix-3.1.8.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/io/atomix/atomix-primitive/3.1.8/atomix-primitive-3.1.8.jar", "atomix-primitive-3.1.8.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/io/atomix/atomix-cluster/3.1.8/atomix-cluster-3.1.8.jar", "atomix-cluster-3.1.8.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/io/atomix/atomix-gossip/3.1.8/atomix-gossip-3.1.8.jar", "atomix-gossip-3.1.8.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/io/atomix/atomix-utils/3.1.8/atomix-utils-3.1.8.jar", "atomix-utils-3.1.8.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar", "slf4j-api-1.7.30.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/org/slf4j/slf4j-nop/1.7.30/slf4j-nop-1.7.30.jar", "slf4j-nop-1.7.30.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/io/github/classgraph/classgraph/4.8.87/classgraph-4.8.87.jar", "classgraph-4.8.87.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/typesafe/config/1.2.1/config-1.2.1.jar", "config-1.2.1.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/esotericsoftware/kryo/3.0.0/kryo-3.0.0.jar", "kryo-3.0.0.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/org/objenesis/objenesis/2.1/objenesis-2.1.jar", "objenesis-2.1.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/esotericsoftware/minlog/minlog/1.2/minlog-1.2.jar", "minlog-1.2.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.8.1/commons-lang3-3.8.1.jar", "commons-lang3-3.8.1.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/io/atomix/atomix-primary-backup/3.1.8/atomix-primary-backup-3.1.8.jar", "atomix-primary-backup-3.1.8.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar", "commons-math3-3.6.1.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/org/yaml/snakeyaml/1.17/snakeyaml-1.17.jar", "snakeyaml-1.17.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/google/guava/guava/23.0/guava-23.0.jar", "guava-23.0.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/google/guava/guava/19.0/guava-19.0.jar", "guava-19.0.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.1/gson-2.8.1.jar", "gson-2.8.1.jar");
        //LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/org/yaml/snakeyaml/1.18/snakeyaml-1.18.jar", "snakeyaml-1.18.jar");

        //LibraryUtils.downloadLibrary("https://ci.md-5.net/job/Yamler/lastSuccessfulBuild/artifact/Yamler-Core/target/Yamler-Core-2.4.0-SNAPSHOT.jar", "Yamler-Core-2.4.0-SNAPSHOT.jar");
        //LibraryUtils.downloadLibrary("http://central.maven.org/maven2/org/yaml/snakeyaml/1.18/snakeyaml-1.18.jar", "snakeyaml-1.18.jar");

        /*
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/com/google/inject/guice/4.0/guice-4.0.jar", "guice-4.0.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/javax/inject/javax.inject/1/javax.inject-1.jar", "javax.inject-1.jar");
        LibraryUtils.downloadLibrary("https://repo1.maven.org/maven2/aopalliance/aopalliance/1.0/aopalliance-1.0.jar", "aopalliance-1.0.jar");
        */

        /*
        LibraryUtils.downloadLibrary("https://github.com/EsotericSoftware/kryo/blob/master/lib/minlog-1.3.0.jar", "minlog-1.3.0.jar");
        LibraryUtils.downloadLibrary("https://github.com/EsotericSoftware/kryo/blob/master/lib/objenesis-2.6.jar", "objenesis-2.6.jar");
        LibraryUtils.downloadLibrary("https://github.com/EsotericSoftware/kryo/blob/master/lib/reflectasm-1.11.6.jar", "reflectasm-1.11.6.jar");
         */
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
