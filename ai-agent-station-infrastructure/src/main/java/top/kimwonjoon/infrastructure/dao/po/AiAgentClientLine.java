package top.kimwonjoon.infrastructure.dao.po;

import top.kimwonjoon.infrastructure.dao.po.base.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 智能体-客户端关联表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AiAgentClientLine extends Page {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 智能体ID
     */
    private Long agentId;

    /**
     * 客户端ID
     */
    private Long clientIdFrom;

    private Long clientIdTo;

    private  String condition;

    /**
     * 创建时间
     */
    private Date createTime;

}