package top.kimwonjoon.domain.agent.service.chat.flow.node;

import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName Node
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/8/31 11:13
 */
@Data
public abstract class Node {

    private  Long clientId;

    private ChatClient clientInstance; // 实际的Client Bean

    // key: condition (前一个client的输出), value: 下一个 ClientNode
    private Map<String, Node> conditionalChildren = new HashMap<>();
    // 如果condition为null/empty或无匹配时的默认下一个节点
    private Node defaultNextChild;

    public Node(Long clientId, ChatClient clientInstance) {
        this.clientId = clientId;
        this.clientInstance = clientInstance;
    }

    public void addConditionalChild(String condition, Node child) {
        if (condition .equals("default")  || condition.trim().isEmpty()) {
            // 如果 condition 为空或空白字符串，视为默认的下一个节点
            this.defaultNextChild = child;
        } else {
            this.conditionalChildren.put(condition, child);
        }
    }

    public Node getNextNode(String previousClientOutput) {
        for(Object a:conditionalChildren.keySet().toArray()){
            if(previousClientOutput.toLowerCase().contains((String)a)) {
                return conditionalChildren.get(a);
            }
        }
        if(defaultNextChild!=null){
            return defaultNextChild;
        }
        return null;
    }
    /**
     * 执行
     * @param messages
     * @param chatId
     * @return
     */
    public abstract String doApply(List<Message> messages, String chatId);


}
