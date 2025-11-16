package top.kimwonjoon.trigger.http.admin;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IAiClientModelConfigDao;
import top.kimwonjoon.infrastructure.dao.po.AiClientModelConfig;

import java.util.Date;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-06 16:43
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/admin/client/model/config/")
public class AiAdminClientModelConfigController {

    @Resource
    private IAiClientModelConfigDao aiClientModelConfigDao;

    /**
     * 分页查询客户端模型配置列表
     *
     * @param aiClientModelConfig 查询条件
     * @return 分页结果
     */
    @RequestMapping(value = "queryClientModelConfigList", method = RequestMethod.POST)
    public ResponseEntity<List<AiClientModelConfig>> queryClientModelConfigList(@RequestBody AiClientModelConfig aiClientModelConfig) {
        try {
            List<AiClientModelConfig> configList = aiClientModelConfigDao.queryAllModelConfig();
            return ResponseEntity.ok(configList);
        } catch (Exception e) {
            log.error("查询客户端模型配置列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID查询客户端模型配置
     *
     * @param id 客户端模型配置ID
     * @return 客户端模型配置
     */
    @RequestMapping(value = "queryClientModelConfigById", method = RequestMethod.GET)
    public ResponseEntity<AiClientModelConfig> queryClientModelConfigById(@RequestParam("id") Long id) {
        try {
            AiClientModelConfig config = aiClientModelConfigDao.queryModelConfigById(id);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("查询客户端模型配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据客户端ID查询模型配置
     *
     * @param clientId 客户端ID
     * @return 模型配置
     */
    @RequestMapping(value = "queryClientModelConfigByClientId", method = RequestMethod.GET)
    public ResponseEntity<AiClientModelConfig> queryClientModelConfigByClientId(@RequestParam("clientId") Long clientId) {
        try {
            AiClientModelConfig config = aiClientModelConfigDao.queryModelConfigByClientId(clientId);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("根据客户端ID查询模型配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据模型ID查询客户端模型配置列表
     *
     * @param modelId 模型ID
     * @return 客户端模型配置列表
     */
    @RequestMapping(value = "queryClientModelConfigByModelId", method = RequestMethod.GET)
    public ResponseEntity<List<AiClientModelConfig>> queryClientModelConfigByModelId(@RequestParam("modelId") Long modelId) {
        try {
            List<AiClientModelConfig> configList = aiClientModelConfigDao.queryModelConfigByModelId(modelId);
            return ResponseEntity.ok(configList);
        } catch (Exception e) {
            log.error("根据模型ID查询客户端模型配置列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 新增客户端模型配置
     *
     * @param aiClientModelConfig 客户端模型配置
     * @return 结果
     */
    @RequestMapping(value = "addClientModelConfig", method = RequestMethod.POST)
    public ResponseEntity<Boolean> addClientModelConfig(@RequestBody AiClientModelConfig aiClientModelConfig) {
        try {
            int count = aiClientModelConfigDao.insert(aiClientModelConfig);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("新增客户端模型配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新客户端模型配置
     *
     * @param aiClientModelConfig 客户端模型配置
     * @return 结果
     */
    @RequestMapping(value = "updateClientModelConfig", method = RequestMethod.POST)
    public ResponseEntity<Boolean> updateClientModelConfig(@RequestBody AiClientModelConfig aiClientModelConfig) {
        try {
            int count = aiClientModelConfigDao.update(aiClientModelConfig);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("更新客户端模型配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除客户端模型配置
     *
     * @param id 客户端模型配置ID
     * @return 结果
     */
    @RequestMapping(value = "deleteClientModelConfig", method = RequestMethod.GET)
    public ResponseEntity<Boolean> deleteClientModelConfig(@RequestParam("id") Long id) {
        try {
            int count = aiClientModelConfigDao.deleteById(id);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("删除客户端模型配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }
}
