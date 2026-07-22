package io.github.aimailabs.nexabase.file.api.client;

import io.github.aimailabs.nexabase.file.api.dto.FileRecordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 文件服务 Feign 客户端。
 * <p>
 * 供其他微服务调用文件服务的 API 接口。
 * <p>
 * 注意：文件上传和下载通常不通过 Feign，而是由前端直传或通过网关代理。
 * 此客户端仅用于查询文件元数据信息。
 */
@FeignClient(name = "nexabase-file", path = "/api/v1/file")
public interface FileClient {

    /**
     * 根据文件ID查询文件元数据（内部调用，不返回 Result 包装）。
     *
     * @param id 文件记录ID
     * @return 文件元数据
     */
    @GetMapping("/internal/{id}")
    FileRecordDTO getFileById(@PathVariable("id") Long id);
}
