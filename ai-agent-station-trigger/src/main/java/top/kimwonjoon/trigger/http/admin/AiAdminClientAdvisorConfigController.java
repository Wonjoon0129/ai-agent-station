package top.kimwonjoon.trigger.http.admin;

import top.kimwonjoon.infrastructure.dao.IAiClientAdvisorConfigDao;
import top.kimwonjoon.infrastructure.dao.po.AiClientAdvisorConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-06 16:42
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/admin/client/advisor/config/")
public class AiAdminClientAdvisorConfigController {

    @Resource
    private IAiClientAdvisorConfigDao aiClientAdvisorConfigDao;

    /**
     * 分页查询客户端顾问配置列表
     *
     * @param aiClientAdvisorConfig 查询条件
     * @return 分页结果
     */
    @RequestMapping(value = "queryClientAdvisorConfigList", method = RequestMethod.POST)
    public ResponseEntity<List<AiClientAdvisorConfig>> queryClientAdvisorConfigList(@RequestBody AiClientAdvisorConfig aiClientAdvisorConfig) {
        try {
            List<AiClientAdvisorConfig> configList = aiClientAdvisorConfigDao.queryClientAdvisorConfigList(aiClientAdvisorConfig);
            return ResponseEntity.ok(configList);
        } catch (Exception e) {
            log.error("查询客户端顾问配置列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID查询客户端顾问配置
     *
     * @param id 客户端顾问配置ID
     * @return 客户端顾问配置
     */
    @RequestMapping(value = "queryClientAdvisorConfigById", method = RequestMethod.GET)
    public ResponseEntity<AiClientAdvisorConfig> queryClientAdvisorConfigById(@RequestParam Long id) {
        try {
            AiClientAdvisorConfig config = aiClientAdvisorConfigDao.queryClientAdvisorConfigById(id);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("查询客户端顾问配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据客户端ID查询顾问配置列表
     *
     * @param clientId 客户端ID
     * @return 顾问配置列表
     */
    @RequestMapping(value = "queryClientAdvisorConfigByClientId", method = RequestMethod.GET)
    public ResponseEntity<List<AiClientAdvisorConfig>> queryClientAdvisorConfigByClientId(@RequestParam("clientId") Long clientId) {
        try {
            List<AiClientAdvisorConfig> configList = aiClientAdvisorConfigDao.queryClientAdvisorConfigByClientId(clientId);
            return ResponseEntity.ok(configList);
        } catch (Exception e) {
            log.error("根据客户端ID查询顾问配置列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 新增客户端顾问配置
     *
     * @param aiClientAdvisorConfig 客户端顾问配置
     * @return 结果
     */
    @RequestMapping(value = "addClientAdvisorConfig", method = RequestMethod.POST)
    public ResponseEntity<Boolean> addClientAdvisorConfig(@RequestBody AiClientAdvisorConfig aiClientAdvisorConfig) {
        try {
            int count = aiClientAdvisorConfigDao.insert(aiClientAdvisorConfig);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("新增客户端顾问配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新客户端顾问配置
     *
     * @param aiClientAdvisorConfig 客户端顾问配置
     * @return 结果
     */
    @RequestMapping(value = "updateClientAdvisorConfig", method = RequestMethod.POST)
    public ResponseEntity<Boolean> updateClientAdvisorConfig(@RequestBody AiClientAdvisorConfig aiClientAdvisorConfig) {
        try {
            int count = aiClientAdvisorConfigDao.update(aiClientAdvisorConfig);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("更新客户端顾问配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除客户端顾问配置
     *
     * @param id 客户端顾问配置ID
     * @return 结果
     */
    @RequestMapping(value = "deleteClientAdvisorConfig", method = RequestMethod.GET)
    public ResponseEntity<Boolean> deleteClientAdvisorConfig(@RequestParam("id") Long id) {
        try {
            int count = aiClientAdvisorConfigDao.deleteById(id);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("删除客户端顾问配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }
}
