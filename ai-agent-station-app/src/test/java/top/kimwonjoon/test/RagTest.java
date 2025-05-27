package top.kimwonjoon.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 知识库测试
 *
 * @author Fuzhengwei bugstack.cn @小傅哥
 * 2025-05-05 08:06
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RagTest {

    @Value("classpath:data/file.txt")
    private org.springframework.core.io.Resource resource;

    @Resource
    private PgVectorStore vectorStore;

    @Resource
    private TokenTextSplitter tokenTextSplitter;

    @Test
    public void test() {
        TikaDocumentReader reader = new TikaDocumentReader(resource);

        List<Document> documents = reader.get();
        List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

        documents.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称"));
        documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", "知识库名称"));

        vectorStore.accept(documentSplitterList);

        log.info("上传完成");
    }

}
