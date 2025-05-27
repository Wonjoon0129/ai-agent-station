package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiClientToolConfig;

import java.util.List;

/**
 * @ClassName IAiClientToolConfigDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/20 19:17
 * @Version 1.0
 */
@Mapper
public interface IAiClientToolConfigDao {

    List<AiClientToolConfig> queryToolConfigByClientIds(List<Long> clientIdList);

    List<AiClientToolConfig> queryToolConfigList(AiClientToolConfig aiClientToolConfig);

    AiClientToolConfig queryToolConfigById(Long id);

    List<AiClientToolConfig> queryToolConfigByClientId(Long clientId);

    int insert(AiClientToolConfig aiClientToolConfig);

    int update(AiClientToolConfig aiClientToolConfig);

    int deleteById(Long id);
}
