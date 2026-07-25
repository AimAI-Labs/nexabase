package io.github.aimailabs.nexabase.ai.chunk;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SentenceTextSplitterTest {

    @Test
    void shouldSplitBySentenceUpToMaxSize() {
        // 4 个中文句号结尾的句子，每句约 10 字
        String text = "这是第一句话的内容。这是第二句话的内容。这是第三句话的内容。这是第四句话的内容。";
        DocumentSplitter splitter = new SentenceTextSplitter(25, 0);

        List<TextSegment> segments = splitter.split(Document.from(text));

        assertTrue(segments.size() >= 2, "应至少切成 2 块");
        for (TextSegment s : segments) {
            assertTrue(s.text().length() <= 25,
                    "块长度 " + s.text().length() + " 超过上限 25: " + s.text());
            assertTrue(s.text().endsWith("。"),
                    "块未在句号处截断: " + s.text());
        }
    }

    @Test
    void shouldRespectOverlap() {
        String text = "句子一内容。句子二内容。句子三内容。句子四内容。句子五内容。";
        DocumentSplitter splitter = new SentenceTextSplitter(20, 10);

        List<TextSegment> segments = splitter.split(Document.from(text));

        assertTrue(segments.size() >= 2);
        assertTrue(segments.get(1).text().contains("句子二内容") || segments.get(1).text().contains("句子三内容"),
                "overlap 块应回退包含上一块末句: " + segments.get(1).text());
    }

    @Test
    void shouldKeepShortTextAsSingleSegment() {
        String text = "短文本。";
        DocumentSplitter splitter = new SentenceTextSplitter(800, 200);
        List<TextSegment> segments = splitter.split(Document.from(text));
        assertEquals(1, segments.size());
        assertEquals("短文本。", segments.get(0).text());
    }
}
