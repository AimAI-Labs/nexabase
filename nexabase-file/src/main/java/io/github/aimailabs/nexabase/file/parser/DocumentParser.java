package io.github.aimailabs.nexabase.file.parser;

/**
 * 文档解析器 SPI 接口。
 * <p>
 * 不同文件格式（txt、md、pdf、docx 等）各实现一个 {@link DocumentParser}，
 * 由 {@link DocumentParserFactory} 根据 MIME 类型或扩展名路由分发。
 *
 * <p>遵循开闭原则：新增格式只需新增实现类 + 在 Factory 注册，不修改既有代码。
 */
public interface DocumentParser {

    /**
     * 支持的 MIME 类型（如 "text/plain"），用于路由匹配。
     *
     * @return MIME 类型字符串
     */
    String supportedContentType();

    /**
     * 从原始字节流解析出纯文本内容。
     *
     * @param bytes    文件字节数组
     * @param fileName 原始文件名（可辅助判断格式，如 .md vs .txt）
     * @return 解析后的纯文本；解析失败返回空字符串
     */
    String parse(byte[] bytes, String fileName);
}
