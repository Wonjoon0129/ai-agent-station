package top.kimwonjoon.domain.agent.service.armory.factory.element;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @ClassName QueryTransformer
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/5/24 11:41
 * @Version 1.0
 */
@Slf4j
public class RagQueryTransformer implements QueryTransformer {
    ChatModel chatModel;
    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("Given a user query, rewrite it to provide better results when querying a {target}.\nRemove any irrelevant information, and ensure the query is concise and specific.\n\nOriginal query:\n{query}\n\nRewritten query:\n");
    private static final String DEFAULT_TARGET = "vector store";
    private final String targetSearchSystem;

    public RagQueryTransformer( @Nullable String targetSearchSystem, ChatModel model) {
        this.chatModel = model;
        this.targetSearchSystem = targetSearchSystem != null ? targetSearchSystem : "vector store";
    }

    @Override
    public Query transform(Query query) {
        Assert.notNull(query, "query cannot be null");
        StringBuilder rewrittenQueryText = new StringBuilder();
        UserMessage userMessage=new UserMessage(query.text()+"请你用一句话回答");

        String content = ChatClient.create(chatModel).prompt().messages(userMessage)
                .call()
                .content();

        String regex = "<think>[\\s\\S]*?</think>";
        String trim = null;
        if (content != null) {
            trim = content.replaceAll(regex, "").trim();
        }
        rewrittenQueryText.append(query.text()).append("\n-----------------\n").append(trim);
        if (!StringUtils.hasText(rewrittenQueryText)) {
            log.warn("Query rewrite result is null/empty. Returning the input query unchanged.");
            return query;
        } else {
            return query.mutate().text(rewrittenQueryText.toString()).build();
        }
    }

    @Override
    public Query apply(Query query) {
        return transform(query);
    }

}
