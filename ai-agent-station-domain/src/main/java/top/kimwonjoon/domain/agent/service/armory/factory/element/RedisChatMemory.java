package top.kimwonjoon.domain.agent.service.armory.factory.element;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import top.kimwonjoon.domain.agent.adapter.repository.IAgentRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName RedisChatMemory
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/25 23:06
 * @Version 1.0
 */
@Slf4j
@Component
public class RedisChatMemory implements ChatMemory, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(RedisChatMemory.class);
    private static final String DEFAULT_KEY_PREFIX = "chat_memory:";

    @Resource
    private IAgentRepository repository;

    private final ObjectMapper objectMapper;

    @Resource
    private RedissonClient redissonClient;


    public RedisChatMemory() {

        this.objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Message.class, new MessageDeserializer());
        this.objectMapper.registerModule(module);
    }


    @Override
    public void add(String conversationId, List<Message> messages) {
        String key = DEFAULT_KEY_PREFIX + conversationId;
        RList<String> list = redissonClient.getList(key);

        try {
            List<String> messageJsons = new ArrayList<>(messages.size());
            for (Message message : messages) {
                messageJsons.add(objectMapper.writeValueAsString(message));
            }
            list.addAll(messageJsons);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing message", e);
        }

        logger.info("Added {} messages to conversationId: {}", messages.size(), conversationId);
    }


    @Override
    public List<Message> get(String conversationId) {
        String key = DEFAULT_KEY_PREFIX + conversationId;
        RList<String> list = redissonClient.getList(key);

        if(list==null||list.isEmpty()){
            return new ArrayList<Message>();

        }
        int totalSize = list.size();
        int fromIndex = Math.max(0, totalSize-100);
        List<String> messageStrings = list.subList(fromIndex, totalSize);

        List<Message> messages = new ArrayList<>(messageStrings.size());
        for (String messageString : messageStrings) {
            try {
                Message message = objectMapper.readValue(messageString, Message.class);
                messages.add(message);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error deserializing message", e);
            }
        }

        logger.info("Retrieved {} messages for conversationId: {}", messages.size(), conversationId);
        return messages;
    }

    @Override
    public void clear(String conversationId) {
        String key = DEFAULT_KEY_PREFIX + conversationId;
        RList<String> list = redissonClient.getList(key);
        list.delete();
        logger.info("Cleared messages for conversationId: {}", conversationId);
    }

    public void clearOverLimit(String conversationId, int maxLimit, int deleteSize) {
        try {
            String key = DEFAULT_KEY_PREFIX + conversationId;
            RList<String> list = redissonClient.getList(key);

            List<String> allMessages = new ArrayList<>(list.readAll());
            if (allMessages.size() >= maxLimit) {
                List<String> remaining = allMessages.subList(deleteSize, allMessages.size());
                list.clear();
                list.addAll(remaining);
                logger.info("Trimmed messages for conversationId: {} to {} messages", conversationId, remaining.size());
            }
        } catch (Exception e) {
            logger.error("Error clearing messages from Redis chat memory", e);
            throw new RuntimeException(e);
        }
    }

    public void updateMessageById(String conversationId, String messages) {
        String key = DEFAULT_KEY_PREFIX + conversationId;
        try {
            RList<String> list = redissonClient.getList(key);
            list.delete();
            list.add(messages);
        } catch (Exception e) {
            logger.error("Error updating messages from Redis chat memory", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (redissonClient != null && !redissonClient.isShutdown()) {
            redissonClient.shutdown();
            logger.info("Redisson client shutdown successfully");
        }
    }

}