package top.kimwonjoon.trigger.http.admin;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IAiAgentClientDao;
import top.kimwonjoon.infrastructure.dao.po.AiAgentClient;

import java.util.Date;
import java.util.List;

/**
 * @ClassName AiAdminAgentClientController
 * @Description AI智能体客户端关联管理服务
 * @Author @kimwonjoon
 * @Date 2025/5/23 11:15
 * @Version 1.0
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/admin/agent/client/")
public class AiAdminAgentClientController {

    @Resource
    private IAiAgentClientDao aiAgentClientDao;

    /**
     * 分页查询AI智能体客户端关联列表
     *
     * @param aiAgentClient 查询条件
     * @return 分页结果
     */
    @RequestMapping(value = "queryAgentClientList", method = RequestMethod.POST)
    public ResponseEntity<List<AiAgentClient>> queryAgentClientList(@RequestBody AiAgentClient aiAgentClient) {
        try {
            List<AiAgentClient> aiAgentClientList = aiAgentClientDao.queryAgentClientList(aiAgentClient);
            return ResponseEntity.ok(aiAgentClientList);
        } catch (Exception e) {
            log.error("查询AI智能体客户端关联列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID查询AI智能体客户端关联
     *
     * @param id AI智能体客户端关联ID
     * @return AI智能体客户端关联
     */
    @RequestMapping(value = "queryAgentClientById", method = RequestMethod.GET)
    public ResponseEntity<AiAgentClient> queryAgentClientById(@RequestParam("id") Long id) {
        try {
            AiAgentClient aiAgentClient = aiAgentClientDao.queryAgentClientConfigById(id);
            return ResponseEntity.ok(aiAgentClient);
        } catch (Exception e) {
            log.error("查询AI智能体客户端关联异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据智能体ID查询客户端关联列表
     *
     * @param agentId 智能体ID
     * @return 客户端关联列表
     */
    @RequestMapping(value = "queryAgentClientByAgentId", method = RequestMethod.GET)
    public ResponseEntity<List<AiAgentClient>> queryAgentClientByAgentId(@RequestParam("agentId") Long agentId) {
        try {
            List<AiAgentClient> aiAgentClientList = aiAgentClientDao.queryAgentClientConfigByAgentId(agentId);
            return ResponseEntity.ok(aiAgentClientList);
        } catch (Exception e) {
            log.error("根据智能体ID查询客户端关联列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据客户端ID查询智能体关联列表
     *
     * @param clientId 客户端ID
     * @return 智能体关联列表
     */
    @RequestMapping(value = "queryAgentClientByClientId", method = RequestMethod.GET)
    public ResponseEntity<List<AiAgentClient>> queryAgentClientByClientId(@RequestParam("clientId") Long clientId) {
        try {
            List<AiAgentClient> aiAgentClientList = aiAgentClientDao.queryAgentClientConfigByClientId(clientId);
            return ResponseEntity.ok(aiAgentClientList);
        } catch (Exception e) {
            log.error("根据客户端ID查询智能体关联列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 新增AI智能体客户端关联
     *
     * @param aiAgentClient AI智能体客户端关联
     * @return 结果
     */
    @RequestMapping(value = "addAgentClient", method = RequestMethod.POST)
    public ResponseEntity<Boolean> addAgentClient(@RequestBody AiAgentClient aiAgentClient) {
        try {
            aiAgentClient.setCreateTime(new Date());
            int count = aiAgentClientDao.insert(aiAgentClient);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("新增AI智能体客户端关联异常", e);
            return ResponseEntity.status(500).build();
        }
    }
    /**
     * 更新AI智能体客户端关联
     *
     * @param aiAgentClient AI智能体客户端关联
     * @return 结果
     */
    @RequestMapping(value = "updateAgentClient", method = RequestMethod.POST)
    public ResponseEntity<Boolean> updateAgentClient(@RequestBody AiAgentClient aiAgentClient) {
        try {
            int count = aiAgentClientDao.update(aiAgentClient);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("更新AI智能体客户端关联异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除AI智能体客户端关联
     *
     * @param id AI智能体客户端关联ID
     * @return 结果
     */
    @RequestMapping(value = "deleteAgentClient", method = RequestMethod.GET)
    public ResponseEntity<Boolean> deleteAgentClient(@RequestParam("id") Long id) {
        try {
            int count = aiAgentClientDao.deleteById(id);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("删除AI智能体客户端关联异常", e);
            return ResponseEntity.status(500).build();
        }
    }
}
