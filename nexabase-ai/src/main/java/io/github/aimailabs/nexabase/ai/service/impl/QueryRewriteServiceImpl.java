package io.github.aimailabs.nexabase.ai.service.impl;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.Response;
import io.github.aimailabs.nexabase.ai.prompt.QueryRewritePromptTemplate;
import io.github.aimailabs.nexabase.ai.service.QueryRewriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 意图改写与独立问题生成服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryRewriteServiceImpl implements QueryRewriteService {

    private final QwenChatModel chatModel;

    @Override
    public String rewrite(String query, List<ChatMessage> history) {
        if (query == null || query.isBlank()) {
            return "";
        }
        if (history == null || history.isEmpty()) {
            log.debug("[QueryRewrite] 首轮提问无历史上下文，跳过改写：query=\"{}\"", query);
            return query;
        }

        try {
            String userPrompt = QueryRewritePromptTemplate.buildUserMessage(query, history);
            List<ChatMessage> messages = List.of(
                    SystemMessage.from(QueryRewritePromptTemplate.SYSTEM),
                    UserMessage.from(userPrompt)
            );

            var response = chatModel.chat(messages);
            if (response != null && response.aiMessage() != null) {
                String rewritten = response.aiMessage().text();
                if (rewritten != null && !rewritten.isBlank()) {
                    rewritten = cleanOutput(rewritten.trim());
                    log.info("[QueryRewrite] 意图改写成功：原 query=\"{}\" -> 改写 query=\"{}\"", query, rewritten);
                    return rewritten;
                }
            }
        } catch (Exception e) {
            log.warn("[QueryRewrite] 意图改写异常，降级使用原始 query=\"{}\", 错误={}", query, e.getMessage());
        }
        return query;
    }

    private String cleanOutput(String text) {
        // 清理可能包裹的前后引号
        if ((text.startsWith("\"") && text.endsWith("\"")) || (text.startsWith("“") && text.endsWith("”"))) {
            text = text.substring(1, text.length() - 1).trim();
        }
        // 清理可能包含的 "改写后：" 等前缀
        if (text.startsWith("改写后的独立查询：")) {
            text = text.substring("改写后的独立查询：".length()).trim();
        } else if (text.startsWith("改写后：")) {
            text = text.substring("改写后：".length()).trim();
        }
        return text;
    }
}
