package com.xiao.xiaopay.domain.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接入应用。
 *
 * <p>每个业务系统在 XiaoPay 中对应一个应用，应用负责持有签名身份、
 * 默认回调地址和运营状态。订单通过 {@code appId} 归属到具体业务系统。</p>
 */
@Data
@TableName("xp_app")
public class XpApp {
    /** 主键 ID。 */
    private Long id;
    /** 接入应用编号，用于业务方签名和订单归属。 */
    private String appId;
    /** 接入应用名称。 */
    private String appName;
    /** 应用签名密钥密文或哈希，创建/重置时仅明文返回一次。 */
    private String appSecret;
    /** 应用状态：ENABLED 启用，DISABLED 停用，DELETED 软删除。 */
    private String status;
    /** 应用默认支付结果回调地址，订单未传 notifyUrl 时使用。 */
    private String notifyUrl;
    /** 应用备注。 */
    private String remark;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
