package top.kimwonjoon.trigger.http.admin;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.kimwonjoon.infrastructure.dao.IAiRagOrderDao;
import top.kimwonjoon.infrastructure.dao.po.AiRagOrder;

import java.util.Date;
import java.util.List;

/**
 * RAG订单管理服务
 *
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-06 16:46
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/ai/admin/rag/")
public class AiAdminRagOrderController {

    @Resource
    private IAiRagOrderDao aiRagOrderDao;

    /**
     * 分页查询RAG订单列表
     *
     * @param aiRagOrder 查询条件
     * @return 分页结果
     */
    @RequestMapping(value = "queryRagOrderList", method = RequestMethod.POST)
    public ResponseEntity<List<AiRagOrder>> queryRagOrderList(@RequestBody AiRagOrder aiRagOrder) {
        try {
            List<AiRagOrder> ragOrderList = aiRagOrderDao.queryRagOrderList(aiRagOrder);
            return ResponseEntity.ok(ragOrderList);
        } catch (Exception e) {
            log.error("查询RAG订单列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    @RequestMapping(value = "queryAllValidRagOrder", method = RequestMethod.POST)
    public ResponseEntity<List<AiRagOrder>> queryAllValidRagOrder() {
        try {
            List<AiRagOrder> ragOrderList = aiRagOrderDao.queryAllValidRagOrder();
            return ResponseEntity.ok(ragOrderList);
        } catch (Exception e) {
            log.error("查询RAG订单列表异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID查询RAG订单
     *
     * @param id RAG订单ID
     * @return RAG订单
     */
    @RequestMapping(value = "queryRagOrderById", method = RequestMethod.GET)
    public ResponseEntity<AiRagOrder> queryRagOrderById(@RequestParam("id") Long id) {
        try {
            AiRagOrder ragOrder = aiRagOrderDao.queryRagOrderById(id);
            return ResponseEntity.ok(ragOrder);
        } catch (Exception e) {
            log.error("查询RAG订单异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 新增RAG订单
     *
     * @param aiRagOrder RAG订单
     * @return 结果
     */
    @RequestMapping(value = "addRagOrder", method = RequestMethod.POST)
    public ResponseEntity<Boolean> addRagOrder(@RequestBody AiRagOrder aiRagOrder) {
        try {
            aiRagOrder.setCreateTime(new Date());
            aiRagOrder.setUpdateTime(new Date());
            int count = aiRagOrderDao.insert(aiRagOrder);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("新增RAG订单异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新RAG订单
     *
     * @param aiRagOrder RAG订单
     * @return 结果
     */
    @RequestMapping(value = "updateRagOrder", method = RequestMethod.POST)
    public ResponseEntity<Boolean> updateRagOrder(@RequestBody AiRagOrder aiRagOrder) {
        try {
            aiRagOrder.setUpdateTime(new Date());
            int count = aiRagOrderDao.update(aiRagOrder);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("更新RAG订单异常", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除RAG订单
     *
     * @param id RAG订单ID
     * @return 结果
     */
    @RequestMapping(value = "deleteRagOrder", method = RequestMethod.GET)
    public ResponseEntity<Boolean> deleteRagOrder(@RequestParam("id") Long id) {
        try {
            int count = aiRagOrderDao.deleteById(id);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            log.error("删除RAG订单异常", e);
            return ResponseEntity.status(500).build();
        }
    }
}
