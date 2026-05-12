# XiaoPay Server Design

## 目标

`xiaopay-server` 是 XiaoPay 的云端支付中台。它面向多个业务项目提供统一的支付订单、到账匹配、回调通知、订单管理和统计能力。

第一版只支持微信个人收款码，通过 `xiaopay-agent` 采集 Windows 微信到账消息。支付通道模型从第一版开始按可扩展设计，后续可接入支付宝等通道。

## 技术栈

- JDK 21
- Spring Boot 3
- MySQL 8
- Redis
- MyBatis Plus
- Sa-Token
- Hutool
- `xiaopay-console`: Vue 3 + TypeScript + Vite + Element Plus + ECharts

## 系统边界

XiaoPay 负责：

- 接入应用管理
- 支付通道管理
- 创建支付订单
- 生成付款二维码、金额、付款备注 `payNum`
- 接收 `xiaopay-agent` 推送的微信到账消息
- 自动匹配到账消息和订单
- 记录原始到账、匹配、回调、人工操作日志
- 通知业务系统支付成功
- 后台订单、到账、统计、异常处理

业务项目负责：

- 用户体系
- 商品、卡密、会员、内容等业务资源
- 发卡、发货、会员开通、内容解锁
- 接收 XiaoPay 回调后完成业务交付

XiaoPay 不解析具体业务 payload，不直接发卡或解锁内容。中台只保存并回传业务上下文。

## 模块划分

### auth

后台管理员登录、退出、会话、权限控制。第一版使用 Sa-Token，先做单管理员或简单角色，后续再扩展细粒度权限。

### app

接入应用管理。每个业务项目一个应用，例如发卡网、商城、小程序、内容站。

### channel

支付通道管理。第一版只启用微信个人收款码，字段保留支付宝和其他通道扩展能力。

### agent

管理 Windows 采集器实例，包括 agentId、绑定通道、最后心跳、健康状态、最近错误。

### order

支付订单创建、查询、过期、关闭、状态流转。订单是业务项目和支付中台之间的核心契约。

### collector

接收 `xiaopay-agent` 推送的微信到账消息，做签名校验、幂等入库和后续匹配触发。

### matcher

到账匹配引擎。第一版按 `channelId + payNum + amount + pending + timeWindow` 匹配订单。

### notify

支付成功回调业务项目。支持订单级 `notifyUrl` 优先，应用默认 `notifyUrl` 兜底。失败回调需要持久化并重试。

### dashboard

后台总览统计，例如今日订单数、待支付、已支付、已完成、收入、项目收入排行。

### audit

记录后台人工操作、状态修改、手动匹配、重发回调等审计日志。

## 核心数据表

### xp_app

接入应用。

```text
id
app_id
app_name
app_secret
status
notify_url
remark
created_at
updated_at
```

约束：

- `app_id` 唯一。
- `app_secret` 只在创建/重置时明文返回一次，数据库建议加密或至少不可在后台明文展示。

### xp_channel

支付通道。

```text
id
channel_code
channel_name
channel_type       wechat / alipay
collector_type     agent / official_api / manual
agent_id
qr_code_url
receiver_name
status             enabled / disabled
config_json
created_at
updated_at
```

第一版示例：

```text
channel_type = wechat
collector_type = agent
channel_code = wechat_personal_001
```

### xp_agent

采集器实例。

```text
id
agent_id
agent_name
channel_id
wechat_account
host_name
status             online / offline / degraded
last_heartbeat_at
last_error
created_at
updated_at
```

### xp_pay_order

支付订单。

```text
id
app_id
app_order_no
order_no
channel_id
pay_type           wechat
amount
pay_num
pay_num_length
subject
description
buyer_id
buyer_name
notify_url
return_url
business_type      card / vip / content / shop / custom
business_payload   JSON
order_status       pending / paid / expired / closed / abnormal
notify_status      pending / success / failed / retrying / ignored
expire_at
paid_at
created_at
updated_at
```

