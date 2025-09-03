package top.kimwonjoon.trigger.http.admin;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IAiClientSystemPromptDao;
import top.kimwonjoon.infrastructure.dao.po.AiClientSystemPrompt;

import java.util.Date;
import java.util.List;

/**
 * 系统提示词管理服务
 *
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-06 15:55
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/admin/client/system/prompt/")
public class AiAdminClientSystemPromptController {

    @Resource
    private IAiClientSystemPromptDao aiClientSystemPromptDao;

    /**
     * 分页查询系统提示词列表
     *
     * @param aiClientSystemPrompt 查询条件
     * @return 分页结果
     */
    @RequestMapping(value = "querySystemPromptList", method = RequestMethod.POST)
    public ResponseEntity<List<AiClientSystemPrompt>> querySystemPromptList(@RequestBody AiClientSystemPrompt aiClientSystemPrompt) {
        try {
            List<AiClientSystemPrompt> systemPromptList = aiClientSystemPromptDao.querySystemPromptList(aiClientSystemPrompt);
            return ResponseEntity.ok(systemPromptList);
        } catch (Exception e) {
            log.error("查询系统提示词列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    @RequestMapping(value = "queryAllSystemPromptConfig", method = RequestMethod.POST)
    public ResponseEntity<List<AiClientSystemPrompt>> queryAllSystemPromptConfig() {
        try {
            List<AiClientSystemPrompt> systemPromptList = aiClientSystemPromptDao.queryAllSystemPromptConfig();
            return ResponseEntity.ok(systemPromptList);
        } catch (Exception e) {
            log.error("查询系统提示词列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID查询系统提示词
     *
     * @param id 系统提示词ID
     * @return 系统提示词
     */
    @RequestMapping(value = "querySystemPromptById", method = RequestMethod.GET)
    public ResponseEntity<AiClientSystemPrompt> querySystemPromptById(@RequestParam("id") Long id) {
        try {
            AiClientSystemPrompt systemPrompt = aiClientSystemPromptDao.querySystemPromptConfigById(id);
            return ResponseEntity.ok(systemPrompt);
        } catch (Exception e) {
            log.error("查询系统提示词异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 新增系统提示词
     *
     * @param aiClientSystemPrompt 系统提示词
     * @return 结果
     */
    @RequestMapping(value = "addSystemPrompt", method = RequestMethod.POST)
    public ResponseEntity<Boolean> addSystemPrompt(@RequestBody AiClientSystemPrompt aiClientSystemPrompt) {
        try {
            int count = aiClientSystemPromptDao.insert(aiClientSystemPrompt);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("新增系统提示词异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新系统提示词
     *
     * @param aiClientSystemPrompt 系统提示词
     * @return 结果
     */
    @RequestMapping(value = "updateSystemPrompt", method = RequestMethod.POST)
    public ResponseEntity<Boolean> updateSystemPrompt(@RequestBody AiClientSystemPrompt aiClientSystemPrompt) {
        try {
            int count = aiClientSystemPromptDao.update(aiClientSystemPrompt);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("更新系统提示词异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除系统提示词
     *
     * @param id 系统提示词ID
     * @return 结果
     */
    @RequestMapping(value = "deleteSystemPrompt", method = RequestMethod.GET)
    public ResponseEntity<Boolean> deleteSystemPrompt(@RequestParam("id") Long id) {
        try {
            int count = aiClientSystemPromptDao.deleteById(id);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("删除系统提示词异常", e);
            return ResponseEntity.status(500).build();
        }
    }
}
