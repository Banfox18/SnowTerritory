package top.arctain.snowTerritory.quest.data;

/**
 * 任务状态枚举
 */
public enum QuestStatus {
    /**
     * 进行中 - 任务已接取/发布，等待完成
     */
    ACTIVE,
    
    /**
     * 已完成 - 任务已完成
     */
    COMPLETED,
    
    /**
     * 已过期 - 任务超时未完成
     */
    EXPIRED
}

