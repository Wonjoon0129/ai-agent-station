package top.kimwonjoon.domain.agent.service.rag.element;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import top.kimwonjoon.domain.agent.adapter.repository.IAgentRepository;
import top.kimwonjoon.domain.agent.model.valobj.AiClientAdvisorVO;
import top.kimwonjoon.domain.agent.service.armory.node.AiClientAdvisorNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * @ClassName ragMessageTool
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/27 20:17
 * @Version 1.0
 */
@Service
public class RagMessageTool {

    @Resource
    private AiClientAdvisorNode aiClientAdvisorNode;

    @Resource
    private IAgentRepository repository;


    public List<Message> ragMessage(Long ragId,String message) {
        // 封装请求参数
        List<Message> messages = new ArrayList<>();

        if (null != ragId && 0 != ragId){
            // 查询RAG标签
            String tag = repository.queryRagKnowledgeTag(ragId);

            SearchRequest searchRequest = SearchRequest.builder()
                    .query(message)
                    .topK(5)
                    .filterExpression("knowledge == '" + tag + "'")
                    .build();
            AiClientAdvisorVO aiClientAdvisorVO=new AiClientAdvisorVO();
            aiClientAdvisorVO.setDatabaseId(1L);
            aiClientAdvisorVO.setEmbeddingModelId(5L);
            aiClientAdvisorVO.setAdvisorName("vector_store_ollama");
            VectorStore vectorStore = aiClientAdvisorNode.createVectorStore(aiClientAdvisorVO);

            List<Document> documents = vectorStore.similaritySearch(searchRequest);
            String documentCollectors = documents.stream().map(Document::getFormattedContent).collect(Collectors.joining());
            Message ragMessage = new SystemPromptTemplate("""
                Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                If unsure, simply state that you don't know.
                Another thing you need to note is that your reply must be in Chinese!
                DOCUMENTS:
                    {documents}
                """).createMessage(Map.of("documents", documentCollectors));

            messages.add(new UserMessage(message));
            messages.add(ragMessage);


        } else {
            messages.add(new UserMessage(message));
        }
        return messages;
    }

}
