package top.kimwonjoon.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Component;
import top.kimwonjoon.domain.agent.model.entity.AiAgentEngineStarterEntity;
import top.kimwonjoon.domain.agent.model.valobj.AiClientAdvisorVO;
import top.kimwonjoon.domain.agent.model.valobj.AiClientModelVO;
import top.kimwonjoon.domain.agent.model.valobj.enums.AiAgentEnumVO;
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

    ChatModel chatModel;



    @Override
    protected String doApply(AiAgentEngineStarterEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 构建，advisor 顾问节点 {}", JSON.toJSONString(requestParameter));

        List<AiClientAdvisorVO> aiClientAdvisorList = dynamicContext.getValue("aiClientAdvisorList");
        List<AiClientModelVO> aiClientModelList = dynamicContext.getValue("aiClientModelList");
        AiClientModelVO aiClientModelVO = aiClientModelList.get(0);
        this.chatModel=getBean(AiAgentEnumVO.AI_CLIENT_MODEL.getBeanNameTag() + aiClientModelVO.getId());

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
        return AiAgentEnumVO.AI_CLIENT_ADVISOR.getBeanNameTag() + id;
    }

    @Override
    public StrategyHandler<AiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(AiAgentEngineStarterEntity aiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientNode;
    }


    protected Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO) {
        String advisorType = aiClientAdvisorVO.getAdvisorType();
        switch (advisorType) {
            case "ChatMemory" -> {
                return PromptChatMemoryAdvisor.builder(chatMemory)
                        .order(2)
                        .build();
            }
            case "RagAnswer" -> {
                VectorStore vectorStore = createVectorStore(aiClientAdvisorVO);
                AiClientAdvisorVO.RagAnswer ragAnswer = aiClientAdvisorVO.getRagAnswer();
                RagQueryTransformer ragQueryTransformer= new RagQueryTransformer("question_answer_context",chatModel);
                return new RagAnswerAdvisor(vectorStore, SearchRequest.builder()
                        .topK(ragAnswer.getTopK())
                        .similarityThreshold(ragAnswer.getSimilarityThreshold())
                        .build(),ragQueryTransformer);

            }
        }

        throw new RuntimeException("err! advisorType " + advisorType + " not exist!");
    }

    public VectorStore createVectorStore(AiClientAdvisorVO aiClientAdvisorVO) {
        EmbeddingModel embeddingModel = getBean(AiAgentEnumVO.AI_CLIENT_EMBEDDING_MODEL.getBeanNameTag() + aiClientAdvisorVO.getEmbeddingModelId());
        return PgVectorStore.builder(getBean(AiAgentEnumVO.DATA_BASE_DRIVE.getBeanNameTag()+aiClientAdvisorVO.getDatabaseId()), embeddingModel)
                .dimensions(768)
                .vectorTableName(aiClientAdvisorVO.getAdvisorName())
                .build();
    }
}
