package top.kimwonjoon.trigger.http.admin;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IAiClientModelDao;
import top.kimwonjoon.infrastructure.dao.po.AiClientModel;

import java.util.Date;
import java.util.List;

/**
 * 客户端模型管理服务
 *
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-06 15:16
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/admin/client/model/")
public class AiAdminClientModelController {

    @Resource
    private IAiClientModelDao aiClientModelDao;

    /**
     * 分页查询客户端模型列表
     *
     * @param aiClientModel 查询条件
     * @return 分页结果
     */
    @RequestMapping(value = "queryClientModelList", method = RequestMethod.POST)
    public ResponseEntity<List<AiClientModel>> queryClientModelList(@RequestBody AiClientModel aiClientModel) {
        try {
            List<AiClientModel> aiClientModelList = aiClientModelDao.queryClientModelList(aiClientModel);
            return ResponseEntity.ok(aiClientModelList);
        } catch (Exception e) {
            log.error("查询客户端模型列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    @RequestMapping(value = "queryClientModelListAll", method = RequestMethod.POST)
    public ResponseEntity<List<AiClientModel>> queryClientModelListAll() {
        try {
            List<AiClientModel> aiClientModelList = aiClientModelDao.queryClientModelList();
            return ResponseEntity.ok(aiClientModelList);
        } catch (Exception e) {
            log.error("查询客户端模型列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }
    @RequestMapping(value = "queryAllModelConfig", method = RequestMethod.POST)
    public ResponseEntity<List<AiClientModel>> queryAllModelConfig() {
        try {
            List<AiClientModel> aiClientModelList = aiClientModelDao.queryAllModelConfig();
            return ResponseEntity.ok(aiClientModelList);
        } catch (Exception e) {
            log.error("查询客户端模型列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID查询客户端模型
     *
     * @param id 客户端模型ID
     * @return 客户端模型
     */
    @RequestMapping(value = "queryClientModelById", method = RequestMethod.GET)
    public ResponseEntity<AiClientModel> queryClientModelById(@RequestParam("id") Long id) {
        try {
            AiClientModel aiClientModel = aiClientModelDao.queryModelConfigById(id);
            return ResponseEntity.ok(aiClientModel);
        } catch (Exception e) {
            log.error("查询客户端模型异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 新增客户端模型
     *
     * @param aiClientModel 客户端模型
     * @return 结果
     */
    @RequestMapping(value = "addClientModel", method = RequestMethod.POST)
    public ResponseEntity<Boolean> addClientModel(@RequestBody AiClientModel aiClientModel) {
        try {
            int count = aiClientModelDao.insert(aiClientModel);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("新增客户端模型异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新客户端模型
     *
     * @param aiClientModel 客户端模型
     * @return 结果
     */
    @RequestMapping(value = "updateClientModel", method = RequestMethod.POST)
    public ResponseEntity<Boolean> updateClientModel(@RequestBody AiClientModel aiClientModel) {
        try {
            int count = aiClientModelDao.update(aiClientModel);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("更新客户端模型异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除客户端模型
     *
     * @param id 客户端模型ID
     * @return 结果
     */
    @RequestMapping(value = "deleteClientModel", method = RequestMethod.GET)
    public ResponseEntity<Boolean> deleteClientModel(@RequestParam("id") Long id) {
        try {
            int count = aiClientModelDao.deleteById(id);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("删除客户端模型异常", e);
            return ResponseEntity.status(500).build();
        }
    }
}
