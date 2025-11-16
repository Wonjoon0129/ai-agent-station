package top.kimwonjoon.trigger.http.admin;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IAiAgentClientDao;
import top.kimwonjoon.infrastructure.dao.IAiClientDao;
import top.kimwonjoon.infrastructure.dao.po.AiAgentClientLine;
import top.kimwonjoon.infrastructure.dao.po.AiClient;

import java.util.Date;
import java.util.List;

/**
 * 客户端管理服务
 *
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-06 15:16
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/admin/client/")
public class AiAdminClientController {

    @Resource
    private IAiAgentClientDao aiAgentClientDao;
    @Resource
    private IAiClientDao aiClientDao;

    @RequestMapping(value = "queryClientListAll", method = RequestMethod.POST)
    public ResponseEntity<List<AiClient>> queryClientListAll(@RequestBody AiClient aiClient) {
        try {
            List<AiClient> clientList = aiClientDao.queryClientListAll(aiClient);
            return ResponseEntity.ok(clientList);
        } catch (Exception e) {
            log.error("查询客户端列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }


    /**
     * 分页查询客户端列表
     *
     * @param aiAgentClientLine 查询条件
     * @return 分页结果
     */
    @RequestMapping(value = "queryClientList", method = RequestMethod.POST)
    public ResponseEntity<List<AiAgentClientLine>> queryClientList(@RequestBody AiAgentClientLine aiAgentClientLine) {
        try {
            List<AiAgentClientLine> clientList = aiAgentClientDao.queryAgentClientList(aiAgentClientLine);
            return ResponseEntity.ok(clientList);
        } catch (Exception e) {
            log.error("查询客户端列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID查询客户端
     *
     * @param id 客户端ID
     * @return 客户端
     */
    @RequestMapping(value = "queryClientById", method = RequestMethod.GET)
    public ResponseEntity<AiAgentClientLine> queryClientById(@RequestParam("id") Long id) {
        try {
            AiAgentClientLine client = aiAgentClientDao.queryAgentClientConfigById(id);
            return ResponseEntity.ok(client);
        } catch (Exception e) {
            log.error("查询客户端异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据智能体ID查询关联的客户端
     *
     * @param agentId 智能体ID
     * @return 客户端列表
     */
    @RequestMapping(value = "queryClientByAgentId", method = RequestMethod.GET)
    public ResponseEntity<List<AiAgentClientLine>> queryClientByAgentId(@RequestParam("agentId") Long agentId) {
        try {
            List<AiAgentClientLine> clientList = aiAgentClientDao.queryAgentClientConfigByAgentId(agentId);
            return ResponseEntity.ok(clientList);
        } catch (Exception e) {
            log.error("根据智能体ID查询客户端异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 新增客户端
     *
     * @param aiAgentClientLine 客户端
     * @return 结果
     */
    @RequestMapping(value = "addClient", method = RequestMethod.POST)
    public ResponseEntity<Boolean> addClient(@RequestBody AiAgentClientLine aiAgentClientLine) {
        try {
            int count = aiAgentClientDao.insert(aiAgentClientLine);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("新增客户端异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新客户端
     *
     * @param aiAgentClientLine 客户端
     * @return 结果
     */
    @RequestMapping(value = "updateClient", method = RequestMethod.POST)
    public ResponseEntity<Boolean> updateClient(@RequestBody AiAgentClientLine aiAgentClientLine) {
        try {
            int count = aiAgentClientDao.update(aiAgentClientLine);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("更新客户端异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除客户端
     *
     * @param id 客户端ID
     * @return 结果
     */
    @RequestMapping(value = "deleteClient", method = RequestMethod.GET)
    public ResponseEntity<Boolean> deleteClient(@RequestParam("id") Long id) {
        try {
            int count = aiAgentClientDao.deleteById(id);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("删除客户端异常", e);
            return ResponseEntity.status(500).build();
        }
    }
}
