package io.github.aimailabs.nexabase.ai.chunk;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 句子感知分块器（中文友好）。
 * <p>规则：
 * <ol>
 *   <li>先按段落分隔符 \n\n 切段</li>
 *   <li>段内按中文标点（。！？!?）+ 换行 切句</li>
 *   <li>累加句子至达到 maxChars 则成块</li>
 *   <li>块间 overlap：从上一块末尾回退若干句子（不超过 overlapChars）作为下一块开头</li>
 * </ol>
 */
public class SentenceTextSplitter implements DocumentSplitter {

    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[^。！？\\n!?]+[。！？\\n!?]*");

    private final int maxChars;
    private final int overlapChars;

    public SentenceTextSplitter(int maxChars, int overlapChars) {
        if (maxChars <= 0) {
            throw new IllegalArgumentException("maxChars 必须为正");
        }
        if (overlapChars < 0) {
            throw new IllegalArgumentException("overlapChars 不可为负");
        }
        this.maxChars = maxChars;
        this.overlapChars = overlapChars;
    }

    @Override
    public List<TextSegment> split(Document document) {
        String text = document.text();
        List<String> sentences = splitIntoSentences(text);
        List<TextSegment> segments = new ArrayList<>();

        StringBuilder current = new StringBuilder();
        List<String> pendingOverlap = new ArrayList<>();
        int chunkIndex = 0;

        for (String sentence : sentences) {
            // 超长单句：硬切（避免无限累积）
            if (sentence.length() > maxChars) {
                if (current.length() > 0) {
                    segments.add(buildSegment(current.toString(), document, chunkIndex++));
                    pendingOverlap = collectOverlap(current.toString());
                    current.setLength(0);
                    appendOverlap(current, pendingOverlap);
                }
                for (int i = 0; i < sentence.length(); i += maxChars) {
                    String hard = sentence.substring(i, Math.min(i + maxChars, sentence.length()));
                    segments.add(buildSegment(hard, document, chunkIndex++));
                }
                pendingOverlap.clear();
                current.setLength(0);
                continue;
            }

            if (current.length() + sentence.length() > maxChars && current.length() > 0) {
                segments.add(buildSegment(current.toString(), document, chunkIndex++));
                pendingOverlap = collectOverlap(current.toString());
                current.setLength(0);
                appendOverlap(current, pendingOverlap);
            }
            current.append(sentence);
        }
        if (current.length() > 0) {
            segments.add(buildSegment(current.toString(), document, chunkIndex++));
        }
        return segments;
    }

    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        for (String paragraph : text.split("\\n{2,}")) {
            Matcher m = SENTENCE_PATTERN.matcher(paragraph);
            while (m.find()) {
                String s = m.group().trim();
                if (!s.isEmpty()) {
                    sentences.add(s);
                }
            }
        }
        if (sentences.isEmpty() && !text.trim().isEmpty()) {
            sentences.add(text.trim());
        }
        return sentences;
    }

    private List<String> collectOverlap(String block) {
        if (overlapChars <= 0) {
            return List.of();
        }
        List<String> tail = new ArrayList<>();
        int len = 0;
        List<String> blockSentences = new ArrayList<>();
        Matcher m = SENTENCE_PATTERN.matcher(block);
        while (m.find()) {
            blockSentences.add(m.group());
        }
        for (int i = blockSentences.size() - 1; i >= 0; i--) {
            String s = blockSentences.get(i);
            if (len + s.length() > overlapChars && !tail.isEmpty()) {
                break;
            }
            tail.add(0, s);
            len += s.length();
        }
        return tail;
    }

    private void appendOverlap(StringBuilder current, List<String> overlap) {
        for (String s : overlap) {
            current.append(s);
        }
    }

    private TextSegment buildSegment(String text, Document document, int chunkIndex) {
        // 附加 chunk 序号到 metadata（Document.from(text) 默认无元数据）
        Metadata metadata = Metadata.from("chunkIndex", String.valueOf(chunkIndex));
        return TextSegment.from(text, metadata);
    }
}
