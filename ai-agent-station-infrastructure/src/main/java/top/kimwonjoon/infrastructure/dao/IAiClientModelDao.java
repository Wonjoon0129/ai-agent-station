package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiClientModel;

import java.util.List;

/**
 * @ClassName IAiClientModelDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025. 5. 19. 15:34
 * @Version 1.0
 */
@Mapper
public interface IAiClientModelDao {

    /**
     * 根据客户端ID列表查询模型配置
     * @param clientIdList 客户端ID列表
     * @return 模型配置列表
     */
    List<AiClientModel> queryModelConfigByClientIds(List<Long> clientIdList);

    List<AiClientModel> queryAllModelConfig();

    List<AiClientModel> queryClientModelList(AiClientModel aiClientModel);

    List<AiClientModel> queryClientModelList();

    AiClientModel queryModelConfigById(Long id);

    int insert(AiClientModel aiClientModel);

    int update(AiClientModel aiClientModel);

    int deleteById(Long id);
}
