package com.xiao.xiaopay;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@MapperScan("com.xiao.xiaopay.domain.**.mapper")
public class XiaoPayServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiaoPayServerApplication.class, args);
    }
}
