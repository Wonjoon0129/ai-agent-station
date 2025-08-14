package top.kimwonjoon.domain.agent.service.preheat;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.Model;
import org.springframework.stereotype.Service;
import top.kimwonjoon.domain.agent.adapter.repository.IAgentRepository;
import top.kimwonjoon.domain.agent.model.entity.AiAgentEngineStarterEntity;
import top.kimwonjoon.domain.agent.model.valobj.AiClientModelVO;
import top.kimwonjoon.domain.agent.model.valobj.AiVectorDatabaseVO;
import top.kimwonjoon.domain.agent.model.valobj.enums.AiAgentEnumVO;
import top.kimwonjoon.domain.agent.service.IAiAgentPreheatService;
import top.kimwonjoon.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import top.kimwonjoon.domain.agent.service.armory.node.AiClientModelNode;
import top.kimwonjoon.domain.agent.service.armory.node.VectorDatabaseNode;

import java.util.List;

/**
 * 装配服务
 */
@Slf4j
@Service
public class AiAgentPreheatService implements IAiAgentPreheatService {

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;
    @Resource
    private IAgentRepository repository;
    @Resource
    VectorDatabaseNode vectorDatabaseNode;
    @Resource
    AiClientModelNode aiClientModelNode;

    @Override
    public void preheat() throws Exception {
        List<AiClientModelVO> aiClientModelList = repository.queryAiClientModelVOList();

        // 遍历模型列表，为每个模型创建对应的Bean
        for (AiClientModelVO modelVO : aiClientModelList) {
            Model openAiChatModel = aiClientModelNode.createOpenAiChatModel(modelVO);
            aiClientModelNode.registerBean(AiAgentEnumVO.AI_CLIENT_EMBEDDING_MODEL.getBeanNameTag()+modelVO.getId(), Model.class, openAiChatModel);
        }

        List<AiVectorDatabaseVO> aiVectorDatabaseVOList=repository.queryAiVectorDatabaseVO();
        for(AiVectorDatabaseVO aiVectorDatabaseVO : aiVectorDatabaseVOList){
            vectorDatabaseNode.createVectorDatabaseDrive(aiVectorDatabaseVO);
        }
    }

    @Override
    public void preheat(Long agiAgentId) throws Exception {

        List<Long> list = repository.queryAiClientIdsByAiAgentId(agiAgentId);
        StrategyHandler<AiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> handler = defaultArmoryStrategyFactory.strategyHandler();
        handler.apply(AiAgentEngineStarterEntity.builder()
                .clientIdList(list)
                .build(), new DefaultArmoryStrategyFactory.DynamicContext());
    }

}
