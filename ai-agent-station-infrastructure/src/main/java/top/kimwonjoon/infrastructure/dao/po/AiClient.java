package top.kimwonjoon.infrastructure.dao.po;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.kimwonjoon.infrastructure.dao.po.base.Page;

import java.util.Date;

/**
 *@ClassName AiClient
 *@Description  
 *@Author @kimwonjoon
 *@Date 2025/5/26 15:29
 *@Version 1.0
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class AiClient extends Page {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 智能体名称
     */
    private String clientName;

    /**
     * 描述
     */
    private String description;


    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}