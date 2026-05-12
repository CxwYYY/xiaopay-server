CREATE TABLE IF NOT EXISTS xp_app (
  id BIGINT PRIMARY KEY COMMENT '主键 ID',
  app_id VARCHAR(64) NOT NULL COMMENT '接入应用编号，用于业务方签名和订单归属',
  app_name VARCHAR(100) NOT NULL COMMENT '接入应用名称',
  app_secret VARCHAR(255) NOT NULL COMMENT '应用签名密钥密文或哈希，创建/重置时仅明文返回一次',
  status VARCHAR(32) NOT NULL COMMENT '应用状态：ENABLED 启用，DISABLED 停用，DELETED 软删除',
  notify_url VARCHAR(500) NULL COMMENT '应用默认支付结果回调地址，订单未传 notifyUrl 时使用',
  remark VARCHAR(500) NULL COMMENT '应用备注',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  UNIQUE KEY uk_xp_app_app_id (app_id)
) COMMENT='接入应用表，保存业务系统接入 XiaoPay 的身份、密钥和默认回调配置';

CREATE TABLE IF NOT EXISTS xp_channel (
  id BIGINT PRIMARY KEY COMMENT '主键 ID',
  channel_code VARCHAR(64) NOT NULL COMMENT '支付通道编码，后台可读的唯一标识',
  channel_name VARCHAR(100) NOT NULL COMMENT '支付通道名称',
  channel_type VARCHAR(32) NOT NULL COMMENT '支付通道类型：wechat 微信，后续可扩展 alipay 等',
  collector_type VARCHAR(32) NOT NULL COMMENT '采集方式：agent 本地采集器，official_api 官方接口，manual 手工',
  agent_id VARCHAR(64) NULL COMMENT '绑定的采集器编号，第一版一个 agent 绑定一个 channel',
  qr_code_url VARCHAR(1000) NOT NULL COMMENT '收款二维码地址或资源路径',
  receiver_name VARCHAR(100) NULL COMMENT '收款人展示名称',
  status VARCHAR(32) NOT NULL COMMENT '通道状态：ENABLED 启用，DISABLED 停用，DELETED 软删除',
  config_json JSON NULL COMMENT '通道扩展配置 JSON',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  UNIQUE KEY uk_xp_channel_code (channel_code)
) COMMENT='支付通道表，管理微信个人收款码及后续可扩展支付通道';

CREATE TABLE IF NOT EXISTS xp_agent (
  id BIGINT PRIMARY KEY COMMENT '主键 ID',
  agent_id VARCHAR(64) NOT NULL COMMENT '采集器编号，用于 agent 签名和身份识别',
  agent_secret VARCHAR(255) NOT NULL COMMENT '采集器签名密钥密文或哈希，创建/重置时仅明文返回一次',
  agent_name VARCHAR(100) NOT NULL COMMENT '采集器名称',
  channel_id BIGINT NOT NULL COMMENT '绑定的支付通道 ID',
  wechat_account VARCHAR(100) NULL COMMENT '采集器对应的微信账号标识',
  host_name VARCHAR(100) NULL COMMENT '采集器所在主机名',
  status VARCHAR(32) NOT NULL COMMENT '采集器状态：ONLINE 在线，OFFLINE 离线，DEGRADED 异常，DISABLED 停用，DELETED 软删除',
  last_heartbeat_at DATETIME NULL COMMENT '最后一次心跳时间',
  last_error VARCHAR(1000) NULL COMMENT '最近一次上报的错误信息',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  UNIQUE KEY uk_xp_agent_id (agent_id),
  KEY idx_xp_agent_channel (channel_id)
) COMMENT='Windows 微信到账采集器表，记录 agent 凭据、绑定通道和在线状态';

CREATE TABLE IF NOT EXISTS xp_agent_bind_code (
  id BIGINT PRIMARY KEY COMMENT 'Primary key',
  bind_code VARCHAR(64) NOT NULL COMMENT 'One-time code used by xiaopay-agent to claim credentials',
  agent_name VARCHAR(100) NOT NULL COMMENT 'Agent name created after successful claim',
  channel_id BIGINT NOT NULL COMMENT 'Bound payment channel ID',
  wechat_account VARCHAR(100) NULL COMMENT 'Optional WeChat account label',
  host_name VARCHAR(100) NULL COMMENT 'Optional host name',
  status VARCHAR(32) NOT NULL COMMENT 'PENDING, CLAIMED, EXPIRED or CANCELED',
  claimed_agent_id VARCHAR(64) NULL COMMENT 'Agent ID created by successful claim',
  expires_at DATETIME NOT NULL COMMENT 'Expiration time',
  claimed_at DATETIME NULL COMMENT 'Claim time',
  created_at DATETIME NOT NULL COMMENT 'Creation time',
  updated_at DATETIME NOT NULL COMMENT 'Last update time',
  UNIQUE KEY uk_xp_agent_bind_code (bind_code),
  KEY idx_xp_agent_bind_code_status (status, expires_at),
  KEY idx_xp_agent_bind_code_agent (claimed_agent_id)
) COMMENT='One-time binding code table for xiaopay-agent onboarding';

