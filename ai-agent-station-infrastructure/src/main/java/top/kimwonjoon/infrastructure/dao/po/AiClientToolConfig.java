package top.kimwonjoon.infrastructure.dao.po;

import top.kimwonjoon.infrastructure.dao.po.base.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 客户端-工具关联表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AiClientToolConfig extends Page {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 客户端ID
     */
    private Long clientId;

    /**
     * 工具类型(mcp/function call)
     */
    private String toolType;

    /**
     * 工具ID(MCP ID/function call ID)
     */
    private Long toolId;

    /**
     * 创建时间
     */
    private Date createTime;
}