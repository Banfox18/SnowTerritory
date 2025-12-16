package top.arctain.snowTerritory.quest.config;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import top.arctain.snowTerritory.utils.ColorUtils;

import java.util.Map;
import java.util.Optional;

/**
 * 消息提供者，支持ChatColor解析
 */
public class MessageProvider {

    private final Map<String, FileConfiguration> packs;
    private final String defaultLang;

    public MessageProvider(Map<String, FileConfiguration> packs, String defaultLang) {
        this.packs = packs;
        this.defaultLang = defaultLang;
    }

    /**
     * 获取消息并应用ChatColor解析
     */
    public String get(CommandSender sender, String path, String def) {
        String lang = defaultLang;
        if (sender instanceof Player) {
            // 可扩展读取玩家语言
        }
        String message = Optional.ofNullable(packs.get(lang))
                .map(cfg -> cfg.getString(path, def))
                .orElse(def);
        return ColorUtils.colorize(message);
    }

    /**
     * 获取原始消息（不应用颜色解析）
     */
    public String getRaw(CommandSender sender, String path, String def) {
        String lang = defaultLang;
        if (sender instanceof Player) {
            // 可扩展读取玩家语言
        }
        return Optional.ofNullable(packs.get(lang))
                .map(cfg -> cfg.getString(path, def))
                .orElse(def);
    }
}