CREATE TABLE IF NOT EXISTS xp_pay_order (
  id BIGINT PRIMARY KEY COMMENT '主键 ID',
  app_id VARCHAR(64) NOT NULL COMMENT '接入应用编号',
  app_order_no VARCHAR(100) NOT NULL COMMENT '业务系统订单号，同一 app 下唯一',
  order_no VARCHAR(64) NOT NULL COMMENT 'XiaoPay 支付订单号，全局唯一',
  channel_id BIGINT NOT NULL COMMENT '本订单使用的支付通道 ID',
  pay_type VARCHAR(32) NOT NULL COMMENT '支付方式：wechat 微信',
  amount DECIMAL(18,2) NOT NULL COMMENT '订单应付金额',
  pay_num VARCHAR(16) NOT NULL COMMENT '用户付款备注识别码，用于到账匹配',
  pay_num_length INT NOT NULL COMMENT 'payNum 位数，默认 4 位，冲突过多时升级为 5 位',
  subject VARCHAR(200) NOT NULL COMMENT '订单标题',
  description VARCHAR(500) NULL COMMENT '订单描述',
  buyer_id VARCHAR(100) NULL COMMENT '业务系统买家 ID',
  buyer_name VARCHAR(100) NULL COMMENT '业务系统买家名称',
  notify_url VARCHAR(500) NULL COMMENT '订单级支付结果回调地址，优先于应用默认回调',
  return_url VARCHAR(500) NULL COMMENT '业务前端支付完成后的返回地址',
  business_type VARCHAR(64) NULL COMMENT '业务类型，如 card、vip、content、shop、custom',
  business_payload JSON NULL COMMENT '业务上下文 JSON，XiaoPay 原样保存并在回调中带回',
  order_status VARCHAR(32) NOT NULL COMMENT '订单状态：PENDING 待支付，PAID 已支付，EXPIRED 已过期，CLOSED 已关闭，ABNORMAL 异常',
  notify_status VARCHAR(32) NOT NULL COMMENT '回调状态：PENDING 待通知，SUCCESS 成功，FAILED 失败，RETRYING 重试中，IGNORED 无需通知',
  expire_at DATETIME NOT NULL COMMENT '订单过期时间',
  paid_at DATETIME NULL COMMENT '支付成功时间',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  UNIQUE KEY uk_xp_order_app_order (app_id, app_order_no),
  UNIQUE KEY uk_xp_order_no (order_no),
  KEY idx_xp_order_match (channel_id, amount, pay_num, order_status, expire_at),
  KEY idx_xp_order_created (created_at)
) COMMENT='支付订单表，承载业务下单、payNum 匹配、订单状态和回调状态';

CREATE TABLE IF NOT EXISTS xp_wechat_message (
  id BIGINT PRIMARY KEY COMMENT '主键 ID',
  agent_id VARCHAR(64) NOT NULL COMMENT '上报该消息的采集器编号',
  channel_id BIGINT NOT NULL COMMENT '消息归属的支付通道 ID',
  message_id VARCHAR(128) NOT NULL COMMENT '微信消息唯一 ID，同一 agent 下用于幂等',
  amount DECIMAL(18,2) NOT NULL COMMENT '微信到账金额',
  pay_num VARCHAR(32) NULL COMMENT '从付款备注中解析出的识别码，可能为空',
  remark_raw VARCHAR(500) NULL COMMENT '微信付款备注原文',
  pay_time DATETIME NOT NULL COMMENT '微信到账时间',
  title VARCHAR(500) NULL COMMENT '微信消息标题',
  description TEXT NULL COMMENT '微信消息描述',
  raw_content MEDIUMTEXT NULL COMMENT '原始消息内容或 XML，用于排查和人工核对',
  match_status VARCHAR(32) NOT NULL COMMENT '匹配状态：UNMATCHED 未匹配，MATCHED 已匹配，DUPLICATE 重复，AMOUNT_MISMATCH 金额不符，MANUAL 手动处理',
  matched_order_no VARCHAR(64) NULL COMMENT '已匹配的 XiaoPay 订单号',
  received_at DATETIME NOT NULL COMMENT '服务端接收消息时间',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  UNIQUE KEY uk_xp_wechat_message (agent_id, message_id),
  KEY idx_xp_wechat_match (channel_id, amount, pay_num, match_status, pay_time)
) COMMENT='微信到账消息表，保存 agent 上报的原始到账记录及匹配结果';

CREATE TABLE IF NOT EXISTS xp_order_match (
  id BIGINT PRIMARY KEY COMMENT '主键 ID',
  order_no VARCHAR(64) NOT NULL COMMENT 'XiaoPay 支付订单号',
  wechat_message_id BIGINT NOT NULL COMMENT '微信到账消息表主键 ID',
  match_type VARCHAR(32) NOT NULL COMMENT '匹配方式：AUTO 自动匹配，MANUAL 手动匹配',
  match_result VARCHAR(32) NOT NULL COMMENT '匹配结果：MATCHED 成功，CONFLICT 冲突，MISMATCH 不匹配，UNBOUND 解除绑定',
  reason VARCHAR(500) NULL COMMENT '匹配原因、异常说明或人工处理备注',
  operator_id BIGINT NULL COMMENT '人工操作管理员 ID，自动匹配为空',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  KEY idx_xp_match_order (order_no),
  KEY idx_xp_match_message (wechat_message_id)
) COMMENT='订单到账匹配记录表，记录自动和人工匹配过程';

