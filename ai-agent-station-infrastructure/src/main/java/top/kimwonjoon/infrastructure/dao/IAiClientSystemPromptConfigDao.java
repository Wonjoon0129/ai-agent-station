package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiClientSystemPromptConfig;

import java.util.List;

/**
 * @ClassName IAiClientSystemPromptConfigDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025. 5. 20. 18:28
 * @Version 1.0
 */
@Mapper
public interface IAiClientSystemPromptConfigDao {

    /**
     * 根据客户端ID列表查询系统提示词配置关联
     * @param clientIdList 客户端ID列表
     * @return 系统提示词配置关联列表
     */
    List<AiClientSystemPromptConfig> querySystemPromptConfigByClientIds(List<Long> clientIdList);
}
