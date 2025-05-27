package top.kimwonjoon.infrastructure.dao.po;

import top.kimwonjoon.infrastructure.dao.po.base.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * AI客户端模型工具配置
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AiClientModelToolConfig extends Page {
    
    /** 主键ID */
    private Integer id;
    /** 模型ID */
    private Long modelId;
    /** 工具类型(mcp/function call) */
    private String toolType;
    /** MCP ID/ function call ID */
    private Long toolId;
    /** 创建时间 */
    private Date createTime;

}