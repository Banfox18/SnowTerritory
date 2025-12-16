package top.arctain.snowTerritory.quest.data;

import java.util.UUID;

/**
 * 任务数据模型
 */
public class Quest {
    
    private final UUID questId;
    private final UUID playerId; // 对于悬赏任务，可能为null（所有玩家可完成）
    private final QuestType type;
    private final QuestReleaseMethod releaseMethod;
    private final String materialKey; // TYPE:NAME格式，例如 MM_DROPS:优质狼皮
    private final int requiredAmount;
    private final int currentAmount;
    private final long startTime; // 任务开始时间（毫秒）
    private final long timeLimit; // 时间限制（毫秒），-1表示无限制
    private final int level; // 任务等级
    private QuestStatus status;
    
    public Quest(UUID questId, UUID playerId, QuestType type, QuestReleaseMethod releaseMethod,
                 String materialKey, int requiredAmount, int currentAmount,
                 long startTime, long timeLimit, int level, QuestStatus status) {
        this.questId = questId;
        this.playerId = playerId;
        this.type = type;
        this.releaseMethod = releaseMethod;
        this.materialKey = materialKey;
        this.requiredAmount = requiredAmount;
        this.currentAmount = currentAmount;
        this.startTime = startTime;
        this.timeLimit = timeLimit;
        this.level = level;
        this.status = status;
    }
    
    public UUID getQuestId() {
        return questId;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public QuestType getType() {
        return type;
    }
    
    public QuestReleaseMethod getReleaseMethod() {
        return releaseMethod;
    }
    
    public String getMaterialKey() {
        return materialKey;
    }
    
    public int getRequiredAmount() {
        return requiredAmount;
    }
    
    public int getCurrentAmount() {
        return currentAmount;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getTimeLimit() {
        return timeLimit;
    }
    
    public int getLevel() {
        return level;
    }
    
    public QuestStatus getStatus() {
        return status;
    }
    
    public void setStatus(QuestStatus status) {
        this.status = status;
    }
    
    /**
     * 检查任务是否已完成
     */
    public boolean isCompleted() {
        return currentAmount >= requiredAmount;
    }
    
    /**
     * 检查任务是否已过期
     */
    public boolean isExpired() {
        if (timeLimit <= 0) {
            return false; // 无时间限制
        }
        long elapsed = System.currentTimeMillis() - startTime;
        return elapsed > timeLimit;
    }
    
    /**
     * 获取已用时间（毫秒）
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * 创建任务副本并更新进度
     */
    public Quest withProgress(int newAmount) {
        return new Quest(questId, playerId, type, releaseMethod, materialKey,
                requiredAmount, newAmount, startTime, timeLimit, level, status);
    }
}

