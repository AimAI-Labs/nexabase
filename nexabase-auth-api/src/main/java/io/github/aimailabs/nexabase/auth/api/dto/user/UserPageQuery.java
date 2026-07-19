package io.github.aimailabs.nexabase.auth.api.dto.user;

import lombok.Data;

/**
 * 用户分页查询条件。
 */
@Data
public class UserPageQuery {

    /** 用户名（模糊匹配） */
    private String username;

    /** 状态：1-正常, 0-禁用, null-全部 */
    private Integer status;

    /** 团队 ID（筛选该团队下用户） */
    private Long teamId;

    /** 页码，默认 1 */
    private Integer pageNum = 1;

    /** 每页条数，默认 10 */
    private Integer pageSize = 10;
}
