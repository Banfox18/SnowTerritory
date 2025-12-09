package top.arctain.snowTerritory.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * 物品编辑监听器
 * 用于处理玩家与物品的交互事件
 */
public class ItemEditListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 可以在这里添加右键物品时的特殊处理
        // 例如：右键查看物品信息、快速强化等
    }
}
