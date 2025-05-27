package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiClientSystemPrompt;

import java.util.List;

/**
 * @ClassName IAiClientSystemPromptDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025. 5. 20. 00:49
 * @Version 1.0
 */
@Mapper
public interface IAiClientSystemPromptDao {
    List<AiClientSystemPrompt> querySystemPromptConfigByClientIds(List<Long> clientIdList);

    List<AiClientSystemPrompt> querySystemPromptList(AiClientSystemPrompt aiClientSystemPrompt);

    List<AiClientSystemPrompt> queryAllSystemPromptConfig();

    AiClientSystemPrompt querySystemPromptConfigById(Long id);

    int insert(AiClientSystemPrompt aiClientSystemPrompt);

    int update(AiClientSystemPrompt aiClientSystemPrompt);

    int deleteById(Long id);
}
