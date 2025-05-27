package top.kimwonjoon.domain.agent.service.preheat;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import top.kimwonjoon.domain.agent.adapter.repository.IAgentRepository;
import top.kimwonjoon.domain.agent.model.entity.AiAgentEngineStarterEntity;
import top.kimwonjoon.domain.agent.model.valobj.AiClientModelVO;
import top.kimwonjoon.domain.agent.service.IAiAgentPreheatService;
import top.kimwonjoon.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import top.kimwonjoon.domain.agent.service.armory.node.AiClientModelNode;

import java.util.Collections;
import java.util.List;

/**
 * 装配服务
 */
@Slf4j
@Service
public class AiAgentPreheatService extends AiClientModelNode implements IAiAgentPreheatService {

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;
    @Resource
    private IAgentRepository repository;
    @Resource
    AiClientModelNode  aiClientModelNode;

    @Override
    public void preheat() throws Exception {
        List<AiClientModelVO> aiClientModelList = repository.queryAiClientModelVOList();
        // 遍历模型列表，为每个模型创建对应的Bean
        for (AiClientModelVO modelVO : aiClientModelList) {
            // 创建OpenAiChatModel对象
            ChatModel chatModel = createOpenAiChatModel(modelVO);
            // 使用父类的通用注册方法
            registerBean(beanName(modelVO.getId()), ChatModel.class, chatModel);
        }

    }

    @Override
    public void preheat(Long aiClientId) throws Exception {

        StrategyHandler<AiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> handler = defaultArmoryStrategyFactory.strategyHandler();
        handler.apply(AiAgentEngineStarterEntity.builder()
                .clientIdList(Collections.singletonList(aiClientId))
                .build(), new DefaultArmoryStrategyFactory.DynamicContext());
    }

}
