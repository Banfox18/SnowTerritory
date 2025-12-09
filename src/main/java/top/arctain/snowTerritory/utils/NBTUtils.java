package top.arctain.snowTerritory.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/**
 * NBT工具类
 * 用于处理物品的NBT数据
 */
public class NBTUtils {

    private static Plugin plugin;

    public static void initialize(Plugin plugin) {
        NBTUtils.plugin = plugin;
    }

    /**
     * 设置NBT数据
     */
    public static void setNBT(ItemStack item, String key, String value) {
        if (item == null || !item.hasItemMeta() || plugin == null) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        container.set(namespacedKey, PersistentDataType.STRING, value);
        
        item.setItemMeta(meta);
    }

    /**
     * 获取NBT数据
     */
    public static String getNBT(ItemStack item, String key) {
        if (item == null || !item.hasItemMeta() || plugin == null) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        
        return container.get(namespacedKey, PersistentDataType.STRING);
    }

    /**
     * 检查NBT数据是否存在
     */
    public static boolean hasNBT(ItemStack item, String key) {
        if (item == null || !item.hasItemMeta() || plugin == null) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        
        return container.has(namespacedKey, PersistentDataType.STRING);
    }
}
