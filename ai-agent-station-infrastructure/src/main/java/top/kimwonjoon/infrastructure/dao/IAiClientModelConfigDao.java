package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.domain.agent.model.valobj.AiClientModelVO;
import top.kimwonjoon.infrastructure.dao.po.AiClientModelConfig;

import java.util.List;

/**
 * @ClassName IAiClientModelConfigDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025. 5. 20. 18:41
 * @Version 1.0
 */
@Mapper
public interface IAiClientModelConfigDao {

    List<AiClientModelConfig> queryModelConfigByClientIds(List<Long> clientIdList);

    Long queryAiClientModelIdByAgentId(Long aiAgentId);

    List<AiClientModelConfig> queryAllModelConfig();

    AiClientModelConfig queryModelConfigById(Long id);

    AiClientModelConfig queryModelConfigByClientId(Long clientId);

    List<AiClientModelConfig> queryModelConfigByModelId(Long modelId);

    int insert(AiClientModelConfig aiClientModelConfig);

    int update(AiClientModelConfig aiClientModelConfig);

    int deleteById(Long id);
}
