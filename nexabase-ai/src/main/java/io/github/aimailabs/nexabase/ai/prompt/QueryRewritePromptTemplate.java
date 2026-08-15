package io.github.aimailabs.nexabase.ai.prompt;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.List;

/**
 * 意图改写 Prompt 模板。
 */
public final class QueryRewritePromptTemplate {

    private QueryRewritePromptTemplate() {
    }

    public static final String SYSTEM = """
            你是一个专业的搜索查询改写助手。你的任务是结合多轮对话历史，将用户的最新提问改写为一个语义完整、不包含代词（如"它"、"该组件"、"其"等）和省略指代的独立查询语句，以便于在知识库中进行精确检索。
            
            【改写规则】
            1. 若最新提问中包含代词或省略了主语/宾语，必须结合上下文补全实体与上下文；
            2. 若最新提问本身语义完整，或者属于通用问候（如"你好"、"早安"），请原样输出最新提问；
            3. 仅输出改写后的一句话，严禁添加任何解释、前缀（如"改写后："）或 markdown 格式。""";

    public static String buildUserMessage(String query, List<ChatMessage> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("【对话历史】\n");
        for (ChatMessage m : history) {
            if (m instanceof UserMessage um) {
                sb.append("User: ").append(um.singleText()).append("\n");
            } else if (m instanceof AiMessage am) {
                // 仅截取助手回答的前 100 字符作为意图指代线索，避免 Prompt 过长
                String text = am.text();
                if (text != null && text.length() > 100) {
                    text = text.substring(0, 100) + "...";
                }
                sb.append("Assistant: ").append(text).append("\n");
            }
        }
        sb.append("\n【最新提问】\n").append(query).append("\n\n【改写后的独立查询】\n");
        return sb.toString();
    }
}
