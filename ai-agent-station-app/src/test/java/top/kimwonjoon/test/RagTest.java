package top.kimwonjoon.test;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.kimwonjoon.domain.agent.service.rag.element.SimilarTextSplitter;

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



    @Test
    public void test() {
        TikaDocumentReader documentReader = new TikaDocumentReader(resource);

        SimilarTextSplitter textSplitter = new SimilarTextSplitter();

        List<Document> documentList = textSplitter.chunkDocument(documentReader.get(),5L);


        // 添加知识库标签
        documentList.forEach(doc -> doc.getMetadata().put("knowledge", "tag"));

        // 存储知识库文件


        log.info("上传完成");
    }

}
