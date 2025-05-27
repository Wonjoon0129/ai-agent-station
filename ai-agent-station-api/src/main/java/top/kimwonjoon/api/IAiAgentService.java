package top.kimwonjoon.api;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import top.kimwonjoon.api.response.Response;

import java.util.List;

/**
 * AiAgent 智能体对话服务接口
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-05 10:14
 */
public interface IAiAgentService {

    Response<String> multiChat(Long modelId,String message,List<MultipartFile> files,Long ragId);

    Response<Boolean> preheat(Long aiClientId);

    Response<String> chatAgent(Long aiAgentId, String message);

    Flux<ChatResponse> chatStream(Long aiAgentId, Long ragId, String message);

    Response<Boolean> uploadRagFile(String name, String tag, List<MultipartFile> files);

}
