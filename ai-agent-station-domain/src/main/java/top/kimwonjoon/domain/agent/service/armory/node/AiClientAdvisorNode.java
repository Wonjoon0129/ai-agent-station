package top.kimwonjoon.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Component;
import top.kimwonjoon.domain.agent.model.entity.AiAgentEngineStarterEntity;
import top.kimwonjoon.domain.agent.model.valobj.AiClientAdvisorVO;
import top.kimwonjoon.domain.agent.service.armory.AbstractArmorySupport;
import top.kimwonjoon.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import top.kimwonjoon.domain.agent.service.armory.factory.element.RagAnswerAdvisor;
import top.kimwonjoon.domain.agent.service.armory.factory.element.RagQueryTransformer;
import top.kimwonjoon.domain.agent.service.armory.factory.element.RedisChatMemory;

import java.util.List;

/**
 * @ClassName AiClientAdvisorNode
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/20 21:13
 * @Version 1.0
 */
@Slf4j
@Component
public class AiClientAdvisorNode extends AbstractArmorySupport {
    @Resource
    AiClientNode aiClientNode;

    @Resource
    RedisChatMemory chatMemory;



    @Override
    protected String doApply(AiAgentEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，advisor 顾问节点 {}", JSON.toJSONString(requestParameter));

        List<AiClientAdvisorVO> aiClientAdvisorList = dynamicContext.getValue("aiClientAdvisorList");
        if (aiClientAdvisorList == null || aiClientAdvisorList.isEmpty()) {
            log.warn("没有可用的AI客户端顾问（advisor）配置");
            return router(requestParameter, dynamicContext);
        }

        for (AiClientAdvisorVO aiClientAdvisorVO : aiClientAdvisorList) {
            // 构建顾问访问对象
            Advisor advisor = createAdvisor(aiClientAdvisorVO);
            // 注册Bean对象
            registerBean(beanName(aiClientAdvisorVO.getId()), Advisor.class, advisor);
        }
        return router(requestParameter, dynamicContext);
    }

    @Override
    protected String beanName(Long id) {
        return "AiClientAdvisor_" + id;
    }

    @Override
    public StrategyHandler<AiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(AiAgentEngineStarterEntity aiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientNode;
    }


    private Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO) {
        String advisorType = aiClientAdvisorVO.getAdvisorType();
        switch (advisorType) {
            case "ChatMemory" -> {
                return PromptChatMemoryAdvisor.builder(chatMemory)
//                        .order()
                        .build();
            }
            case "RagAnswer" -> {
                VectorStore vectorStore = createVectorStore(aiClientAdvisorVO);
                AiClientAdvisorVO.RagAnswer ragAnswer = aiClientAdvisorVO.getRagAnswer();
                RagQueryTransformer ragQueryTransformer= new RagQueryTransformer("question_answer_context");
                VectorStoreDocumentRetriever vectorStoreDocumentRetriever = new VectorStoreDocumentRetriever(vectorStore, 0.8,ragAnswer.getTopK(), null);
                return RagAnswerAdvisor.builder(vectorStore)
                        .queryTransformers(ragQueryTransformer)
                        .documentRetriever(vectorStoreDocumentRetriever)
                        .searchRequest(SearchRequest.builder()
                                .topK(ragAnswer.getTopK())
                                .filterExpression(ragAnswer.getFilterExpression())
                                .build())
//                        .order()
                        .build();
            }
        }

        throw new RuntimeException("err! advisorType " + advisorType + " not exist!");
    }

    public VectorStore createVectorStore(AiClientAdvisorVO aiClientAdvisorVO) {
        OllamaEmbeddingModel embeddingModel = getBean("AiClientEmbeddingModel_" + aiClientAdvisorVO.getEmbeddingModelId());
        return PgVectorStore.builder(getBean("DataBaseDrive_"+aiClientAdvisorVO.getDatabaseId()), embeddingModel)
                .vectorTableName(aiClientAdvisorVO.getAdvisorName())
                .build();
    }
}
