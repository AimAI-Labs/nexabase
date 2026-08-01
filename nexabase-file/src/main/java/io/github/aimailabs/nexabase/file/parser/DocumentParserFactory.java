package io.github.aimailabs.nexabase.file.parser;

import io.github.aimailabs.nexabase.file.parser.impl.TextDocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档解析器工厂。
 * <p>
 * 启动时收集所有 {@link DocumentParser} Bean，按 MIME 类型构建路由表。
 * 同时支持通过文件扩展名做 fallback 匹配（如 .md 虽未声明 text/markdown）。
 * <p>
 * 设计原则：开闭原则——新增格式只需新增 {@link DocumentParser} 实现类，
 * 无需修改工厂逻辑。
 */
@Slf4j
@Component
public class DocumentParserFactory {

    private final Map<String, DocumentParser> parserByContentType = new HashMap<>();
    private final DocumentParser fallbackParser;

    public DocumentParserFactory(List<DocumentParser> parsers) {
        // 默认回退解析器：纯文本
        this.fallbackParser = new TextDocumentParser();
        for (DocumentParser p : parsers) {
            parserByContentType.put(p.supportedContentType().toLowerCase(), p);
            log.info("注册文档解析器: {} -> {}", p.supportedContentType(), p.getClass().getSimpleName());
        }
    }

    /**
     * 根据 MIME 类型和文件名选择解析器。
     * <p>匹配优先级：精确 MIME 匹配 > 扩展名推断 > 纯文本回退。
     *
     * @param contentType MIME 类型
     * @param fileName    原始文件名
     * @return 文档解析器实例（永不返回 null，至少有纯文本回退）
     */
    public DocumentParser getParser(String contentType, String fileName) {
        if (contentType != null) {
            DocumentParser parser = parserByContentType.get(contentType.toLowerCase());
            if (parser != null) {
                return parser;
            }
        }
        // markdown 扩展名 fallback：.md / .markdown 也使用纯文本解析器
        if (fileName != null) {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".md") || lower.endsWith(".markdown") || lower.endsWith(".txt")) {
                return fallbackParser;
            }
        }
        log.warn("未找到匹配的文档解析器, contentType={}, fileName={}, 回退至纯文本解析", contentType, fileName);
        return fallbackParser;
    }
}
