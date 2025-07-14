package top.kimwonjoon.domain.agent.service.chat;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import top.kimwonjoon.domain.agent.adapter.repository.IAgentRepository;
import top.kimwonjoon.domain.agent.model.valobj.AiAgentClientVO;
import top.kimwonjoon.domain.agent.service.chat.node.ClientNode;

import java.util.*;

/**
 * @ClassName ClientAssemblyService
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/6/1 20:29
 * @Version 1.0
 */

@Service
public class ClientAssemblyService {

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private IAgentRepository agentRepository;

    /**
     * 根据Agent ID组装客户端流程节点网络。
     * @param agentId 智能体ID
     * @return 流程的起始节点列表 (没有被其他节点指向的节点)
     */
    public ClientNode assembleClientFlow(Long agentId) {

        Long aiClientId = agentRepository.queryHeadClientByAgentId(agentId);
        List<AiAgentClientVO> relations =  agentRepository.queryAgentClientConfigByAgentId(agentId);

        if (relations == null || relations.isEmpty()) {
            return new ClientNode(aiClientId,applicationContext.getBean("ChatClient_" + aiClientId, ChatClient.class));
        }

        Map<Long, ClientNode> clientNodeMap = new HashMap<>();
        Set<Long> allClientIdsInFlow = new HashSet<>();
        relations.forEach(r -> {
            allClientIdsInFlow.add(r.getClientIdFrom());
            if (r.getClientIdTo() != null) {
                allClientIdsInFlow.add(r.getClientIdTo());
            }
        });

        // 初始化所有节点
        for (Long clientId : allClientIdsInFlow) {
            try {
                // 假设Bean的名称规则为 "clientBeanPrefix" + clientId
                // 您需要根据实际的Bean命名规则调整
                String beanName = "ChatClient_" + clientId;
                ChatClient clientBean = applicationContext.getBean(beanName, ChatClient.class);
                clientNodeMap.put(clientId, new ClientNode(clientId, clientBean));
            } catch (Exception e) {
                // 处理Bean未找到或类型不匹配的异常
                // log.error("Failed to get or cast client bean for ID: {}", clientId, e);
                // 可以选择抛出异常或跳过此节点
                throw new RuntimeException("Failed to initialize client bean: " + clientId, e);

            }

        }

        // 构建条件转换关系
        for (AiAgentClientVO relation : relations) {
            ClientNode parentNode = clientNodeMap.get(relation.getClientIdFrom());
            ClientNode childNode = null;
            if (relation.getClientIdTo() != null) {
                childNode = clientNodeMap.get(relation.getClientIdTo());
            }
            parentNode.addConditionalChild(relation.getCondition(), childNode);

        }


        return clientNodeMap.get(aiClientId);
    }
}
