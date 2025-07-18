package top.kimwonjoon.infrastructure.dao.po;

import top.kimwonjoon.infrastructure.dao.po.base.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 知识库配置表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AiRagOrder extends Page {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 知识库名称
     */
    private String ragName;

    /**
     * 知识标签
     */
    private String knowledgeTag;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * multimedia
     */
    private Integer multimedia;


    private Integer headClientId;

    /**
     * 更新时间
     */
    private Date updateTime;

}