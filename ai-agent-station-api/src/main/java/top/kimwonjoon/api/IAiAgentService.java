package top.kimwonjoon.api;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import top.kimwonjoon.api.dto.ChatStreamRequestDTO;
import top.kimwonjoon.api.response.Response;

import java.util.List;

/**
 * AiAgent 智能体对话服务接口
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-05 10:14
 */
public interface IAiAgentService {

    Response<Boolean> preheat(Long aiClientId);

    Response<String> chatAgent(Long aiAgentId, String message,String chatId);

    Flux<ChatResponse> chatStream(String chatId,Long modelId,Long ragId, String message);

    Response<Boolean> uploadRagFile(String name, String tag, List<MultipartFile> files,Long advisorId );

}
