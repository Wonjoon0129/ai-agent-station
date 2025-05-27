package top.kimwonjoon.domain.agent.service.armory.factory.element;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.function.Predicate;

/**
 * @ClassName RagAnswerAdvisor
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/21 09:14
 * @Version 1.0
 */
public class RagAnswerAdvisor implements CallAdvisor, StreamAdvisor {
    private final VectorStore vectorStore;
    private final SearchRequest searchRequest;
    private final boolean protectFromBlocking;
    private final int order;
    private final List<QueryTransformer> queryTransformers;
    private final DocumentRetriever documentRetriever;
    private final List<DocumentPostProcessor> documentPostProcessors;
    private final QueryAugmenter queryAugmenter;



    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest, String userTextAdvise, boolean protectFromBlocking, int order, List<QueryTransformer> queryTransformers, DocumentRetriever documentRetriever, List<DocumentPostProcessor> documentPostProcessors, QueryAugmenter queryAugmenter) {
        Assert.notNull(vectorStore, "The vectorStore must not be null!");
        Assert.notNull(searchRequest, "The searchRequest must not be null!");
        Assert.hasText(userTextAdvise, "The userTextAdvise must not be empty!");
        this.vectorStore = vectorStore;
        this.searchRequest = searchRequest;
        this.protectFromBlocking = protectFromBlocking;
        this.order = order;
        this.queryTransformers=queryTransformers;
        this.documentRetriever = documentRetriever;
        this.documentPostProcessors = documentPostProcessors != null ? documentPostProcessors : List.of();
        this.queryAugmenter = (QueryAugmenter)(queryAugmenter != null ? queryAugmenter : ContextualQueryAugmenter.builder().build());

    }

    public static Builder builder(VectorStore vectorStore) {
        return new Builder(vectorStore);
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public int getOrder() {
        return this.order;
    }

    private ChatClientRequest before(ChatClientRequest chatClientRequest) {
        Map<String, Object> context = new HashMap(chatClientRequest.context());
        Query originalQuery = Query.builder().text(chatClientRequest.prompt().getUserMessage().getText()).history(chatClientRequest.prompt().getInstructions()).context(context).build();
        Query transformedQuery = originalQuery;

        for(QueryTransformer queryTransformer : this.queryTransformers) {
            transformedQuery = queryTransformer.apply(transformedQuery);
        }

        SearchRequest searchRequestToUse = SearchRequest.from(this.searchRequest).query(originalQuery.text()).filterExpression(this.doGetFilterExpression(context)).build();
        List<Document> documents = this.vectorStore.similaritySearch(searchRequestToUse);

        for(DocumentPostProcessor documentPostProcessor : this.documentPostProcessors) {
            documents = documentPostProcessor.process(originalQuery, documents);
        }

        context.put("rag_document_context", documents);
        Query augmentedQuery = this.queryAugmenter.augment(originalQuery, documents);
        return chatClientRequest.mutate().prompt(chatClientRequest.prompt().augmentUserMessage(augmentedQuery.text())).context(context).build();

    }

    private ChatClientResponse after(ChatClientResponse chatClientResponse) {
        ChatResponse.Builder chatResponseBuilder;
        if (chatClientResponse.chatResponse() == null) {
            chatResponseBuilder = ChatResponse.builder();
        } else {
            chatResponseBuilder = ChatResponse.builder().from(chatClientResponse.chatResponse());
        }

        chatResponseBuilder.metadata("rag_document_context", chatClientResponse.context().get("rag_document_context"));
        return ChatClientResponse.builder().chatResponse(chatResponseBuilder.build()).context(chatClientResponse.context()).build();
    }

    protected Filter.Expression doGetFilterExpression(Map<String, Object> context) {
        return context.containsKey("qa_filter_expression") && StringUtils.hasText(context.get("qa_filter_expression").toString()) ? (new FilterExpressionTextParser()).parse(context.get("qa_filter_expression").toString()) : this.searchRequest.getFilterExpression();
    }

    private Predicate<ChatClientResponse> onFinishReason() {
        return (advisedResponse) -> advisedResponse.chatResponse().getResults().stream().filter((result) -> result != null && result.getMetadata() != null && StringUtils.hasText(result.getMetadata().getFinishReason())).findFirst().isPresent();
    }


    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        ChatClientRequest advisedRequest2 = this.before(request);
        ChatClientResponse advisedResponse = chain.nextCall(advisedRequest2);
        return this.after(advisedResponse);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
        Flux<ChatClientResponse> advisedResponses = this.protectFromBlocking ? Mono.just(chatClientRequest).publishOn(Schedulers.boundedElastic()).map(this::before).flatMapMany((request) -> chain.nextStream(request)) : chain.nextStream(this.before(chatClientRequest));
        return advisedResponses.map((ar) -> {
            if (this.onFinishReason().test(ar)) {
                ar = this.after(ar);
            }

            return ar;
        });
    }

    public static final class Builder {
        private final VectorStore vectorStore;
        private SearchRequest searchRequest = SearchRequest.builder().build();
        private String userTextAdvise = "\nContext information is below, surrounded by ---------------------\n\n---------------------\n{context}\n---------------------\n\nGiven the context and provided history information and not prior knowledge,\nreply to the user comment. If the answer is not in the context, inform\nthe user that you can't answer the question.\n";
        private boolean protectFromBlocking = true;
        private int order = 0;
        private List<QueryTransformer> queryTransformers;
        private DocumentRetriever documentRetriever;
        private List<DocumentPostProcessor> documentPostProcessors;
        private QueryAugmenter queryAugmenter;


        private Builder(VectorStore vectorStore) {
            Assert.notNull(vectorStore, "The vectorStore must not be null!");
            this.vectorStore = vectorStore;
        }

        public Builder searchRequest(SearchRequest searchRequest) {
            Assert.notNull(searchRequest, "The searchRequest must not be null!");
            this.searchRequest = searchRequest;
            return this;
        }

        public Builder userTextAdvise(String userTextAdvise) {
            Assert.hasText(userTextAdvise, "The userTextAdvise must not be empty!");
            this.userTextAdvise = userTextAdvise;
            return this;
        }

        public Builder protectFromBlocking(boolean protectFromBlocking) {
            this.protectFromBlocking = protectFromBlocking;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }
        public Builder queryTransformers(List<QueryTransformer> queryTransformers) {
            Assert.noNullElements(queryTransformers, "queryTransformers cannot contain null elements");
            this.queryTransformers = queryTransformers;
            return this;
        }

        public Builder queryTransformers(QueryTransformer... queryTransformers) {
            Assert.notNull(queryTransformers, "queryTransformers cannot be null");
            Assert.noNullElements(queryTransformers, "queryTransformers cannot contain null elements");
            this.queryTransformers = Arrays.asList(queryTransformers);
            return this;
        }


        public Builder documentRetriever(DocumentRetriever documentRetriever) {
            this.documentRetriever = documentRetriever;
            return this;
        }


        public Builder documentPostProcessors(List<DocumentPostProcessor> documentPostProcessors) {
            Assert.noNullElements(documentPostProcessors, "documentPostProcessors cannot contain null elements");
            this.documentPostProcessors = documentPostProcessors;
            return this;
        }

        public Builder documentPostProcessors(DocumentPostProcessor... documentPostProcessors) {
            Assert.notNull(documentPostProcessors, "documentPostProcessors cannot be null");
            Assert.noNullElements(documentPostProcessors, "documentPostProcessors cannot contain null elements");
            this.documentPostProcessors = Arrays.asList(documentPostProcessors);
            return this;
        }

        public Builder queryAugmenter(QueryAugmenter queryAugmenter) {
            this.queryAugmenter = queryAugmenter;
            return this;
        }

        public RagAnswerAdvisor build() {
            return new RagAnswerAdvisor(this.vectorStore, this.searchRequest, this.userTextAdvise, this.protectFromBlocking, this.order, this.queryTransformers, this.documentRetriever, this.documentPostProcessors, this.queryAugmenter);
        }

    }

    private static TaskExecutor buildDefaultTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("ai-advisor-");
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setMaxPoolSize(16);
        taskExecutor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        taskExecutor.initialize();
        return taskExecutor;
    }

    private Map.Entry<Query, List<Document>> getDocumentsForQuery(Query query) {
        List<Document> documents = this.documentRetriever.retrieve(query);
        return Map.entry(query, documents);
    }

}
