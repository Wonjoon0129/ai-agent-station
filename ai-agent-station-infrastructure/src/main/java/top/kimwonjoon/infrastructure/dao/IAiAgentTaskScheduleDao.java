package top.kimwonjoon.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import top.kimwonjoon.infrastructure.dao.po.AiAgentTaskSchedule;

import java.util.List;

/**
 * @ClassName IAiAgentTaskScheduleDao
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/22 23:43
 * @Version 1.0
 */
@Mapper
public interface IAiAgentTaskScheduleDao {
    List<AiAgentTaskSchedule> queryAllValidTaskSchedule();

    List<Long> queryAllInvalidTaskScheduleIds();

    List<AiAgentTaskSchedule> queryTaskScheduleList(AiAgentTaskSchedule aiAgentTaskSchedule);

    AiAgentTaskSchedule queryTaskScheduleById(Long id);

    int insert(AiAgentTaskSchedule aiAgentTaskSchedule);

    int update(AiAgentTaskSchedule aiAgentTaskSchedule);

    int deleteById(Long id);

    int deleteByAgentId(Long agentId);
}
