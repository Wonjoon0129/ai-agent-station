package top.kimwonjoon.domain.agent.service.armory.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import top.kimwonjoon.domain.agent.model.entity.AiAgentEngineStarterEntity;
import top.kimwonjoon.domain.agent.service.armory.node.RootNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName DefaultArmoryStrategyFactory
 * @Description
 * @Author @kimwonjoon
 * @Date 2025. 5. 19. 10:35
 * @Version 1.0
 */
@Service
public class DefaultArmoryStrategyFactory {

    @Resource
    private ApplicationContext applicationContext;

    private final RootNode rootNode;

    public DefaultArmoryStrategyFactory(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    public StrategyHandler<AiAgentEngineStarterEntity, DynamicContext, String> strategyHandler() {
        return rootNode;
    }
    public ChatClient chatClient(Long clientId) {
        return (ChatClient) applicationContext.getBean("ChatClient_" + clientId);
    }

    public ChatModel chatModel(Long modelId) {
        return (ChatModel) applicationContext.getBean("AiClientModel_" + modelId);
    }

    public EmbeddingModel embeddingModel(Long modelId) {
        return (EmbeddingModel) applicationContext.getBean("AiClientEmbeddingModel_" + modelId);
    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private int level;

        private Map<String, Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key, T value) {
            dataObjects.put(key, value);
        }

        public <T> T getValue(String key) {
            return (T) dataObjects.get(key);
        }

    }



}
