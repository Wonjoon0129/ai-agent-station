package top.kimwonjoon.domain.agent.service.rag.element;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import top.kimwonjoon.domain.agent.model.valobj.enums.AiAgentEnumVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
/**
 * @ClassName TextSplitter
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/7/8 09:26
 * @Version 1.0
 */
@Slf4j
@Service
public class SimilarTextSplitter {

    private  int bufferSize=1;
    private  double percentileThreshold=5;

    @Resource
    private ApplicationContext applicationContext;


    // 主分块方法
    public List<Document> chunkDocument(List<Document> documents,Long modelId) {
        // 1. 句子分割
        String text = documents.stream().map(Document::getText).collect(Collectors.joining("\n"));
        List<String> sentences = splitIntoSentences(text);

        // 2. 计算句子嵌入向量
        List<float[]> embeddings = calculateEmbeddings(sentences,modelId);

        // 3. 计算相邻句子相似度
        List<Double> similarities = calculateSimilarities(embeddings);

        // 4. 动态检测分块断点
        List<Integer> splitPoints = findSplitPoints(similarities);

        // 5. 根据断点生成文本块
        return buildChunks(sentences, splitPoints);
    }

    private static final Pattern SENTENCE_DELIMITERS =
            Pattern.compile("(?<=[。.！!？?])\\s*");

    private List<String> splitIntoSentences(String text) {
        // 标准化标点（可选：将中文标点转为英文对应标点）
        String normalized = text.replaceAll("\\uFF0E", ".")  // 中文句号
                .replaceAll("\\uFF1F", "?"); // 中文问号

        // 分割句子（保留标点）
        return Arrays.stream(SENTENCE_DELIMITERS.split(normalized))
                .filter(s -> !s.trim().isEmpty())
                .collect(Collectors.toList());
    }
    private List<Document> buildChunks(List<String> sentences, List<Integer> splitPoints) {
        List<Document> documents=new ArrayList<>();
        List<String> chunks = new ArrayList<>();
        int start = 0;

        for (int split : splitPoints) {
            // 合并当前块的句子
            String chunk = String.join(" ", sentences.subList(start, split + 1));
            chunks.add(chunk);
            start = split + 1;
        }

        // 添加最后一块
        if (start < sentences.size()) {
            chunks.add(String.join(" ", sentences.subList(start, sentences.size())));
        }

        for(String chunk : chunks) {
            Document newDoc = new Document(chunk);
            documents.add(newDoc);
        }

        return documents;

    }

    private List<Integer> findSplitPoints(List<Double> similarities) {
        List<Integer> splitPoints = new ArrayList<>();
        if (similarities.size() < 2 * bufferSize + 1) return splitPoints;

        // 参数配置（可根据实际数据调整）
        final double PERCENTILE_THRESHOLD = 80;  // 更敏感的百分位
        final int MIN_GAP = bufferSize / 3;        // 最小分割间隔
        final int LOCAL_WINDOW = 5;                // 局部极小值检测窗口

        for (int i = bufferSize; i < similarities.size() - bufferSize; i++) {
            // 创建动态窗口（跳过当前点避免影响）
            double[] leftWindow = new double[bufferSize];
            double[] rightWindow = new double[bufferSize];
            for (int j = 0; j < bufferSize; j++) {
                leftWindow[j] = similarities.get(i - bufferSize + j);
                rightWindow[j] = similarities.get(i + j + 1);
            }

            // 双窗口独立计算百分位
            Percentile leftPercentile = new Percentile();
            Percentile rightPercentile = new Percentile();
            double leftThreshold = leftPercentile.evaluate(leftWindow, PERCENTILE_THRESHOLD);
            double rightThreshold = rightPercentile.evaluate(rightWindow, PERCENTILE_THRESHOLD);
            double dynamicThreshold = Math.min(leftThreshold, rightThreshold);

            // 局部极小值检测
            boolean isLocalMinima = true;
            double current = similarities.get(i);
            for (int j = 1; j <= LOCAL_WINDOW; j++) {
                if ((i - j >= 0 && similarities.get(i - j) < current) ||
                        (i + j < similarities.size() && similarities.get(i + j) < current)) {
                    isLocalMinima = false;
                    break;
                }
            }

            // 复合条件检测
            if (current < dynamicThreshold && isLocalMinima) {
                // 冲突处理：邻近点取最小值
                if (!splitPoints.isEmpty() && i - splitPoints.get(splitPoints.size() - 1) < MIN_GAP) {
                    int lastIndex = splitPoints.get(splitPoints.size() - 1);
                    if (current < similarities.get(lastIndex)) {
                        splitPoints.remove(splitPoints.size() - 1);
                        splitPoints.add(i);
                    }
                } else {
                    splitPoints.add(i);
                }
            }
        }
        return splitPoints;
    }

    private List<Double> calculateSimilarities(List<float[]> embeddings) {
        List<Double> similarities = new ArrayList<>();
        for (int i = 0; i < embeddings.size() - 1; i++) {
            double sim = cosineSimilarity(embeddings.get(i), embeddings.get(i + 1));
            similarities.add(sim);
        }
        return similarities;
    }

    // 计算两个向量的余弦相似度
    public static float cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        float dotProduct = 0L;
        float normA = 0L;
        float normB = 0L;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += (float) Math.pow(vectorA[i], 2);
            normB += (float) Math.pow(vectorB[i], 2);
        }

        if (normA == 0 || normB == 0) {
            return 0L; // 避免除以零
        }

        return (float) (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }


    private List<float[]> calculateEmbeddings(List<String> sentences,Long modelId) {

        List<float[]> embeddings = new ArrayList<>();
        // 获取嵌入模型
        EmbeddingModel embeddingModel= (EmbeddingModel) applicationContext.getBean(AiAgentEnumVO.AI_CLIENT_EMBEDDING_MODEL.getBeanNameTag() + modelId);
        // 调用模型生成嵌入向量
        for(String sentence:sentences){
            log.info("sentence:{}",sentence);
            log.info("embeddingModel:{}",embeddingModel);
            float[] embed = embeddingModel.embed(sentence);
            embeddings.add(embed);

        }
        return embeddings;
    }

}
