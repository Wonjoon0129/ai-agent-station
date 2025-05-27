package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiAgent;

import java.util.List;

/**
 * @ClassName IAiAgentDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/22 22:49
 * @Version 1.0
 */
@Mapper
public interface IAiAgentDao {

    List<Long> queryClientIdsByAgentId(Long aiAgentId);

    List<Long> queryValidClientIds();
    /**
     * 根据条件查询智能体列表
     * @param aiAgent 查询条件
     * @return 智能体列表
     */
    List<AiAgent> queryAiAgentList(AiAgent aiAgent);

    List<AiAgent> queryAllAgentConfigByChannel(String channel);
    /**
     * 根据ID查询智能体配置
     * @param id 智能体配置ID
     * @return 智能体配置
     */
    AiAgent queryAgentConfigById(Long id);

    int insert(AiAgent aiAgent);

    int update(AiAgent aiAgent);

    int deleteById(Long id);
}
