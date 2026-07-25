package io.github.aimailabs.nexabase.ai.prompt;

import io.github.aimailabs.nexabase.ai.dto.FusedHit;

import java.util.List;

/**
 * RAG 中文 Prompt 模板构建。
 * <p>{@link #SYSTEM} 为编译期常量，可被 AiServices 的 {@code @SystemMessage} 直接引用。
 */
public final class RagPromptTemplate {

    private RagPromptTemplate() {
    }

    public static final String SYSTEM = """
            你是一个企业知识库问答助手。请仅根据以下参考资料回答用户问题。
            若资料中没有答案，请明确说明"根据现有资料无法回答"，不要编造。
            回答时在关键信息后用 [编号] 标注引用来源。""";

    public static String buildContext(List<FusedHit> hits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hits.size(); i++) {
            FusedHit h = hits.get(i);
            sb.append("[").append(i + 1).append("] ")
                    .append(h.getTitle() == null ? "" : h.getTitle()).append("\n");
            sb.append(h.getSnippet() == null ? "" : h.getSnippet()).append("\n\n");
        }
        return sb.toString();
    }

    public static String buildUserMessage(String query, List<FusedHit> hits) {
        return """
                参考资料：
                %s

                用户问题：%s""".formatted(buildContext(hits), query);
    }
}
