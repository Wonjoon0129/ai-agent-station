package top.kimwonjoon.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @ClassName AiVectorDatabaseVO
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/7/14 11:06
 * @Version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiVectorDatabaseVO {

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


}
