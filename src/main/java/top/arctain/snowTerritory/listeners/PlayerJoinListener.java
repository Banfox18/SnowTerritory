package top.arctain.snowTerritory.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import top.arctain.snowTerritory.Main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 玩家登录监听器
 * 用于在管理员登录时发送欢迎消息
 */
public class PlayerJoinListener implements Listener {

    private final Main plugin;

    public PlayerJoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 检查是否有管理员权限（OP或特定权限）
        if (!player.isOp() && !player.hasPermission("mmoitemseditor.use")) {
            return;
        }

        // 延迟发送，确保玩家完全登录
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            sendWelcomeMessage(player);
        }, 20L); // 延迟1秒
    }

    /**
     * 发送欢迎消息
     */
    private void sendWelcomeMessage(Player player) {
        // 获取插件版本
        String version = plugin.getPluginMeta().getVersion();
        
        // 获取jar文件修改日期
        String buildDate = getBuildDate();
        
        // 构建消息
        String[] lines = {
            "",
            "&{#4a95ff}═════════════════════════",
            "&{#99c2ff}  &{#ffffff}欢迎您使用 &{#4a95ff}&l雪域插件&r &{#ffffff}!",
            "&{#99c2ff}  &{#ffffff}当前版本: &{#4a95ff}" + version,
            "&{#99c2ff}  &{#ffffff}构建日期: &{#4a95ff}" + buildDate,
            "&{#99c2ff}  &{#ffffff}插件作者: &{#4a95ff}Arctain",
            "&{#99c2ff}  &{#ffffff}作者QQ: &{#4a95ff}1546025015",
            "&{#4a95ff}═════════════════════════",
            ""
        };
        
        // 发送消息（使用MessageUtils的colorize方法）
        for (String line : lines) {
            player.sendMessage(top.arctain.snowTerritory.utils.MessageUtils.colorize(line));
        }
    }

    /**
     * 获取jar文件构建日期
     */
    private String getBuildDate() {
        try {
            // 通过类文件路径获取jar文件
            java.net.URL url = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            if (url != null) {
                File jarFile = new File(url.toURI());
                if (jarFile.exists()) {
                    long lastModified = jarFile.lastModified();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return sdf.format(new Date(lastModified));
                }
            }
        } catch (Exception e) {
            // 忽略错误
        }
        return "未知";
    }
}

