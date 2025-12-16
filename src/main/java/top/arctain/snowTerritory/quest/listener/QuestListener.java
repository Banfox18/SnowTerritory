package top.arctain.snowTerritory.quest.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import top.arctain.snowTerritory.Main;
import top.arctain.snowTerritory.quest.config.MessageProvider;
import top.arctain.snowTerritory.quest.config.QuestConfigManager;
import top.arctain.snowTerritory.quest.data.Quest;
import top.arctain.snowTerritory.quest.data.QuestReleaseMethod;
import top.arctain.snowTerritory.quest.service.QuestService;
import top.arctain.snowTerritory.quest.utils.QuestUtils;
import top.arctain.snowTerritory.utils.ColorUtils;

import java.util.List;

/**
 * 任务监听器
 * 处理材料提交等事件
 */
public class QuestListener implements Listener {

    private final QuestService questService;
    private final MessageProvider messages;

    public QuestListener(Main plugin, QuestService questService, QuestConfigManager configManager, MessageProvider messages) {
        this.questService = questService;
        this.messages = messages;
    }

    /**
     * 处理玩家交互事件（用于材料提交）
     * 玩家右键点击物品时，如果该物品匹配任务需求，则提交材料
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType().isAir()) {
            return;
        }
        
        // 检查是否为MMOItems物品
        if (!QuestUtils.isMMOItem(item)) {
            return;
        }
        
        String itemKey = QuestUtils.getMMOItemKey(item);
        if (itemKey == null) {
            return;
        }
        
        // 检查玩家是否有匹配的任务
        List<Quest> activeQuests = questService.getActiveQuests(player.getUniqueId());
        Quest matchingQuest = null;
        
        for (Quest quest : activeQuests) {
            if (quest.getType().name().equals("MATERIAL") && quest.getMaterialKey().equals(itemKey)) {
                matchingQuest = quest;
                break;
            }
        }
        
        // 如果没有普通任务，检查悬赏任务
        if (matchingQuest == null) {
            List<Quest> bountyQuests = questService.getActiveBountyQuests();
            for (Quest quest : bountyQuests) {
                if (quest.getType().name().equals("MATERIAL") && quest.getMaterialKey().equals(itemKey)) {
                    matchingQuest = quest;
                    break;
                }
            }
        }
        
        if (matchingQuest == null) {
            return; // 没有匹配的任务
        }
        
        // 检查任务是否已完成或过期
        if (matchingQuest.isCompleted()) {
            player.sendMessage(ColorUtils.colorize(messages.get(player, "quest-already-completed", "&c✗ &f任务已完成")));
            return;
        }
        
        if (matchingQuest.isExpired()) {
            player.sendMessage(ColorUtils.colorize(messages.get(player, "quest-expired", "&c✗ &f任务已过期")));
            return;
        }
        
        // 计算可提交的数量
        int itemAmount = item.getAmount();
        int needed = matchingQuest.getRequiredAmount() - matchingQuest.getCurrentAmount();
        int toSubmit = Math.min(itemAmount, needed);
        
        if (toSubmit <= 0) {
            return;
        }
        
        // 更新任务进度
        boolean updated = questService.updateQuestProgress(player.getUniqueId(), itemKey, toSubmit);
        
        if (updated) {
            // 移除物品
            if (item.getAmount() == toSubmit) {
                player.getInventory().setItemInMainHand(null);
            } else {
                item.setAmount(item.getAmount() - toSubmit);
            }
            
            // 获取更新后的任务
            Quest updatedQuest = null;
            if (matchingQuest.getReleaseMethod() == QuestReleaseMethod.NORMAL) {
                updatedQuest = questService.getActiveQuest(player.getUniqueId(), matchingQuest.getType());
            } else {
                // 对于悬赏任务，从悬赏任务列表中查找
                List<Quest> bounties = questService.getActiveBountyQuests();
                for (Quest q : bounties) {
                    if (q.getQuestId().equals(matchingQuest.getQuestId())) {
                        updatedQuest = q;
                        break;
                    }
                }
            }
            
            // 发送进度消息
            if (updatedQuest != null) {
                String materialName = itemKey.split(":")[1];
                String progressMsg = messages.get(player, "material-submitted",
                        "&a✓ &f已提交 &e%amount%x %item% &7(进度: %current%/%required%)");
                progressMsg = progressMsg.replace("%amount%", String.valueOf(toSubmit))
                        .replace("%item%", materialName)
                        .replace("%current%", String.valueOf(updatedQuest.getCurrentAmount()))
                        .replace("%required%", String.valueOf(updatedQuest.getRequiredAmount()));
                player.sendMessage(ColorUtils.colorize(progressMsg));
                
                // 如果完成，提示完成任务
                if (updatedQuest.isCompleted()) {
                    if (updatedQuest.getReleaseMethod() == QuestReleaseMethod.BOUNTY) {
                        player.sendMessage(ColorUtils.colorize(messages.get(player, "quest-completed",
                                "&a✓ &f悬赏任务完成！使用 &e/sn q complete <id> &f领取奖励")));
                    } else {
                        // 普通任务自动完成
                        player.sendMessage(ColorUtils.colorize(messages.get(player, "quest-completed",
                                "&a✓ &f任务完成！奖励已发放")));
                    }
                }
            }
        }
    }
}

