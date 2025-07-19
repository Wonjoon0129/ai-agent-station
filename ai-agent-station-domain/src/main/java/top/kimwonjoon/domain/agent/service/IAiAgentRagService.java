package top.kimwonjoon.domain.agent.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * RAG 知识库服务接口
 */
public interface IAiAgentRagService {

    void storeRagFile(String name, String tag, List<MultipartFile> files,Long databaseId);

}
