package com.xiao.xiaopay.domain.audit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台审计日志。
 *
 * <p>记录后台人工操作、状态变更、手动匹配和回调重试，保证异常处理可追溯。</p>
 */
@Data
@TableName("xp_audit_log")
public class XpAuditLog {
    /** 主键 ID。 */
    private Long id;
    /** 操作管理员 ID。 */
    private Long operatorId;
    /** 操作动作编码。 */
    private String action;
    /** 操作对象类型。 */
    private String targetType;
    /** 操作对象 ID 或业务编号。 */
    private String targetId;
    /** 操作前对象快照 JSON。 */
    private String beforeJson;
    /** 操作后对象快照 JSON。 */
    private String afterJson;
    /** 操作者 IP。 */
    private String ip;
    /** 操作者 User-Agent。 */
    private String userAgent;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}
