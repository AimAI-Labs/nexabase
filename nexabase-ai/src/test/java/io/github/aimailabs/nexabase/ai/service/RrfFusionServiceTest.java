package io.github.aimailabs.nexabase.ai.service;

import io.github.aimailabs.nexabase.ai.dto.FusedHit;
import io.github.aimailabs.nexabase.ai.service.impl.RrfFusionServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RrfFusionServiceTest {

    private final RrfFusionService service = new RrfFusionServiceImpl();

    @Test
    void shouldRankDocAppearingInBothPathsHigher() {
        var vectorHits = List.of(
                new FusedHit(1L, "A", "va", 0.9, "VECTOR"),
                new FusedHit(2L, "B", "vb", 0.8, "VECTOR"));
        var bm25Hits = List.of(
                new FusedHit(2L, "B", "bb", 5.0, "BM25"),
                new FusedHit(3L, "C", "cc", 4.0, "BM25"));

        List<FusedHit> fused = service.fuse(vectorHits, bm25Hits, 60, 3);

        // docB 同时出现在两路，融合分最高
        assertEquals(2L, fused.get(0).getDocId(), "docB 应排第一");
        assertEquals(3, fused.size());
    }

    @Test
    void shouldReturnEmptyWhenBothEmpty() {
        assertTrue(service.fuse(List.of(), List.of(), 60, 5).isEmpty());
    }

    @Test
    void shouldUseRRFFormula() {
        // 单路单命中：score = 1/(60+1)
        var single = service.fuse(List.of(new FusedHit(1L, "A", "a", 0.5, "VECTOR")), List.of(), 60, 5);
        assertEquals(1.0 / 61.0, single.get(0).getScore(), 1e-9);
    }
}
