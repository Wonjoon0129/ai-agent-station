package top.kimwonjoon.trigger.http.admin;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IAiAgentDao;
import top.kimwonjoon.infrastructure.dao.po.AiAgent;

import java.util.Date;
import java.util.List;

/**
 * @ClassName AiAdminAgentController
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/23 10:56
 * @Version 1.0
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/admin/agent/")
public class AiAdminAgentController {
    @Resource
    private IAiAgentDao aiAgentDao;

    /**
     * 分页查询AI智能体列表
     *
     * @param aiAgent 查询条件
     * @return 分页结果
     */
    @RequestMapping(value = "queryAiAgentList", method = RequestMethod.POST)
    public ResponseEntity<List<AiAgent>> queryAiAgentList(@RequestBody AiAgent aiAgent) {
        try {
            List<AiAgent> aiAgentList = aiAgentDao.queryAiAgentList(aiAgent);

            return ResponseEntity.ok(aiAgentList);
        } catch (Exception e) {
            log.error("查询AI智能体列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    @RequestMapping(value = "queryAllAgentConfigListByChannel", method = RequestMethod.POST)
    public ResponseEntity<List<AiAgent>> queryAllAgentConfig(@RequestParam("channel") String channel) {
        try {
            List<AiAgent> aiAgentList = aiAgentDao.queryAllAgentConfigByChannel(channel);
            return ResponseEntity.ok(aiAgentList);
        } catch (Exception e) {
            log.error("查询AI智能体列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID查询AI智能体
     *
     * @param id AI智能体ID
     * @return AI智能体
     */
    @RequestMapping(value = "queryAiAgentById", method = RequestMethod.GET)
    public ResponseEntity<AiAgent> queryAiAgentById(@RequestParam("id") Long id) {
        try {
            AiAgent aiAgent = aiAgentDao.queryAgentConfigById(id);
            return ResponseEntity.ok(aiAgent);
        } catch (Exception e) {
            log.error("查询AI智能体异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 新增AI智能体
     *
     * @param aiAgent AI智能体
     * @return 结果
     */
    @RequestMapping(value = "addAiAgent", method = RequestMethod.POST)
    public ResponseEntity<Boolean> addAiAgent(@RequestBody AiAgent aiAgent) {
        try {
            aiAgent.setCreateTime(new Date());
            aiAgent.setUpdateTime(new Date());
            int count = aiAgentDao.insert(aiAgent);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("新增AI智能体异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新AI智能体
     *
     * @param aiAgent AI智能体
     * @return 结果
     */
    @RequestMapping(value = "updateAiAgent", method = RequestMethod.POST)
    public ResponseEntity<Boolean> updateAiAgent(@RequestBody AiAgent aiAgent) {
        try {
            aiAgent.setUpdateTime(new Date());
            int count = aiAgentDao.update(aiAgent);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("更新AI智能体异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除AI智能体
     *
     * @param id AI智能体ID
     * @return 结果
     */
    @RequestMapping(value = "deleteAiAgent", method = RequestMethod.GET)
    public ResponseEntity<Boolean> deleteAiAgent(@RequestParam("id") Long id) {
        try {
            int count = aiAgentDao.deleteById(id);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("删除AI智能体异常", e);
            return ResponseEntity.status(500).build();
        }
    }
}
