package top.kimwonjoon.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.model.Model;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;
import top.kimwonjoon.domain.agent.model.entity.AiAgentEngineStarterEntity;
import top.kimwonjoon.domain.agent.model.valobj.AiClientModelVO;
import top.kimwonjoon.domain.agent.model.valobj.enums.AiAgentEnumVO;
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
    private VectorDatabaseNode vectorDatabaseNode;

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
            ChatModel chatModel =(ChatModel) createOpenAiChatModel(modelVO);
            // 使用父类的通用注册方法
            registerBean(beanName(modelVO.getId()), ChatModel.class, chatModel);
        }

        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<AiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(AiAgentEngineStarterEntity aiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return vectorDatabaseNode;
    }


    @Override
    protected String beanName(Long id) {
        return AiAgentEnumVO.AI_CLIENT_MODEL.getBeanNameTag() + id;
    }

    /**
     * 创建OpenAiChatModel对象
     *
     * @param modelVO 模型配置值对象
     * @return OpenAiChatModel实例
     */
    public Model createOpenAiChatModel(AiClientModelVO modelVO) {
        String modelApiType=modelVO.getModelApiType();
        switch (modelApiType){
            case "openai"->{
                // 构建OpenAiApi
                OpenAiApi openAiApi = OpenAiApi.builder()
                        .baseUrl(modelVO.getBaseUrl())
                        .apiKey(modelVO.getApiKey())
                        .completionsPath(modelVO.getCompletionsPath())
                        .embeddingsPath(modelVO.getEmbeddingsPath())
                        .build();

                if(modelVO.getModelType().equals("2")){
                    return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED,OpenAiEmbeddingOptions.builder().dimensions(768).model(modelVO.getModelVersion()).build());
                }
                List<McpSyncClient> mcpSyncClients = new ArrayList<>();
                List<AiClientModelVO.AiClientModelToolConfigVO> toolConfigs = modelVO.getAiClientModelToolConfigs();
                if (null != toolConfigs && !toolConfigs.isEmpty()) {

                    for (AiClientModelVO.AiClientModelToolConfigVO toolConfig : toolConfigs) {
                        Long toolId = toolConfig.getToolId();
                        McpSyncClient mcpSyncClient = getBean(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanNameTag() + toolId);
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
            case "ollama"-> {
                OllamaApi ollamaApi = OllamaApi.builder()
                        .baseUrl(modelVO.getBaseUrl())
                        .build();

                if(modelVO.getModelType().equals("2")){
                    return OllamaEmbeddingModel
                            .builder()
                            .ollamaApi(ollamaApi)
                            .defaultOptions(OllamaOptions.builder().model(modelVO.getModelVersion()).build())
                            .build();
                }
                List<McpSyncClient> mcpSyncClients = new ArrayList<>();
                List<AiClientModelVO.AiClientModelToolConfigVO> toolConfigs = modelVO.getAiClientModelToolConfigs();
                if (null != toolConfigs && !toolConfigs.isEmpty()) {

                    for (AiClientModelVO.AiClientModelToolConfigVO toolConfig : toolConfigs) {
                        Long toolId = toolConfig.getToolId();
                        McpSyncClient mcpSyncClient = getBean(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanNameTag() + toolId);
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
        throw new RuntimeException("err! transportType " + modelApiType + " not exist!");
    }


}



