package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiClientAdvisor;

import java.util.List;

/**
 * @ClassName IAiAdvisorDap
 * @Description
 * @Author @kimwonjoon
 * @Date 2025. 5. 20. 00:35
 * @Version 1.0
 */
@Mapper
public interface IAiClientAdvisorDao {
    List<AiClientAdvisor> queryAdvisorConfigByClientIds(List<Long> clientIdList);

    List<AiClientAdvisor> queryClientAdvisorList(AiClientAdvisor aiClientAdvisor);

    AiClientAdvisor queryAdvisorConfigById(Long id);

    int insert(AiClientAdvisor aiClientAdvisor);

    int update(AiClientAdvisor aiClientAdvisor);

    int deleteById(Long id);
}
