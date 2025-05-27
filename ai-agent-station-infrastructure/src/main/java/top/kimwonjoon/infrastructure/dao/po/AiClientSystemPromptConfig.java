package top.kimwonjoon.infrastructure.dao.po;

import top.kimwonjoon.infrastructure.dao.po.base.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 系统提示词配置表映射
 *
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-05 10:50
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AiClientSystemPromptConfig extends Page {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 客户端ID
     */
    private Long clientId;

    /**
     * 系统提示词ID
     */
    private Long systemPromptId;

    /**
     * 创建时间
     */
    private Date createTime;

}
