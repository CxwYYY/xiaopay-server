package com.xiao.xiaopay.domain.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台管理员用户。
 *
 * <p>第一版用于管理端登录和操作归属，后续可扩展角色和权限模型。</p>
 */
@Data
@TableName("xp_admin_user")
public class XpAdminUser {
    /** 主键 ID。 */
    private Long id;
    /** 管理员登录用户名。 */
    private String username;
    /** 管理员密码哈希。 */
    private String passwordHash;
    /** 管理员昵称。 */
    private String nickname;
    /** 管理员状态：ENABLED 启用，DISABLED 停用。 */
    private String status;
    /** 最后登录时间。 */
    private LocalDateTime lastLoginAt;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
