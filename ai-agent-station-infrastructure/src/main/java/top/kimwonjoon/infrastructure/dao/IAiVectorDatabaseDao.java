package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiVectorDatabase;

import java.util.List;

/**
 * @ClassName IAiVectorDatabaseDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/7/14 11:10
 * @Version 1.0
 */
@Mapper
public interface IAiVectorDatabaseDao {
    List<AiVectorDatabase> queryAiVectorDatabase();

    List<AiVectorDatabase> queryAiVectorDatabaseByClientIds(List<Long> clientIdList);
}
