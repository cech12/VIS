package de.cech12.vis.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtils {

    private final static Properties properties = new Properties();
    private static File configFile;

    private ConfigUtils() {}

    public static void initConfig(File configDir) throws Exception {
        String fileName = "vis.config";
        configFile = new File(configDir.toURI().resolve(fileName));
        if (!configFile.exists() && !configFile.createNewFile()) {
            throw new Exception(fileName + " file cannot be created.");
        }
        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
        }
    }

    public static String getProperty(String key){
        return properties.getProperty(key);
    }

    public static String getPropertyOrDefault(String key, String defaultValue) throws IOException {
        if (!properties.containsKey(key)) {
            setProperty(key, defaultValue);
        }
        return getProperty(key);
    }

    public static void setProperty(String key, String value) throws IOException {
        properties.setProperty(key, value);
        properties.store(new FileOutputStream(configFile), null);
    }

    public static double getDoubleProperty(String key){
        return Double.parseDouble(properties.getProperty(key));
    }

    public static double getDoublePropertyOrDefault(String key, double defaultValue) throws IOException {
        if (!properties.containsKey(key)) {
            setDoubleProperty(key, defaultValue);
        }
        return getDoubleProperty(key);
    }

    public static void setDoubleProperty(String key, double value) throws IOException {
        setProperty(key, String.valueOf(value));
        properties.store(new FileOutputStream(configFile), null);
    }

}
