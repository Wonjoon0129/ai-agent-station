package top.kimwonjoon.domain.agent.service.chat.flow.node;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static top.kimwonjoon.domain.agent.service.chat.flow.AiAgentChatService.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static top.kimwonjoon.domain.agent.service.chat.flow.AiAgentChatService.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @ClassName ClientNode
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/6/1 20:18
 * @Version 1.0
 */
@Slf4j
public class ClientNode extends Node{


    public ClientNode(Long clientId, ChatClient clientInstance) {
        super(clientId,clientInstance);
    }

    public String doApply(List<Message> messages,String chatId){
        String previousClientOutput;
        ChatResponse response = getClientInstance()
                .prompt()
                .messages(messages)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .call().chatResponse();
        previousClientOutput=response.getResult().getOutput().getText();
        log.info("Client {} output: {}", getClientId(), previousClientOutput);
        return previousClientOutput;
    }

}
