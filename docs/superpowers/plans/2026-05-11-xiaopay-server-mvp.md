# XiaoPay Server MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the backend MVP for `xiaopay-server`: app/channel/agent management, signed order creation, WeChat message collection, order matching, reliable notification records, and admin query APIs.

**Architecture:** Implement a Spring Boot 3 modular monolith with clear package boundaries under `com.xiao.xiaopay`. Keep payment core logic in domain services, HTTP controllers thin, persistence isolated in mapper/entity classes, and cross-cutting concerns such as signing, id generation, and errors in shared packages. First backend MVP uses MySQL as the reliable source and Redis only for short payNum generation locks.

**Tech Stack:** JDK 21, Spring Boot 3, MyBatis Plus, MySQL 8, Redis, Sa-Token, Hutool, JUnit 5, AssertJ, Testcontainers optional for integration tests.

---

## Scope

This plan covers backend only:

- `xiaopay-server` Spring Boot 3 project scaffold.
- Core database schema.
- Business app, channel, agent, order, collector, matcher, event, notify, and admin query APIs.
- HMAC-SHA256 signing for business app and agent requests.
- Unit tests for non-trivial domain logic.

This plan does not cover:

- `xiaopay-console` Vue frontend.
- Windows service/tray packaging for `xiaopay-agent`.
- Alipay or other payment channels.
- Built-in card delivery, shop, membership, or content unlock business logic.

## File Structure

Create a Spring Boot backend with these focused packages:

```text
xiaopay-server/
  pom.xml
  src/main/java/com/xiao/xiaopay/XiaoPayServerApplication.java
  src/main/java/com/xiao/xiaopay/common/
    api/ApiResponse.java
    error/BusinessException.java
    error/GlobalExceptionHandler.java
    id/IdGenerator.java
    money/MoneyUtils.java
    security/SignatureService.java
    security/SignatureHeaders.java
    time/TimeProvider.java
  src/main/java/com/xiao/xiaopay/domain/
    app/
    channel/
    agent/
    order/
    collector/
    matcher/
    event/
    notify/
    admin/
    audit/
  src/main/resources/
    application.yml
    db/schema.sql
  src/test/java/com/xiao/xiaopay/
```

Rules:

- Controllers only validate transport-level input and call services.
- Services own business decisions and transactions.
- Mappers only perform persistence.
- Do not put matching, signing, or payNum logic inside controllers.
- Do not use float/double for money. Use `BigDecimal`.
- Do not log raw `appSecret`, `agentSecret`, or full request signatures.

---

### Task 1: Scaffold Spring Boot 3 Backend

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/com/xiao/xiaopay/XiaoPayServerApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `src/test/java/com/xiao/xiaopay/XiaoPayServerApplicationTests.java`

- [ ] **Step 1: Create Maven project file**

Create `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.6</version>
        <relativePath/>
    </parent>

    <groupId>com.xiao</groupId>
    <artifactId>xiaopay-server</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <name>xiaopay-server</name>

    <properties>
        <java.version>21</java.version>
        <mybatis-plus.version>3.5.9</mybatis-plus.version>
        <sa-token.version>1.39.0</sa-token.version>
        <hutool.version>5.8.34</hutool.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-spring-boot3-starter</artifactId>
            <version>${sa-token.version}</version>
        </dependency>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>${hutool.version}</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create application entrypoint**

Create `src/main/java/com/xiao/xiaopay/XiaoPayServerApplication.java`:

```java
package com.xiao.xiaopay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xiao.xiaopay.domain.**.mapper")
public class XiaoPayServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiaoPayServerApplication.class, args);
    }
}
```

- [ ] **Step 3: Create local configuration**

Create `src/main/resources/application.yml`:

```yaml
server:
  port: 18080

spring:
  application:
    name: xiaopay-server
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/xiaopay?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: root
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password:
      database: 0

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: assign_id

sa-token:
  token-name: xiaopay-token
  timeout: 2592000
  active-timeout: -1
  is-concurrent: true
  is-share: true
  token-style: uuid
  is-log: false

xiaopay:
  security:
    signature-window-seconds: 300
  paynum:
    default-length: 4
    fallback-length: 5
    max-attempts: 20
