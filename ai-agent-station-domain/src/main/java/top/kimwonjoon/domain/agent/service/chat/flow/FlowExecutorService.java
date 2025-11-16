package top.kimwonjoon.domain.agent.service.chat.flow;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import top.kimwonjoon.domain.agent.service.chat.flow.node.ClientNode;
import top.kimwonjoon.domain.agent.service.chat.flow.node.Node;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static top.kimwonjoon.domain.agent.service.chat.flow.AiAgentChatService.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static top.kimwonjoon.domain.agent.service.chat.flow.AiAgentChatService.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @ClassName FlowExecutorService
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/6/1 21:02
 * @Version 1.0
 */
@Slf4j
@Service
public class FlowExecutorService {

    @Resource
    private ClientAssemblyService clientAssemblyService;

    /**
     * 执行指定Agent的客户端流程。
     * @param agentId 智能体ID
     * @param message 初始输入参数，可以是一个Map或自定义对象
     * @return 最终的执行结果或状态
     */
    public String executeAgentFlow(Long agentId, String message,String chatId) throws Exception {
        Node startNodes = clientAssemblyService.assembleClientFlow(agentId);
        if (startNodes==null) {
            // log.warn("No start nodes found for agentId: {}", agentId);
            return "No flow configured or no start nodes found.";
        }

        // 简单起见，这里只执行第一个起始节点。实际可能需要更复杂的逻辑处理多个起始点。
        Node currentNode = startNodes;

        List<Message> messages=new ArrayList<>();

        String previousClientOutput;
        int stepCount = 0; // 防止无限循环
        int maxSteps = 100; // 最大执行步骤

        UserMessage userMessage=new UserMessage(message);
        messages.add(userMessage);

        while (currentNode != null && stepCount < maxSteps) {

            log.info("Executing client: {}, step: {}", currentNode.getClientId(), stepCount);
            try {
                ChatResponse response = currentNode.getClientInstance()
                    .prompt()
                    .messages(messages)
                    .system(s -> s.param("current_date", LocalDate.now().toString()))
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                    .call().chatResponse();
                previousClientOutput=response.getResult().getOutput().getText();
                log.info("Client {} output: {}", currentNode.getClientId(), previousClientOutput);

                UserMessage assistantMessage=new UserMessage(previousClientOutput);
                if(currentNode.getConditionalChildren().isEmpty()){
                    messages.add(assistantMessage);
                }

            } catch (Exception e) {
                // log.error("Error executing client: {}", currentNode.getClientId(), e);
                // 可以选择中断流程，或者记录错误并尝试执行默认路径等
                throw new RuntimeException("Error in client execution: " + currentNode.getClientId(), e);
            }

            ClientNode nextNode =(ClientNode)currentNode.getNextNode(previousClientOutput);
            if (nextNode == null) {
                // log.info("Flow finished after client: {}. Final output: {}", currentNode.getClientId(), previousClientOutput);
                return previousClientOutput; // 返回最后一个客户端的输出作为流程结果
            }
            currentNode = nextNode;
            stepCount++;
        }

        if (stepCount >= maxSteps) {
            // log.warn("Flow execution exceeded max steps for agentId: {}", agentId);
            return "由于超过了最大步骤数，流程执行已中止。";
        }

        return "流程未按预期完成。"; // 如果currentNode为null但没有返回结果
    }
}