package top.kimwonjoon.domain.agent.service.chat.auto;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import top.kimwonjoon.domain.agent.model.entity.ExecuteCommandEntity;

/**
 * 执行策略接口
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/8/5 09:48
 */
public interface IExecuteStrategy {

    void execute(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter) throws Exception;

}
