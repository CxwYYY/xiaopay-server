package com.xiao.xiaopay.domain.matcher.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单到账匹配记录。
 *
 * <p>记录自动匹配和后台人工匹配的全过程，用于订单详情追溯和异常核对。</p>
 */
@Data
@TableName("xp_order_match")
public class XpOrderMatch {
    /** 主键 ID。 */
    private Long id;
    /** XiaoPay 支付订单号。 */
    private String orderNo;
    /** 微信到账消息表主键 ID。 */
    private Long wechatMessageId;
    /** 匹配方式：AUTO 自动匹配，MANUAL 手动匹配。 */
    private String matchType;
    /** 匹配结果：MATCHED 成功，CONFLICT 冲突，MISMATCH 不匹配，UNBOUND 解除绑定。 */
    private String matchResult;
    /** 匹配原因、异常说明或人工处理备注。 */
    private String reason;
    /** 人工操作管理员 ID，自动匹配为空。 */
    private Long operatorId;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}