```

- [ ] **Step 4: Create boot smoke test**

Create `src/test/java/com/xiao/xiaopay/XiaoPayServerApplicationTests.java`:

```java
package com.xiao.xiaopay;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class XiaoPayServerApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 5: Run scaffold verification**

Run:

```powershell
mvn test
```

Expected: build succeeds or fails only because local MySQL/Redis is not available. If it fails due datasource, update the test to use a `test` profile with datasource auto-config excluded before continuing.

---

### Task 2: Common API, Errors, Time, IDs, Money, and Signing

**Files:**
- Create: `src/main/java/com/xiao/xiaopay/common/api/ApiResponse.java`
- Create: `src/main/java/com/xiao/xiaopay/common/error/BusinessException.java`
- Create: `src/main/java/com/xiao/xiaopay/common/error/GlobalExceptionHandler.java`
- Create: `src/main/java/com/xiao/xiaopay/common/id/IdGenerator.java`
- Create: `src/main/java/com/xiao/xiaopay/common/money/MoneyUtils.java`
- Create: `src/main/java/com/xiao/xiaopay/common/security/SignatureHeaders.java`
- Create: `src/main/java/com/xiao/xiaopay/common/security/SignatureService.java`
- Create: `src/main/java/com/xiao/xiaopay/common/time/TimeProvider.java`
- Test: `src/test/java/com/xiao/xiaopay/common/security/SignatureServiceTest.java`
- Test: `src/test/java/com/xiao/xiaopay/common/money/MoneyUtilsTest.java`

- [ ] **Step 1: Write failing signature test**

Create `SignatureServiceTest.java`:

```java
package com.xiao.xiaopay.common.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignatureServiceTest {
    @Test
    void signsTimestampNonceAndBodyWithHmacSha256() {
        SignatureService service = new SignatureService();

        String signature = service.sign("secret-123", "1778490000000", "nonce-1", "{\"amount\":\"19.90\"}");

        assertThat(signature).isEqualTo("6f0eca8e5f4b63325b74eea3f9eda328a8e66e5f6963150195e50624c22067d7");
    }

    @Test
    void verifiesSignatureCaseInsensitively() {
        SignatureService service = new SignatureService();
        String body = "{\"amount\":\"19.90\"}";
        String signature = service.sign("secret-123", "1778490000000", "nonce-1", body).toUpperCase();

        assertThat(service.verify("secret-123", "1778490000000", "nonce-1", body, signature)).isTrue();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
mvn -Dtest=SignatureServiceTest test
```

Expected: FAIL because `SignatureService` does not exist.

- [ ] **Step 3: Implement signature service**

Create `SignatureService.java`:

```java
package com.xiao.xiaopay.common.security;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;

import java.nio.charset.StandardCharsets;

public class SignatureService {
    public String sign(String secret, String timestamp, String nonce, String body) {
        String canonical = timestamp + "\n" + nonce + "\n" + (body == null ? "" : body);
        HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, secret.getBytes(StandardCharsets.UTF_8));
        return hmac.digestHex(canonical);
    }

    public boolean verify(String secret, String timestamp, String nonce, String body, String signature) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        return sign(secret, timestamp, nonce, body).equalsIgnoreCase(signature.trim());
    }
}
```

Create `SignatureHeaders.java`:

```java
package com.xiao.xiaopay.common.security;

public record SignatureHeaders(
        String identity,
        String timestamp,
        String nonce,
        String signature
) {
}
```

- [ ] **Step 4: Write and run money normalization test**

Create `MoneyUtilsTest.java`:

```java
package com.xiao.xiaopay.common.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyUtilsTest {
    @Test
    void normalizesMoneyToTwoDecimalPlaces() {
        assertThat(MoneyUtils.normalize(new BigDecimal("19.9"))).isEqualByComparingTo("19.90");
    }

    @Test
    void rejectsMoreThanTwoDecimalPlaces() {
        assertThatThrownBy(() -> MoneyUtils.normalize(new BigDecimal("19.999")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("two decimal places");
    }
}
```

Run:

```powershell
mvn -Dtest=MoneyUtilsTest test
```

Expected: FAIL because `MoneyUtils` does not exist.

- [ ] **Step 5: Implement common classes**

Create `MoneyUtils.java`:

