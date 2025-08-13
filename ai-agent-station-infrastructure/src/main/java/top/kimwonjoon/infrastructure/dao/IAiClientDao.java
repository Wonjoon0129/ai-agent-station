package top.kimwonjoon.infrastructure.dao;

/**
 * @ClassName IAiClientDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/26 15:33
 * @Version 1.0
 */

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiClient;

import java.util.List;

@Mapper
public interface IAiClientDao {
    List<AiClient> queryClientListAll(AiClient aiClient);

    AiClient queryClientById(Long id);
}
