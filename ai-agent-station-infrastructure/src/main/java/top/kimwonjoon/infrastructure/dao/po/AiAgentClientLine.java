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
     * 客户端from
     */
    private Long clientIdFrom;
    /**
     * 客户端 to
     */
    private Long clientIdTo;
    /**
     * 条件
     */
    private  String condition;
    /**
     * promt传递模版
     */
    private String stepPrompt;

    /**
     * 创建时间
     */
    private Date createTime;

}