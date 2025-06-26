package top.kimwonjoon.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName AiAgentClientVO
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/6/1 20:33
 * @Version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgentClientVO {

    /**
     * 主键ID
     */
    private Long id;

    private Long aiAgentId;

    private Long clientIdFrom;

    private Long clientIdTo;

    private String condition;

}
