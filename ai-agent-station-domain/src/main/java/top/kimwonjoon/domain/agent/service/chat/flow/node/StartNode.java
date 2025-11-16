package top.kimwonjoon.domain.agent.service.chat.flow.node;

import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * @ClassName StartNode
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/8/31 11:23
 */
public class StartNode extends Node{
    public StartNode(Long clientId, ChatClient clientInstance) {
        super(clientId,clientInstance);
    }

    @Override
    public String doApply(List<Message> messages, String chatId) {
        return messages.get(0).getText();
    }
}
