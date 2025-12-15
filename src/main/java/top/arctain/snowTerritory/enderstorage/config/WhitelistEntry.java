package top.arctain.snowTerritory.enderstorage.config;

import org.bukkit.Material;

import java.util.List;

/**
 * 白名单物品定义。
 */
public class WhitelistEntry {
    private final String key;
    private final String display;
    private final String mmoType;
    private final String mmoItemId;
    private final Material material;
    private final int defaultMax;
    private final List<String> lore;

    public WhitelistEntry(String key, String display, String mmoType, String mmoItemId, Material material, int defaultMax) {
        this(key, display, mmoType, mmoItemId, material, defaultMax, null);
    }

    public WhitelistEntry(String key, String display, String mmoType, String mmoItemId, Material material, int defaultMax, List<String> lore) {
        this.key = key;
        this.display = display;
        this.mmoType = mmoType;
        this.mmoItemId = mmoItemId;
        this.material = material;
        this.defaultMax = defaultMax;
        this.lore = lore;
    }

    public String getKey() {
        return key;
    }

    public String getDisplay() {
        return display;
    }

    public String getMmoType() {
        return mmoType;
    }

    public String getMmoItemId() {
        return mmoItemId;
    }

    public Material getMaterial() {
        return material;
    }

    public int getDefaultMax() {
        return defaultMax;
    }

    public List<String> getLore() {
        return lore;
    }
}

