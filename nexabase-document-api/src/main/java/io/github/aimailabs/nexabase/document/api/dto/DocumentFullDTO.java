package io.github.aimailabs.nexabase.document.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档完整视图 DTO（跨模块共享）。
 * <p>合并 MySQL {@code doc_info} 元数据与 MongoDB {@code doc_content} 正文，
 * 供 search/ai 消费者经 Feign 内部接口拉取后构建索引/向量。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFullDTO implements Serializable {

    /** 文档ID（对应 doc_info.id） */
    private Long docId;

    /** 文档标题 */
    private String title;

    /** 文档正文（来自 MongoDB doc_content） */
    private String content;

    /** 所属知识库ID */
    private Long kbId;

    /** 所属目录ID */
    private Long categoryId;

    /** 租户ID/团队ID */
    private Long tenantId;

    /** 文档状态：0-草稿，1-已发布 */
    private Integer status;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