约束：

- `app_id + app_order_no` 唯一。
- `order_no` 唯一。
- `channel_id + amount + pay_num` 不能在未过期 `pending` 订单集合里重复。MySQL 不直接表达时间窗口唯一，由生成逻辑和事务锁保证。

### xp_wechat_message

微信到账原始消息。

```text
id
agent_id
channel_id
message_id
amount
pay_num
remark_raw
pay_time
title
description
raw_content
match_status       unmatched / matched / duplicate / amount_mismatch / manual
matched_order_no
received_at
created_at
updated_at
```

约束：

- `agent_id + message_id` 唯一，保证 agent 重推幂等。

### xp_order_match

订单和到账消息匹配记录。

```text
id
order_no
wechat_message_id
match_type         auto / manual
match_result       matched / conflict / mismatch
reason
operator_id
created_at
```

### xp_notify_record

业务回调记录。

```text
id
app_id
order_no
notify_url
request_body
response_status
response_body
attempt_count
next_retry_at
notify_status      pending / success / failed / retrying
last_error
created_at
updated_at
```

### xp_pay_event

支付事件表，用于可靠驱动通知和后续扩展。第一版不引入 MQ，使用 MySQL 事件表作为可靠来源，再由定时任务扫描 `pending/retrying` 事件处理。

```text
id
event_id
event_type         PAY_ORDER_PAID / PAY_ORDER_EXPIRED / PAY_NOTIFY_SUCCESS / PAY_NOTIFY_FAILED
order_no
app_id
payload_json
event_status       pending / processing / success / failed / retrying
attempt_count
next_retry_at
last_error
created_at
updated_at
```

约束：

- `event_id` 唯一。
- 订单支付成功后先写 `PAY_ORDER_PAID` 事件，再由 notify 模块消费并写 `xp_notify_record`。

### xp_admin_user

后台管理员。

```text
id
username
password_hash
nickname
status
last_login_at
created_at
updated_at
```

### xp_audit_log

后台人工操作日志。

```text
id
operator_id
action
target_type
target_id
before_json
after_json
ip
user_agent
created_at
```

## payNum 规则

第一版微信订单使用用户付款备注 `payNum` 匹配。

默认规则：

```text
payNum = 1000-9999 四位纯数字
```

生成时保证：

```text
同一 channelId
同一 amount
pending 状态
未过期订单
payNum 不重复
```

如果四位数字冲突过多，自动升级为五位数字。第一版不做金额尾差，避免用户对付款金额产生疑惑。

并发控制：

- 创建订单时按 `channelId + amount` 加短 Redis lock。
- 在事务内生成 `payNum`、检查未过期 pending 冲突、插入订单。
- Redis lock 只是并发保护，MySQL 订单查询和状态约束仍是最终判断。

匹配时必须满足：

```text
channelId + payNum + amount + pending + 合理时间窗口
```

如果唯一命中，订单标记为已支付。如果多个命中、金额不一致、备注缺失或订单已过期，进入异常/人工处理。

## 订单级业务上下文

`notifyUrl` 只表示通知入口，不承载主要业务参数。

创建订单支持：

```text
notifyUrl
businessType
businessPayload
```

规则：

- `xp_pay_order.notify_url` 优先。
- 订单没有 `notify_url` 时回退到 `xp_app.notify_url`。
- `business_type` 标识业务类型，例如 `card`, `vip`, `content`, `shop`, `custom`。
- `business_payload` 保存业务上下文 JSON。
- XiaoPay 不解析业务 payload，只保存并在回调中原样带回。

示例：

```json
{
  "notifyUrl": "https://demo.com/api/card/pay-notify",
  "businessType": "card",
  "businessPayload": {
    "skuId": "card_100",
    "quantity": 1
  }
}
```

## 状态划分

不要用一个状态表达所有流程。

