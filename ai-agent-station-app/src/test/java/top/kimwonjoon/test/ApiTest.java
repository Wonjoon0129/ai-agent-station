package top.kimwonjoon.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.kimwonjoon.domain.agent.service.IAiAgentChatService;
import top.kimwonjoon.domain.agent.service.IAiAgentPreheatService;
import top.kimwonjoon.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import java.time.LocalDate;

import static top.kimwonjoon.domain.agent.service.chat.AiAgentChatService.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static top.kimwonjoon.domain.agent.service.chat.AiAgentChatService.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Resource
    private IAiAgentPreheatService aiAgentPreheatService;
    @Test
    public void test() throws Exception {

        aiAgentPreheatService.preheat(3L);

        ChatClient chatClient = defaultArmoryStrategyFactory.chatClient(3L);
        String content1 = chatClient.prompt( "我叫金元俊")
                .system(s -> s.param("current_date", LocalDate.now().toString()))
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, "chatId-1")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .call().content();
        String content2 = chatClient.prompt("你好，我叫什么？")
               .system(s -> s.param("current_date", LocalDate.now().toString()))
               .advisors(a -> a
                       .param(CHAT_MEMORY_CONVERSATION_ID_KEY, "chatId-1")
                       .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
               .call().content();
        log.info("测试结果：{}", content1);
        log.info("测试结果：{}", content2);

    }

}
