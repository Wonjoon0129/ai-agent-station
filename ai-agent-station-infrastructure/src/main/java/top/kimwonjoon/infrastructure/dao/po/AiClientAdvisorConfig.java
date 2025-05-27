package top.kimwonjoon.infrastructure.dao.po;

import top.kimwonjoon.infrastructure.dao.po.base.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 客户端-顾问关联表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AiClientAdvisorConfig extends Page {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 客户端ID
     */
    private Long clientId;

    /**
     * 顾问ID
     */
    private Long advisorId;

    /**
     * 创建时间
     */
    private Date createTime;
}