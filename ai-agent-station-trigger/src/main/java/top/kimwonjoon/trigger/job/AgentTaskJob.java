package top.kimwonjoon.trigger.job;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import top.kimwonjoon.domain.agent.model.valobj.AiAgentTaskScheduleVO;
import top.kimwonjoon.domain.agent.service.IAiAgentChatService;
import top.kimwonjoon.domain.agent.service.task.AiAgentTaskService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 智能体任务调度作业
 * 定时获取有效的任务调度配置，并动态创建新的任务
 */
@Slf4j
@Component
public class AgentTaskJob implements DisposableBean {

    @Resource
    private AiAgentTaskService aiAgentTaskService;

    @Resource
    private IAiAgentChatService aiAgentChatService;

    private TaskScheduler taskScheduler;

    /**
     * 任务ID与任务执行器的映射，用于记录已添加的任务
     */
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 初始化任务调度器
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("agent-task-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    /**
     * 每分钟执行一次，检查并更新任务调度配置
     */
    @Scheduled(fixedRate = 60000)
    public void refreshTasks() {
        log.info("开始刷新智能体任务调度配置");
        try {
            // 获取所有有效的任务调度配置
            List<AiAgentTaskScheduleVO> taskSchedules = aiAgentTaskService.queryAllValidTaskSchedule();
            
            // 记录当前配置中的任务ID
            Map<Long, Boolean> currentTaskIds = new ConcurrentHashMap<>();
            
            // 处理每个任务调度配置
            for (AiAgentTaskScheduleVO task : taskSchedules) {
                Long taskId = task.getId();
                currentTaskIds.put(taskId, true);
                
                // 如果任务已经存在，则跳过
                if (scheduledTasks.containsKey(taskId)) {
                    continue;
                }
                
                // 创建并调度新任务
                scheduleTask(task);
            }
            
            // 移除已不存在的任务
            scheduledTasks.keySet().removeIf(taskId -> {
                if (!currentTaskIds.containsKey(taskId)) {
                    ScheduledFuture<?> future = scheduledTasks.remove(taskId);
                    if (future != null) {
                        future.cancel(true);
                        log.info("已移除任务，ID: {}", taskId);
                    }
                    return true;
                }
                return false;
            });
            
            log.info("智能体任务调度配置刷新完成，当前活跃任务数: {}", scheduledTasks.size());
        } catch (Exception e) {
            log.error("刷新智能体任务调度配置时发生错误", e);
        }
    }
    
    /**
     * 每天凌晨2点执行一次，清理数据库中的无效任务
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void cleanInvalidTasks() {
        log.info("开始清理无效的智能体任务");
        try {
            // 获取所有无效的任务ID
            List<Long> invalidTaskIds = aiAgentTaskService.queryAllInvalidTaskScheduleIds();
            
            if (invalidTaskIds == null || invalidTaskIds.isEmpty()) {
                log.info("没有发现无效的任务需要清理");
                return;
            }
            
            log.info("发现{}个无效任务需要清理", invalidTaskIds.size());
            
            // 从调度器中移除这些任务
            for (Long taskId : invalidTaskIds) {
                ScheduledFuture<?> future = scheduledTasks.remove(taskId);
                if (future != null) {
                    future.cancel(true);
                    log.info("已移除无效任务，ID: {}", taskId);
                }
            }
            
            log.info("无效任务清理完成，当前活跃任务数: {}", scheduledTasks.size());
        } catch (Exception e) {
            log.error("清理无效任务时发生错误", e);
        }
    }

    /**
     * 调度单个任务
     */
    private void scheduleTask(AiAgentTaskScheduleVO task) {
        try {
            log.info("开始调度任务，ID: {}, 描述: {}, Cron表达式: {}", task.getId(), task.getDescription(), task.getCronExpression());
            
            // 创建任务执行器
            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> executeTask(task),
                    new CronTrigger(task.getCronExpression())
            );
            
            // 记录任务
            scheduledTasks.put(task.getId(), future);
            
            log.info("任务调度成功，ID: {}", task.getId());
        } catch (Exception e) {
            log.error("调度任务时发生错误，ID: {}", task.getId(), e);
        }
    }

    /**
     * 执行任务
     */
    private void executeTask(AiAgentTaskScheduleVO task) {
        try {
            log.info("开始执行任务，ID: {}, 描述: {}", task.getId(), task.getDescription());
            
            // 获取任务参数
            String taskParam = task.getTaskParam();

            // 执行任务
            aiAgentChatService.aiAgentChat(task.getAgentId(), taskParam,null);

            log.info("任务执行完成，ID: {}", task.getId());
        } catch (Exception e) {
            log.error("执行任务时发生错误，ID: {}", task.getId(), e);
        }
    }

    @Override
    public void destroy() {
        // 关闭时取消所有任务
        scheduledTasks.forEach((id, future) -> {
            if (future != null) {
                future.cancel(true);
                log.info("已取消任务，ID: {}", id);
            }
        });
        scheduledTasks.clear();
        log.info("所有智能体任务已取消");
    }
}