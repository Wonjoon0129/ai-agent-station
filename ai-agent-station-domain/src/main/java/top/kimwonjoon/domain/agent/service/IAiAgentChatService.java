package top.kimwonjoon.domain.agent.service;

import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Ai智能体对话服务接口
 */
public interface IAiAgentChatService {


    /**
     * 智能体对话
     */
    String aiAgentChat(Long aiAgentId,String message,String chatId) throws Exception;

    String aiMultiChat(Long modelId,String message,List<String> types,List<org.springframework.core.io.Resource> resource,Long ragId) throws Exception;

    Flux<ChatResponse> aiAgentChatStream(Long aiAgentId, Long ragId, String message);

}
