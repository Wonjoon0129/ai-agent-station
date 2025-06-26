package top.kimwonjoon.domain.agent.service.chat.node;

import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName ClientNode
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/6/1 20:18
 * @Version 1.0
 */
@Data
public class ClientNode {
    private final Long clientId;
    private final ChatClient clientInstance; // 实际的Client Bean

    // key: condition (前一个client的输出), value: 下一个 ClientNode
    private final Map<String, ClientNode> conditionalChildren = new HashMap<>();

    private ClientNode defaultNextChild; // 如果condition为null/empty或无匹配时的默认下一个节点

    public ClientNode(Long clientId, ChatClient clientInstance) {
        this.clientId = clientId;
        this.clientInstance = clientInstance;
    }

    public void addConditionalChild(String condition, ClientNode child) {
        if (condition .equals("default")  || condition.trim().isEmpty()) {
            // 如果 condition 为空或空白字符串，视为默认的下一个节点
            this.defaultNextChild = child;
        } else {
            this.conditionalChildren.put(condition, child);
        }
    }

    public ClientNode getNextNode(String previousClientOutput) {
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

}
