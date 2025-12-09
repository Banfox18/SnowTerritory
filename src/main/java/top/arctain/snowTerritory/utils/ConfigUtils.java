package top.arctain.snowTerritory.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * 配置工具类
 * 提供配置文件的读取和保存功能
 */
public class ConfigUtils {

    /**
     * 加载配置文件
     */
    public static FileConfiguration loadConfig(File file) {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * 保存配置文件
     */
    public static void saveConfig(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取配置值，如果不存在则返回默认值
     */
    public static String getString(FileConfiguration config, String path, String defaultValue) {
        if (config.contains(path)) {
            return config.getString(path);
        }
        config.set(path, defaultValue);
        return defaultValue;
    }

    /**
     * 获取配置值，如果不存在则返回默认值
     */
    public static int getInt(FileConfiguration config, String path, int defaultValue) {
        if (config.contains(path)) {
            return config.getInt(path);
        }
        config.set(path, defaultValue);
        return defaultValue;
    }

    /**
     * 获取配置值，如果不存在则返回默认值
     */
    public static double getDouble(FileConfiguration config, String path, double defaultValue) {
        if (config.contains(path)) {
            return config.getDouble(path);
        }
        config.set(path, defaultValue);
        return defaultValue;
    }

    /**
     * 获取配置值，如果不存在则返回默认值
     */
    public static boolean getBoolean(FileConfiguration config, String path, boolean defaultValue) {
        if (config.contains(path)) {
            return config.getBoolean(path);
        }
        config.set(path, defaultValue);
        return defaultValue;
    }
}
