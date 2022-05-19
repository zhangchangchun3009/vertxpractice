package pers.zcc.vertxprc.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentProps {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentProps.class);

    private static Map<String, String> propCache = new HashMap<>(32);

    private static String appPropPath = "application.properties";

    private static String baseDir;

    static {
        String codePath = EnvironmentProps.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (codePath.contains(".jar!")) {
            baseDir = codePath.substring(0, codePath.indexOf("!"));
            baseDir = baseDir.substring(0, baseDir.lastIndexOf("/"));
            try {
                baseDir = new File(new URI(baseDir)).getPath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            baseDir = codePath;
        }
    }

    /**
     * when run in jar redirect to the same directory of this jar,
     * when run in unpacked directory,its the base code dir
     * @return baseDir
     */
    public static String getBaseDir() {
        return baseDir;
    }

    public static String getAppPropPath() {
        return appPropPath;
    }

    public static Properties getProperties(String path) {
        Properties properties = new Properties();
        try {
            properties.load(EnvironmentProps.class.getClassLoader().getResourceAsStream(path));
        } catch (Exception e) {
            LOGGER.error("loadAllProperties e,", e);
            return properties;
        }
        try {
            for (Entry<Object, Object> entry : properties.entrySet()) {
                propCache.put(path + "/" + (String) entry.getKey(), (String) entry.getValue());
            }
        } catch (Exception e) {
            LOGGER.error("propCache.put e,", e);
        }
        return properties;
    }

    public static String getProperty(String path, String key) {
        String value = propCache.get(path + "/" + key);
        if (value != null) {
            return value;
        }
        Properties props = getProperties(path);
        return props.getProperty(key);
    }

    public static String getApplicationProp(String key) {
        return getProperty(appPropPath, key);
    }

    public static int getAppPropAsInteger(String key, int defaultValue) throws NumberFormatException {
        return getInteger(appPropPath, key, defaultValue);
    }

    /**
     * get value as long
     * @param key
     * @param defaultValue
     * @return
     * @throws NumberFormatException
     */
    public static long getAppPropAsLong(String key, long defaultValue) throws NumberFormatException {
        return getLong(appPropPath, key, defaultValue);
    }

    /**
     * get value as string
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getAppPropAsString(String key, String defaultValue) {
        return getString(appPropPath, key, defaultValue);
    }

    /**
     * get value as int
     * @param path
     * @param key
     * @param defaultValue
     * @return
     * @throws NumberFormatException
     */
    public static int getInteger(String path, String key, int defaultValue) throws NumberFormatException {
        String value = getProperty(path, key);
        return (value != null && !"".equals(value)) ? Integer.parseInt(value) : defaultValue;
    }

    /**
     * get value as long
     * @param path
     * @param key
     * @param defaultValue
     * @return
     * @throws NumberFormatException
     */
    public static long getLong(String path, String key, long defaultValue) throws NumberFormatException {
        String value = getProperty(path, key);
        return (value != null && !"".equals(value)) ? Long.parseLong(value) : defaultValue;
    }

    /**
     * get value as string
     * @param path
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getString(String path, String key, String defaultValue) {
        String value = getProperty(path, key);
        return (value != null && !"".equals(value)) ? value : defaultValue;
    }

    /**
     * get as Properties
     * @param in
     * @return
     * @throws IOException
     */
    public static Properties getProperties(InputStream in) {
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            LOGGER.error("loadAllProperties e,", e);
        }
        return properties;
    }

    /**
     * getInteger
     * @param prop
     * @param key
     * @param defaultValue
     * @return
     * @throws NumberFormatException
     */
    public static int getInteger(Properties prop, String key, int defaultValue) throws NumberFormatException {
        String value = prop.getProperty(key);
        return (value != null && !"".equals(value)) ? Integer.parseInt(value) : defaultValue;
    }

    /**
     * getLong
     * @param prop
     * @param key
     * @param defaultValue
     * @return
     * @throws NumberFormatException
     */
    public static long getLong(Properties prop, String key, long defaultValue) throws NumberFormatException {
        String value = prop.getProperty(key);
        return (value != null && !"".equals(value)) ? Long.parseLong(value) : defaultValue;
    }

    /**
     * getString
     * @param prop
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getString(Properties prop, String key, String defaultValue) {
        String value = prop.getProperty(key);
        return (value != null && !"".equals(value)) ? value : defaultValue;
    }

}
