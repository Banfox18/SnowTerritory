package top.arctain.snowTerritory.quest.service;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.arctain.snowTerritory.Main;
import top.arctain.snowTerritory.quest.config.QuestConfigManager;
import top.arctain.snowTerritory.quest.data.Quest;
import top.arctain.snowTerritory.quest.data.QuestReleaseMethod;
import top.arctain.snowTerritory.quest.data.QuestStatus;
import top.arctain.snowTerritory.quest.data.QuestType;
import top.arctain.snowTerritory.quest.utils.QuestUtils;
import top.arctain.snowTerritory.utils.ColorUtils;
import top.arctain.snowTerritory.utils.MessageUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 任务服务实现
 */
public class QuestServiceImpl implements QuestService {

    private final Main plugin;
    private final QuestConfigManager configManager;
    
    // 玩家任务映射: playerId -> List<Quest>
    private final Map<UUID, List<Quest>> playerQuests = new ConcurrentHashMap<>();
    
    // 悬赏任务列表
    private final List<Quest> bountyQuests = Collections.synchronizedList(new ArrayList<>());
    
    // 悬赏任务调度器任务ID
    private int bountyTaskId = -1;
    
    public QuestServiceImpl(Main plugin, QuestConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public void initialize() {
        startBountyScheduler();
    }

    @Override
    public void shutdown() {
        stopBountyScheduler();
        playerQuests.clear();
        bountyQuests.clear();
    }

    @Override
    public void reload() {
        stopBountyScheduler();
        configManager.loadAll();
        startBountyScheduler();
    }

    @Override
    public Quest acceptNormalQuest(Player player, QuestType type) {
        UUID playerId = player.getUniqueId();
        
        // 检查是否已有同类型的活跃任务
        Quest existing = getActiveQuest(playerId, type);
        if (existing != null) {
            return null; // 已有活跃任务
        }
        
        // 生成新任务
        Quest quest = generateQuest(playerId, type, QuestReleaseMethod.NORMAL);
        if (quest == null) {
            return null;
        }
        
        // 添加到玩家任务列表
        playerQuests.computeIfAbsent(playerId, k -> new ArrayList<>()).add(quest);
        
        return quest;
    }

    @Override
    public List<Quest> getActiveQuests(UUID playerId) {
        List<Quest> quests = playerQuests.getOrDefault(playerId, new ArrayList<>());
        // 过滤出活跃且未过期的任务
        return quests.stream()
                .filter(q -> q.getStatus() == QuestStatus.ACTIVE && !q.isExpired())
                .collect(Collectors.toList());
    }

    @Override
    public Quest getActiveQuest(UUID playerId, QuestType type) {
        List<Quest> quests = getActiveQuests(playerId);
        return quests.stream()
                .filter(q -> q.getType() == type)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean updateQuestProgress(UUID playerId, String materialKey, int amount) {
        List<Quest> quests = playerQuests.get(playerId);
        if (quests == null) {
            return false;
        }
        
        for (int i = 0; i < quests.size(); i++) {
            Quest quest = quests.get(i);
            if (quest.getStatus() == QuestStatus.ACTIVE 
                    && quest.getType() == QuestType.MATERIAL
                    && quest.getMaterialKey().equals(materialKey)
                    && !quest.isExpired()) {
                
                int newAmount = Math.min(quest.getCurrentAmount() + amount, quest.getRequiredAmount());
                Quest updated = quest.withProgress(newAmount);
                quests.set(i, updated);
                
                // 如果完成，自动完成任务
                if (updated.isCompleted()) {
                    completeQuest(playerId, updated.getQuestId());
                }
                
                return true;
            }
        }
        
        // 检查悬赏任务
        synchronized (bountyQuests) {
            for (int i = 0; i < bountyQuests.size(); i++) {
                Quest quest = bountyQuests.get(i);
                if (quest.getStatus() == QuestStatus.ACTIVE
                        && quest.getType() == QuestType.MATERIAL
                        && quest.getMaterialKey().equals(materialKey)
                        && !quest.isExpired()) {
                    
                    int newAmount = Math.min(quest.getCurrentAmount() + amount, quest.getRequiredAmount());
                    Quest updated = quest.withProgress(newAmount);
                    bountyQuests.set(i, updated);
                    
                    // 如果完成，允许玩家完成悬赏
                    if (updated.isCompleted()) {
                        // 悬赏任务需要玩家主动完成
                    }
                    
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    public boolean completeQuest(UUID playerId, UUID questId) {
        List<Quest> quests = playerQuests.get(playerId);
        if (quests == null) {
            return false;
        }
        
        for (int i = 0; i < quests.size(); i++) {
            Quest quest = quests.get(i);
            if (quest.getQuestId().equals(questId) && quest.getStatus() == QuestStatus.ACTIVE) {
                if (!quest.isCompleted()) {
                    return false; // 任务未完成
                }
                
                quest.setStatus(QuestStatus.COMPLETED);
                distributeRewards(Bukkit.getPlayer(playerId), quest);
                return true;
            }
        }
        
        return false;
    }

    @Override
    public List<Quest> getActiveBountyQuests() {
        synchronized (bountyQuests) {
            return bountyQuests.stream()
                    .filter(q -> q.getStatus() == QuestStatus.ACTIVE && !q.isExpired())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public boolean completeBountyQuest(Player player, UUID questId) {
        synchronized (bountyQuests) {
            for (Quest quest : bountyQuests) {
                if (quest.getQuestId().equals(questId) && quest.getStatus() == QuestStatus.ACTIVE) {
                    if (!quest.isCompleted()) {
                        return false; // 任务未完成
                    }
                    
                    // 悬赏任务可以被多个玩家完成，不改变状态
                    distributeRewards(player, quest);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void startBountyScheduler() {
        stopBountyScheduler(); // 确保没有重复的调度器
        
        FileConfiguration bountyConfig = configManager.getBountyConfig();
        if (bountyConfig == null) {
            return;
        }
        
        int minInterval = bountyConfig.getInt("bounty.interval-min", 20) * 60 * 20; // 转换为tick
        int maxInterval = bountyConfig.getInt("bounty.interval-max", 40) * 60 * 20;
        
        if (minInterval <= 0 || maxInterval <= 0 || minInterval > maxInterval) {
            MessageUtils.logWarning("悬赏任务间隔配置无效，已禁用悬赏任务发布");
            return;
        }
        
        scheduleNextBounty(minInterval, maxInterval);
    }

    @Override
    public void stopBountyScheduler() {
        if (bountyTaskId != -1) {
            Bukkit.getScheduler().cancelTask(bountyTaskId);
            bountyTaskId = -1;
        }
    }

    /**
     * 调度下一个悬赏任务
     */
    private void scheduleNextBounty(int minInterval, int maxInterval) {
        Random random = new Random();
        int delay = minInterval + random.nextInt(maxInterval - minInterval + 1);
        
        bountyTaskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            publishBountyQuest();
            scheduleNextBounty(minInterval, maxInterval); // 递归调度下一个
        }, delay).getTaskId();
    }

    /**
     * 发布悬赏任务
     */
    private void publishBountyQuest() {
        FileConfiguration bountyConfig = configManager.getBountyConfig();
        if (bountyConfig == null) {
            return;
        }
        
        String allowedTypes = bountyConfig.getString("bounty.allowed-types", "MATERIAL");
        QuestType type;
        
        if (allowedTypes.equalsIgnoreCase("MATERIAL")) {
            type = QuestType.MATERIAL;
        } else if (allowedTypes.equalsIgnoreCase("KILL")) {
            type = QuestType.KILL;
            // TODO: 实现击杀任务
            return;
        } else if (allowedTypes.equalsIgnoreCase("BOTH")) {
            // 随机选择类型
            type = new Random().nextBoolean() ? QuestType.MATERIAL : QuestType.KILL;
            if (type == QuestType.KILL) {
                // TODO: 实现击杀任务
                return;
            }
        } else {
            type = QuestType.MATERIAL;
        }
        
        // 生成悬赏任务（playerId为null，表示所有玩家可完成）
        Quest bounty = generateQuest(null, type, QuestReleaseMethod.BOUNTY);
        if (bounty == null) {
            return;
        }
        
        synchronized (bountyQuests) {
            bountyQuests.add(bounty);
        }
        
        // 广播悬赏任务
        broadcastBountyQuest(bounty);
    }

    /**
     * 广播悬赏任务
     */
    private void broadcastBountyQuest(Quest quest) {
        String message = formatBountyAnnouncement(quest);
        String colored = ColorUtils.colorize(message);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(colored);
        }
        
        MessageUtils.logInfo("悬赏任务已发布: " + quest.getMaterialKey() + " x" + quest.getRequiredAmount());
    }

    /**
     * 格式化悬赏任务公告
     */
    private String formatBountyAnnouncement(Quest quest) {
        String materialName = quest.getMaterialKey().split(":")[1]; // 提取物品名称
        return String.format("&6[悬赏任务] &e收集 %s x%d &7- &f完成任务可获得丰厚奖励！",
                materialName, quest.getRequiredAmount());
    }

    /**
     * 生成任务
     */
    private Quest generateQuest(UUID playerId, QuestType type, QuestReleaseMethod releaseMethod) {
        if (type == QuestType.MATERIAL) {
            return generateMaterialQuest(playerId, releaseMethod);
        } else if (type == QuestType.KILL) {
            // TODO: 实现击杀任务生成
            return null;
        }
        return null;
    }

    /**
     * 生成材料任务
     */
    private Quest generateMaterialQuest(UUID playerId, QuestReleaseMethod releaseMethod) {
        FileConfiguration whitelist = configManager.getMaterialsWhitelist();
        FileConfiguration tasksMaterial = configManager.getTasksMaterial();
        
        if (whitelist == null || tasksMaterial == null) {
            return null;
        }
        
        // 从白名单中随机选择材料
        ConfigurationSection materialsSection = whitelist.getConfigurationSection("materials");
        if (materialsSection == null) {
            return null;
        }
        
        List<String> materialKeys = new ArrayList<>();
        Map<String, MaterialInfo> materialInfos = new HashMap<>();
        
        for (String type : materialsSection.getKeys(false)) {
            ConfigurationSection typeSection = materialsSection.getConfigurationSection(type);
            if (typeSection == null) continue;
            
            for (String name : typeSection.getKeys(false)) {
                String key = type + ":" + name;
                materialKeys.add(key);
                
                ConfigurationSection itemSection = typeSection.getConfigurationSection(name);
                int min = itemSection != null ? itemSection.getInt("min", 16) : 16;
                int max = itemSection != null ? itemSection.getInt("max", 256) : 256;
                int materialLevel = itemSection != null ? itemSection.getInt("material-level", 1) : 1;
                
                materialInfos.put(key, new MaterialInfo(min, max, materialLevel));
            }
        }
        
        if (materialKeys.isEmpty()) {
            return null;
        }
        
        // 随机选择材料
        Random random = new Random();
        String selectedKey = materialKeys.get(random.nextInt(materialKeys.size()));
        MaterialInfo info = materialInfos.get(selectedKey);
        
        // 随机生成数量
        int requiredAmount = info.min + random.nextInt(info.max - info.min + 1);
        
        // 计算任务等级
        int level = QuestUtils.calculateQuestLevel(info.materialLevel, requiredAmount);
        
        // 获取时间限制
        long timeLimit = tasksMaterial.getLong("material.default-time-limit", 3600000);
        
        // 创建任务
        UUID questId = UUID.randomUUID();
        long startTime = System.currentTimeMillis();
        
        return new Quest(questId, playerId, QuestType.MATERIAL, releaseMethod,
                selectedKey, requiredAmount, 0, startTime, timeLimit, level, QuestStatus.ACTIVE);
    }

    /**
     * 分发奖励
     */
    private void distributeRewards(Player player, Quest quest) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        FileConfiguration rewardsDefault = configManager.getRewardsDefault();
        FileConfiguration rewardsLevel = configManager.getRewardsLevel();
        FileConfiguration timeBonus = configManager.getBonusTimeBonus();
        FileConfiguration bountyConfig = configManager.getBountyConfig();
        
        QuestUtils.RewardCalculation calc = QuestUtils.calculateReward(
                quest, rewardsDefault, rewardsLevel, timeBonus, bountyConfig);
        
        // 发放成就点数
        if (calc.getQuestPoint() > 0) {
            String command = String.format("qp give %s %d", player.getName(), calc.getQuestPoint());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
        
        // 计算货币数量（需要根据基础奖励计算）
        int baseCurrency = rewardsDefault.getInt("default.currency.amount", 1);
        int totalCurrency = (int) Math.round(baseCurrency * calc.getLevelBonus() * calc.getBountyBonus() * calc.getTimeBonus());
        
        // 分发货币（64进制）
        if (totalCurrency > 0) {
            List<QuestUtils.CurrencyStack> stacks = QuestUtils.distributeCurrency64Base(totalCurrency, rewardsDefault);
            String currencyType = calc.getCurrencyType();
            
            for (QuestUtils.CurrencyStack stack : stacks) {
                try {
                    net.Indyuce.mmoitems.api.Type mmoType = MMOItems.plugin.getTypes().get(currencyType);
                    if (mmoType == null) {
                        MessageUtils.logWarning("货币类型不存在: " + currencyType);
                        continue;
                    }
                    
                    MMOItem mmoItem = MMOItems.plugin.getMMOItem(mmoType, stack.getItemId());
                    if (mmoItem == null) {
                        MessageUtils.logWarning("货币物品不存在: " + stack.getItemId());
                        continue;
                    }
                    
                    ItemStack item = mmoItem.newBuilder().build();
                    item.setAmount(stack.getCount());
                    
                    // 给予玩家物品
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                    if (!leftover.isEmpty()) {
                        // 背包满了，掉落物品
                        for (ItemStack drop : leftover.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), drop);
                        }
                    }
                } catch (Exception e) {
                    MessageUtils.logError("发放货币失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        // 发送完成消息
        String rating = QuestUtils.getTimeRatingDisplay(quest.getElapsedTime(), timeBonus);
        player.sendMessage(ColorUtils.colorize(String.format(
                "&a✓ &f任务完成！评级: &e%s &7(奖励倍数: %.2fx)", rating,
                calc.getLevelBonus() * calc.getBountyBonus() * calc.getTimeBonus())));
    }

    /**
     * 材料信息
     */
    private static class MaterialInfo {
        final int min;
        final int max;
        final int materialLevel;

        MaterialInfo(int min, int max, int materialLevel) {
            this.min = min;
            this.max = max;
            this.materialLevel = materialLevel;
        }
    }
}

