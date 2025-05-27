package top.kimwonjoon.test.domain;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.kimwonjoon.domain.agent.model.entity.AiAgentEngineStarterEntity;
import top.kimwonjoon.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

import static top.kimwonjoon.domain.agent.service.chat.AiAgentChatService.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static top.kimwonjoon.domain.agent.service.chat.AiAgentChatService.CHAT_MEMORY_RETRIEVE_SIZE_KEY;


/**
 * 功能测试
 *
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-04 07:29
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AgentTest {

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Test
    public void test() throws Exception {
        StrategyHandler<AiAgentEngineStarterEntity, DefaultArmoryStrategyFactory.DynamicContext, String> handler = defaultArmoryStrategyFactory.strategyHandler();

        AiAgentEngineStarterEntity aiAgentEngineStarterEntity = new AiAgentEngineStarterEntity();
        aiAgentEngineStarterEntity.setClientIdList(new ArrayList<>() {{
            add(1L);
        }});

        String apply = handler.apply(aiAgentEngineStarterEntity, new DefaultArmoryStrategyFactory.DynamicContext());

        log.info("测试结果：{}", apply);

        ChatClient chatClient = defaultArmoryStrategyFactory.chatClient(1L);

        String userInput = "有哪些工具可以使用";
//        String userInput = "你好";
        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient
                .prompt(userInput)
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, "chatId-101")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .call().content());
    }
    @Test
    public void test_2() {
        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder().baseUrl("http://localhost:11434").build())
                .defaultOptions(OllamaOptions.builder()
                        .model("qwen3:1.7b")
                        .toolCallbacks(new SyncMcpToolCallbackProvider(stdioMcpClient()).getToolCallbacks())
                        .build())
                .build();
        ChatResponse call = chatModel.call(Prompt.builder().messages(new UserMessage("小傅哥是干什么的")).build());
        log.info("测试结果:{}", JSON.toJSONString(call.getResult()));
    }

    public McpSyncClient stdioMcpClient() {

        // https://github.com/jae-jae/fetcher-mcp
        var stdioParams = ServerParameters.builder("npx")
                .args("-y",
                        "fetcher-mcp")
                .build();

        var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
                .requestTimeout(Duration.ofSeconds(50)).build();

        var init = mcpClient.initialize();

        System.out.println("Stdio MCP Initialized: " + init);

        return mcpClient;

    }

}