CREATE TABLE IF NOT EXISTS xp_pay_event (
  id BIGINT PRIMARY KEY COMMENT '主键 ID',
  event_id VARCHAR(64) NOT NULL COMMENT '支付事件编号，用于幂等',
  event_type VARCHAR(64) NOT NULL COMMENT '事件类型，如 PAY_ORDER_PAID、PAY_ORDER_EXPIRED、PAY_NOTIFY_SUCCESS、PAY_NOTIFY_FAILED',
  order_no VARCHAR(64) NOT NULL COMMENT '关联的 XiaoPay 支付订单号',
  app_id VARCHAR(64) NOT NULL COMMENT '接入应用编号',
  payload_json JSON NULL COMMENT '事件载荷 JSON',
  event_status VARCHAR(32) NOT NULL COMMENT '事件状态：PENDING 待处理，PROCESSING 处理中，SUCCESS 成功，FAILED 失败，RETRYING 重试中',
  attempt_count INT NOT NULL COMMENT '事件处理尝试次数',
  next_retry_at DATETIME NULL COMMENT '下次重试时间',
  last_error VARCHAR(1000) NULL COMMENT '最近一次处理错误',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  UNIQUE KEY uk_xp_pay_event_id (event_id),
  KEY idx_xp_pay_event_scan (event_status, next_retry_at)
) COMMENT='支付事件表，用 MySQL 事件队列驱动回调通知和后续扩展';

CREATE TABLE IF NOT EXISTS xp_notify_record (
  id BIGINT PRIMARY KEY COMMENT '主键 ID',
  app_id VARCHAR(64) NOT NULL COMMENT '接入应用编号',
  order_no VARCHAR(64) NOT NULL COMMENT 'XiaoPay 支付订单号',
  notify_event_id VARCHAR(64) NOT NULL COMMENT '通知事件编号，用于业务方幂等处理',
  notify_url VARCHAR(500) NOT NULL COMMENT '实际请求的业务回调地址',
  request_body MEDIUMTEXT NOT NULL COMMENT '回调请求体 JSON',
  response_status INT NULL COMMENT '业务方 HTTP 响应状态码',
  response_body TEXT NULL COMMENT '业务方 HTTP 响应内容',
  attempt_count INT NOT NULL COMMENT '回调尝试次数',
  next_retry_at DATETIME NULL COMMENT '下次重试时间',
  notify_status VARCHAR(32) NOT NULL COMMENT '通知状态：PENDING 待通知，SUCCESS 成功，FAILED 失败，RETRYING 重试中',
  last_error VARCHAR(1000) NULL COMMENT '最近一次回调错误',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  UNIQUE KEY uk_xp_notify_event (notify_event_id),
  KEY idx_xp_notify_order (order_no),
  KEY idx_xp_notify_scan (notify_status, next_retry_at)
) COMMENT='业务回调记录表，保存支付成功通知请求、响应和重试状态';

CREATE TABLE IF NOT EXISTS xp_admin_user (
  id BIGINT PRIMARY KEY COMMENT '主键 ID',
  username VARCHAR(64) NOT NULL COMMENT '管理员登录用户名',
  password_hash VARCHAR(255) NOT NULL COMMENT '管理员密码哈希',
  nickname VARCHAR(100) NOT NULL COMMENT '管理员昵称',
  status VARCHAR(32) NOT NULL COMMENT '管理员状态：ENABLED 启用，DISABLED 停用',
  last_login_at DATETIME NULL COMMENT '最后登录时间',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  UNIQUE KEY uk_xp_admin_username (username)
) COMMENT='后台管理员用户表，第一版用于管理端登录和操作归属';

CREATE TABLE IF NOT EXISTS xp_audit_log (
  id BIGINT PRIMARY KEY COMMENT '主键 ID',
  operator_id BIGINT NULL COMMENT '操作管理员 ID',
  action VARCHAR(100) NOT NULL COMMENT '操作动作编码',
  target_type VARCHAR(100) NOT NULL COMMENT '操作对象类型',
  target_id VARCHAR(100) NOT NULL COMMENT '操作对象 ID 或业务编号',
  before_json JSON NULL COMMENT '操作前对象快照 JSON',
  after_json JSON NULL COMMENT '操作后对象快照 JSON',
  ip VARCHAR(64) NULL COMMENT '操作者 IP',
  user_agent VARCHAR(500) NULL COMMENT '操作者 User-Agent',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  KEY idx_xp_audit_target (target_type, target_id),
  KEY idx_xp_audit_created (created_at)
) COMMENT='后台审计日志表，记录人工操作、状态变更、手动匹配和回调重试';
