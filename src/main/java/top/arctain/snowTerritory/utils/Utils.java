package top.arctain.snowTerritory.utils;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;

public class Utils {

    private static final Random RANDOM = new Random();

    /**
     * 尝试强化，根据概率返回结果（成功/失败/维持）
     */
    public static ReinforceResult attemptReinforce(double successRate, double failDegradeChance, double maintainChance) {
        double roll = RANDOM.nextDouble();
        if (roll <= successRate) return ReinforceResult.SUCCESS;
        if (roll <= successRate + maintainChance) return ReinforceResult.MAINTAIN;
        if (roll <= successRate + maintainChance + failDegradeChance) return ReinforceResult.FAIL_DEGRADE;
        return ReinforceResult.MAINTAIN;  // 默认维持
    }

    /**
     * 检查物品是否为 MMOItems 物品
     */
    public static boolean isMMOItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        try {
            // 使用 MMOItems 的静态方法检查
            net.Indyuce.mmoitems.api.Type type = MMOItems.getType(item);
            String id = MMOItems.getID(item);
            return type != null && id != null && !id.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 ItemStack 获取 MMOItem
     */
    public static MMOItem getMMOItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        try {
            net.Indyuce.mmoitems.api.Type type = MMOItems.getType(item);
            String id = MMOItems.getID(item);
            if (type == null || id == null || id.isEmpty()) {
                return null;
            }
            return MMOItems.plugin.getMMOItem(type, id);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从玩家手中获取 MMOItem
     */
    public static MMOItem getHeldMMOItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return getMMOItem(item);
    }

    /**
     * 检查物品是否可强化（基于 MMOItems 物品 ID 匹配）
     */
    public static boolean isReinforceable(ItemStack item, List<String> reinforceableItems) {
        if (item == null) return false;
        if (!isMMOItem(item)) return false;  // 必须是 MMOItems 物品
        
        // 获取 MMOItems 物品 ID
        String itemId = MMOItems.getID(item);
        if (itemId == null || itemId.isEmpty()) {
            return false;
        }

        // 如果列表为空，允许所有 MMOItems 物品
        if (reinforceableItems == null || reinforceableItems.isEmpty()) {
            return true;
        }

        // 检查物品 ID 是否在可强化列表中（精确匹配）
        for (String matcher : reinforceableItems) {
            if (matcher == null || matcher.isEmpty()) continue;
            if (itemId.equals(matcher)) return true;
        }
        return false;
    }

    /**
     * 获取当前强化等级（从名字后缀解析，如 +1）
     */
    public static int getCurrentLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return 0;
        
        String name = meta.getDisplayName();
        // 匹配 " +数字" 格式
        if (name.matches(".* \\+\\d+")) {
            try {
                String levelStr = name.substring(name.lastIndexOf("+") + 1).trim();
                return Integer.parseInt(levelStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * 更新物品名字添加 +N
     */
    public static void updateItemName(ItemStack item, int newLevel) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        String currentName = meta.hasDisplayName() ? meta.getDisplayName() : "";
        // 移除旧的等级后缀
        String baseName = currentName.replaceAll(" \\+\\d+", "").trim();
        if (baseName.isEmpty()) {
            baseName = "未命名物品";
        }
        meta.setDisplayName(baseName + (newLevel > 0 ? " +" + newLevel : ""));
        item.setItemMeta(meta);
    }

    /**
     * 修改 MMOItems 属性（按百分比）
     * 注意：由于 MMOItems API 版本差异，属性修改功能需要根据实际 API 调整
     * 当前版本仅作为占位符，实际属性修改需要根据 MMOItems 版本实现
     */
    public static void modifyMMOAttribute(LiveMMOItem mmoItem, double multiplier) {
        if (mmoItem == null) return;
        
        // TODO: 根据实际的 MMOItems API 版本实现属性修改
        // 不同版本的 MMOItems API 可能有不同的方法
        // 当前版本先保留方法签名，实际功能需要根据 API 文档实现
        
        // 示例：如果需要修改属性，可能需要使用类似以下方式：
        // 1. 获取 StatMap: mmoItem.getStats()
        // 2. 获取特定属性: stats.getStat(StatType.ATTACK_DAMAGE)
        // 3. 设置属性: stats.setStat(StatType.ATTACK_DAMAGE, newValue)
        // 4. 更新物品: mmoItem.updateItem()
        
        // 由于 API 版本差异，这里暂时不实现具体逻辑
        // 在实际使用时，需要根据 MMOItems 版本调整
    }

    /**
     * 强化结果枚举
     */
    public enum ReinforceResult {
        SUCCESS,      // 成功
        FAIL_DEGRADE, // 失败并降级
        MAINTAIN      // 维持不变
    }
}