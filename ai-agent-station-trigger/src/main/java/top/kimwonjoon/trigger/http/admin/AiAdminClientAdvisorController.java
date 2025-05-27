package top.kimwonjoon.trigger.http.admin;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IAiClientAdvisorDao;
import top.kimwonjoon.infrastructure.dao.po.AiClientAdvisor;

import java.util.Date;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-06 15:45
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/admin/client/advisor/")
public class AiAdminClientAdvisorController {

    @Resource
    private IAiClientAdvisorDao aiClientAdvisorDao;

    /**
     * 分页查询客户端顾问列表
     *
     * @param aiClientAdvisor 查询条件
     * @return 分页结果
     */
    @RequestMapping(value = "queryClientAdvisorList", method = RequestMethod.POST)
    public ResponseEntity<List<AiClientAdvisor>> queryClientAdvisorList(@RequestBody AiClientAdvisor aiClientAdvisor) {
        try {
            List<AiClientAdvisor> aiClientAdvisorList = aiClientAdvisorDao.queryClientAdvisorList(aiClientAdvisor);
            return ResponseEntity.ok(aiClientAdvisorList);
        } catch (Exception e) {
            log.error("查询客户端顾问列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID查询客户端顾问
     *
     * @param id 客户端顾问ID
     * @return 客户端顾问
     */
    @RequestMapping(value = "queryClientAdvisorById", method = RequestMethod.GET)
    public ResponseEntity<AiClientAdvisor> queryClientAdvisorById(@RequestParam("id") Long id) {
        try {
            AiClientAdvisor aiClientAdvisor = aiClientAdvisorDao.queryAdvisorConfigById(id);
            return ResponseEntity.ok(aiClientAdvisor);
        } catch (Exception e) {
            log.error("查询客户端顾问异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 新增客户端顾问
     *
     * @param aiClientAdvisor 客户端顾问
     * @return 结果
     */
    @RequestMapping(value = "addClientAdvisor", method = RequestMethod.POST)
    public ResponseEntity<Boolean> addClientAdvisor(@RequestBody AiClientAdvisor aiClientAdvisor) {
        try {
            aiClientAdvisor.setCreateTime(new Date());
            aiClientAdvisor.setUpdateTime(new Date());
            int count = aiClientAdvisorDao.insert(aiClientAdvisor);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("新增客户端顾问异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新客户端顾问
     *
     * @param aiClientAdvisor 客户端顾问
     * @return 结果
     */
    @RequestMapping(value = "updateClientAdvisor", method = RequestMethod.POST)
    public ResponseEntity<Boolean> updateClientAdvisor(@RequestBody AiClientAdvisor aiClientAdvisor) {
        try {
            aiClientAdvisor.setUpdateTime(new Date());
            int count = aiClientAdvisorDao.update(aiClientAdvisor);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("更新客户端顾问异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除客户端顾问
     *
     * @param id 客户端顾问ID
     * @return 结果
     */
    @RequestMapping(value = "deleteClientAdvisor", method = RequestMethod.GET)
    public ResponseEntity<Boolean> deleteClientAdvisor(@RequestParam("id") Long id) {
        try {
            int count = aiClientAdvisorDao.deleteById(id);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("删除客户端顾问异常", e);
            return ResponseEntity.status(500).build();
        }
    }
}