```java
package com.xiao.xiaopay.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {
    private MoneyUtils() {
    }

    public static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("amount is required");
        }
        if (value.scale() > 2) {
            throw new IllegalArgumentException("amount must have at most two decimal places");
        }
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }
}
```

Create `ApiResponse.java`:

```java
package com.xiao.xiaopay.common.api;

public record ApiResponse<T>(int code, String message, T data) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

Create `BusinessException.java`:

```java
package com.xiao.xiaopay.common.error;

public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int code() {
        return code;
    }
}
```

Create `GlobalExceptionHandler.java`:

```java
package com.xiao.xiaopay.common.error;

import com.xiao.xiaopay.common.api.ApiResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.error(ex.code(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("invalid request");
        return ApiResponse.error(400, message);
    }
}
```

Create `IdGenerator.java`:

```java
package com.xiao.xiaopay.common.id;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class IdGenerator {
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

    public String orderNo() {
        return "XP" + DATE.format(LocalDate.now()) + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public String eventId() {
        return "EVT" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    public String appId() {
        return "APP" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    public String secret() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }
}
```

Create `TimeProvider.java`:

```java
package com.xiao.xiaopay.common.time;

import java.time.Clock;
import java.time.LocalDateTime;

public class TimeProvider {
    private final Clock clock;

    public TimeProvider() {
        this(Clock.systemDefaultZone());
    }

    public TimeProvider(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
```

- [ ] **Step 6: Run common tests**

Run:

```powershell
mvn -Dtest=SignatureServiceTest,MoneyUtilsTest test
```

Expected: PASS.

---

### Task 3: Database Schema and Domain Enums

**Files:**
- Create: `src/main/resources/db/schema.sql`
- Create enum files under `src/main/java/com/xiao/xiaopay/domain/**/model/`
- Test: `src/test/java/com/xiao/xiaopay/domain/order/model/PayOrderStatusTest.java`

- [ ] **Step 1: Create schema**

Create `src/main/resources/db/schema.sql` with all MVP tables:

```sql
CREATE TABLE IF NOT EXISTS xp_app (
  id BIGINT PRIMARY KEY,
  app_id VARCHAR(64) NOT NULL,
  app_name VARCHAR(100) NOT NULL,
  app_secret VARCHAR(255) NOT NULL,
  status VARCHAR(32) NOT NULL,
  notify_url VARCHAR(500) NULL,
  remark VARCHAR(500) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_xp_app_app_id (app_id)
);

CREATE TABLE IF NOT EXISTS xp_channel (
  id BIGINT PRIMARY KEY,
  channel_code VARCHAR(64) NOT NULL,
  channel_name VARCHAR(100) NOT NULL,
  channel_type VARCHAR(32) NOT NULL,
  collector_type VARCHAR(32) NOT NULL,
  agent_id VARCHAR(64) NULL,
  qr_code_url VARCHAR(1000) NOT NULL,
  receiver_name VARCHAR(100) NULL,
  status VARCHAR(32) NOT NULL,
  config_json JSON NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_xp_channel_code (channel_code)
);

CREATE TABLE IF NOT EXISTS xp_agent (
  id BIGINT PRIMARY KEY,
  agent_id VARCHAR(64) NOT NULL,
  agent_secret VARCHAR(255) NOT NULL,
  agent_name VARCHAR(100) NOT NULL,
  channel_id BIGINT NOT NULL,
  wechat_account VARCHAR(100) NULL,
  host_name VARCHAR(100) NULL,
  status VARCHAR(32) NOT NULL,
  last_heartbeat_at DATETIME NULL,
  last_error VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_xp_agent_id (agent_id),
  KEY idx_xp_agent_channel (channel_id)
);

CREATE TABLE IF NOT EXISTS xp_pay_order (
  id BIGINT PRIMARY KEY,
  app_id VARCHAR(64) NOT NULL,
  app_order_no VARCHAR(100) NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  channel_id BIGINT NOT NULL,
  pay_type VARCHAR(32) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  pay_num VARCHAR(16) NOT NULL,
  pay_num_length INT NOT NULL,
  subject VARCHAR(200) NOT NULL,
  description VARCHAR(500) NULL,
  buyer_id VARCHAR(100) NULL,
  buyer_name VARCHAR(100) NULL,
  notify_url VARCHAR(500) NULL,
  return_url VARCHAR(500) NULL,
  business_type VARCHAR(64) NULL,
  business_payload JSON NULL,
  order_status VARCHAR(32) NOT NULL,
  notify_status VARCHAR(32) NOT NULL,
  expire_at DATETIME NOT NULL,
  paid_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_xp_order_app_order (app_id, app_order_no),
  UNIQUE KEY uk_xp_order_no (order_no),
  KEY idx_xp_order_match (channel_id, amount, pay_num, order_status, expire_at),
  KEY idx_xp_order_created (created_at)
);

CREATE TABLE IF NOT EXISTS xp_wechat_message (
  id BIGINT PRIMARY KEY,
  agent_id VARCHAR(64) NOT NULL,
  channel_id BIGINT NOT NULL,
  message_id VARCHAR(128) NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  pay_num VARCHAR(32) NULL,
  remark_raw VARCHAR(500) NULL,
  pay_time DATETIME NOT NULL,
  title VARCHAR(500) NULL,
  description TEXT NULL,
  raw_content MEDIUMTEXT NULL,
  match_status VARCHAR(32) NOT NULL,
  matched_order_no VARCHAR(64) NULL,
  received_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_xp_wechat_message (agent_id, message_id),
  KEY idx_xp_wechat_match (channel_id, amount, pay_num, match_status, pay_time)
);

CREATE TABLE IF NOT EXISTS xp_order_match (
  id BIGINT PRIMARY KEY,
  order_no VARCHAR(64) NOT NULL,
  wechat_message_id BIGINT NOT NULL,
  match_type VARCHAR(32) NOT NULL,
  match_result VARCHAR(32) NOT NULL,
  reason VARCHAR(500) NULL,
  operator_id BIGINT NULL,
  created_at DATETIME NOT NULL,
  KEY idx_xp_match_order (order_no),
  KEY idx_xp_match_message (wechat_message_id)
);

CREATE TABLE IF NOT EXISTS xp_pay_event (
  id BIGINT PRIMARY KEY,
  event_id VARCHAR(64) NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  app_id VARCHAR(64) NOT NULL,
  payload_json JSON NULL,
  event_status VARCHAR(32) NOT NULL,
  attempt_count INT NOT NULL,
  next_retry_at DATETIME NULL,
  last_error VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_xp_pay_event_id (event_id),
  KEY idx_xp_pay_event_scan (event_status, next_retry_at)
);

CREATE TABLE IF NOT EXISTS xp_notify_record (
  id BIGINT PRIMARY KEY,
  app_id VARCHAR(64) NOT NULL,
  order_no VARCHAR(64) NOT NULL,
  notify_event_id VARCHAR(64) NOT NULL,
  notify_url VARCHAR(500) NOT NULL,
  request_body MEDIUMTEXT NOT NULL,
  response_status INT NULL,
  response_body TEXT NULL,
  attempt_count INT NOT NULL,
  next_retry_at DATETIME NULL,
  notify_status VARCHAR(32) NOT NULL,
  last_error VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_xp_notify_event (notify_event_id),
  KEY idx_xp_notify_order (order_no),
  KEY idx_xp_notify_scan (notify_status, next_retry_at)
);

CREATE TABLE IF NOT EXISTS xp_admin_user (
  id BIGINT PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(100) NOT NULL,
  status VARCHAR(32) NOT NULL,
  last_login_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_xp_admin_username (username)
);

CREATE TABLE IF NOT EXISTS xp_audit_log (
  id BIGINT PRIMARY KEY,
  operator_id BIGINT NULL,
  action VARCHAR(100) NOT NULL,
  target_type VARCHAR(100) NOT NULL,
  target_id VARCHAR(100) NOT NULL,
  before_json JSON NULL,
  after_json JSON NULL,
  ip VARCHAR(64) NULL,
  user_agent VARCHAR(500) NULL,
  created_at DATETIME NOT NULL,
  KEY idx_xp_audit_target (target_type, target_id),
  KEY idx_xp_audit_created (created_at)
);
```

- [ ] **Step 2: Create enum classes**

Create these enums:

```java
package com.xiao.xiaopay.domain.order.model;

public enum OrderStatus {
    PENDING, PAID, EXPIRED, CLOSED, ABNORMAL
}
```

```java
package com.xiao.xiaopay.domain.order.model;

public enum NotifyStatus {
    PENDING, SUCCESS, FAILED, RETRYING, IGNORED
}
```

```java
package com.xiao.xiaopay.domain.collector.model;

public enum MatchStatus {
    UNMATCHED, MATCHED, DUPLICATE, AMOUNT_MISMATCH, MANUAL
}
```

```java
package com.xiao.xiaopay.domain.event.model;

public enum PayEventStatus {
    PENDING, PROCESSING, SUCCESS, FAILED, RETRYING
}
```

```java
package com.xiao.xiaopay.domain.event.model;

public enum PayEventType {
    PAY_ORDER_PAID, PAY_ORDER_EXPIRED, PAY_NOTIFY_SUCCESS, PAY_NOTIFY_FAILED
}
```

- [ ] **Step 3: Add simple enum behavior test**

Create `PayOrderStatusTest.java`:

```java
package com.xiao.xiaopay.domain.order.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PayOrderStatusTest {
    @Test
    void pendingStatusNameMatchesDatabaseValue() {
        assertThat(OrderStatus.PENDING.name()).isEqualTo("PENDING");
    }
}
```

- [ ] **Step 4: Run enum test**

Run:

```powershell
mvn -Dtest=PayOrderStatusTest test
```

Expected: PASS.

---

### Task 4: App, Channel, and Agent Management Domain

**Files:**
- Create entities, mappers, services, and admin controllers under:
  - `src/main/java/com/xiao/xiaopay/domain/app/`
  - `src/main/java/com/xiao/xiaopay/domain/channel/`
  - `src/main/java/com/xiao/xiaopay/domain/agent/`
- Test: `src/test/java/com/xiao/xiaopay/domain/app/AppSecretPolicyTest.java`

- [ ] **Step 1: Write app secret policy test**

Create `AppSecretPolicyTest.java`:

```java
package com.xiao.xiaopay.domain.app;

import com.xiao.xiaopay.common.id.IdGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppSecretPolicyTest {
    @Test
    void generatedSecretIsLongEnoughForHmac() {
        String secret = new IdGenerator().secret();

        assertThat(secret).hasSizeGreaterThanOrEqualTo(64);
    }
}
```

- [ ] **Step 2: Create app entity and mapper**

Create `domain/app/entity/XpApp.java`:

```java
package com.xiao.xiaopay.domain.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("xp_app")
public class XpApp {
    private Long id;
    private String appId;
    private String appName;
    private String appSecret;
    private String status;
    private String notifyUrl;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

Create `domain/app/mapper/XpAppMapper.java`:

```java
package com.xiao.xiaopay.domain.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiao.xiaopay.domain.app.entity.XpApp;

public interface XpAppMapper extends BaseMapper<XpApp> {
}
```

- [ ] **Step 3: Create channel and agent entities**

Create `domain/channel/entity/XpChannel.java` and `domain/agent/entity/XpAgent.java` with fields matching `schema.sql`.

Required rule: `XpAgent` must include `agentSecret`; `XpChannel` must include `agentId`; do not store agent secret on channel.

- [ ] **Step 4: Create simple admin CRUD controllers**

Create:

```text
domain/app/controller/AdminAppController.java
domain/channel/controller/AdminChannelController.java
domain/agent/controller/AdminAgentController.java
```

Each controller should expose list/detail/create/update endpoints returning `ApiResponse`.

- [ ] **Step 5: Run app domain test**

Run:

```powershell
mvn -Dtest=AppSecretPolicyTest test
```

Expected: PASS.

---

### Task 5: payNum Generation Service

**Files:**
- Create: `src/main/java/com/xiao/xiaopay/domain/order/service/PayNumService.java`
- Test: `src/test/java/com/xiao/xiaopay/domain/order/service/PayNumServiceTest.java`

- [ ] **Step 1: Write failing payNum tests**

Create `PayNumServiceTest.java`:

```java
package com.xiao.xiaopay.domain.order.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class PayNumServiceTest {
    @Test
    void generatesFourDigitPayNumByDefault() {
        PayNumService service = new PayNumService();

        String payNum = service.generate(4, value -> false);

        assertThat(payNum).matches("[1-9][0-9]{3}");
    }

    @Test
    void skipsConflictingPayNums() {
        PayNumService service = new PayNumService(() -> 1234);
        Set<String> conflicts = new HashSet<>();
        conflicts.add("1234");
        Predicate<String> exists = conflicts::contains;

        String payNum = service.generate(4, exists);

        assertThat(payNum).isNotEqualTo("1234");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
mvn -Dtest=PayNumServiceTest test
```

Expected: FAIL because `PayNumService` does not exist.

- [ ] **Step 3: Implement payNum generator**

Create `PayNumService.java`:

```java
package com.xiao.xiaopay.domain.order.service;

import java.security.SecureRandom;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

public class PayNumService {
    private static final int MAX_ATTEMPTS = 20;
    private final IntSupplier randomFourDigit;

    public PayNumService() {
        SecureRandom random = new SecureRandom();
        this.randomFourDigit = () -> 1000 + random.nextInt(9000);
    }

    PayNumService(IntSupplier randomFourDigit) {
        this.randomFourDigit = randomFourDigit;
    }

    public String generate(int length, Predicate<String> exists) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String candidate = candidate(length);
            if (!exists.test(candidate)) {
                return candidate;
            }
        }
        if (length == 4) {
            return generate(5, exists);
        }
        throw new IllegalStateException("unable to generate unique payNum");
    }

    private String candidate(int length) {
        if (length == 4) {
            return String.valueOf(randomFourDigit.getAsInt());
        }
        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - min;
        return String.valueOf(min + new SecureRandom().nextInt(max));
    }
}
```

- [ ] **Step 4: Run payNum tests**

Run:

```powershell
mvn -Dtest=PayNumServiceTest test
```

Expected: PASS.

---

### Task 6: Order Creation API

**Files:**
- Create:
  - `domain/order/entity/XpPayOrder.java`
  - `domain/order/mapper/XpPayOrderMapper.java`
  - `domain/order/dto/CreateOrderRequest.java`
  - `domain/order/dto/CreateOrderResponse.java`
  - `domain/order/service/PayOrderService.java`
  - `domain/order/controller/PayOrderController.java`
- Test:
  - `src/test/java/com/xiao/xiaopay/domain/order/service/PayOrderServiceTest.java`

- [ ] **Step 1: Write failing service test**

Create `PayOrderServiceTest.java` using mocks or in-memory fakes for mapper dependencies:

```java
package com.xiao.xiaopay.domain.order.service;

import com.xiao.xiaopay.domain.order.dto.CreateOrderRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PayOrderServiceTest {
    @Test
    void createOrderReturnsQrCodeAmountAndPayNum() {
        CreateOrderRequest request = new CreateOrderRequest(
                "CARD202605110001",
                new BigDecimal("29.90"),
                "自动发卡商品",
                "测试商品",
                "user-1",
                "小明",
                "https://demo.com/api/card/pay-notify",
                "https://demo.com/pay/result",
                "card",
                "{\"skuId\":\"card_100\",\"quantity\":1}",
                900
        );

        FakePayOrderService service = new FakePayOrderService();

        var response = service.create("APP_TEST", request);

        assertThat(response.orderNo()).startsWith("XP");
        assertThat(response.amount()).isEqualByComparingTo("29.90");
        assertThat(response.payNum()).matches("[1-9][0-9]{3}");
        assertThat(response.qrCodeUrl()).isEqualTo("https://qr.example/wechat.png");
    }
}
```

Implement `FakePayOrderService` inside the test only if the real service has too many persistence dependencies. Once the real service exists, replace fake assertions with real service and mapper fakes.

- [ ] **Step 2: Implement DTOs**

Create `CreateOrderRequest.java`:

```java
package com.xiao.xiaopay.domain.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank String appOrderNo,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String subject,
        String description,
        String buyerId,
        String buyerName,
        String notifyUrl,
        String returnUrl,
        String businessType,
        String businessPayload,
        Integer expireSeconds
) {
}
```

Create `CreateOrderResponse.java`:

```java
package com.xiao.xiaopay.domain.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateOrderResponse(
        String orderNo,
        String appOrderNo,
        BigDecimal amount,
        String payNum,
        String payType,
        Long channelId,
        String qrCodeUrl,
        String orderStatus,
        LocalDateTime expireAt,
        LocalDateTime createdAt
) {
}
```

- [ ] **Step 3: Implement order entity and mapper**

Create `XpPayOrder.java` matching `xp_pay_order` fields and `XpPayOrderMapper extends BaseMapper<XpPayOrder>`.

- [ ] **Step 4: Implement order service**

`PayOrderService.create(appId, request)` must:

1. Normalize amount with `MoneyUtils`.
2. Verify `appId + appOrderNo` does not already exist.
3. Select enabled WeChat channel.
4. Acquire Redis lock key `paynum:{channelId}:{amount}`.
5. Generate payNum using `PayNumService`.
6. Insert `xp_pay_order`.
7. Return QR code, amount, payNum, orderNo, expireAt.

- [ ] **Step 5: Implement controller**

Create `PayOrderController`:

```java
package com.xiao.xiaopay.domain.order.controller;

import com.xiao.xiaopay.common.api.ApiResponse;
import com.xiao.xiaopay.domain.order.dto.CreateOrderRequest;
import com.xiao.xiaopay.domain.order.dto.CreateOrderResponse;
import com.xiao.xiaopay.domain.order.service.PayOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pay/orders")
public class PayOrderController {
    private final PayOrderService payOrderService;

    @PostMapping
    public ApiResponse<CreateOrderResponse> create(@RequestHeader("X-XiaoPay-App") String appId,
                                                   @Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(payOrderService.create(appId, request));
    }
}
```

- [ ] **Step 6: Run order tests**

Run:

```powershell
mvn -Dtest=PayOrderServiceTest test
```

Expected: PASS.

---

### Task 7: WeChat Collector and Idempotent Message Storage

**Files:**
- Create collector DTO/entity/mapper/service/controller under `domain/collector/`
- Test: `src/test/java/com/xiao/xiaopay/domain/collector/WechatCollectorServiceTest.java`

- [ ] **Step 1: Write failing idempotency test**

Create a test proving duplicate `agentId + messageId` is ignored and does not create duplicate match attempts.

Expected behavior:

```text
first insert -> inserted=true
second insert same agentId/messageId -> inserted=false
```

- [ ] **Step 2: Implement collector DTO**

Collector request shape:

```java
public record WechatMessagePushRequest(List<WechatMessageItem> messages) {}
public record WechatMessageItem(
    String messageId,
    BigDecimal amount,
    String payNum,
    String remarkRaw,
    LocalDateTime payTime,
    String title,
    String description,
    String rawContent
) {}
```

- [ ] **Step 3: Implement storage**

`WechatCollectorService.receive(agentId, request)` must:

1. Resolve `agentId -> channelId`.
2. Insert each message with `match_status=UNMATCHED`.
3. Ignore duplicate key `agentId + messageId`.
4. Trigger matcher for newly inserted messages only.

- [ ] **Step 4: Implement controller**

Endpoint:

```text
POST /api/collector/wechat/messages
```

Headers:

```text
X-XiaoPay-Agent
X-XiaoPay-Timestamp
X-XiaoPay-Nonce
X-XiaoPay-Signature
```

- [ ] **Step 5: Run collector tests**

Run:

```powershell
mvn -Dtest=WechatCollectorServiceTest test
```

Expected: PASS.

---

### Task 8: Matcher and Payment Event Creation

**Files:**
- Create: `domain/matcher/service/OrderMatcherService.java`
- Create: `domain/event/entity/XpPayEvent.java`
- Create: `domain/event/mapper/XpPayEventMapper.java`
- Test: `src/test/java/com/xiao/xiaopay/domain/matcher/OrderMatcherServiceTest.java`

- [ ] **Step 1: Write failing matcher tests**

Tests required:

```text
unique pending order with same channelId+amount+payNum -> order paid, message matched, PAY_ORDER_PAID event created
no payNum -> message stays unmatched
multiple candidates -> message status manual
amount mismatch -> message status amount_mismatch
expired order -> message stays unmatched
```

- [ ] **Step 2: Implement matcher**

`OrderMatcherService.match(wechatMessageId)` must run in a transaction:

1. Load unmatched message.
2. Reject empty `payNum`.
3. Query `PENDING` orders by `channelId + amount + payNum + expireAt >= now`.
4. If one candidate:
   - update order `PAID`, `paidAt=message.payTime`
   - update message `MATCHED`, `matchedOrderNo=orderNo`
   - insert `xp_order_match`
   - insert `xp_pay_event` with `PAY_ORDER_PAID`
5. If zero candidates: keep `UNMATCHED`.
6. If multiple candidates: set `MANUAL`.

- [ ] **Step 3: Run matcher tests**

Run:

```powershell
mvn -Dtest=OrderMatcherServiceTest test
```

Expected: PASS.

---

### Task 9: Reliable Notify Worker

**Files:**
- Create notify entity/mapper/service/scheduler under `domain/notify/`
- Test: `src/test/java/com/xiao/xiaopay/domain/notify/NotifyServiceTest.java`

- [ ] **Step 1: Write failing notify body test**

Test must assert callback body contains:

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

- [ ] **Step 2: Implement notify service**

`NotifyService.processPendingEvents()`:

1. Scan `xp_pay_event` where `event_status in (PENDING, RETRYING)` and `next_retry_at <= now or null`.
2. Mark event `PROCESSING`.
3. Load order and app.
4. Resolve callback URL: order notifyUrl first, app notifyUrl fallback.
5. Create `notifyEventId`.
6. Sign callback body with app secret.
7. POST callback.
8. Insert `xp_notify_record`.
9. On 2xx response: event `SUCCESS`, order notifyStatus `SUCCESS`.
10. On failure: event `RETRYING`, notifyStatus `RETRYING`, set `nextRetryAt`.

- [ ] **Step 3: Add scheduler**

Create scheduled job every 10 seconds:

```java
@Scheduled(fixedDelay = 10000)
public void processNotifyEvents() {
    notifyService.processPendingEvents();
}
```

- [ ] **Step 4: Run notify tests**

Run:

```powershell
mvn -Dtest=NotifyServiceTest test
```

Expected: PASS.

---

### Task 10: Admin Query APIs and Dashboard

**Files:**
- Create admin controllers and services under:
  - `domain/order/controller/AdminOrderController.java`
  - `domain/collector/controller/AdminWechatMessageController.java`
  - `domain/notify/controller/AdminNotifyRecordController.java`
  - `domain/dashboard/controller/AdminDashboardController.java`
- Test: controller tests with `@WebMvcTest` or service tests.

- [ ] **Step 1: Implement order list/detail query**

Endpoints:

```text
GET /api/admin/orders
GET /api/admin/orders/{orderNo}
```

Order detail must include:

```text
order
matchedMessage
matchRecords
notifyRecords
auditLogs
```

- [ ] **Step 2: Implement WeChat message list**

Endpoint:

```text
GET /api/admin/wechat-messages
```

Filters:

```text
channelId
matchStatus
payNum
startTime
endTime
```

- [ ] **Step 3: Implement dashboard summary**

Endpoint:

```text
GET /api/admin/dashboard/summary
```

Return:

```text
todayOrderCount
todayPendingCount
todayPaidCount
todayAbnormalCount
todayActualReceivedAmount
todayUnmatchedAmount
appIncomeRanking
```

- [ ] **Step 4: Run admin tests**

Run:

```powershell
mvn test
```

Expected: PASS.

---

## Verification Checklist

Before considering backend MVP complete:

- [ ] `mvn test` passes.
- [ ] `mvn package` succeeds.
- [ ] `schema.sql` can initialize a clean MySQL database.
- [ ] Create app/channel/agent records.
- [ ] Create order API returns `qrCodeUrl`, `amount`, `payNum`, `expireAt`.
- [ ] Duplicate `appId + appOrderNo` does not create a second order.
- [ ] Collector accepts a WeChat payment message once and ignores duplicate `agentId + messageId`.
- [ ] Matcher marks a unique matching order paid and writes `PAY_ORDER_PAID`.
- [ ] Notify worker records success/failure and retries failed callbacks.
- [ ] Admin order detail shows order, message, match, notify, and audit context.

## Execution Notes

- Keep tasks small and commit after each task when the repo is under git.
- Use TDD for logic-heavy services: signature, money, payNum, matcher, notify.
- Avoid building `xiaopay-console` before backend contracts are stable.
- Do not add Alipay or internal card delivery in this MVP.
