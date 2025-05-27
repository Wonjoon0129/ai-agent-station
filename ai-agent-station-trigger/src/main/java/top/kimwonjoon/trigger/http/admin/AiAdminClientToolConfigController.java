package top.kimwonjoon.trigger.http.admin;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IAiClientToolConfigDao;
import top.kimwonjoon.infrastructure.dao.po.AiClientToolConfig;

import java.util.Date;
import java.util.List;

/**
 * 客户端工具配置管理服务
 *
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-06 16:44
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/admin/client/tool/config/")
public class AiAdminClientToolConfigController {

    @Resource
    private IAiClientToolConfigDao aiClientToolConfigDao;

    /**
     * 分页查询客户端工具配置列表
     *
     * @param aiClientToolConfig 查询条件
     * @return 分页结果
     */
    @RequestMapping(value = "queryClientToolConfigList", method = RequestMethod.POST)
    public ResponseEntity<List<AiClientToolConfig>> queryClientToolConfigList(@RequestBody AiClientToolConfig aiClientToolConfig) {
        try {
            List<AiClientToolConfig> configList = aiClientToolConfigDao.queryToolConfigList(aiClientToolConfig);
            return ResponseEntity.ok(configList);
        } catch (Exception e) {
            log.error("查询客户端工具配置列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID查询客户端工具配置
     *
     * @param id 客户端工具配置ID
     * @return 客户端工具配置
     */
    @RequestMapping(value = "queryClientToolConfigById", method = RequestMethod.GET)
    public ResponseEntity<AiClientToolConfig> queryClientToolConfigById(@RequestParam("id") Long id) {
        try {
            AiClientToolConfig config = aiClientToolConfigDao.queryToolConfigById(id);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("查询客户端工具配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据客户端ID查询工具配置列表
     *
     * @param clientId 客户端ID
     * @return 工具配置列表
     */
    @RequestMapping(value = "queryClientToolConfigByClientId", method = RequestMethod.GET)
    public ResponseEntity<List<AiClientToolConfig>> queryClientToolConfigByClientId(@RequestParam("clientId") Long clientId) {
        try {
            List<AiClientToolConfig> configList = aiClientToolConfigDao.queryToolConfigByClientId(clientId);
            return ResponseEntity.ok(configList);
        } catch (Exception e) {
            log.error("根据客户端ID查询工具配置列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 新增客户端工具配置
     *
     * @param aiClientToolConfig 客户端工具配置
     * @return 结果
     */
    @RequestMapping(value = "addClientToolConfig", method = RequestMethod.POST)
    public ResponseEntity<Boolean> addClientToolConfig(@RequestBody AiClientToolConfig aiClientToolConfig) {
        try {
            aiClientToolConfig.setCreateTime(new Date());
            int count = aiClientToolConfigDao.insert(aiClientToolConfig);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("新增客户端工具配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新客户端工具配置
     *
     * @param aiClientToolConfig 客户端工具配置
     * @return 结果
     */
    @RequestMapping(value = "updateClientToolConfig", method = RequestMethod.POST)
    public ResponseEntity<Boolean> updateClientToolConfig(@RequestBody AiClientToolConfig aiClientToolConfig) {
        try {
            int count = aiClientToolConfigDao.update(aiClientToolConfig);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("更新客户端工具配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除客户端工具配置
     *
     * @param id 客户端工具配置ID
     * @return 结果
     */
    @RequestMapping(value = "deleteClientToolConfig", method = RequestMethod.GET)
    public ResponseEntity<Boolean> deleteClientToolConfig(@RequestParam("id") Long id) {
        try {
            int count = aiClientToolConfigDao.deleteById(id);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("删除客户端工具配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }
}
