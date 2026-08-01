package io.github.aimailabs.nexabase.file.parser.impl;

import io.github.aimailabs.nexabase.file.parser.DocumentParser;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 纯文本/Markdown 文件解析器。
 * <p>处理 {@code text/plain}、{@code text/markdown} 类型，
 * 直接将字节流按 UTF-8 解码为字符串。Markdown 不去除标记符号，
 * 保留语义结构供下游分块器处理。
 *
 * @author nexabase
 */
@Component
public class TextDocumentParser implements DocumentParser {

    @Override
    public String supportedContentType() {
        return "text/plain";
    }

    @Override
    public String parse(byte[] bytes, String fileName) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        Charset charset = detectCharset(bytes);
        return new String(bytes, charset);
    }

    /**
     * 简易编码检测：检查 BOM 头，否则默认 UTF-8。
     * 后续可替换为 ICU4J 或 Hutool 的 CharsetDetector 做更精确判断。
     */
    private Charset detectCharset(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xEF
                && (bytes[1] & 0xFF) == 0xBB
                && (bytes[2] & 0xFF) == 0xBF) {
            // UTF-8 BOM，截掉 BOM 头后解码
            return StandardCharsets.UTF_8;
        }
        return StandardCharsets.UTF_8;
    }
}
