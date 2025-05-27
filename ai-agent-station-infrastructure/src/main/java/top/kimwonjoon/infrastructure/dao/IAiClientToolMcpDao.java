package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiClientToolMcp;

import java.util.List;

/**
 * @ClassName IAiClientToolMcpDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025. 5. 19. 16:17
 * @Version 1.0
 */
@Mapper
public interface IAiClientToolMcpDao {

    List<AiClientToolMcp> queryMcpConfigByClientIds(List<Long> clientIdList);

    List<AiClientToolMcp> queryMcpList(AiClientToolMcp aiClientToolMcp);

    AiClientToolMcp queryMcpConfigById(Long id);

    int insert(AiClientToolMcp aiClientToolMcp);

    int update(AiClientToolMcp aiClientToolMcp);

    int deleteById(Long id);
}
