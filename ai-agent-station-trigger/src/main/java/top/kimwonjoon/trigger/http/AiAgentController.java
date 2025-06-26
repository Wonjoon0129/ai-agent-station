package top.kimwonjoon.trigger.http;


import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import top.kimwonjoon.api.IAiAgentService;
import top.kimwonjoon.api.response.Response;
import top.kimwonjoon.domain.agent.service.IAiAgentChatService;
import top.kimwonjoon.domain.agent.service.IAiAgentPreheatService;
import top.kimwonjoon.domain.agent.service.IAiAgentRagService;
import top.kimwonjoon.domain.agent.service.chat.FlowExecutorService;
import top.kimwonjoon.types.common.Constants;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/ai/agent")
public class AiAgentController implements IAiAgentService {


    @Resource
    private IAiAgentPreheatService aiAgentPreheatService;
    @Resource
    private IAiAgentChatService aiAgentChatService;
    @Resource
    private IAiAgentRagService aiAgentRagService;


    @Resource
    FlowExecutorService flowExecutorService;
    /**
     * AI代理预热
     * curl --request GET \
     *   --url 'http://localhost:8091/ai-agent-station/api/v1/ai/agent/preheat?aiAgentId=1'
     */
    @RequestMapping(value = "preheat", method = RequestMethod.GET)
    @Override
    public Response<Boolean> preheat(@RequestParam("aiAgentId") Long aiAgentId) {
        try {
            log.info("预热装配 AiAgent {}", aiAgentId);
            aiAgentPreheatService.preheat(aiAgentId);
            return Response.<Boolean>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (Exception e) {
            log.error("预热装配 AiAgent {}", aiAgentId,e);
            return Response.<Boolean>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }


    /**
     * AI代理执行方法，用于处理用户输入的消息并返回AI代理的回复
     * <p>
     * 示例请求:
     * curl -X GET "http://localhost:8091/ai-agent-station/api/v1/ai/agent/chat_agent?aiAgentId=1&message=生成一篇文章" -H "Content-Type: application/json"
     *
     * @param aiAgentId AI代理ID，用于标识使用哪个AI代理
     * @param message   用户输入的消息内容
     * @return AI代理的回复内容
     */
    @RequestMapping(value = "chat_agent", method = RequestMethod.GET)
    @Override
    public Response<String> chatAgent(@RequestParam("aiAgentId") Long aiAgentId, @RequestParam("message") String message, @RequestParam("chatId") String chatId) {
        try {
            log.info("AiAgent 智能体对话，请求 {} {}", aiAgentId, message);
            String content = flowExecutorService.executeAgentFlow(aiAgentId, message,chatId);
            Response<String> response = Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(content)
                    .build();
            log.info("AiAgent 智能体对话，结果 {} {}", aiAgentId, JSON.toJSONString(response));
            return response;
        } catch (Exception e) {
            log.error("AiAgent 智能体对话，异常 {} {}", aiAgentId, message, e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.AGENT_NULL_EXCEPTION.getCode())
                    .info(Constants.ResponseCode.AGENT_NULL_EXCEPTION.getInfo())
                    .build();
        }
    }

    /**
     * curl http://localhost:8091/ai-agent-station/api/v1/ai/agent/chat_stream?modelId=1&ragId=1&message=hi
     */
    @RequestMapping(value = "chat_stream", method = RequestMethod.GET)
    @Override
    public Flux<ChatResponse> chatStream(@RequestParam("modelId") Long modelId, @RequestParam("ragId") Long ragId, @RequestParam("message") String message) {
        try {
            log.info("AiAgent 智能体对话(stream)，请求 {} {} {}", modelId, ragId, message);
            if(modelId == 0){
                throw new Exception("请选择模型");
            }
            return aiAgentChatService.aiAgentChatStream(modelId, ragId, message);
        } catch (Exception e) {
            log.error("AiAgent 智能体对话(stream)，失败 {} {} {}", modelId, ragId, message, e);
            return Flux.error(e);
        }
    }
    @Override
    @RequestMapping(value = "multi_chat", method = RequestMethod.POST)
    public Response<String> multiChat(@RequestParam("modelId") Long modelId, @RequestParam("message") String message,@RequestParam("image") List<MultipartFile> files, @RequestParam("ragId") Long ragId) {
        try {
            log.info("multi_chat，请求 {} {} {} {}", modelId, message,files,ragId);
            if(modelId == 0){
                throw new Exception("请选择模型");
            }
            List<org.springframework.core.io.Resource> resource = new ArrayList<>();
            List<String> types=new ArrayList<>();
            for(MultipartFile file : files){
                resource.add(file.getResource());
                types.add(file.getContentType());
            }
            String content= aiAgentChatService.aiMultiChat(modelId, message,types,resource,ragId);
            Response<String> response = Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(content)
                    .build();
            return response;
        } catch (Exception e) {
            log.error("AiAgent 智能体对话，异常 {} {}", message, e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.AGENT_NULL_EXCEPTION.getCode())
                    .info(Constants.ResponseCode.AGENT_NULL_EXCEPTION.getInfo())
                    .build();
        }
    }

    /**
     * http://localhost:8091/ai-agent-station/api/v1/ai/agent/file/upload
     */
    @RequestMapping(value = "file/upload", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
    @Override
    public Response<Boolean> uploadRagFile(@RequestParam("name") String name, @RequestParam("tag") String tag, @RequestParam("files") List<MultipartFile> files) {
        try {
            log.info("上传知识库，请求 {}", name);
            aiAgentRagService.storeRagFile(name, tag, files);
            Response<Boolean> response = Response.<Boolean>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
            log.info("上传知识库，结果 {} {}", name, JSON.toJSONString(response));
            return response;
        } catch (Exception e) {
            log.error("上传知识库，异常 {}", name, e);
            return Response.<Boolean>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }




}
