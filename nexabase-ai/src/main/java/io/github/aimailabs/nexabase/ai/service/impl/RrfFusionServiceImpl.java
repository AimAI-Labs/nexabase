package io.github.aimailabs.nexabase.ai.service.impl;

import io.github.aimailabs.nexabase.ai.dto.FusedHit;
import io.github.aimailabs.nexabase.ai.service.RrfFusionService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reciprocal Rank Fusion 实现。
 * <p>score(doc) = Σ 1/(k + rank)，rank 从 1 开始。
 * 同一 docId 多 chunk 取最高分 chunk 代表。
 */
@Service
public class RrfFusionServiceImpl implements RrfFusionService {

    @Override
    public List<FusedHit> fuse(List<FusedHit> vectorHits, List<FusedHit> bm25Hits, int k, int topK) {
        Map<Long, FusedHit> bestByDoc = new HashMap<>();
        Map<Long, Double> scoreByDoc = new HashMap<>();

        accumulate(vectorHits, k, bestByDoc, scoreByDoc, "VECTOR");
        accumulate(bm25Hits, k, bestByDoc, scoreByDoc, "BM25");

        return scoreByDoc.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topK)
                .map(e -> {
                    FusedHit h = bestByDoc.get(e.getKey());
                    h.setScore(e.getValue());
                    return h;
                })
                .collect(Collectors.toList());
    }

    private void accumulate(List<FusedHit> hits, int k,
                            Map<Long, FusedHit> bestByDoc,
                            Map<Long, Double> scoreByDoc,
                            String source) {
        List<FusedHit> sorted = hits.stream()
                .sorted(Comparator.comparingDouble(FusedHit::getScore).reversed())
                .collect(Collectors.toList());
        for (int rank = 0; rank < sorted.size(); rank++) {
            FusedHit h = sorted.get(rank);
            double contribution = 1.0 / (k + rank + 1);
            scoreByDoc.merge(h.getDocId(), contribution, Double::sum);
            // 保留该 doc 首次出现的 chunk（rank 最高）作为代表
            bestByDoc.putIfAbsent(h.getDocId(), copyWithSource(h, source));
        }
    }

    private FusedHit copyWithSource(FusedHit h, String source) {
        return new FusedHit(h.getDocId(), h.getTitle(), h.getSnippet(), h.getScore(), source);
    }
}
