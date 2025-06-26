package top.kimwonjoon.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;
import top.kimwonjoon.domain.agent.model.entity.AiAgentEngineStarterEntity;
import top.kimwonjoon.domain.agent.model.valobj.AiClientSystemPromptVO;
import top.kimwonjoon.domain.agent.model.valobj.AiClientVO;
import top.kimwonjoon.domain.agent.service.armory.AbstractArmorySupport;
import top.kimwonjoon.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import top.kimwonjoon.domain.agent.service.armory.factory.element.RedisChatMemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AiClientNode extends AbstractArmorySupport {

    @Override
    protected String doApply(AiAgentEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，对话模型节点 {}", JSON.toJSONString(requestParameter));

        List<AiClientVO> aiClientVOList = dynamicContext.getValue("aiClientList");
        Map<Long, AiClientSystemPromptVO> aiClientSystemPromptVOMap = dynamicContext.getValue("aiSystemPromptConfig");

        for (AiClientVO aiClientVO : aiClientVOList) {
            // 1. 预设话术
            String defaultSystem = "AI 智能体";
            AiClientSystemPromptVO systemPrompt = aiClientSystemPromptVOMap.get(aiClientVO.getSystemPromptId());
            if (null != systemPrompt) {
                defaultSystem = systemPrompt.getPromptContent();
            }

            // 2. chatModel
            ChatModel chatModel = getBean(aiClientVO.getModelBeanName());

            // 3. ToolCallbackProvider
            List<McpSyncClient> mcpSyncClients = new ArrayList<>();
            List<String> mcpBeanNameList = aiClientVO.getMcpBeanNameList();
            for (String mcpBeanName : mcpBeanNameList) {
                mcpSyncClients.add(getBean(mcpBeanName));
            }

            // 4. Advisor
            List<Advisor> advisors = new ArrayList<>();
            List<String> advisorBeanNameList = aiClientVO.getAdvisorBeanNameList();
            for (String advisorBeanName : advisorBeanNameList) {
                advisors.add(getBean(advisorBeanName));
            }

            Advisor[] advisorArray = advisors.toArray(new Advisor[]{});

            // 5. 构建对话客户端
            ChatClient chatClient = ChatClient.builder(chatModel)
                    .defaultSystem(defaultSystem)
                    .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients.toArray(new McpSyncClient[]{})))
                    .defaultAdvisors(advisorArray)
                    .build();

            registerBean(beanName(aiClientVO.getClientId()), ChatClient.class, chatClient);
        }

        return router(requestParameter, dynamicContext);

    }

    @Override
    public StrategyHandler<AiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(AiAgentEngineStarterEntity aiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }

    @Override
    protected String beanName(Long id) {
        return "ChatClient_" + id;
    }
}
