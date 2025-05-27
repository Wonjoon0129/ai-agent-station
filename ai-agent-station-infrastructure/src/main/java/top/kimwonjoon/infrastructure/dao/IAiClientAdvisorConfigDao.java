package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.kimwonjoon.infrastructure.dao.po.AiClientAdvisorConfig;

import java.util.List;

/**
 * @ClassName IAiClientAdvisorConfigDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/20 19:38
 * @Version 1.0
 */

@Mapper
public interface IAiClientAdvisorConfigDao {
    List<AiClientAdvisorConfig> queryClientAdvisorConfigByClientIds(List<Long> clientIdList);

    List<AiClientAdvisorConfig> queryClientAdvisorConfigList(AiClientAdvisorConfig aiClientAdvisorConfig);

    AiClientAdvisorConfig queryClientAdvisorConfigById(Long id);

    List<AiClientAdvisorConfig> queryClientAdvisorConfigByClientId(Long clientId);

    int insert(AiClientAdvisorConfig aiClientAdvisorConfig);

    int update(AiClientAdvisorConfig aiClientAdvisorConfig);

    int deleteById(Long id);

    int deleteByClientIdAndAdvisorId(@Param("clientId") Long clientId, @Param("advisorId") Long advisorId);

}
