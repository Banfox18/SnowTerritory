package top.arctain.snowTerritory.quest.service.generator;

import top.arctain.snowTerritory.quest.data.Quest;
import top.arctain.snowTerritory.quest.data.QuestReleaseMethod;
import top.arctain.snowTerritory.quest.data.QuestType;

import java.util.UUID;

/**
 * 任务生成器接口
 * 负责根据类型和发布方式生成任务
 */
public interface QuestGenerator {
    
    /**
     * 生成任务
     * 
     * @param playerId 玩家ID（悬赏任务可为null）
     * @param type 任务类型
     * @param releaseMethod 发布方式
     * @return 生成的任务，失败返回null
     */
    Quest generate(UUID playerId, QuestType type, QuestReleaseMethod releaseMethod);
    
    /**
     * 是否支持指定任务类型
     */
    boolean supports(QuestType type);
}

