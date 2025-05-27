package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiRagOrder;

import java.util.List;

/**
 * @ClassName IAiRagOrderDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/22 23:04
 * @Version 1.0
 */
@Mapper
public interface IAiRagOrderDao {
    AiRagOrder queryRagOrderById(Long ragId);

    int insert(AiRagOrder aiRagOrder);

    List<AiRagOrder> queryRagOrderList(AiRagOrder aiRagOrder);

    List<AiRagOrder> queryAllValidRagOrder();

    int update(AiRagOrder aiRagOrder);

    int deleteById(Long id);
}
