package top.kimwonjoon.trigger.http.admin;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IAiAgentTaskScheduleDao;
import top.kimwonjoon.infrastructure.dao.po.AiAgentTaskSchedule;

import java.util.Date;
import java.util.List;

/**
 * AI代理任务调度管理服务
 *
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-06 16:25
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/admin/agent/task/")
public class AiAdminAgentTaskScheduleController {

    @Resource
    private IAiAgentTaskScheduleDao aiAgentTaskScheduleDao;

    /**
     * 分页查询AI代理任务调度列表
     *
     * @param aiAgentTaskSchedule 查询条件
     * @return 分页结果
     */
    @RequestMapping(value = "queryTaskScheduleList", method = RequestMethod.POST)
    public ResponseEntity<List<AiAgentTaskSchedule>> queryTaskScheduleList(@RequestBody AiAgentTaskSchedule aiAgentTaskSchedule) {
        try {
            List<AiAgentTaskSchedule> taskScheduleList = aiAgentTaskScheduleDao.queryTaskScheduleList(aiAgentTaskSchedule);
            return ResponseEntity.ok(taskScheduleList);
        } catch (Exception e) {
            log.error("查询AI代理任务调度列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID查询AI代理任务调度
     *
     * @param id AI代理任务调度ID
     * @return AI代理任务调度
     */
    @RequestMapping(value = "queryTaskScheduleById", method = RequestMethod.GET)
    public ResponseEntity<AiAgentTaskSchedule> queryTaskScheduleById(@RequestParam("id") Long id) {
        try {
            AiAgentTaskSchedule taskSchedule = aiAgentTaskScheduleDao.queryTaskScheduleById(id);
            return ResponseEntity.ok(taskSchedule);
        } catch (Exception e) {
            log.error("查询AI代理任务调度异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 新增AI代理任务调度
     *
     * @param aiAgentTaskSchedule AI代理任务调度
     * @return 结果
     */
    @RequestMapping(value = "addTaskSchedule", method = RequestMethod.POST)
    public ResponseEntity<Boolean> addTaskSchedule(@RequestBody AiAgentTaskSchedule aiAgentTaskSchedule) {
        try {
            int count = aiAgentTaskScheduleDao.insert(aiAgentTaskSchedule);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("新增AI代理任务调度异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新AI代理任务调度
     *
     * @param aiAgentTaskSchedule AI代理任务调度
     * @return 结果
     */
    @RequestMapping(value = "updateTaskSchedule", method = RequestMethod.POST)
    public ResponseEntity<Boolean> updateTaskSchedule(@RequestBody AiAgentTaskSchedule aiAgentTaskSchedule) {
        try {
            int count = aiAgentTaskScheduleDao.update(aiAgentTaskSchedule);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("更新AI代理任务调度异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除AI代理任务调度
     *
     * @param id AI代理任务调度ID
     * @return 结果
     */
    @RequestMapping(value = "deleteTaskSchedule", method = RequestMethod.GET)
    public ResponseEntity<Boolean> deleteTaskSchedule(@RequestParam("id") Long id) {
        try {
            int count = aiAgentTaskScheduleDao.deleteById(id);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("删除AI代理任务调度异常", e);
            return ResponseEntity.status(500).build();
        }
    }
}
