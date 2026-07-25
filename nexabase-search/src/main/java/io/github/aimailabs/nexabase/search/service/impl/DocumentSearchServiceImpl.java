package io.github.aimailabs.nexabase.search.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import io.github.aimailabs.nexabase.search.dto.Bm25Hit;
import io.github.aimailabs.nexabase.search.dto.Bm25SearchRequest;
import io.github.aimailabs.nexabase.search.dto.Bm25SearchResponse;
import io.github.aimailabs.nexabase.search.entity.EsDoc;
import io.github.aimailabs.nexabase.search.service.DocumentSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * BM25 关键词检索实现。
 * <p>使用 NativeQuery + bool(multi_match must + term filter) + 高亮，
 * title 加权 ^3 高于 content。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentSearchServiceImpl implements DocumentSearchService {

    private final ElasticsearchOperations operations;

    @Override
    public Bm25SearchResponse bm25Search(Bm25SearchRequest req) {
        // bool 查询：must=multi_match，filter=kbId/categoryId 精确过滤
        Query boolQuery = Query.of(q -> q.bool(b -> {
            b.must(m -> m.multiMatch(mm -> mm
                    .query(req.getQuery())
                    .fields("title^3", "content")));
            if (req.getKbId() != null) {
                b.filter(f -> f.term(t -> t.field("kbId").value(req.getKbId())));
            }
            if (req.getCategoryId() != null) {
                b.filter(f -> f.term(t -> t.field("categoryId").value(req.getCategoryId())));
            }
            return b;
        }));

        Highlight highlight = new Highlight(
                HighlightParameters.builder()
                        .withPreTags("<em>")
                        .withPostTags("</em>")
                        .withFragmentSize(150)
                        .withNumberOfFragments(3)
                        .build(),
                List.of(new HighlightField("title"), new HighlightField("content"))
        );

        NativeQuery query = NativeQuery.builder()
                .withQuery(boolQuery)
                .withHighlightQuery(new HighlightQuery(highlight, EsDoc.class))
                .withPageable(PageRequest.of(req.getPage(), req.getSize()))
                .build();

        SearchHits<EsDoc> hits = operations.search(query, EsDoc.class);

        List<Bm25Hit> hitList = new ArrayList<>();
        for (SearchHit<EsDoc> hit : hits) {
            Bm25Hit h = new Bm25Hit();
            EsDoc doc = hit.getContent();
            h.setDocId(doc.getDocId());
            h.setTitle(doc.getTitle());
            h.setScore(hit.getScore());
            h.setKbId(doc.getKbId());
            // 高亮片段：title 优先，content 补充
            List<String> fragments = new ArrayList<>();
            List<String> titleHl = hit.getHighlightField("title");
            if (titleHl != null) {
                fragments.addAll(titleHl);
            }
            List<String> contentHl = hit.getHighlightField("content");
            if (contentHl != null) {
                fragments.addAll(contentHl);
            }
            h.setHighlightFragments(fragments);
            hitList.add(h);
        }

        Bm25SearchResponse resp = new Bm25SearchResponse();
        resp.setTotal(hits.getTotalHits());
        resp.setHits(hitList);
        return resp;
    }
}
