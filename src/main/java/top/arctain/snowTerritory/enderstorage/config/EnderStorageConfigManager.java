package top.arctain.snowTerritory.enderstorage.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.arctain.snowTerritory.Main;
import top.arctain.snowTerritory.utils.MessageUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 管理 ender-storage 配置的加载与默认文件生成。
 * 所有配置放在 plugins/SnowTerritory/ender-storage/ 下。
 */
public class EnderStorageConfigManager {

    private final File baseDir;
    private FileConfiguration mainConfig;
    private FileConfiguration guiConfig;
    private Map<String, FileConfiguration> messagePacks = new HashMap<>();
    private ProgressionConfig progressionConfig;

    public EnderStorageConfigManager(Main plugin) {
        this.baseDir = new File(plugin.getDataFolder(), "ender-storage");
    }

    public void loadAll() {
        ensureDefaults();
        loadMainConfig();
        loadGui();
        loadMessages();
        loadProgression();
    }

    private void ensureDefaults() {
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            MessageUtils.logWarning("创建 ender-storage 目录失败: " + baseDir.getAbsolutePath());
        }
        copyIfMissing(new File(baseDir, "config.yml"), DefaultFiles.DEFAULT_CONFIG);
        copyIfMissing(new File(baseDir, "gui.yml"), DefaultFiles.DEFAULT_GUI);
        copyIfMissing(new File(baseDir, "progression/size.yml"), DefaultFiles.DEFAULT_SIZE);
        copyIfMissing(new File(baseDir, "progression/stack.yml"), DefaultFiles.DEFAULT_STACK);
        copyIfMissing(new File(baseDir, "messages/zh_CN.yml"), DefaultFiles.DEFAULT_MESSAGES_ZH);
    }

    private void copyIfMissing(File target, String content) {
        try {
            if (!target.exists()) {
                if (target.getParentFile() != null) {
                    target.getParentFile().mkdirs();
                }
                Files.writeString(target.toPath(), content);
            }
        } catch (IOException e) {
            MessageUtils.logError("写入默认配置失败: " + target.getAbsolutePath() + " - " + e.getMessage());
        }
    }

    private void loadMainConfig() {
        this.mainConfig = YamlConfiguration.loadConfiguration(new File(baseDir, "config.yml"));
    }

    private void loadGui() {
        this.guiConfig = YamlConfiguration.loadConfiguration(new File(baseDir, "gui.yml"));
    }

    private void loadMessages() {
        File msgDir = new File(baseDir, "messages");
        this.messagePacks = new HashMap<>();
        File[] files = msgDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String key = file.getName().replace(".yml", "");
                messagePacks.put(key, YamlConfiguration.loadConfiguration(file));
            }
        }
    }

    private void loadProgression() {
        this.progressionConfig = new ProgressionConfig(
                YamlConfiguration.loadConfiguration(new File(baseDir, "progression/size.yml")),
                YamlConfiguration.loadConfiguration(new File(baseDir, "progression/stack.yml"))
        );
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }

    public Map<String, FileConfiguration> getMessagePacks() {
        return Collections.unmodifiableMap(messagePacks);
    }

    public ProgressionConfig getProgressionConfig() {
        return progressionConfig;
    }

    public File getBaseDir() {
        return baseDir;
    }

    // ===== GUI 辅助方法 =====

    /**
     * 获取 GUI 标题（带默认值）
     */
    public String getGuiTitle() {
        if (guiConfig == null) return "战利品仓库";
        return guiConfig.getString("gui.title", "战利品仓库");
    }

    /**
     * 获取 GUI 大小
     */
    public int getGuiSize() {
        if (guiConfig == null) return 54;
        return guiConfig.getInt("gui.size", 54);
    }

    public int getPreviousPageSlot() {
        if (guiConfig == null) return 48;
        return guiConfig.getInt("gui.slots.previous-page", 48);
    }

    public int getNextPageSlot() {
        if (guiConfig == null) return 50;
        return guiConfig.getInt("gui.slots.next-page", 50);
    }

    /**
     * 从 gui.yml 解析物品槽位列表
     */
    public java.util.List<Integer> getMaterialSlots() {
        int size = getGuiSize();
        if (guiConfig == null) {
            return java.util.Collections.emptyList();
        }
        java.util.List<String> ranges = guiConfig.getStringList("gui.slots.material-slots");
        return top.arctain.snowTerritory.utils.GuiSlotUtils.parseSlotRanges(ranges, size);
    }

    /**
     * 加载 GUI 装饰槽位配置
     */
    public Map<Integer, top.arctain.snowTerritory.utils.GuiSlotUtils.SlotItem> getDecorationSlots() {
        if (guiConfig == null) {
            return java.util.Collections.emptyMap();
        }
        Set<Integer> reserved = new java.util.HashSet<>();
        reserved.add(getPreviousPageSlot());
        reserved.add(getNextPageSlot());
        reserved.addAll(getMaterialSlots());
        return top.arctain.snowTerritory.utils.GuiSlotUtils.loadSlotItems(
                guiConfig,
                "gui.slots.decoration-slots",
                reserved,
                getGuiSize()
        );
    }

    /**
     * 从 gui.yml 中按顺序获取需要展示的物品 key 列表（形如 TYPE:NAME）
     */
    public java.util.List<String> getGuiMaterialKeys() {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (guiConfig == null) return result;
        org.bukkit.configuration.ConfigurationSection root = guiConfig.getConfigurationSection("gui.materials");
        if (root == null) return result;
        for (String type : root.getKeys(false)) {
            org.bukkit.configuration.ConfigurationSection typeSec = root.getConfigurationSection(type);
            if (typeSec == null) continue;
            for (String name : typeSec.getKeys(false)) {
                result.add(type + ":" + name);
            }
        }
        return result;
    }
}

