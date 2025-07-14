package top.kimwonjoon.domain.agent.service.rag;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.kimwonjoon.domain.agent.adapter.repository.IAgentRepository;
import top.kimwonjoon.domain.agent.model.valobj.AiClientAdvisorVO;
import top.kimwonjoon.domain.agent.model.valobj.AiRagOrderVO;
import top.kimwonjoon.domain.agent.service.IAiAgentRagService;
import top.kimwonjoon.domain.agent.service.armory.node.AiClientAdvisorNode;
import top.kimwonjoon.domain.agent.service.rag.element.SimilarTextSplitter;

import java.util.List;

/**
 * RAG 知识库服务
 *
 * @author kimwonjoon
 * 2025-05-05 16:41
 */
@Slf4j
@Service
public class AiAgentRagService implements IAiAgentRagService {

    @Resource
    private AiClientAdvisorNode aiClientAdvisorNode;
    @Resource
    private IAgentRepository repository;

    @Resource
    SimilarTextSplitter textSplitter;

    @Override
    public void storeRagFile(String name, String tag, List<MultipartFile> files) {

        Long id;
        for (MultipartFile file : files) {
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());

            List<Document> documentList = textSplitter.chunkDocument(documentReader.get(),6L);

            // 添加知识库标签
            documentList.forEach(doc -> doc.getMetadata().put("knowledge", tag));
            AiClientAdvisorVO aiClientAdvisorVO=new AiClientAdvisorVO();
            aiClientAdvisorVO.setDatabaseId(1L);
            aiClientAdvisorVO.setEmbeddingModelId(5L);
            aiClientAdvisorVO.setAdvisorName("vector_store_ollama");
            VectorStore vectorStore = aiClientAdvisorNode.createVectorStore(aiClientAdvisorVO);

            vectorStore.accept(documentList);

            // 存储到数据库
            AiRagOrderVO aiRagOrderVO = new AiRagOrderVO();
            aiRagOrderVO.setRagName(name);
            aiRagOrderVO.setKnowledgeTag(tag);
            repository.createTagOrder(aiRagOrderVO);
        }
    }

}
