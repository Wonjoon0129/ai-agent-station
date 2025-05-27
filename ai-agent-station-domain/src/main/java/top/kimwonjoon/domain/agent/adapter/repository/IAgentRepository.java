package top.kimwonjoon.domain.agent.adapter.repository;


import org.redisson.api.RList;
import top.kimwonjoon.domain.agent.model.valobj.*;

import java.util.List;
import java.util.Map;

/**
 * 仓储服务
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-02 14:15
 */
public interface IAgentRepository {

    List<AiClientModelVO> queryAiClientModelVOListByClientIds(List<Long> clientIdList);

    List<AiClientToolMcpVO> queryAiClientToolMcpVOListByClientIds(List<Long> clientIdList);

    List<AiClientAdvisorVO> queryAdvisorConfigByClientIds(List<Long> clientIdList);

    Map<Long, AiClientSystemPromptVO> querySystemPromptConfigByClientIds(List<Long> clientIdList);

    List<AiClientVO> queryAiClientByClientIds(List<Long> clientIdList);

    List<Long> queryAiClientIdsByAiAgentId(Long aiAgentId);

    Long queryAiClientModelIdByAgentId(Long aiAgentId);

    String queryRagKnowledgeTag(Long ragId);

    void createTagOrder(AiRagOrderVO aiRagOrderVO);

    List<Long> queryAiClientIds();

    List<AiAgentTaskScheduleVO> queryAllValidTaskSchedule();

    List<Long> queryAllInvalidTaskScheduleIds();

    List<AiClientModelVO> queryAiClientModelVOList();

}
