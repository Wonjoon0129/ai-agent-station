package top.kimwonjoon.trigger.http.admin;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IAiClientToolMcpDao;
import top.kimwonjoon.infrastructure.dao.po.AiClientToolMcp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MCP工具管理服务
 * 2025-05-06 16:01
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/admin/client/tool/mcp/")
public class AiAdminClientToolMcpController {

    @Resource
    private IAiClientToolMcpDao aiClientToolMcpDao;

    /**
     * 分页查询MCP配置列表
     *
     * @param aiClientToolMcp 查询条件
     * @return 分页结果
     */
    @RequestMapping(value = "queryMcpList", method = RequestMethod.POST)
    public ResponseEntity<List<AiClientToolMcp>> queryMcpList(@RequestBody AiClientToolMcp aiClientToolMcp) {
        try {
            List<AiClientToolMcp> mcpList = aiClientToolMcpDao.queryMcpList(aiClientToolMcp);
            return ResponseEntity.ok(mcpList);
        } catch (Exception e) {
            log.error("查询MCP配置列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID查询MCP配置
     *
     * @param id MCP配置ID
     * @return MCP配置
     */
    @RequestMapping(value = "queryMcpById", method = RequestMethod.GET)
    public ResponseEntity<AiClientToolMcp> queryMcpById(@RequestParam("id") Long id) {
        try {
            AiClientToolMcp mcp = aiClientToolMcpDao.queryMcpConfigById(id);
            return ResponseEntity.ok(mcp);
        } catch (Exception e) {
            log.error("查询MCP配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 新增MCP配置
     *
     * @param aiClientToolMcp MCP配置
     * @return 结果
     */
    @RequestMapping(value = "addMcp", method = RequestMethod.POST)
    public ResponseEntity<Boolean> addMcp(@RequestBody AiClientToolMcp aiClientToolMcp) {
        try {
            int count = aiClientToolMcpDao.insert(aiClientToolMcp);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("新增MCP配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新MCP配置
     *
     * @param aiClientToolMcp MCP配置
     * @return 结果
     */
    @RequestMapping(value = "updateMcp", method = RequestMethod.POST)
    public ResponseEntity<Boolean> updateMcp(@RequestBody AiClientToolMcp aiClientToolMcp) {
        try {
            int count = aiClientToolMcpDao.update(aiClientToolMcp);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("更新MCP配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除MCP配置
     *
     * @param id MCP配置ID
     * @return 结果
     */
    @RequestMapping(value = "deleteMcp", method = RequestMethod.GET)
    public ResponseEntity<Boolean> deleteMcp(@RequestParam("id") Long id) {
        try {
            int count = aiClientToolMcpDao.deleteById(id);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("删除MCP配置异常", e);
            return ResponseEntity.status(500).build();
        }
    }
}