订单状态：

```text
pending
paid
expired
closed
abnormal
```

到账匹配状态：

```text
unmatched
matched
duplicate
amount_mismatch
manual
```

通知状态：

```text
pending
success
failed
retrying
ignored
```

典型场景：

- 钱没到：`order_status=pending`
- 钱到了并匹配：`order_status=paid`
- 钱到了但回调失败：`order_status=paid`, `notify_status=failed`
- 到账备注错了：微信消息 `match_status=unmatched`
- 到账金额不一致：微信消息 `match_status=amount_mismatch`

## 核心接口

## 签名和幂等

统一使用 HMAC-SHA256，不沿用旧 paypro 的 MD5。

业务项目请求头：

```text
X-XiaoPay-App
X-XiaoPay-Timestamp
X-XiaoPay-Nonce
X-XiaoPay-Signature
```

Agent 请求头：

```text
X-XiaoPay-Agent
X-XiaoPay-Timestamp
X-XiaoPay-Nonce
X-XiaoPay-Signature
```

签名基准字符串：

```text
timestamp + "\n" + nonce + "\n" + body-json
```

幂等规则：

- 创建订单：`appId + appOrderNo` 唯一。
- 到账消息：`agentId + messageId` 唯一。
- 支付事件：`eventId` 唯一。
- 通知业务项目：`notifyEventId` 唯一，业务项目收到重复回调时必须只处理一次。

密钥规则：

- `appSecret` 和 `agentSecret` 分离，不能混用。
- 创建或重置 secret 时只明文显示一次。
- 后台不明文展示 secret。
- 敏感字段不写入日志。

## 可靠通知

支付成功后不直接把 HTTP 回调当作唯一结果。流程为：

```text
订单匹配成功
  -> 更新 xp_pay_order 为 paid
  -> 写 xp_pay_event: PAY_ORDER_PAID
  -> notify 定时任务消费事件
  -> 写 xp_notify_record
  -> 回调成功则 notify_status=success
  -> 回调失败则记录错误并按 next_retry_at 重试
```

第一版不使用 MQ。以 MySQL 表保存事件和回调记录，适合每天几千单的规模，也方便后台排查。

XiaoPay 回调业务项目也需要签名：

```text
X-XiaoPay-App
X-XiaoPay-Timestamp
X-XiaoPay-Nonce
X-XiaoPay-Signature
```

回调 body 必须包含：

```text
notifyEventId
orderNo
appOrderNo
payStatus
amount
payNum
paidAt
businessType
businessPayload
```

### 业务项目接口

```text
POST /api/pay/orders
GET  /api/pay/orders/{orderNo}
POST /api/pay/orders/{orderNo}/close
```

创建订单请求字段：

```text
appOrderNo
amount
subject
description
buyerId
buyerName
notifyUrl
returnUrl
businessType
businessPayload
expireSeconds
timestamp
sign
```

创建订单返回字段：

```text
orderNo
appOrderNo
amount
payNum
payType
channelId
qrCodeUrl
orderStatus
expireAt
createdAt
```

### Agent 接口

```text
POST /api/collector/wechat/messages
POST /api/collector/heartbeat
```

`wechat/messages` 接收 agent 批量推送，要求签名、时间戳、agentId。服务端按 `agentId + messageId` 幂等入库。

### 后台接口

```text
POST /api/admin/login
POST /api/admin/logout
GET  /api/admin/dashboard/summary
CRUD /api/admin/apps
CRUD /api/admin/channels
GET  /api/admin/agents
GET  /api/admin/orders
GET  /api/admin/orders/{orderNo}
GET  /api/admin/wechat-messages
GET  /api/admin/notify-records
POST /api/admin/orders/{orderNo}/notify/retry
POST /api/admin/matches/manual
```

## 后台页面

第一版管理后台：

