package top.kimwonjoon.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;
import top.kimwonjoon.domain.agent.model.entity.AiAgentEngineStarterEntity;
import top.kimwonjoon.domain.agent.model.valobj.AiClientModelVO;
import top.kimwonjoon.domain.agent.service.armory.AbstractArmorySupport;
import top.kimwonjoon.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AiClientModelNode
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/21 09:04
 * @Version 1.0
 */
@Slf4j
@Component
public class AiClientModelNode extends AbstractArmorySupport {
    @Resource
    private AiClientNode aiClientNode;
    @Override
    protected String doApply(AiAgentEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，客户端构建节点 {}", JSON.toJSONString(requestParameter));

        List<AiClientModelVO> aiClientModelList = dynamicContext.getValue("aiClientModelList");

        if (aiClientModelList == null || aiClientModelList.isEmpty()) {
            log.warn("没有可用的AI客户端模型配置");
            return null;
        }

        // 遍历模型列表，为每个模型创建对应的Bean
        for (AiClientModelVO modelVO : aiClientModelList) {
            // 创建OpenAiChatModel对象
            ChatModel chatModel = createOpenAiChatModel(modelVO);
            // 使用父类的通用注册方法
            registerBean(beanName(modelVO.getId()), ChatModel.class, chatModel);
        }

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<AiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(AiAgentEngineStarterEntity aiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientNode;
    }


    @Override
    protected String beanName(Long id) {
        return "AiClientModel_" + id;
    }

    /**
     * 创建OpenAiChatModel对象
     *
     * @param modelVO 模型配置值对象
     * @return OpenAiChatModel实例
     */
    protected ChatModel createOpenAiChatModel(AiClientModelVO modelVO) {
        String modelType=modelVO.getModelType();
        switch (modelType){
            case "openai"->{
                // 构建OpenAiApi
                OpenAiApi openAiApi = OpenAiApi.builder()
                        .baseUrl(modelVO.getBaseUrl())
                        .apiKey(modelVO.getApiKey())
                        .completionsPath(modelVO.getCompletionsPath())
                        .embeddingsPath(modelVO.getEmbeddingsPath())
                        .build();

                List<McpSyncClient> mcpSyncClients = new ArrayList<>();
                List<AiClientModelVO.AiClientModelToolConfigVO> toolConfigs = modelVO.getAiClientModelToolConfigs();
                if (null != toolConfigs && !toolConfigs.isEmpty()) {

                    for (AiClientModelVO.AiClientModelToolConfigVO toolConfig : toolConfigs) {
                        Long toolId = toolConfig.getToolId();
                        McpSyncClient mcpSyncClient = getBean("AiClientToolMcp_" + toolId);
                        mcpSyncClients.add(mcpSyncClient);
                    }
                }

                // 构建OpenAiChatModel
                return OpenAiChatModel.builder()
                        .openAiApi(openAiApi)
                        .defaultOptions(OpenAiChatOptions.builder()
                                .model(modelVO.getModelVersion())
                                .toolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients).getToolCallbacks())
                                .build())
                        .build();
            }
            case "ollama"->{
                OllamaApi ollamaApi=OllamaApi.builder()
                        .baseUrl(modelVO.getBaseUrl())
                        .build();

                List<McpSyncClient> mcpSyncClients = new ArrayList<>();
                List<AiClientModelVO.AiClientModelToolConfigVO> toolConfigs = modelVO.getAiClientModelToolConfigs();
                if (null != toolConfigs && !toolConfigs.isEmpty()) {

                    for (AiClientModelVO.AiClientModelToolConfigVO toolConfig : toolConfigs) {
                        Long toolId = toolConfig.getToolId();
                        McpSyncClient mcpSyncClient = getBean("AiClientToolMcp_" + toolId);
                        mcpSyncClients.add(mcpSyncClient);
                    }
                }

                return OllamaChatModel.builder()
                        .ollamaApi(ollamaApi)
                        .defaultOptions(OllamaOptions.builder()
                                .model(modelVO.getModelVersion())
                                .toolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients).getToolCallbacks())
                                .build())
                        .build();

            }
        }
        throw new RuntimeException("err! transportType " + modelType + " not exist!");
    }


}



