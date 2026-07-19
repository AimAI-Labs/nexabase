package io.github.aimailabs.nexabase.auth.api.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色视图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
}
