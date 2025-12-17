package top.arctain.snowTerritory.quest.service.scheduler;

/**
 * 悬赏任务调度器接口
 */
public interface BountyScheduler {
    
    /**
     * 启动调度器
     */
    void start();
    
    /**
     * 停止调度器
     */
    void stop();
    
    /**
     * 是否正在运行
     */
    boolean isRunning();
}

