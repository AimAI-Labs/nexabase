package io.github.aimailabs.nexabase.ai.feign;

import io.github.aimailabs.nexabase.foundation.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 调用 nexabase-search BM25 召回接口。
 * <p>search 侧返回 {@code Result<Bm25SearchResponse>}，此处以 Map 接收避免 ai 模块依赖 search 的 DTO。
 */
@FeignClient(name = "nexabase-search", path = "/api/v1/search")
public interface SearchBm25Client {

    @PostMapping("/bm25")
    Result<Map<String, Object>> bm25Search(@RequestBody Map<String, Object> request);
}
