package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiAgentClient;

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


    List<AiAgentClient> queryAgentClientList(AiAgentClient aiAgentClient);

    AiAgentClient queryAgentClientConfigById(Long id);

    List<AiAgentClient> queryAgentClientConfigByAgentId(Long agentId);

    List<AiAgentClient> queryAgentClientConfigByClientId(Long clientId);

    int insert(AiAgentClient aiAgentClient);

    int update(AiAgentClient aiAgentClient);

    int deleteById(Long id);
}
