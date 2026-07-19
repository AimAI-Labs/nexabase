package io.github.aimailabs.nexabase.auth.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体。
 * <p>
 * 不继承 {@link io.github.aimailabs.nexabase.auth.entity.base.BaseEntity}：
 * 仅追加写入，无逻辑删除与更新审计字段。
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 操作人 ID */
    private Long userId;

    /** 操作人名称 */
    private String username;

    /** 业务模块 */
    private String module;

    /** 操作动作 */
    private String action;

    /** HTTP 请求方法 */
    private String method;

    /** 请求 URL */
    private String url;

    /** 操作 IP 地址 */
    private String ipAddress;

    /** 请求参数(脱敏) */
    private String requestParams;

    /** 响应结果(截断存放) */
    private String responseResult;

    /** 状态: 1-成功, 0-失败 */
    private Integer status;

    /** 错误信息 */
    private String errorMsg;

    /** 耗时(毫秒) */
    private Long costTime;

    /** 操作时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
