package io.github.aimailabs.nexabase.search.init;

import io.github.aimailabs.nexabase.search.entity.EsDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ES 索引初始化器。
 * <p>启动时检查 {@code doc_index_v1} 是否存在，不存在则创建并写入 IK mapping。
 * 已存在则跳过（避免覆盖线上 mapping）。
 * 若由于 ES 实例未安装 IK 插件导致创建失败，将自动降级为 standard 分词器进行 fallback 创建。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EsIndexInitializer implements ApplicationRunner {

    private final ElasticsearchOperations operations;

    @Override
    public void run(ApplicationArguments args) {
        IndexOperations indexOps = operations.indexOps(EsDoc.class);
        if (indexOps.exists()) {
            log.info("ES 索引 doc_index_v1 已存在，跳过创建");
            return;
        }
        try {
            indexOps.createWithMapping();
            log.info("ES 索引 doc_index_v1 创建成功");
        } catch (Exception e) {
            log.warn("默认创建 ES 索引失败，可能因为未安装 IK 分词器插件。尝试使用 standard 分词器作为 fallback 创建。错误信息: {}", e.getMessage());
            try {
                // 构造 fallback settings，自定义 ik_smart 和 ik_max_word 指向 standard 分词器
                Map<String, Object> settings = new HashMap<>();
                Map<String, Object> analysis = new HashMap<>();
                Map<String, Object> analyzer = new HashMap<>();

                Map<String, Object> ikSmart = new HashMap<>();
                ikSmart.put("type", "standard");
                analyzer.put("ik_smart", ikSmart);

                Map<String, Object> ikMaxWord = new HashMap<>();
                ikMaxWord.put("type", "standard");
                analyzer.put("ik_max_word", ikMaxWord);

                analysis.put("analyzer", analyzer);
                settings.put("analysis", analysis);

                // 创建索引并应用 settings
                indexOps.create(Document.from(settings));
                // 写入 mapping
                indexOps.putMapping(indexOps.createMapping());
                log.info("ES 索引 doc_index_v1 使用 fallback standard 分词器创建成功");
            } catch (Exception ex) {
                log.error("使用 fallback 创建 ES 索引依然失败", ex);
                throw ex;
            }
        }
    }
}
