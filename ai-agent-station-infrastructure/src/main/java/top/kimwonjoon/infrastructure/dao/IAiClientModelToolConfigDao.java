package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiClientModelToolConfig;

import java.util.List;

/**
 * @ClassName IAiModelConfigToolDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025. 5. 19. 15:42
 * @Version 1.0
 */
@Mapper
public interface IAiClientModelToolConfigDao {

    public List<AiClientModelToolConfig> queryModelToolConfigByModelIds(List<Long> modelIds);
}
