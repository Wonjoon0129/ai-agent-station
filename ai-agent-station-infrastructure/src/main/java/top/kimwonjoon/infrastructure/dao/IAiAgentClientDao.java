package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiAgentClientLine;

import java.util.List;

/**
 * @ClassName IAiAgentClientDao
 * @Description智能体-客户端关联表DAO接口
 * @Author @kimwonjoon
 * @Date 2025/5/23 11:16
 * @Version 1.0
 */
@Mapper
public interface IAiAgentClientDao {


    List<AiAgentClientLine> queryAgentClientList(AiAgentClientLine aiAgentClientLine);

    AiAgentClientLine queryAgentClientConfigById(Long id);

    List<AiAgentClientLine> queryAgentClientConfigByAgentId(Long agentId);

    List<AiAgentClientLine> queryAgentClientConfigByClientId(Long clientId);

    int insert(AiAgentClientLine aiAgentClientLine);

    int update(AiAgentClientLine aiAgentClientLine);

    int deleteById(Long id);
}
