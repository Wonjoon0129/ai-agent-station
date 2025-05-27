package top.kimwonjoon.domain.agent.service.armory.factory.element;



import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeType;

import java.io.IOException;
import java.net.URL;
import java.util.*;
/**
 * @ClassName MessageDeserializer
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/25 23:30
 * @Version 1.0
 */
public class MessageDeserializer extends JsonDeserializer<Message> {

    private static final Logger logger = LoggerFactory.getLogger(MessageDeserializer.class);

    public Message deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        String messageType = node.get("messageType").asText();
        String text = node.has("text") ? node.get("text").asText() : null;

        Map<String, Object> metadata = node.has("metadata")
                ? mapper.convertValue(node.get("metadata"), new TypeReference<Map<String, Object>>() {})
                : new HashMap<>();

        List<Media> mediaList = new ArrayList<>();
        if (node.has("media")) {
            for (JsonNode mediaNode : node.get("media")) {
                Media media = deserializeMedia(mapper, mediaNode);
                mediaList.add(media);
            }
        }

        switch (messageType) {
            case "USER":
                return UserMessage.builder()
                        .text(text)
                        .media(mediaList)
                        .metadata(metadata)
                        .build();

            case "ASSISTANT":
                List<AssistantMessage.ToolCall> toolCalls = node.has("toolCalls")
                        ? mapper.convertValue(node.get("toolCalls"),
                        new TypeReference<List<AssistantMessage.ToolCall>>() {})
                        : Collections.emptyList();
                return new AssistantMessage(text, metadata, toolCalls, mediaList);
            default:
                throw new IllegalArgumentException("Unknown message type: " + messageType);
        }
    }

    private Media deserializeMedia(ObjectMapper mapper, JsonNode mediaNode) throws IOException {
        Media.Builder builder = Media.builder();

        // Handle MIME type
        if (mediaNode.has("mimeType")) {
            JsonNode mimeNode = mediaNode.get("mimeType");
            String type = mimeNode.get("type").asText();
            String subtype = mimeNode.get("subtype").asText();
            builder.mimeType(new MimeType(type, subtype));
        }

        // Handle data - could be either URL string or byte array
        if (mediaNode.has("data")) {
            String data = mediaNode.get("data").asText();
            if (data.startsWith("http://") || data.startsWith("https://")) {
                builder.data(new URL(data));
            } else {
                // Assume it's base64 encoded binary data
                byte[] bytes = Base64.getDecoder().decode(data);
                builder.data(bytes);
            }
        }

        // Handle dataAsByteArray if present (overrides data if both exist)
        if (mediaNode.has("dataAsByteArray")) {
            byte[] bytes = Base64.getDecoder().decode(mediaNode.get("dataAsByteArray").asText());
            builder.data(bytes);
        }

        // Handle optional fields
        if (mediaNode.has("id")) {
            builder.id(mediaNode.get("id").asText());
        }

        if (mediaNode.has("name")) {
            builder.name(mediaNode.get("name").asText());
        }

        return builder.build();
    }

}

