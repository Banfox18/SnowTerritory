package top.arctain.snowTerritory.utils;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 通用的 GUI 装饰/自定义槽位工具，支持范围解析与占位检查。
 */
public final class GuiSlotUtils {

    private GuiSlotUtils() {
    }

    /**
     * 从配置加载槽位物品（支持单槽位或范围表达式，如 "10-15"）。
     *
     * @param config         配置对象
     * @param basePath       配置路径前缀（例如 "gui.decoration-slots"）
     * @param reservedSlots  需要跳过的功能槽位
     * @param inventorySize  目标 GUI 大小，用于校验槽位范围
     * @return 槽位 -> 物品配置 映射
     */
    public static Map<Integer, SlotItem> loadSlotItems(FileConfiguration config,
                                                       String basePath,
                                                       Set<Integer> reservedSlots,
                                                       int inventorySize) {
        Map<Integer, SlotItem> result = new HashMap<>();
        if (config == null) {
            return result;
        }
        var section = config.getConfigurationSection(basePath);
        if (section == null) {
            return result;
        }

        for (String key : section.getKeys(false)) {
            String path = basePath + "." + key;
            String materialStr = config.getString(path + ".material", "GRAY_STAINED_GLASS_PANE");
            Material material;
            try {
                material = Material.valueOf(materialStr.toUpperCase());
            } catch (IllegalArgumentException ex) {
                MessageUtils.logWarning("无效的槽位材质: " + materialStr + " (path=" + path + ")");
                continue;
            }
            String name = config.getString(path + ".name", "");
            List<String> lore = config.getStringList(path + ".lore");
            SlotItem slotItem = new SlotItem(material, name, lore);

            if (key.contains("-")) {
                // 范围表达式
                String[] parts = key.split("-", 2);
                if (parts.length != 2) {
                    MessageUtils.logWarning("槽位范围格式无效: " + key + " (path=" + path + ")");
                    continue;
                }
                try {
                    int start = Integer.parseInt(parts[0].trim());
                    int end = Integer.parseInt(parts[1].trim());
                    if (start > end) {
                        int tmp = start;
                        start = end;
                        end = tmp;
                    }
                    for (int slot = start; slot <= end; slot++) {
                        if (!isValidSlot(slot, inventorySize, reservedSlots)) continue;
                        result.put(slot, slotItem);
                    }
                } catch (NumberFormatException e) {
                    MessageUtils.logWarning("槽位范围数字无效: " + key + " (path=" + path + ")");
                }
            } else {
                try {
                    int slot = Integer.parseInt(key.trim());
                    if (!isValidSlot(slot, inventorySize, reservedSlots)) continue;
                    result.put(slot, slotItem);
                } catch (NumberFormatException e) {
                    MessageUtils.logWarning("槽位编号无效: " + key + " (path=" + path + ")");
                }
            }
        }

        return result;
    }

    /**
     * 将 ["1-7","10","15-16"] 解析为槽位列表。
     */
    public static java.util.List<Integer> parseSlotRanges(java.util.List<String> ranges, int inventorySize) {
        java.util.List<Integer> slots = new java.util.ArrayList<>();
        if (ranges == null) {
            return slots;
        }
        for (String part : ranges) {
            if (part == null) continue;
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.contains("-")) {
                String[] seg = trimmed.split("-", 2);
                if (seg.length != 2) continue;
                try {
                    int start = Integer.parseInt(seg[0].trim());
                    int end = Integer.parseInt(seg[1].trim());
                    if (start > end) {
                        int tmp = start;
                        start = end;
                        end = tmp;
                    }
                    for (int i = start; i <= end; i++) {
                        if (i < 0 || i >= inventorySize) continue;
                        if (!slots.contains(i)) {
                            slots.add(i);
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            } else {
                try {
                    int slot = Integer.parseInt(trimmed);
                    if (slot < 0 || slot >= inventorySize) continue;
                    if (!slots.contains(slot)) {
                        slots.add(slot);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return slots;
    }

    /**
     * 将槽位物品应用到 GUI。
     *
     * @param inv          目标背包
     * @param items        槽位物品映射
     * @param skipOccupied 是否跳过已有物品的槽位
     */
    public static void applySlotItems(Inventory inv, Map<Integer, SlotItem> items, boolean skipOccupied) {
        if (items == null || items.isEmpty()) {
            return;
        }
        items.forEach((slot, itemConfig) -> {
            if (slot < 0 || slot >= inv.getSize()) return;
            if (skipOccupied) {
                ItemStack existing = inv.getItem(slot);
                if (existing != null && !existing.getType().isAir()) {
                    return;
                }
            }
            inv.setItem(slot, buildItem(itemConfig));
        });
    }

    private static boolean isValidSlot(int slot, int inventorySize, Set<Integer> reservedSlots) {
        if (slot < 0 || slot >= inventorySize) return false;
        return reservedSlots == null || !reservedSlots.contains(slot);
    }

    /**
     * 将 SlotItem 转为 ItemStack，带颜色处理。
     */
    public static ItemStack buildItem(SlotItem config) {
        ItemStack item = new ItemStack(config.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (config.name() != null && !config.name().isEmpty()) {
                meta.setDisplayName(MessageUtils.colorize(config.name()));
            }
            if (config.lore() != null && !config.lore().isEmpty()) {
                List<String> lore = new java.util.ArrayList<>();
                for (String line : config.lore()) {
                    lore.add(MessageUtils.colorize(line));
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * 槽位物品定义。
     */
    public record SlotItem(Material material, String name, List<String> lore) {
        public SlotItem(Material material, String name, List<String> lore) {
            this.material = material;
            this.name = name;
            this.lore = lore == null ? List.of() : lore;
        }
    }
}


