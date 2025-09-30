package top.kimwonjoon.domain.agent.service.chat.flow;

import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import top.kimwonjoon.domain.agent.adapter.repository.IAgentRepository;
import top.kimwonjoon.domain.agent.service.IAiAgentChatService;
import top.kimwonjoon.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import top.kimwonjoon.domain.agent.service.armory.factory.element.RedisChatMemory;
import top.kimwonjoon.domain.agent.service.rag.element.RagMessageTool;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class AiAgentChatService implements IAiAgentChatService {
    public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    public static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_response_size";
    @Resource
    private IAgentRepository repository;
    @Resource
    DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Resource
    RedisChatMemory chatMemory;
    @Resource
    RagMessageTool ragMessageTool;
    @Autowired
    private ChatModel chatModel;

    @Override
    public String aiAgentChat(Long aiAgentId, String message,String chatId) throws Exception {
        log.info("智能体对话请求，参数 {} {}", aiAgentId, message);

        List<Long> aiClientIds = repository.queryAiClientIdsByAiAgentId(aiAgentId);

        String content = "";
        for (Long aiClientId : aiClientIds) {
            ChatClient chatClient = defaultArmoryStrategyFactory.chatClient(aiClientId);
            try{
                content = chatClient.prompt(message + "，" + content)
                        .system(s -> s.param("current_date", LocalDate.now().toString()))
                        .advisors(a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                        .call().content();
            }catch (NoSuchBeanDefinitionException e){
                throw new Exception("请重新配置智能体对话客户端");
            }
            log.info("智能体对话进行，客户端ID {}", aiClientId);
        }
        log.info("智能体对话请求，结果 {} {}", aiAgentId, content);

        return content;
    }

    @Override
    public Flux<ChatResponse> aiMultiChat(Long modelId,String message,List<String> types,List<org.springframework.core.io.Resource> resource,Long ragId) throws Exception {

        ChatModel chatModel = defaultArmoryStrategyFactory.chatModel(modelId);

        Media[] media = new Media[resource.size()];
        for(int i=0;i<resource.size();i++){
            media[i]=new Media(MimeTypeUtils.parseMimeType(types.get(i)),resource.get(i));
        }
        List<Message> messages=ragMessageTool.ragMessage(ragId,message);
        StringBuilder stringBuilder = new StringBuilder();
        for(Message message1:messages){
            stringBuilder.append(message1.getText());
        }


        return ChatClient.create(chatModel).prompt()
                .user(u -> u.text(stringBuilder.toString())
                        .media(media))
                .stream().chatResponse();

    }


    @Override
    public Flux<ChatResponse> aiAgentChatStream(String chatId,Long modelId, Long ragId, String message) {
        log.info("智能体对话请求，参数 aiAgentId {} message {}", modelId, message);

        PromptChatMemoryAdvisor promptChatMemoryAdvisor=PromptChatMemoryAdvisor.builder(chatMemory)
                .order(2)
                .conversationId(chatId)
                .build();

        ChatModel chatModel = defaultArmoryStrategyFactory.chatModel(modelId);
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(promptChatMemoryAdvisor)
                .build();
        List<Message> messages=ragMessageTool.ragMessage(ragId,message);
        return chatClient.prompt(Prompt.builder()
                .messages(messages)
                .build()).stream().chatResponse();
    }
}