```text
登录页
总览仪表盘
订单列表
订单详情
到账消息列表
应用管理
支付通道管理
Agent 状态
回调记录
系统设置
```

订单详情必须聚合展示：

```text
订单基础信息
业务应用
付款二维码
payNum
订单状态
到账消息
匹配记录
回调记录
人工操作日志
```

人工操作按钮：

```text
手动绑定到账消息
解除错误绑定
标记到账为重复付款
重新发送回调
关闭订单
标记异常
延长订单过期时间
```

所有人工动作必须写 `xp_audit_log`。

## Agent 和通道绑定

第一版采用简单绑定：

```text
一个微信收款号 = 一个 channel
一个 xiaopay-agent = 绑定一个 channel
```

规则：

- `agentId` 绑定 `channelId`。
- agent 推送的微信到账消息只能进入绑定通道。
- agent 使用独立 `agentSecret` 签名，不能使用业务应用的 `appSecret`。
- 后续如需多个微信号或多个 agent，再扩展 channel 和 agent 关系。

## 金额和时间

金额：

- 数据库使用 `DECIMAL(18,2)`。
- Java 使用 `BigDecimal`。
- 禁止使用 float/double 表示金额。

时间：

- 订单过期、匹配窗口、重试时间以服务端时间为准。
- 数据库存储时间需要统一时区，建议生产统一使用 Asia/Shanghai 或 UTC，并在配置中明确。
- API 返回时间统一 ISO8601 字符串或毫秒时间戳，第一版建议统一毫秒时间戳，便于前端和 SDK 处理。

## 支付前端体验

业务项目不跳转到 XiaoPay。业务项目创建订单后，在自己的页面展示：

```text
付款二维码
付款金额
付款备注 payNum
过期倒计时
```

业务前端通过查询接口轮询订单状态：

```text
GET /api/pay/orders/{orderNo}
```

第一版建议每 2 秒轮询一次。后续如需要更实时体验，可增加 SSE/WebSocket，但不放入第一版 MVP。

## 统计口径

后台统计需要区分：

```text
订单金额
实际到账金额
已支付订单金额
已成功通知金额
异常到账金额
未匹配到账金额
```

总览页第一版至少展示：

```text
今日订单数
今日待支付订单数
今日已支付订单数
今日异常订单数
今日实际到账金额
今日未匹配到账金额
按应用收入排行
```

## 第一版 MVP

第一版只做完整支付闭环：

1. 后台创建接入应用。
2. 后台配置微信收款通道。
3. 业务项目调用创建订单 API。
4. 返回二维码、金额、payNum。
5. 用户扫码付款并填写备注。
6. agent 推送到账消息。
7. server 自动匹配订单。
8. server 回调业务项目。
9. 后台能查看订单、到账、回调、今日统计。

第一版暂不做：

- 内置发卡系统
- 商城系统
- 小程序支付组件
- 多管理员复杂权限
- 退款流程
- 多收款账号智能轮询
- 复杂财务报表
- 风控系统
- 支付宝接入

## 风险和边界

- 用户未填写备注：不自动匹配，进入未匹配到账。
- 用户填错备注：不自动匹配，后台人工处理。
- 用户重复付款：保存到账消息，订单只匹配一次，多余到账进入异常。
- 订单过期后付款：不自动成功，进入人工处理。
- 回调失败：订单仍为已支付，回调记录进入重试。
- agent 延迟推送：以订单状态和时间窗口判断，必要时人工处理。
- 同金额高并发：四位 payNum 冲突时自动升级五位数字。

## 下一步

设计确认后，进入实施计划：

1. 初始化 `xiaopay-server` Spring Boot 3 工程。
2. 建立数据库迁移或初始化 SQL。
3. 实现应用、通道、订单、collector、matcher、notify 的后端 MVP。
4. 初始化 `xiaopay-console` Vue 3 后台。
5. 联调 `xiaopay-agent -> xiaopay-server -> 业务回调`。
