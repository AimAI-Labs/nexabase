package io.github.aimailabs.nexabase.file.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 文件内容 Feign 客户端。
 * <p>
 * 供 nexabase-document 等模块回调拉取文件解析后的纯文本内容，
 * 用于写入 MongoDB 正文并触发后续向量化流程。
 *
 * @module nexabase-file
 */
@FeignClient(name = "nexabase-file", contextId = "fileContent", path = "/api/v1/file/internal")
public interface FileContentClient {

    /**
     * 拉取文件解析后的纯文本内容。
     * <p>
     * 文件服务根据 content_type 自动选择解析器（txt/md 直接读取，
     * pdf/docx 等二进制格式由对应的解析器提取文本）。
     * 若文件类型暂不支持解析，返回空字符串。
     *
     * @param id 文件记录ID
     * @return 解析后的纯文本内容
     */
    @GetMapping("/content/{id}")
    String getFileContent(@PathVariable("id") Long id);
}
