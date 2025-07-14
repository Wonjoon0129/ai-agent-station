package top.kimwonjoon.infrastructure.dao.po;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.kimwonjoon.infrastructure.dao.po.base.Page;

import java.util.Date;

/**
 * @ClassName AiVectorDatabase
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/7/14 10:07
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AiVectorDatabase extends Page {

    /**
     * 主键ID
     */
    private Long id;
    /**
     * 向量库名称
     */
    private String dbName;
    /**
     * 向量库种类
     */
    private String dbType;
    /**
     * url
     */
    private String url;
    /**
     * port
     */
    private String port;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String password;
    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;
    /**
     * 额外参数
     */
    private String extra;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
