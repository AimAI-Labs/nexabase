package io.github.aimailabs.nexabase.ai.prompt;

import io.github.aimailabs.nexabase.ai.dto.FusedHit;

import java.util.List;

/**
 * 多轮 RAG 提示词模板构建。
 */
public final class MultiTurnRagPromptTemplate {

    private MultiTurnRagPromptTemplate() {
    }

    public static final String DEFAULT_SYSTEM_INSTRUCTION = """
            你是一个企业知识库智能问答助手。请根据提供的【参考资料】和上下文历史回答用户的问题。
            若参考资料中没有相关答案，请明确说明"根据现有知识库资料无法回答该问题"，严禁胡乱编造。
            回答时，请在关键事实与论述后使用 [编号] 格式准确标注参考资料的引用角标（如 [1]、[2]）。""";

    public static String buildSystemPrompt(String customSystemPrompt, List<FusedHit> hits) {
        StringBuilder sb = new StringBuilder();
        if (customSystemPrompt != null && !customSystemPrompt.isBlank()) {
            sb.append(customSystemPrompt.trim()).append("\n\n");
        } else {
            sb.append(DEFAULT_SYSTEM_INSTRUCTION).append("\n\n");
        }

        if (hits == null || hits.isEmpty()) {
            sb.append("【参考资料】\n（暂无匹配的知识库切片资料）\n");
            return sb.toString();
        }

        sb.append("【参考资料】\n");
        for (int i = 0; i < hits.size(); i++) {
            FusedHit h = hits.get(i);
            sb.append("[").append(i + 1).append("] ")
                    .append(h.getTitle() == null ? "无标题文档" : h.getTitle()).append("\n");
            sb.append(h.getSnippet() == null ? "" : h.getSnippet().trim()).append("\n\n");
        }
        return sb.toString();
    }
}
