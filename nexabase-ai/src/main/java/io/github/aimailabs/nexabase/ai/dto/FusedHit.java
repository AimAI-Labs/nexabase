package io.github.aimailabs.nexabase.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** RRF 融合后的命中（统一两路召回结果）。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FusedHit {
    private Long docId;
    private String title;
    private String snippet;     // 向量路取 chunk content，BM25 路取 highlight 片段
    private double score;       // RRF 融合分
    private String source;      // VECTOR / BM25（主要来源）
}
